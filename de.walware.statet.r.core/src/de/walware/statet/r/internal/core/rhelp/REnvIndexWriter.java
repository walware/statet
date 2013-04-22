/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.rhelp;

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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.text.HtmlParseInput;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.IRPackageDescription;
import de.walware.statet.r.core.rhelp.IRHelpKeyword;
import de.walware.statet.r.core.rhelp.IRHelpKeyword.Group;
import de.walware.statet.r.core.rhelp.IRHelpKeywordNode;
import de.walware.statet.r.core.rhelp.IRPackageHelp;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.RPackageDescription;


public class REnvIndexWriter implements IREnvIndex {
	
	
	public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("de.walware.statet.r.rhelp.debug") ); //$NON-NLS-1$
	
	
	public static class AbortIndexException extends Exception {
		
		private static final long serialVersionUID = 1L;
		
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
			fPkg = pkg;
			fName = name;
		}
		
		
		public String getPkg() {
			return fPkg;
		}
		
		public String getName() {
			return fName;
		}
		
		public List<String> getTopics() {
			return fTopics;
		}
		
		public void addTopic(final String alias) {
			if (fTopics == null) {
				fTopics = new ArrayList<String>(8);
			}
			if (!fTopics.contains(alias)) {
				fTopics.add(alias);
			}
		}
		
		public String getTitle() {
			return (fTitle != null) ? fTitle : ""; //$NON-NLS-1$
		}
		
		public void setTitle(final String title) {
			fTitle = title;
		}
		
		public List<String> getKeywords() {
			return fKeywords;
		}
		
		public void addKeyword(final String keyword) {
			if (fKeywords == null) {
				fKeywords = new ArrayList<String>(8);
			}
			if (!fKeywords.contains(keyword)) {
				fKeywords.add(keyword);
			}
		}
		
		public List<String> getConcepts() {
			return fConcepts;
		}
		
		public void addConcept(final String concept) {
			if (fConcepts == null) {
				fConcepts = new ArrayList<String>(8);
			}
			fConcepts.add(concept);
		}
		
		public String getHtml() {
			return fHtml;
		}
		
		public void setHtml(final String html) {
			fHtml = html;
		}
		
	}
	
	
	public static final Collection<String> IGNORE_PKG_NAMES;
	static {
		IGNORE_PKG_NAMES = new ArrayList<String>();
		IGNORE_PKG_NAMES.add("translations"); //$NON-NLS-1$
	}
	
	
	private final IREnvConfiguration fREnvConfig;
	
	private String fDocDir;
	private Map<String, IRPackageHelp> fExistingPackages;
	private Map<String, IRPackageHelp> fPackages;
	private LinkedHashMap<String, RHelpKeywordGroup> fKeywordGroups;
	
	private final File fIndexDirectory;
	private Directory fLuceneDirectory;
	private IndexWriter fLuceneWriter;
	
	private RPackageHelp fCurrentPackage;
	
	private Object fIndexLock;
	
	private Map<String, String> fREnvSharedProperties;
	
	private boolean fReset;
	
	private final StringBuilder fTempBuilder = new StringBuilder(65536);
	private final HtmlParseInput fTempHtmlInput = new HtmlParseInput();
//	private char[] fTempBuffer = new char[512];
	
	private MultiStatus fStatus;
	
	
	public REnvIndexWriter(final IREnvConfiguration rEnvConfig) {
		fREnvConfig = rEnvConfig;
		fIndexDirectory = SaveUtil.getIndexDirectory(rEnvConfig);
	}
	
	
	public void log(final IStatus status) {
		final MultiStatus multiStatus = fStatus;
		if (multiStatus != null) {
			multiStatus.add(status);
		}
		else {
			RCorePlugin.log(status);
		}
	}
	
	public void beginBatch(final boolean reset) throws AbortIndexException {
		if (fLuceneWriter != null) {
			throw new IllegalStateException();
		}
		
		fStatus = new MultiStatus(RCore.PLUGIN_ID, 0, "Indexing: '" + fREnvConfig.getName() + "'.", null); //$NON-NLS-1$ //$NON-NLS-2$
		fStatus.add(new Status(IStatus.INFO, RCore.PLUGIN_ID, "Beginning batch."));
		
		try {
			final RHelpManager rHelpManager = RCorePlugin.getDefault().getRHelpManager();
			fIndexLock = rHelpManager.getIndexLock(fREnvConfig.getReference());
			
			synchronized (fIndexLock) {
				fReset = reset;
				fLuceneDirectory = new SimpleFSDirectory(fIndexDirectory);
				if (!reset) {
					final IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_31, WRITE_ANALYZER);
					config.setOpenMode(OpenMode.CREATE_OR_APPEND);
					
					final REnvHelp oldHelp = rHelpManager.getHelp(fREnvConfig.getReference());
					IndexReader reader = null;
					try {
						reader = IndexReader.open(fLuceneDirectory, true);
						
						fExistingPackages = new HashMap<String, IRPackageHelp>(64);
						final TermEnum terms = reader.terms(new Term(PACKAGE_FIELD_NAME));
						do {
							final Term term = terms.term();
							if (term == null || term.field() != PACKAGE_FIELD_NAME) {
								break;
							}
							final String name = terms.term().text();
							final IRPackageHelp packageHelp = (oldHelp != null) ? oldHelp.getRPackage(name) : null;
							fExistingPackages.put(name, packageHelp);
						}
						while (terms.next());
						
						fLuceneWriter = new IndexWriter(fLuceneDirectory, config);
					}
					catch (final IOException e) {
						assert (fLuceneWriter == null);
						// try again new
					}
					finally {
						if (oldHelp != null) {
							oldHelp.unlock();
						}
						if (reader != null) {
							reader.close();
						}
					}
				}
				if (fLuceneWriter == null) {
					fReset = true;
					final IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_31, WRITE_ANALYZER);
					config.setOpenMode(OpenMode.CREATE);
					fExistingPackages = new HashMap<String, IRPackageHelp>(0);
					fLuceneWriter = new IndexWriter(fLuceneDirectory, config);
				}
			}
			
			fPackages = new LinkedHashMap<String, IRPackageHelp>();
			fKeywordGroups = new LinkedHashMap<String, RHelpKeywordGroup>();
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
			docDir = null;
		}
		fStatus.add(new Status(IStatus.INFO, RCore.PLUGIN_ID, "Setting doc dir to: " + //$NON-NLS-1$
				((docDir != null) ? ('\''+docDir+'\'') : "<missing>") + '.')); //$NON-NLS-1$
		fDocDir = docDir;
	}
	
	public String getDocDir() {
		return fDocDir;
	}
	
	public void setREnvSharedProperties(final Map<String, String> properties) {
		fREnvSharedProperties = properties;
	}
	
	
	public void addDefaultKeyword(final String[] path, final String description) {
		if (path == null || path.length == 0) {
			return;
		}
		if (path.length == 1) { // group
			final String key = path[0].trim().intern();
			if (key.length() > 0) {
				fKeywordGroups.put(key, new RHelpKeywordGroup(key, description,
						new ArrayList<IRHelpKeyword>()));
			}
			return;
		}
		else {
			IRHelpKeywordNode node = fKeywordGroups.get(path[0]);
			int i = 1;
			while (node != null) {
				if (i == path.length-1) {
					if (path[i].length() > 0) {
						final String key = path[i].intern();
						node.getNestedKeywords().add(new RHelpKeyword(key, description,
								new ArrayList<IRHelpKeyword>()));
						return;
					}
				}
				else {
					node = node.getNestedKeyword(path[i++]);
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
	public boolean checkPackage(final String name, final String version) {
		if (IGNORE_PKG_NAMES.contains(name)) {
			return true;
		}
		synchronized (fPackages) {
			if (fPackages.containsKey(name)) {
				return true;
			}
			final IRPackageHelp packageHelp = fExistingPackages.remove(name);
			if (!fReset && packageHelp != null && packageHelp.getVersion().equals(version)) {
				fPackages.put(name, packageHelp); // reuse
				return true;
			}
			fPackages.put(name, null); // placeholder
			return false;
		}
	}
	
	public void beginPackage(final RPackageDescription packageDesription) throws AbortIndexException {
		final String name = packageDesription.getName();
		if (fCurrentPackage != null) {
			throw new IllegalArgumentException();
		}
		try {
			fStatus.add(new Status(IStatus.INFO, RCore.PLUGIN_ID, "Beginning package: '" + name + "'.")); //$NON-NLS-1$ //$NON-NLS-2$
			
			fCurrentPackage = new RPackageHelp(name, packageDesription.getTitle(),
					packageDesription.getVersion().toString(),
					fREnvConfig.getReference());
			synchronized (fPackages) {
				fExistingPackages.remove(name);
				fPackages.put(name, fCurrentPackage);
			}
			fLuceneWriter.deleteDocuments(new Term(PACKAGE_FIELD_NAME, name));
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
		if (fCurrentPackage == null || !fCurrentPackage.getName().equals(item.getPkg())) {
			throw new IllegalArgumentException();
		}
		try {
			fCurrentPackage.addPage(new RHelpPage(fCurrentPackage, item.getName(), item.getTitle()));
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
			fStatus.add(new Status(IStatus.INFO, RCore.PLUGIN_ID, "Finishing package.")); //$NON-NLS-1$
			
			final Runtime runtime = Runtime.getRuntime();
			final long maxMemory = runtime.maxMemory();
			final long allocatedMemory = runtime.totalMemory();
			final long freeMemory = runtime.freeMemory();
			final IndexWriterConfig config = fLuceneWriter.getConfig();
			final StringBuilder sb = new StringBuilder("Memory status:\n"); //$NON-NLS-1$
			sb.append("TempBuilder-capycity: ").append(fTempBuilder.capacity()).append('\n'); //$NON-NLS-1$
			sb.append("Lucene-buffersize: ").append((long) (config.getRAMBufferSizeMB() * 1024.0)).append('\n'); //$NON-NLS-1$
			sb.append("Memory-free: ").append(freeMemory / 1024L).append('\n'); //$NON-NLS-1$
			sb.append("Memory-total: ").append(allocatedMemory / 1024L).append('\n'); //$NON-NLS-1$
			sb.append("Memory-max: ").append(maxMemory / 1024L).append('\n'); //$NON-NLS-1$
			fStatus.add(new Status(IStatus.INFO, RCore.PLUGIN_ID, sb.toString()));
		}
		
		if (fCurrentPackage == null) {
			return;
		}
		fCurrentPackage.freeze();
		fCurrentPackage = null;
	}
	
	public IStatus endBatch() throws AbortIndexException {
		if (fLuceneWriter == null) {
			return null;
		}
		
		final MultiStatus status = fStatus;
		fStatus = null;
		if (status != null) {
			status.add(new Status(IStatus.INFO, RCore.PLUGIN_ID, -1, "Finishing batch.", null)); //$NON-NLS-1$
			RCorePlugin.log(status);
		}
		try {
			final RHelpManager rHelpManager = RCorePlugin.getDefault().getRHelpManager();
			
			for (final String packageName : fExistingPackages.keySet()) {
				fLuceneWriter.deleteDocuments(new Term(PACKAGE_FIELD_NAME, packageName));
			}
			fExistingPackages.clear();
			
			final Collection<RHelpKeywordGroup> values = fKeywordGroups.values();
			for (final RHelpKeywordGroup group : values) {
				group.freeze();
			}
			
			final ConstList<Group> keywords = new ConstList<IRHelpKeyword.Group>(values.toArray(new IRHelpKeyword.Group[values.size()]));
			for (final Iterator<IRPackageHelp> iter = fPackages.values().iterator(); iter.hasNext(); ) {
				if (iter.next() == null) {
					iter.remove();
				}
			}
			final IRPackageHelp[] array = fPackages.values().toArray(new IRPackageHelp[fPackages.size()]);
			Arrays.sort(array);
			final ConstList<IRPackageHelp> packages = new ConstList<IRPackageHelp>(array);
			
			final REnvHelp help = new REnvHelp(fREnvConfig.getReference(), fDocDir, keywords, packages);
			
			fLuceneWriter.optimize();
			
			synchronized (fIndexLock) {
				fLuceneWriter.close();
				
				rHelpManager.updateHelp(fREnvConfig, fREnvSharedProperties, help);
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
	
	private void addToLucene(final IRPackageDescription item) throws CorruptIndexException, IOException {
		final Document doc = new Document();
		doc.add(new Field(DOCTYPE_FIELD_NAME, PACKAGE_DOC_TYPE, Store.YES, Index.NOT_ANALYZED));
		doc.add(new Field(PACKAGE_FIELD_NAME, item.getName(), Store.YES, Index.NOT_ANALYZED));
		doc.add(new Field(DESCRIPTION_TXT_FIELD_NAME, item.getDescription(), Store.YES, Index.ANALYZED,
				TermVector.WITH_POSITIONS_OFFSETS));
		if (item.getPriority() != null) {
			doc.add(new Field(PKG_PRIORITY_FIELD_NAME, item.getPriority(), Store.YES, Index.NO));
		}
		if (item.getAuthor() != null) {
			doc.add(new Field(AUTHORS_TXT_FIELD_NAME, item.getAuthor(), Store.YES, Index.ANALYZED,
					TermVector.WITH_POSITIONS_OFFSETS));
		}
		if (item.getMaintainer() != null) {
			doc.add(new Field(MAINTAINER_TXT_FIELD_NAME, item.getMaintainer(), Store.YES, Index.ANALYZED,
					TermVector.WITH_POSITIONS_OFFSETS));
		}
		if (item.getUrl() != null) {
			doc.add(new Field(URL_TXT_FIELD_NAME, item.getUrl(), Store.YES, Index.ANALYZED,
					TermVector.WITH_POSITIONS_OFFSETS));
		}
		fLuceneWriter.addDocument(doc);
	}
	
	private void addToLucene(final RdItem item) throws CorruptIndexException, IOException {
		final Document doc = new Document();
		doc.add(new Field(DOCTYPE_FIELD_NAME, PAGE_DOC_TYPE, Store.YES, Index.NOT_ANALYZED));
		doc.add(new Field(PACKAGE_FIELD_NAME, item.getPkg(), Store.YES, Index.NOT_ANALYZED));
		doc.add(new Field(PAGE_FIELD_NAME, item.getName(), Store.YES, Index.NOT_ANALYZED));
		doc.add(new Field(TITLE_TXT_FIELD_NAME, item.getTitle(), Store.YES, Index.ANALYZED,
				TermVector.WITH_POSITIONS_OFFSETS));
		if (item.fTopics != null) {
			final List<String> topics = item.fTopics;
			for (int i = 0; i < topics.size(); i++) {
				doc.add(new Field(ALIAS_FIELD_NAME, topics.get(i), Store.YES, Index.NOT_ANALYZED));
				doc.add(new Field(ALIAS_TXT_FIELD_NAME, topics.get(i), Store.YES, Index.ANALYZED,
						TermVector.WITH_POSITIONS_OFFSETS));
			}
		}
		if (item.fKeywords != null) {
			final List<String> keywords = item.fKeywords;
			for (int i = 0; i < keywords.size(); i++) {
				doc.add(new Field(KEYWORD_FIELD_NAME, keywords.get(i), Store.NO, Index.NOT_ANALYZED));
			}
		}
		if (item.fConcepts != null) {
			final List<String> concepts = item.fConcepts;
			for (int i = 0; i < concepts.size(); i++) {
				doc.add(new Field(CONCEPT_TXT_FIELD_NAME, concepts.get(i), Store.YES, Index.ANALYZED,
						TermVector.WITH_POSITIONS_OFFSETS));
			}
		}
		if (item.fHtml != null) {
			doc.add(new Field(DOC_HTML_FIELD_NAME, item.fHtml, Store.YES, Index.ANALYZED,
					TermVector.WITH_POSITIONS_OFFSETS));
			createSectionsTxt(item);
			if (item.descrTxt != null) {
				doc.add(new Field(DESCRIPTION_TXT_FIELD_NAME, item.descrTxt, Store.YES, Index.ANALYZED,
						TermVector.WITH_POSITIONS_OFFSETS));
			}
			doc.add(new Field(DOC_TXT_FIELD_NAME, item.mainTxt, Store.YES, Index.ANALYZED,
					TermVector.WITH_POSITIONS_OFFSETS));
			if (item.examplesTxt != null) {
				final Field field = new Field(EXAMPLES_TXT_FIELD_NAME, item.examplesTxt, Store.YES,
						Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS);
				field.setBoost(0.5f);
				doc.add(field);
			}
		}
		fLuceneWriter.addDocument(doc);
	}
	
	private void createSectionsTxt(final RdItem item) throws IOException {
		String html = item.fHtml;
		fTempBuilder.setLength(0);
		{	final int idx1 = html.indexOf("</h2>");
			if (idx1 >= 0) {
				html = html.substring(idx1+5);
			}
		}
		{	final int idx1 = html.lastIndexOf("<hr/>");
			if (idx1 >= 0) {
				html = html.substring(0, idx1);
			}
		}
		{	int idxBegin = html.indexOf("<h3 id=\"description\"");
			if (idxBegin >= 0) {
				idxBegin = html.indexOf('>', idxBegin+20);
				if (idxBegin >= 0) {
					idxBegin = html.indexOf("</h3>", idxBegin+1);
					if (idxBegin >= 0) {
						idxBegin += 5;
						int idxEnd = html.indexOf("<h3", idxBegin);
						if (idxEnd < 0) {
							idxEnd = html.indexOf("<hr/>", idxBegin);
						}
						if (idxEnd >= 0) {
							item.descrTxt = html2txt(html.substring(idxBegin, idxEnd));
							html = html.substring(idxEnd);
						}
					}
				}
			}
		}
		final String[] s = new String[] { html, null };
		{	if (extract(s, "<h3 id=\"examples\"")) {
				item.examplesTxt = html2txt(s[1]);
			}
		}
		item.mainTxt = html2txt(s[0]);
	}
	
	private boolean extract(final String[] s, final String h3) {
		final String html = s[0];
		final int idx0 = html.indexOf(h3);
		if (idx0 >= 0) {
			int idxBegin = html.indexOf('>', idx0+h3.length());
			if (idxBegin >= 0) {
				idxBegin = html.indexOf("</h3>", idxBegin+1);
				if (idxBegin >= 0) {
					idxBegin += 5;
					final int idxEnd = html.indexOf("<h3", idxBegin);
					if (idxEnd >= 0) {
						fTempBuilder.setLength(0);
						fTempBuilder.append(html, 0, idx0);
						fTempBuilder.append(html, idxEnd, html.length());
						s[0] = fTempBuilder.toString();
						s[1] = html.substring(idxBegin, idxEnd);
					}
					else {
						s[0] = html.substring(0, idx0);
						s[1] = html.substring(idxBegin, html.length());
					}
					return true;
				}
			}
		}
		return false;
	}
	
	private String html2txt(final String html) {
		fTempBuilder.setLength(0);
		fTempHtmlInput.reset(html);
		int c;
		boolean blank = true;
		while ((c = fTempHtmlInput.get(1)) >= 0) {
			if (c <= 0x20) {
				if (!blank) {
					blank = true;
					fTempBuilder.append(' ');
				}
			}
			else {
				if (blank) {
					blank = false;
				}
				fTempBuilder.append((char) c);
			}
			fTempHtmlInput.consume(1);
		}
		c = fTempBuilder.length();
		return (c > 0 && fTempBuilder.charAt(c-1) == ' ') ?
				fTempBuilder.substring(0, c-1) : fTempBuilder.toString();
	}
	
	public IStatus cancel() {
		final MultiStatus status;
		try {
			if (fLuceneWriter != null) {
				try {
					fLuceneWriter.rollback();
				}
				catch (final Exception e) {
					if (fStatus != null) {
						fStatus.add(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, "Error when rolling back.", null)); //$NON-NLS-1$
					}
				}
				
				fLuceneWriter.close();
			}
		}
		catch (final Exception close) {
		}
		finally {
			clear();
			
			status = fStatus;
			fStatus = null;
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
		if (fLuceneWriter != null) {
			try {
				if (IndexWriter.isLocked(fLuceneDirectory)) {
					IndexWriter.unlock(fLuceneDirectory);
				}
			} catch (final Exception ignore) {}
		}
		fLuceneWriter = null;
		fLuceneDirectory = null;
		fCurrentPackage = null;
		fIndexLock = null;
	}
	
}
