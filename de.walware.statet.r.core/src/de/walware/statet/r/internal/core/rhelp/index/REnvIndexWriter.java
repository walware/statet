/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.rhelp.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.LiveIndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.collections.ConstArrayList;
import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.text.HtmlParseInput;

import de.walware.rj.renv.IRPkgDescription;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.rhelp.IRHelpKeyword;
import de.walware.statet.r.core.rhelp.IRHelpKeywordNode;
import de.walware.statet.r.core.rhelp.IRPkgHelp;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.rhelp.REnvHelp;
import de.walware.statet.r.internal.core.rhelp.RHelpKeyword;
import de.walware.statet.r.internal.core.rhelp.RHelpKeywordGroup;
import de.walware.statet.r.internal.core.rhelp.RHelpManager;
import de.walware.statet.r.internal.core.rhelp.RHelpPage;
import de.walware.statet.r.internal.core.rhelp.RPkgHelp;


public class REnvIndexWriter implements IREnvIndex {
	
	
	public static final boolean DEBUG= Boolean.parseBoolean(System.getProperty("de.walware.statet.r.rhelp.debug") ); //$NON-NLS-1$
	
	
	public static class AbortIndexException extends Exception {
		
		private static final long serialVersionUID= 1L;
		
		public AbortIndexException(final Throwable cause) {
			super(cause);
		}
		
	}
	
	
	public static class RdItem {
		
		
		private final String fPkg;
		private final String fName;
		
		private List<String> fTopics;
		private String fTitle;
		private List<String> fKeywords;
		private List<String> fConcepts;
		
		private String fHtml;
		
		private String descrTxt;
		private String mainTxt;
		private String examplesTxt;
		
		
		public RdItem(final String pkg, final String name) {
			this.fPkg= pkg;
			this.fName= name;
		}
		
		
		public String getPkg() {
			return this.fPkg;
		}
		
		public String getName() {
			return this.fName;
		}
		
		public List<String> getTopics() {
			return this.fTopics;
		}
		
		public void addTopic(final String alias) {
			if (this.fTopics == null) {
				this.fTopics= new ArrayList<>(8);
			}
			if (!this.fTopics.contains(alias)) {
				this.fTopics.add(alias);
			}
		}
		
		public String getTitle() {
			return (this.fTitle != null) ? this.fTitle : ""; //$NON-NLS-1$
		}
		
		public void setTitle(final String title) {
			this.fTitle= title;
		}
		
		public List<String> getKeywords() {
			return this.fKeywords;
		}
		
		public void addKeyword(final String keyword) {
			if (this.fKeywords == null) {
				this.fKeywords= new ArrayList<>(8);
			}
			if (!this.fKeywords.contains(keyword)) {
				this.fKeywords.add(keyword);
			}
		}
		
		public List<String> getConcepts() {
			return this.fConcepts;
		}
		
		public void addConcept(final String concept) {
			if (this.fConcepts == null) {
				this.fConcepts= new ArrayList<>(8);
			}
			this.fConcepts.add(concept);
		}
		
		public String getHtml() {
			return this.fHtml;
		}
		
		public void setHtml(final String html) {
			this.fHtml= html;
		}
		
	}
	
	
	public static final Collection<String> IGNORE_PKG_NAMES;
	static {
		IGNORE_PKG_NAMES= new ArrayList<>();
		IGNORE_PKG_NAMES.add("translations"); //$NON-NLS-1$
	}
	
	static File getIndexDirectory(final IREnvConfiguration rEnvConfig) {
		try {
			final IFileStore indexDirectory= rEnvConfig.getIndexDirectoryStore();
			return indexDirectory.getChild("index").toLocalFile(0, null); //$NON-NLS-1$
		}
		catch (final Exception e) {
			return null;
		}
	}
	
	
	private static Analyzer WRITE_ANALYZER= new WriteAnalyzer();
	
	
	private final IREnvConfiguration rEnvConfig;
	
	private String docDir;
	private Map<String, IRPkgHelp> existingPackages;
	private Map<String, IRPkgHelp> packages;
	private LinkedHashMap<String, RHelpKeywordGroup> keywordGroups;
	
	private final File indexDirectory;
	private FSDirectory luceneDirectory;
	private IndexWriter luceneWriter;
	
	private RPkgHelp currentPackage;
	
	private Object indexLock;
	
	private Map<String, String> rEnvSharedProperties;
	
	private boolean reset;
	
	private final StringBuilder tempBuilder= new StringBuilder(65536);
	private final HtmlParseInput tempHtmlInput= new HtmlParseInput();
//	private char[] fTempBuffer= new char[512];
	
	private MultiStatus status;
	
	
	public REnvIndexWriter(final IREnvConfiguration rEnvConfig) {
		this.rEnvConfig= rEnvConfig;
		this.indexDirectory= getIndexDirectory(rEnvConfig);
	}
	
	
	public void log(final IStatus status) {
		final MultiStatus multiStatus= this.status;
		if (multiStatus != null) {
			multiStatus.add(status);
		}
		else {
			RCorePlugin.log(status);
		}
	}
	
	public void beginBatch(final boolean reset) throws AbortIndexException {
		if (this.luceneWriter != null) {
			throw new IllegalStateException();
		}
		
		this.status= new MultiStatus(RCore.PLUGIN_ID, 0, "Indexing: '" + this.rEnvConfig.getName() + "'.", null); //$NON-NLS-1$ //$NON-NLS-2$
		this.status.add(new Status(IStatus.INFO, RCore.PLUGIN_ID, "Beginning batch (index directory= '" + this.indexDirectory.getAbsolutePath() + "')."));
		
		try {
			final RHelpManager rHelpManager= RCorePlugin.getDefault().getRHelpManager();
			this.indexLock= rHelpManager.getIndexLock(this.rEnvConfig.getReference());
			
			synchronized (this.indexLock) {
				this.reset= reset;
				this.luceneDirectory= new SimpleFSDirectory(this.indexDirectory);
				final IndexWriterConfig config= new IndexWriterConfig(IREnvIndex.LUCENE_VERSION, WRITE_ANALYZER);
				config.setSimilarity(SIMILARITY);
				config.setMaxThreadStates(Math.min(Math.max(2, Runtime.getRuntime().availableProcessors() - 3), 8));
				config.setRAMPerThreadHardLimitMB(512);
				if (!reset) {
					final REnvHelp oldHelp= rHelpManager.getHelp(this.rEnvConfig.getReference());
					try (final IndexReader dirReader= DirectoryReader.open(this.luceneDirectory)) {
						this.existingPackages= new HashMap<>(64);
						TermsEnum termsEnum= null;
						for (final AtomicReaderContext leave : dirReader.leaves()) {
							final AtomicReader aReader= leave.reader();
							final Terms terms= aReader.terms(PACKAGE_FIELD_NAME);
							if (terms != null) {
								termsEnum= terms.iterator(termsEnum);
								BytesRef term;
								while ((term= termsEnum.next()) != null) {
									final String name= term.utf8ToString();
									final IRPkgHelp pkgHelp= (oldHelp != null) ? oldHelp.getRPackage(name) : null;
									this.existingPackages.put(name, pkgHelp);
								}
							}
						}
						
						config.setOpenMode(OpenMode.CREATE_OR_APPEND);
						this.luceneWriter= new IndexWriter(this.luceneDirectory, config);
					}
					catch (final IOException e) {
						assert (this.luceneWriter == null);
						// try again new
					}
					finally {
						if (oldHelp != null) {
							oldHelp.unlock();
						}
					}
				}
				if (this.luceneWriter == null) {
					this.reset= true;
					this.existingPackages= new HashMap<>(0);
					
					config.setOpenMode(OpenMode.CREATE);
					this.luceneWriter= new IndexWriter(this.luceneDirectory, config);
				}
			}
			
			this.packages= new LinkedHashMap<>();
			this.keywordGroups= new LinkedHashMap<>();
		}
		catch (final IOException e) {
			throw new AbortIndexException(e);
		}
		catch (final OutOfMemoryError e) {
			throw new AbortIndexException(e);
		}
	}
	
	public void setDocDir(String docDir) {
		if (docDir != null && docDir.isEmpty()) {
			docDir= null;
		}
		this.status.add(new Status(IStatus.INFO, RCore.PLUGIN_ID, "Setting doc dir to: " + //$NON-NLS-1$
				((docDir != null) ? ('\''+docDir+'\'') : "<missing>") + '.')); //$NON-NLS-1$
		this.docDir= docDir;
	}
	
	public String getDocDir() {
		return this.docDir;
	}
	
	public void setREnvSharedProperties(final Map<String, String> properties) {
		this.rEnvSharedProperties= properties;
	}
	
	
	public void addDefaultKeyword(final String[] path, final String description) {
		if (path == null || path.length == 0) {
			return;
		}
		if (path.length == 1) { // group
			final String key= path[0].trim().intern();
			if (key.length() > 0) {
				this.keywordGroups.put(key, new RHelpKeywordGroup(key, description,
						new ArrayList<IRHelpKeyword>()));
			}
			return;
		}
		else {
			IRHelpKeywordNode node= this.keywordGroups.get(path[0]);
			int i= 1;
			while (node != null) {
				if (i == path.length-1) {
					if (path[i].length() > 0) {
						final String key= path[i].intern();
						node.getNestedKeywords().add(new RHelpKeyword(key, description,
								new ArrayList<IRHelpKeyword>()));
						return;
					}
				}
				else {
					node= node.getNestedKeyword(path[i++]);
					continue;
				}
			}
			return;
		}
	}
	
	/**
	 * 
	 * @param name package name
	 * @param version
	 * @return <code>true</code> if seems OK, otherwise false
	 */
	public boolean checkPackage(final String name, final String version, final String built) {
		if (IGNORE_PKG_NAMES.contains(name)) {
			return true;
		}
		synchronized (this.packages) {
			if (this.packages.containsKey(name)) {
				return true;
			}
			final IRPkgHelp pkgHelp= this.existingPackages.remove(name);
			if (!this.reset && pkgHelp != null && version.equals(pkgHelp.getVersion())
					&& ((built != null) ? built.equals(pkgHelp.getBuilt()) : null == pkgHelp.getBuilt()) ) {
				this.packages.put(name, pkgHelp); // reuse
				return true;
			}
			this.packages.put(name, null); // placeholder
			return false;
		}
	}
	
	public void beginPackage(final IRPkgDescription packageDesription) throws AbortIndexException {
		final String name= packageDesription.getName();
		if (this.currentPackage != null) {
			throw new IllegalArgumentException();
		}
		try {
			this.status.add(new Status(IStatus.INFO, RCore.PLUGIN_ID, "Beginning package: '" + name + "'.")); //$NON-NLS-1$ //$NON-NLS-2$
			
			this.currentPackage= new RPkgHelp(name, packageDesription.getTitle(),
					packageDesription.getVersion().toString(),
					this.rEnvConfig.getReference(), packageDesription.getBuilt() );
			synchronized (this.packages) {
				this.existingPackages.remove(name);
				this.packages.put(name, this.currentPackage);
			}
			this.luceneWriter.deleteDocuments(new Term(PACKAGE_FIELD_NAME, name));
			addToLucene(packageDesription);
		}
		catch (final IOException e) {
			throw new AbortIndexException(e);
		}
		catch (final OutOfMemoryError e) {
			throw new AbortIndexException(e);
		}
	}
	
	public void add(final RdItem item) throws AbortIndexException {
		if (this.currentPackage == null || !this.currentPackage.getName().equals(item.getPkg())) {
			throw new IllegalArgumentException();
		}
		try {
			this.currentPackage.addPage(new RHelpPage(this.currentPackage, item.getName(), item.getTitle()));
			addToLucene(item);
		}
		catch (final IOException e) {
			throw new AbortIndexException(e);
		}
		catch (final OutOfMemoryError e) {
			throw new AbortIndexException(e);
		}
	}
	
	public void endPackage() throws AbortIndexException {
		if (DEBUG) {
			this.status.add(new Status(IStatus.INFO, RCore.PLUGIN_ID, "Finishing package.")); //$NON-NLS-1$
			
			final Runtime runtime= Runtime.getRuntime();
			final long maxMemory= runtime.maxMemory();
			final long allocatedMemory= runtime.totalMemory();
			final long freeMemory= runtime.freeMemory();
			final LiveIndexWriterConfig config= this.luceneWriter.getConfig();
			final StringBuilder sb= new StringBuilder("Memory status:\n"); //$NON-NLS-1$
			sb.append("TempBuilder-capycity: ").append(this.tempBuilder.capacity()).append('\n'); //$NON-NLS-1$
			sb.append("Lucene-buffersize: ").append((long) (config.getRAMBufferSizeMB() * 1024.0)).append('\n'); //$NON-NLS-1$
			sb.append("Memory-free: ").append(freeMemory / 1024L).append('\n'); //$NON-NLS-1$
			sb.append("Memory-total: ").append(allocatedMemory / 1024L).append('\n'); //$NON-NLS-1$
			sb.append("Memory-max: ").append(maxMemory / 1024L).append('\n'); //$NON-NLS-1$
			this.status.add(new Status(IStatus.INFO, RCore.PLUGIN_ID, sb.toString()));
		}
		
		if (this.currentPackage == null) {
			return;
		}
		this.currentPackage.freeze();
		this.currentPackage= null;
	}
	
	public IStatus endBatch() throws AbortIndexException {
		if (this.luceneWriter == null) {
			return null;
		}
		
		final MultiStatus status= this.status;
		this.status= null;
		if (status != null) {
			status.add(new Status(IStatus.INFO, RCore.PLUGIN_ID, -1, "Finishing batch.", null)); //$NON-NLS-1$
			RCorePlugin.log(status);
		}
		try {
			final RHelpManager rHelpManager= RCorePlugin.getDefault().getRHelpManager();
			
			for (final String packageName : this.existingPackages.keySet()) {
				this.luceneWriter.deleteDocuments(new Term(PACKAGE_FIELD_NAME, packageName));
			}
			this.existingPackages.clear();
			
			final Collection<RHelpKeywordGroup> values= this.keywordGroups.values();
			for (final RHelpKeywordGroup group : values) {
				group.freeze();
			}
			
			final ConstList<IRHelpKeyword.Group> keywords= new ConstArrayList<IRHelpKeyword.Group>(values);
			for (final Iterator<IRPkgHelp> iter= this.packages.values().iterator(); iter.hasNext(); ) {
				if (iter.next() == null) {
					iter.remove();
				}
			}
			final IRPkgHelp[] array= this.packages.values().toArray(new IRPkgHelp[this.packages.size()]);
			Arrays.sort(array);
			final ConstList<IRPkgHelp> packages= new ConstArrayList<>(array);
			
			final REnvHelp help= new REnvHelp(this.rEnvConfig.getReference(), this.docDir, keywords, packages);
			
//			fLuceneWriter.maybeMerge();
			
			synchronized (this.indexLock) {
				this.luceneWriter.close(true);
				
				rHelpManager.updateHelp(this.rEnvConfig, this.rEnvSharedProperties, help);
			}
			
			if (status != null && status.getSeverity() >= IStatus.WARNING) {
				return status;
			}
			return null;
		}
		catch (final IOException e) {
			cancel();
			throw new AbortIndexException(e);
		}
		catch (final OutOfMemoryError e) {
			throw new AbortIndexException(e);
		}
		finally {
			clear();
		}
	}
	
	private void addToLucene(final IRPkgDescription item) throws CorruptIndexException, IOException {
		final Document doc= new Document();
		// replace using int field
		doc.add(new IntField(DOCTYPE_FIELD_NAME, PKG_DESCRIPTION_DOCTYPE, Store.YES));
		doc.add(new NameField(PACKAGE_FIELD_NAME, item.getName()));
		doc.add(new TxtField(DESCRIPTION_TXT_FIELD_NAME, item.getDescription(), 1.5f));
		if (item.getAuthor() != null) {
			doc.add(new TxtField(AUTHORS_TXT_FIELD_NAME, item.getAuthor()));
		}
		if (item.getMaintainer() != null) {
			doc.add(new TxtField(MAINTAINER_TXT_FIELD_NAME, item.getMaintainer()));
		}
		if (item.getUrl() != null) {
			doc.add(new TxtField(URL_TXT_FIELD_NAME, item.getUrl()));
		}
		this.luceneWriter.addDocument(doc);
	}
	
	private void addToLucene(final RdItem item) throws CorruptIndexException, IOException {
		final Document doc= new Document();
		doc.add(new IntField(DOCTYPE_FIELD_NAME, PAGE_DOCTYPE, Store.YES));
		doc.add(new NameField(PACKAGE_FIELD_NAME, item.getPkg()));
		doc.add(new NameField(PAGE_FIELD_NAME, item.getName()));
		doc.add(new TxtField(TITLE_TXT_FIELD_NAME, item.getTitle(), 2.0f));
		if (item.fTopics != null) {
			final List<String> topics= item.fTopics;
			for (int i= 0; i < topics.size(); i++) {
				doc.add(new NameField(ALIAS_FIELD_NAME, topics.get(i)));
				doc.add(new TxtField(ALIAS_TXT_FIELD_NAME, topics.get(i), 2.0f));
			}
		}
		if (item.fKeywords != null) {
			final List<String> keywords= item.fKeywords;
			for (int i= 0; i < keywords.size(); i++) {
				doc.add(new StringField(KEYWORD_FIELD_NAME, keywords.get(i), Store.NO));
			}
		}
		if (item.fConcepts != null) {
			final List<String> concepts= item.fConcepts;
			for (int i= 0; i < concepts.size(); i++) {
				doc.add(new TxtField(CONCEPT_TXT_FIELD_NAME, concepts.get(i)));
			}
		}
		if (item.fHtml != null) {
			doc.add(new TxtField.OmitNorm(DOC_HTML_FIELD_NAME, item.fHtml));
			createSectionsTxt(item);
			if (item.descrTxt != null) {
				doc.add(new TxtField(DESCRIPTION_TXT_FIELD_NAME, item.descrTxt, 1.5f));
			}
			doc.add(new TxtField(DOC_TXT_FIELD_NAME, item.mainTxt, 1.0f));
			if (item.examplesTxt != null) {
				doc.add(new TxtField(EXAMPLES_TXT_FIELD_NAME, item.examplesTxt, 0.5f));
			}
		}
		this.luceneWriter.addDocument(doc);
	}
	
	private void createSectionsTxt(final RdItem item) throws IOException {
		String html= item.fHtml;
		this.tempBuilder.setLength(0);
		{	final int idx1= html.indexOf("</h2>"); //$NON-NLS-1$
			if (idx1 >= 0) {
				html= html.substring(idx1+5);
			}
		}
		{	final int idx1= html.lastIndexOf("<hr/>"); //$NON-NLS-1$
			if (idx1 >= 0) {
				html= html.substring(0, idx1);
			}
		}
		{	int idxBegin= html.indexOf("<h3 id=\"description\""); //$NON-NLS-1$
			if (idxBegin >= 0) {
				idxBegin= html.indexOf('>', idxBegin+20);
				if (idxBegin >= 0) {
					idxBegin= html.indexOf("</h3>", idxBegin+1); //$NON-NLS-1$
					if (idxBegin >= 0) {
						idxBegin += 5;
						int idxEnd= html.indexOf("<h3", idxBegin); //$NON-NLS-1$
						if (idxEnd < 0) {
							idxEnd= html.indexOf("<hr/>", idxBegin); //$NON-NLS-1$
						}
						if (idxEnd >= 0) {
							item.descrTxt= html2txt(html.substring(idxBegin, idxEnd));
							html= html.substring(idxEnd);
						}
					}
				}
			}
		}
		final String[] s= new String[] { html, null };
		{	if (extract(s, "<h3 id=\"examples\"")) { //$NON-NLS-1$
				item.examplesTxt= html2txt(s[1]);
			}
		}
		item.mainTxt= html2txt(s[0]);
	}
	
	private boolean extract(final String[] s, final String h3) {
		final String html= s[0];
		final int idx0= html.indexOf(h3);
		if (idx0 >= 0) {
			int idxBegin= html.indexOf('>', idx0+h3.length());
			if (idxBegin >= 0) {
				idxBegin= html.indexOf("</h3>", idxBegin+1); //$NON-NLS-1$
				if (idxBegin >= 0) {
					idxBegin += 5;
					final int idxEnd= html.indexOf("<h3", idxBegin); //$NON-NLS-1$
					if (idxEnd >= 0) {
						this.tempBuilder.setLength(0);
						this.tempBuilder.append(html, 0, idx0);
						this.tempBuilder.append(html, idxEnd, html.length());
						s[0]= this.tempBuilder.toString();
						s[1]= html.substring(idxBegin, idxEnd);
					}
					else {
						s[0]= html.substring(0, idx0);
						s[1]= html.substring(idxBegin, html.length());
					}
					return true;
				}
			}
		}
		return false;
	}
	
	private String html2txt(final String html) {
		this.tempBuilder.setLength(0);
		this.tempHtmlInput.reset(html);
		int c;
		boolean blank= true;
		while ((c= this.tempHtmlInput.get(1)) >= 0) {
			if (c <= 0x20) {
				if (!blank) {
					blank= true;
					this.tempBuilder.append(' ');
				}
			}
			else {
				if (blank) {
					blank= false;
				}
				this.tempBuilder.append((char) c);
			}
			this.tempHtmlInput.consume(1);
		}
		c= this.tempBuilder.length();
		return (c > 0 && this.tempBuilder.charAt(c-1) == ' ') ?
				this.tempBuilder.substring(0, c-1) : this.tempBuilder.toString();
	}
	
	public IStatus cancel() {
		final MultiStatus status;
		try {
			if (this.luceneWriter != null) {
				try {
					this.luceneWriter.rollback();
				}
				catch (final Exception e) {
					if (this.status != null) {
						this.status.add(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, "Error when rolling back.", null)); //$NON-NLS-1$
					}
				}
				
				this.luceneWriter.close();
			}
		}
		catch (final Exception close) {
		}
		finally {
			clear();
			
			status= this.status;
			this.status= null;
			if (status != null) {
				status.add(new Status(IStatus.INFO, RCore.PLUGIN_ID, -1, "Canceling batch.", null)); //$NON-NLS-1$
				RCorePlugin.log(status);
			}
		}
		if (status != null && status.getSeverity() >= IStatus.WARNING) {
			return status;
		}
		return null;
	}
	
	private void clear() {
		if (this.luceneWriter != null) {
			try {
				if (IndexWriter.isLocked(this.luceneDirectory)) {
					IndexWriter.unlock(this.luceneDirectory);
				}
			} catch (final Exception ignore) {}
		}
		this.luceneWriter= null;
		this.luceneDirectory= null;
		this.currentPackage= null;
		this.indexLock= null;
	}
	
}
