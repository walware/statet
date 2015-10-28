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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.primitives.IntList;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.DefaultEncoder;
import org.apache.lucene.search.highlight.Encoder;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImSet;

import de.walware.rj.renv.IRPkgDescription;
import de.walware.rj.renv.RNumVersion;
import de.walware.rj.renv.RPkgDescription;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRHelpSearchRequestor;
import de.walware.statet.r.core.rhelp.IRPkgHelp;
import de.walware.statet.r.core.rhelp.RHelpSearchQuery;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.rhelp.RHelpSearchMatch;
import de.walware.statet.r.internal.core.rhelp.RHelpTopicEntry;


public class REnvIndexReader implements IREnvIndex {
	
	
	private static final ImSet<String> LOAD_NAME_SELECTOR= ImCollections.newSet(
			PAGE_FIELD_NAME );
	
	private static final ImSet<String> LOAD_ID_SELECTOR= ImCollections.newSet(
			PAGE_FIELD_NAME,
			PACKAGE_FIELD_NAME );
	
	private static final ImSet<String> LOAD_HTML_SELECTOR= ImCollections.newSet(
			DOC_HTML_FIELD_NAME );
	
	private static final ImSet<String> LOAD_PKG_TOPICS_SELECTOR= ImCollections.newSet(
			PAGE_FIELD_NAME,
			ALIAS_FIELD_NAME );
	
	private static final ImSet<String> LOAD_PKG_DESCRIPTION_SELECTOR= ImCollections.newSet(
			DESCRIPTION_TXT_FIELD_NAME,
			AUTHORS_TXT_FIELD_NAME,
			MAINTAINER_TXT_FIELD_NAME,
			URL_TXT_FIELD_NAME );
	
	
	private static final String[] DOC_HTML_SEARCH_FIELDS= new String[] {
			DOC_HTML_FIELD_NAME };
	
	
	static final NumericRangeQuery<Integer> DOCTYPE_PKG_DESCRIPTION_QUERY= NumericRangeQuery.newIntRange(
			DOCTYPE_FIELD_NAME, Integer.MAX_VALUE, PKG_DESCRIPTION_DOCTYPE, PKG_DESCRIPTION_DOCTYPE, true, true );
	
	static final NumericRangeQuery<Integer> DOCTYPE_PAGE_QUERY= NumericRangeQuery.newIntRange(
			DOCTYPE_FIELD_NAME, Integer.MAX_VALUE, PAGE_DOCTYPE, PAGE_DOCTYPE, true, true );
	
	
	private static final FastVectorHighlighter HTML_PAGE_QUERY_HIGHLIGHTER= new FastVectorHighlighter(true, true, null, null);
	
	static final Encoder DEFAULT_ENCODER= new DefaultEncoder();
	
	
	private final IndexReader indexReader;
	private IndexSearcher indexSearcher;
	
	
	public REnvIndexReader(final IREnvConfiguration rEnvConfig) throws Exception {
//		NIOFSDirectory doesn't like Thread#interrupt() used by the information hover manager
//		final FSDirectory directory= FSDirectory.open(SaveUtil.getIndexDirectory(rEnvConfig));
		final FSDirectory directory= new SimpleFSDirectory(REnvIndexWriter.getIndexDirectory(rEnvConfig), null);
		this.indexReader= DirectoryReader.open(directory);
		this.indexSearcher= new IndexSearcher(this.indexReader);
		this.indexSearcher.setSimilarity(SIMILARITY);
	}
	
	
	private void check() {
		if (this.indexSearcher == null) {
			throw new IllegalStateException();
		}
	}
	
	public void dispose() {
		if (this.indexSearcher != null) {
			try {
				this.indexReader.close();
			}
			catch (final IOException e) {
				RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
						"An error occurred when disposing searcher for the R help index.", e)); //$NON-NLS-1$
			}
			finally {
				this.indexSearcher= null;
			}
		}
	}
	
	public IRHelpPage getPageForTopic(final IRPkgHelp pkgHelp, final String topic) {
		check();
		try {
			final BooleanQuery q= new BooleanQuery(true);
			q.add(DOCTYPE_PAGE_QUERY, Occur.MUST);
			q.add(new TermQuery(new Term(PACKAGE_FIELD_NAME, pkgHelp.getName())), Occur.MUST);
			q.add(new TermQuery(new Term(ALIAS_FIELD_NAME, topic)), Occur.MUST);
			final TopDocs docs= this.indexSearcher.search(q, null, 1);
			if (docs.totalHits > 1) {
				RCorePlugin.log(new Status(IStatus.WARNING, RCore.PLUGIN_ID,
						NLS.bind("Unexpected search result: total hits= {0}; in search: {1}." + //$NON-NLS-1$
								docs.totalHits, getPageForTopicDescription(pkgHelp, topic) )));
			}
			if (docs.totalHits >= 1) {
				return pkgHelp.getHelpPage(this.indexSearcher.doc(docs.scoreDocs[0].doc, LOAD_NAME_SELECTOR).
						get(PAGE_FIELD_NAME) );
			}
			return null;
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					NLS.bind("An error occurred in search: {0}.", //$NON-NLS-1$
							getPageForTopicDescription(pkgHelp, topic) ),
					e ));
			throw new RuntimeException("R help index search error.");
		}
	}
	
	private String getPageForTopicDescription(final IRPkgHelp pkgHelp, final String topic) {
		final String packageName= (pkgHelp != null) ? pkgHelp.getName() : "<null>"; //$NON-NLS-1$
		return "#getPageForTopic '" + packageName + "', '" + topic + "'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	public List<IRHelpPage> getPagesForTopic(final String topic, final Map<String, IRPkgHelp> packageMap) {
		check();
		try {
			final BooleanQuery q= new BooleanQuery(true);
			q.add(DOCTYPE_PAGE_QUERY, Occur.MUST);
			q.add(new TermQuery(new Term(ALIAS_FIELD_NAME, topic)), Occur.MUST);
			final AllDocCollector collector= new AllDocCollector();
			this.indexSearcher.search(q, collector);
			final IntList docs= collector.getDocs();
			final int count= docs.size();
			final ArrayList<IRHelpPage> pages= new ArrayList<>(count);
			for (int i= 0; i < count; i++) {
				final Document document= this.indexSearcher.doc(docs.get(i), LOAD_ID_SELECTOR);
				final IRPkgHelp pkgHelp= packageMap.get(document.get(PACKAGE_FIELD_NAME));
				final String name= document.get(PAGE_FIELD_NAME);
				if (name == null) {
					continue;
				}
				if (pkgHelp != null) {
					final IRHelpPage page= pkgHelp.getHelpPage(name);
					if (page != null) {
						pages.add(page);
						continue;
					}
				}
				RCorePlugin.log(new Status(IStatus.WARNING, RCore.PLUGIN_ID,
						"Unexpected search result: page '"+ document.get(PACKAGE_FIELD_NAME) + "::" +
						document.get(PAGE_FIELD_NAME) + "' object not found; in search: " +
						getPagesForTopicDescription(topic) + "." ));
			}
			return pages;
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"An error occurred in search: " + getPagesForTopicDescription(topic), e));
			throw new RuntimeException("R help index search error.");
		}
	}
	
	private String getPagesForTopicDescription(final String topic) {
		return "#getPagesForTopic '" + topic + "'"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public String getHtmlPage(final String packageName, final String pageName,
			final String queryString, final String[] preTags, final String[] postTags) {
		check();
		try {
			final BooleanQuery q= new BooleanQuery(true);
			q.add(DOCTYPE_PAGE_QUERY, Occur.MUST);
			q.add(new TermQuery(new Term(PACKAGE_FIELD_NAME, packageName)), Occur.MUST);
			q.add(new TermQuery(new Term(PAGE_FIELD_NAME, pageName)), Occur.MUST);
			final TopDocs docs= this.indexSearcher.search(q, null, 1);
			if (docs.totalHits > 1) {
				RCorePlugin.log(new Status(IStatus.WARNING, RCore.PLUGIN_ID,
						NLS.bind("Unexpected search result: total hits= {0}; in search: {1}.", //$NON-NLS-1$
								docs.totalHits, getHtmlPageDescription(packageName, pageName) )));
			}
			
			if (docs.totalHits >= 1) {
				final int docId= docs.scoreDocs[0].doc;
				final Document document= this.indexSearcher.doc(docs.scoreDocs[0].doc, LOAD_HTML_SELECTOR);
				
				if (queryString != null && queryString.length() > 0) {
					final FieldQuery fieldQuery= HTML_PAGE_QUERY_HIGHLIGHTER.getFieldQuery(
							SearchQuery.createMainQuery(DOC_HTML_SEARCH_FIELDS, queryString),
							this.indexReader );
					return HTML_PAGE_QUERY_HIGHLIGHTER.getComplete(fieldQuery, this.indexReader, docId,
							DOC_HTML_FIELD_NAME,
							preTags, postTags, DEFAULT_ENCODER );
				}
				else {
					return document.get(DOC_HTML_FIELD_NAME);
				}
			}
			return null;
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					NLS.bind("An error occurred in search: {0}.", //$NON-NLS-1$
							getHtmlPageDescription(packageName, pageName) ),
					e ));
			throw new RuntimeException("R help index search error.");
		}
	}
	
	private String getHtmlPageDescription(final String packageName, final String pageName) {
		return "#getHtmlPage' " + packageName + "', '" + pageName + "'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	
	public boolean search(final RHelpSearchQuery.Compiled query,
			final List<IRPkgHelp> packageList, final Map<String, IRPkgHelp> packageMap,
			final IRHelpSearchRequestor requestor) {
		check();
		try {
			final SearchQuery internal= (SearchQuery) query.compiled();
			final BooleanQuery q= internal.luceneQuery;
			if (q.clauses().size() <= 1) {
				for (final IRPkgHelp pkgHelp : packageList) {
					for (final IRHelpPage page : pkgHelp.getHelpPages()) {
						requestor.matchFound(new RHelpSearchMatch(page, 1.0f));
					}
				}
			}
			else {
				final RequestStreamCollector collector= new RequestStreamCollector(packageMap,
						internal, this.indexReader, requestor );
				this.indexSearcher.search(q, collector);
			}
			return true;
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"An error occurred in search: " + query.toString() + ".", e));
			return false;
		}
	}
	
	
	public List<RHelpTopicEntry> getPackageTopics(final IRPkgHelp pkgHelp) {
		final List<RHelpTopicEntry> list= new ArrayList<>(64);
		try {
			final BooleanQuery q= new BooleanQuery(true);
			q.add(DOCTYPE_PAGE_QUERY, Occur.MUST);
			q.add(new TermQuery(new Term(PACKAGE_FIELD_NAME, pkgHelp.getName())), Occur.MUST);
			this.indexSearcher.search(q, new Collector() {
				
				private Scorer scorer;
				
				private AtomicReader reader;
				private int docBase;
				
				@Override
				public void setScorer(final Scorer scorer) throws IOException {
					this.scorer= scorer;
				}
				
				@Override
				public boolean acceptsDocsOutOfOrder() {
					return true;
				}
				
				@Override
				public void setNextReader(final AtomicReaderContext context) throws IOException {
					this.reader= context.reader();
					this.docBase= context.docBase;
				}
				
				@Override
				public void collect(final int doc) throws IOException {
					if (this.scorer.score() > 0.0f) {
						// TODO: reader#document not recommend
						final Document document= this.reader.document(doc, LOAD_PKG_TOPICS_SELECTOR);
						final String pageName= document.get(PAGE_FIELD_NAME);
						final IRHelpPage page= pkgHelp.getHelpPage(pageName);
						final String[] topics= document.getValues(ALIAS_FIELD_NAME);
						for (int i= 0; i < topics.length; i++) {
							list.add(new RHelpTopicEntry(topics[i], page));
						}
					}
				}
				
			});
			Collections.sort(list);
			return list;
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					NLS.bind("An error occurred in search: {0}.", //$NON-NLS-1$
							getPackageTopicsDescription(pkgHelp.getName()) ),
					e ));
			throw new RuntimeException("R help index search error.");
		}
	}
	
	private String getPackageTopicsDescription(final String packageName) {
		return "#getPackageTopicsDescription '" + packageName + "'"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public IRPkgDescription getPkgDescription(final IRPkgHelp pkgHelp) {
		try {
			final BooleanQuery q= new BooleanQuery(true);
			q.add(DOCTYPE_PKG_DESCRIPTION_QUERY, Occur.MUST);
			q.add(new TermQuery(new Term(PACKAGE_FIELD_NAME, pkgHelp.getName())), Occur.MUST);
			final TopDocs docs= this.indexSearcher.search(q, null, 1);
			if (docs.totalHits > 1) {
				RCorePlugin.log(new Status(IStatus.WARNING, RCore.PLUGIN_ID,
						NLS.bind("Unexpected search result: total hits= {0}; in search: {1}.", //$NON-NLS-1$
								docs.totalHits, getPkgDescriptionDescription(pkgHelp) )));
			}
			if (docs.totalHits >= 1) {
				final int docId= docs.scoreDocs[0].doc;
				final Document document= this.indexSearcher.doc(docId, LOAD_PKG_DESCRIPTION_SELECTOR);
				
				return new RPkgDescription(pkgHelp.getName(), 
						RNumVersion.create(pkgHelp.getVersion()),
						pkgHelp.getTitle(),
						document.get(DESCRIPTION_TXT_FIELD_NAME),
						document.get(AUTHORS_TXT_FIELD_NAME),
						document.get(MAINTAINER_TXT_FIELD_NAME),
						document.get(URL_TXT_FIELD_NAME),
						pkgHelp.getBuilt() );
			}
			return null;
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					NLS.bind("An error occurred in search: {0}.", //$NON-NLS-1$
							getPkgDescriptionDescription(pkgHelp) ),
					e ));
			return null;
		}
	}
	
	private String getPkgDescriptionDescription(final IRPkgHelp pkgHelp) {
		final String packageName= (pkgHelp != null) ? pkgHelp.getName() : "<null>"; //$NON-NLS-1$
		return "#getPkgDescription '" + packageName + "'"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
}
