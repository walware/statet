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
import org.apache.lucene.util.Version;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.collections.ImCollections;
import de.walware.ecommons.collections.ImList;
import de.walware.ecommons.text.core.input.StringParserInput;
import de.walware.ecommons.text.core.util.HtmlStripParserInput;

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
	
	
	public static final boolean DEBUG= true;
	
	
	public static class AbortIndexException extends Exception {
		
		private static final long serialVersionUID= 1L;
		
		public AbortIndexException(final Throwable cause) {
			super(cause);
		}
		
	}
	
	
	public static class RdItem {
		
		
		private final String pkg;
		private final String name;
		
		private List<String> topics;
		private String title;
		private List<String> keywords;
		private List<String> concepts;
		
		private String html;
		
		private String descrTxt;
		private String mainTxt;
		private String examplesTxt;
		
		
		public RdItem(final String pkg, final String name) {
			this.pkg= pkg;
			this.name= name;
		}
		
		
		public String getPkg() {
			return this.pkg;
		}
		
		public String getName() {
			return this.name;
		}
		
		public List<String> getTopics() {
			return this.topics;
		}
		
		public void addTopic(final String alias) {
			if (this.topics == null) {
				this.topics= new ArrayList<>(8);
			}
			if (!this.topics.contains(alias)) {
				this.topics.add(alias);
			}
		}
		
		public String getTitle() {
			return (this.title != null) ? this.title : ""; //$NON-NLS-1$
		}
		
		public void setTitle(final String title) {
			this.title= title;
		}
		
		public List<String> getKeywords() {
			return this.keywords;
		}
		
		public void addKeyword(final String keyword) {
			if (this.keywords == null) {
				this.keywords= new ArrayList<>(8);
			}
			if (!this.keywords.contains(keyword)) {
				this.keywords.add(keyword);
			}
		}
		
		public List<String> getConcepts() {
			return this.concepts;
		}
		
		public void addConcept(final String concept) {
			if (this.concepts == null) {
				this.concepts= new ArrayList<>(8);
			}
			this.concepts.add(concept);
		}
		
		public String getHtml() {
			return this.html;
		}
		
		public void setHtml(final String html) {
			this.html= html;
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
	
	
	/** Worker for packages. Single thread! */
	class PackageWorker {
		
		
		private final FlagField doctypeField_PKG_DESCRIPTION= new FlagField(DOCTYPE_FIELD_NAME, PKG_DESCRIPTION_DOCTYPE);
		private final FlagField doctypeField_PAGE= new FlagField(DOCTYPE_FIELD_NAME, PAGE_DOCTYPE);
		private final NameField packageField= new NameField(PACKAGE_FIELD_NAME);
		private final NameField pageField= new NameField(PAGE_FIELD_NAME);
		private final TxtField titleTxtField= new TxtField(TITLE_TXT_FIELD_NAME, 2.0f);
		private final MultiValueFieldList<NameField> aliasFields= MultiValueFieldList.forNameField(
				ALIAS_FIELD_NAME );
		private final MultiValueFieldList<TxtField> aliasTxtFields= MultiValueFieldList.forTxtField(
				ALIAS_TXT_FIELD_NAME, 2.0f );
		private final TxtField descriptionTxtField= new TxtField(DESCRIPTION_TXT_FIELD_NAME, 1.5f);
		private final TxtField authorsTxtField= new TxtField(AUTHORS_TXT_FIELD_NAME);
		private final TxtField maintainerTxtField= new TxtField(MAINTAINER_TXT_FIELD_NAME);
		private final TxtField urlTxtField= new TxtField(URL_TXT_FIELD_NAME);
		private final MultiValueFieldList<KeywordField> keywordTxtFields= MultiValueFieldList.forKeywordField(
				KEYWORD_FIELD_NAME );
		private final MultiValueFieldList<TxtField> conteptTxtFields= MultiValueFieldList.forTxtField(
				CONCEPT_TXT_FIELD_NAME );
		private final TxtField docTxtField= new TxtField(DOC_TXT_FIELD_NAME);
		private final TxtField.OmitNorm docHtmlField= new TxtField.OmitNorm(DOC_HTML_FIELD_NAME);
		private final TxtField examplesTxtField= new TxtField(EXAMPLES_TXT_FIELD_NAME, 0.5f);
		
		private final StringBuilder tempBuilder= new StringBuilder(65536);
		private final HtmlStripParserInput tempHtmlInput= new HtmlStripParserInput(
				new StringParserInput(0x800), 0x800 );
		
		
		public PackageWorker() {
		}
		
		
		private void addToLucene(final IRPkgDescription item) throws CorruptIndexException, IOException {
			final Document doc= new Document();
			doc.add(this.doctypeField_PKG_DESCRIPTION);
			this.packageField.setStringValue(item.getName());
			doc.add(this.packageField);
			this.descriptionTxtField.setStringValue(item.getDescription());
			doc.add(this.descriptionTxtField);
			if (item.getAuthor() != null) {
				this.authorsTxtField.setStringValue(item.getAuthor());
				doc.add(this.authorsTxtField);
			}
			if (item.getMaintainer() != null) {
				this.maintainerTxtField.setStringValue(item.getMaintainer());
				doc.add(this.maintainerTxtField);
			}
			if (item.getUrl() != null) {
				this.urlTxtField.setStringValue(item.getUrl());
				doc.add(this.urlTxtField);
			}
			REnvIndexWriter.this.luceneWriter.addDocument(doc);
		}
		
		private void addToLucene(final RdItem item) throws CorruptIndexException, IOException {
			final Document doc= new Document();
			doc.add(this.doctypeField_PAGE);
			this.packageField.setStringValue(item.getPkg());
			doc.add(this.packageField);
			this.pageField.setStringValue(item.getName());
			doc.add(this.pageField);
			this.titleTxtField.setStringValue(item.getTitle());
			doc.add(this.titleTxtField);
			if (item.topics != null) {
				final List<String> topics= item.topics;
				for (int i= 0; i < topics.size(); i++) {
					final NameField nameField= this.aliasFields.get(i);
					nameField.setStringValue(topics.get(i));
					doc.add(nameField);
					final TxtField txtField= this.aliasTxtFields.get(i);
					txtField.setStringValue(topics.get(i));
					doc.add(txtField);
				}
			}
			if (item.keywords != null) {
				final List<String> keywords= item.keywords;
				for (int i= 0; i < keywords.size(); i++) {
					final KeywordField field= this.keywordTxtFields.get(i);
					field.setStringValue(keywords.get(i));
					doc.add(field);
				}
			}
			if (item.concepts != null) {
				final List<String> concepts= item.concepts;
				for (int i= 0; i < concepts.size(); i++) {
					final TxtField txtField= this.conteptTxtFields.get(i);
					txtField.setStringValue(concepts.get(i));
					doc.add(txtField);
				}
			}
			if (item.html != null) {
				createSectionsTxt(item);
				if (item.descrTxt != null) {
					this.descriptionTxtField.setStringValue(item.descrTxt);
					doc.add(this.descriptionTxtField);
				}
				this.docTxtField.setStringValue(item.mainTxt);
				doc.add(this.docTxtField);
				if (item.examplesTxt != null) {
					this.examplesTxtField.setStringValue(item.examplesTxt);
					doc.add(this.examplesTxtField);
				}
				this.docHtmlField.setStringValue(item.html);
				doc.add(this.docHtmlField);
			}
			REnvIndexWriter.this.luceneWriter.addDocument(doc);
		}
		
		private void createSectionsTxt(final RdItem item) throws IOException {
			String html= item.html;
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
			((StringParserInput) this.tempHtmlInput.getSource()).reset(html);
			this.tempHtmlInput.init();
			int c;
			boolean blank= true;
			while ((c= this.tempHtmlInput.get(0)) >= 0) {
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
		
	}
	
	
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
	
	// At moment we use a single worker; multi-threading is not worth, because R is the bottleneck.
	// For multi-threading: thread pool / jobs with worker / thread, currentPackage to worker, ...
	private final PackageWorker worker= new PackageWorker();
	
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
						
						final IndexWriterConfig config= createWriterConfig();
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
					
					final IndexWriterConfig config= createWriterConfig();
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
	
	private IndexWriterConfig createWriterConfig() {
		final IndexWriterConfig config= new IndexWriterConfig(Version.LATEST, WRITE_ANALYZER);
		config.setSimilarity(SIMILARITY);
		config.setMaxThreadStates(Math.min(Math.max(2, Runtime.getRuntime().availableProcessors() - 3), 8));
		config.setRAMPerThreadHardLimitMB(512);
		return config;
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
			this.worker.addToLucene(packageDesription);
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
			this.worker.addToLucene(item);
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
			sb.append("TempBuilder-capycity: ").append(this.worker.tempBuilder.capacity()).append('\n'); //$NON-NLS-1$
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
			
			final ImList<IRHelpKeyword.Group> keywords= ImCollections.<IRHelpKeyword.Group>toList(values);
			for (final Iterator<IRPkgHelp> iter= this.packages.values().iterator(); iter.hasNext(); ) {
				if (iter.next() == null) {
					iter.remove();
				}
			}
			final IRPkgHelp[] array= this.packages.values().toArray(new IRPkgHelp[this.packages.size()]);
			Arrays.sort(array);
			final ImList<IRPkgHelp> packages= ImCollections.newList(array);
			
			final REnvHelp help= new REnvHelp(this.rEnvConfig.getReference(), this.docDir, keywords, packages);
			
//			fLuceneWriter.maybeMerge();
			
			synchronized (this.indexLock) {
				this.luceneWriter.close();
				
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
