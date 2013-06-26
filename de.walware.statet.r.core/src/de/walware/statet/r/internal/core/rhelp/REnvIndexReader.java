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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.primitives.IntList;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.extra.queryParser.ExtendedQueryParser;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.rj.renv.IRPackageDescription;
import de.walware.rj.renv.RNumVersion;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRHelpSearchRequestor;
import de.walware.statet.r.core.rhelp.IRPackageHelp;
import de.walware.statet.r.core.rhelp.RHelpSearchQuery;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.RPackageDescription;


public class REnvIndexReader implements IREnvIndex {
	
	
	private static final FieldSelector LOAD_NAME_SELECTOR = new FieldSelector() {
		private static final long serialVersionUID = 1L;
		@Override
		public FieldSelectorResult accept(final String fieldName) {
			if (fieldName == PAGE_FIELD_NAME) {
				return FieldSelectorResult.LOAD;
			}
			return FieldSelectorResult.NO_LOAD;
		}
	};
	private static final FieldSelector LOAD_ID_SELECTOR = new FieldSelector() {
		private static final long serialVersionUID = 1L;
		@Override
		public FieldSelectorResult accept(final String fieldName) {
			if (fieldName == PAGE_FIELD_NAME
					|| fieldName == PACKAGE_FIELD_NAME) {
				return FieldSelectorResult.LOAD;
			}
			return FieldSelectorResult.NO_LOAD;
		}
	};
	private static final FieldSelector LOAD_HTML_SELECTOR = new FieldSelector() {
		private static final long serialVersionUID = 1L;
		@Override
		public FieldSelectorResult accept(final String fieldName) {
			if (fieldName == DOC_HTML_FIELD_NAME) {
				return FieldSelectorResult.LOAD;
			}
			return FieldSelectorResult.NO_LOAD;
		}
	};
	
	private static final FieldSelector LOAD_PKG_TOPICS_SELECTOR = new FieldSelector() {
		private static final long serialVersionUID = 1L;
		@Override
		public FieldSelectorResult accept(final String fieldName) {
			if (fieldName == PAGE_FIELD_NAME
					|| fieldName == ALIAS_FIELD_NAME) {
				return FieldSelectorResult.LOAD;
			}
			return FieldSelectorResult.NO_LOAD;
		}
	};
	
	private static final FieldSelector LOAD_PKG_DESCRIPTION_SELECTOR = new FieldSelector() {
		private static final long serialVersionUID = 1L;
		@Override
		public FieldSelectorResult accept(final String fieldName) {
			if (fieldName == DESCRIPTION_TXT_FIELD_NAME
					|| fieldName == PKG_PRIORITY_FIELD_NAME
					|| fieldName == AUTHORS_TXT_FIELD_NAME
					|| fieldName == MAINTAINER_TXT_FIELD_NAME
					|| fieldName == URL_TXT_FIELD_NAME) {
				return FieldSelectorResult.LOAD;
			}
			return FieldSelectorResult.NO_LOAD;
		}
	};
	
	private static final String[] TOPIC_SEARCH_FIELDS = new String[] {
		ALIAS_FIELD_NAME
	};
	private static final String[] DOC_SEARCH_FIELDS = new String[] {
		TITLE_TXT_FIELD_NAME, DESCRIPTION_TXT_FIELD_NAME,
		DOC_TXT_FIELD_NAME,
		EXAMPLES_TXT_FIELD_NAME,
	};
	private static final String[] DOC_HTML_SEARCH_FIELDS = new String[] {
		DOC_HTML_FIELD_NAME
	};
	private static final String[] NO_FIELDS = new String[0];
	
	public static class InternalQuery {
		
		public final String[] fieldNames;
		
		public final BooleanQuery luceneQuery;
		
		public InternalQuery(final String[] fieldNames, final BooleanQuery luceneQuery) {
			this.fieldNames = fieldNames;
			this.luceneQuery = luceneQuery;
		}
		
	}
	
	
	public static InternalQuery compile(final RHelpSearchQuery query) throws CoreException {
		try {
			final BooleanQuery q = new BooleanQuery();
			q.add(new TermQuery(new Term(DOCTYPE_FIELD_NAME, PAGE_DOC_TYPE)), Occur.MUST);
			String[] fieldNames = NO_FIELDS;
			if (query.getSearchString().length() > 0) {
				switch (query.getSearchType()) {
				case RHelpSearchQuery.TOPIC_SEARCH:
					fieldNames = TOPIC_SEARCH_FIELDS;
					q.add(createMainQuery(fieldNames, query.getSearchString()), Occur.MUST);
					break;
				case RHelpSearchQuery.FIELD_SEARCH:
					fieldNames = sortFields(query.getEnabledFields());
					if (fieldNames.length == 0) {
						break;
					}
					q.add(createMainQuery(fieldNames, query.getSearchString()), Occur.MUST);
					break;
				case RHelpSearchQuery.DOC_SEARCH:
					fieldNames = DOC_SEARCH_FIELDS;
					q.add(createMainQuery(fieldNames, query.getSearchString()), Occur.MUST);
					break;
				default:
					break;
				}
			}
			
			final List<String> keywords = query.getKeywords();
			if (!keywords.isEmpty()) {
				q.add(createOrQuery(KEYWORD_FIELD_NAME, keywords), Occur.MUST);
			}
			final List<String> packages = query.getPackages();
			if (!packages.isEmpty()) {
				q.add(createOrQuery(PACKAGE_FIELD_NAME, packages), Occur.MUST);
			}
			
			return new InternalQuery(fieldNames, q);
		}
		catch (final ParseException e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"An error occurred when creating the Lucene query for: " + query.toString() + ".", e));
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"The search string is invalid: " + e.getLocalizedMessage(), null));
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"An error occurred when creating the Lucene query for: " + query.toString() + ".", e));
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"An error occurred when preparing the R help query.", null));
		}
	}
	
	private static int getRank(final String o) {
		if (o == PAGE_FIELD_NAME) {
			return 1;
		}
		if (o == ALIAS_FIELD_NAME || o == ALIAS_TXT_FIELD_NAME) {
			return 2;
		}
		if (o == TITLE_TXT_FIELD_NAME) {
			return 3;
		}
		if (o == CONCEPT_TXT_FIELD_NAME) {
			return 4;
		}
		if (o == DESCRIPTION_TXT_FIELD_NAME) {
			return 5;
		}
		if (o == DOC_TXT_FIELD_NAME || o == DOC_HTML_FIELD_NAME) {
			return 6;
		}
		if (o == EXAMPLES_TXT_FIELD_NAME) {
			return 15;
		}
		return 10;
	}
	
	private static String[] sortFields(final List<String> fieldNames) {
		final String[] array = fieldNames.toArray(new String[fieldNames.size()]);
		Arrays.sort(array, new Comparator<String>() {
			@Override
			public int compare(final String o1, final String o2) {
				return getRank(o1) - getRank(o2);
			}
		});
		return array;
	}
	
	private static Query createMainQuery(final String fields[], final String queryText) throws ParseException {
		final QueryParser p = new ExtendedQueryParser(Version.LUCENE_31, fields, READ_ANALYZER);
		p.setDefaultOperator(Operator.AND);
		p.setAllowLeadingWildcard(true);
		return p.parse(queryText);
	}
	
	private static Query createOrQuery(final String field, final List<String> terms) {
		if (terms.size() == 1) {
			return new TermQuery(new Term(field, terms.get(0)));
		}
		else if (terms.size() > 1) {
			final BooleanQuery q = new BooleanQuery();
			for (final String keyword : terms) {
				q.add(new TermQuery(new Term(field, keyword)), Occur.SHOULD);
			}
			return q;
		}
		return null;
	}
	
	
	private final IndexReader fIndexReader;
	private IndexSearcher fIndexSearcher;
	
	
	REnvIndexReader(final IREnvConfiguration rEnvConfig) throws Exception {
//		NIOFSDirectory doesn't like Thread#interrupt() used by the information hover manager
//		final FSDirectory directory = FSDirectory.open(SaveUtil.getIndexDirectory(rEnvConfig));
		final FSDirectory directory = new SimpleFSDirectory(SaveUtil.getIndexDirectory(rEnvConfig), null);
		fIndexReader = IndexReader.open(directory, true);
		fIndexSearcher = new IndexSearcher(fIndexReader);
	}
	
	
	private void check() {
		if (fIndexSearcher == null) {
			throw new IllegalStateException();
		}
	}
	
	public void dispose() {
		if (fIndexSearcher != null) {
			try {
				fIndexSearcher.close();
			}
			catch (final IOException e) {
				RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
						"An error occurred when disposing searcher for the R help index.", e));
			}
			fIndexSearcher = null;
		}
	}
	
	public IRHelpPage getPageForTopic(final IRPackageHelp packageHelp, final String topic) {
		check();
		try {
			final BooleanQuery q = new BooleanQuery(true);
			q.add(new TermQuery(new Term(DOCTYPE_FIELD_NAME, PAGE_DOC_TYPE)), Occur.MUST);
			q.add(new TermQuery(new Term(PACKAGE_FIELD_NAME, packageHelp.getName())), Occur.MUST);
			q.add(new TermQuery(new Term(ALIAS_FIELD_NAME, topic)), Occur.MUST);
			final TopDocs docs = fIndexSearcher.search(q, null, 1);
			if (docs.totalHits > 1) {
				RCorePlugin.log(new Status(IStatus.WARNING, RCore.PLUGIN_ID,
						"Unexpected search result: total hits = " + docs.totalHits + "; in search: " +
						getPageForTopicDescription(packageHelp, topic) + "." ));
			}
			if (docs.totalHits >= 1) {
				return packageHelp.getHelpPage(fIndexSearcher.doc(docs.scoreDocs[0].doc, LOAD_NAME_SELECTOR).
						get(PAGE_FIELD_NAME) );
			}
			return null;
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"An error occurred in search: " + getPageForTopicDescription(packageHelp, topic), e));
			throw new RuntimeException("R help index search error.");
		}
	}
	
	private String getPageForTopicDescription(final IRPackageHelp packageHelp, final String topic) {
		final String packageName = (packageHelp != null) ? packageHelp.getName() : "<null>";
		return "#getPageForTopic '" + packageName + "', '" + topic + "'";
	}
	
	public List<IRHelpPage> getPagesForTopic(final String topic, final Map<String, IRPackageHelp> packageMap) {
		check();
		try {
			final BooleanQuery q = new BooleanQuery(true);
			q.add(new TermQuery(new Term(DOCTYPE_FIELD_NAME, PAGE_DOC_TYPE)), Occur.MUST);
			q.add(new TermQuery(new Term(ALIAS_FIELD_NAME, topic)), Occur.MUST);
			final AllDocCollector collector = new AllDocCollector();
			fIndexSearcher.search(q, collector);
			final IntList docs = collector.getDocs();
			final int count = docs.size();
			final ArrayList<IRHelpPage> pages = new ArrayList<IRHelpPage>(count);
			for (int i = 0; i < count; i++) {
				final Document document = fIndexSearcher.doc(docs.get(i), LOAD_ID_SELECTOR);
				final IRPackageHelp packageHelp = packageMap.get(document.get(PACKAGE_FIELD_NAME));
				final String name = document.get(PAGE_FIELD_NAME);
				if (name == null) {
					continue;
				}
				if (packageHelp != null) {
					final IRHelpPage page = packageHelp.getHelpPage(name);
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
		return "#getPagesForTopic '" + topic + "'";
	}
	
	public String getHtmlPage(final String packageName, final String pageName,
			final String queryString, final String[] preTags, final String[] postTags) {
		check();
		try {
			final BooleanQuery q = new BooleanQuery(true);
			q.add(new TermQuery(new Term(DOCTYPE_FIELD_NAME, PAGE_DOC_TYPE)), Occur.MUST);
			q.add(new TermQuery(new Term(PACKAGE_FIELD_NAME, packageName)), Occur.MUST);
			q.add(new TermQuery(new Term(PAGE_FIELD_NAME, pageName)), Occur.MUST);
			final TopDocs docs = fIndexSearcher.search(q, null, 1);
			if (docs.totalHits > 1) {
				RCorePlugin.log(new Status(IStatus.WARNING, RCore.PLUGIN_ID,
						"Unexpected search result: total hits = " + docs.totalHits + "; in search: " +
						getHtmlPageDescription(packageName, pageName) + "." ));
			}
			
			if (docs.totalHits >= 1) {
				final int docId = docs.scoreDocs[0].doc;
				final Document document = fIndexSearcher.doc(docs.scoreDocs[0].doc, LOAD_HTML_SELECTOR);
				
				if (queryString != null && queryString.length() > 0) {
					final FastVectorHighlighter highlighter = new FastVectorHighlighter(true, true);
					highlighter.setTags(preTags, postTags);
					highlighter.setQuery(createMainQuery(DOC_HTML_SEARCH_FIELDS, queryString));
					return highlighter.getComplete(fIndexReader, docId, DOC_HTML_FIELD_NAME);
				}
				else {
					return document.get(DOC_HTML_FIELD_NAME);
				}
			}
			return null;
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"An error occurred in search: " + getHtmlPageDescription(packageName, pageName) + ".", e));
			throw new RuntimeException("R help index search error.");
		}
	}
	
	private String getHtmlPageDescription(final String packageName, final String pageName) {
		return "#getHtmlPage' " + packageName + "', '" + pageName + "'";
	}
	
	
	public boolean search(final RHelpSearchQuery.Compiled query,
			final List<IRPackageHelp> packageList, final Map<String, IRPackageHelp> packageMap,
			final IRHelpSearchRequestor requestor) {
		check();
		try {
			final InternalQuery internal = (InternalQuery) query.compiled();
			final BooleanQuery q = internal.luceneQuery;
			if (q.clauses().size() <= 1) {
				for (final IRPackageHelp packageHelp : packageList) {
					for (final IRHelpPage page : packageHelp.getHelpPages()) {
						requestor.matchFound(new RHelpSearchMatch(page, 1.0f));
					}
				}
			}
			else {
				final RequestStreamCollector collector = new RequestStreamCollector(packageMap,
						internal, requestor);
				fIndexSearcher.search(q, collector);
			}
			return true;
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"An error occurred in search: " + query.toString() + ".", e));
			return false;
		}
	}
	
	
	public List<RHelpTopicEntry> getPackageTopics(final IRPackageHelp packageHelp) {
		final List<RHelpTopicEntry> list = new ArrayList<RHelpTopicEntry>(64);
		try {
			final BooleanQuery q = new BooleanQuery(true);
			q.add(new TermQuery(new Term(DOCTYPE_FIELD_NAME, PAGE_DOC_TYPE)), Occur.MUST);
			q.add(new TermQuery(new Term(PACKAGE_FIELD_NAME, packageHelp.getName())), Occur.MUST);
			fIndexSearcher.search(q, new Collector() {
				
				private Scorer fScorer;
				
				private IndexReader fReader;
				private int fDocBase;
				
				@Override
				public void setScorer(final Scorer scorer) throws IOException {
					fScorer = scorer;
				}
				
				@Override
				public boolean acceptsDocsOutOfOrder() {
					return true;
				}
				
				@Override
				public void setNextReader(final IndexReader reader, final int docBase) throws IOException {
					fReader = reader;
					fDocBase = docBase;
				}
				
				@Override
				public void collect(final int doc) throws IOException {
					if (fScorer.score() > 0.0f) {
						final Document document = fReader.document(fDocBase + doc, LOAD_PKG_TOPICS_SELECTOR);
						final String pageName = document.get(PAGE_FIELD_NAME);
						final IRHelpPage page = packageHelp.getHelpPage(pageName);
						final String[] topics = document.getValues(ALIAS_FIELD_NAME);
						for (int i = 0; i < topics.length; i++) {
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
					"An error occurred in search: " + getPackageTopicsDescription(packageHelp.getName()) + ".", e));
			throw new RuntimeException("R help index search error.");
		}
	}
	
	private String getPackageTopicsDescription(final String packageName) {
		return "#getPackageTopicsDescription '" + packageName + "'";
	}
	
	public IRPackageDescription getPackageDescription(final String packageName, final String title, final String version) {
		try {
			final BooleanQuery q = new BooleanQuery(true);
			q.add(new TermQuery(new Term(DOCTYPE_FIELD_NAME, PACKAGE_DOC_TYPE)), Occur.MUST);
			q.add(new TermQuery(new Term(PACKAGE_FIELD_NAME, packageName)), Occur.MUST);
			final TopDocs docs = fIndexSearcher.search(q, null, 1);
			if (docs.totalHits > 1) {
				RCorePlugin.log(new Status(IStatus.WARNING, RCore.PLUGIN_ID,
						"Unexpected search result: total hits = " + docs.totalHits + "; in search: " +
						getPackageDescriptionDescription(packageName) + "." ));
			}
			if (docs.totalHits >= 1) {
				final int docId = docs.scoreDocs[0].doc;
				final Document document = fIndexSearcher.doc(docId, LOAD_PKG_DESCRIPTION_SELECTOR);
				
				return new RPackageDescription(packageName, title,
						document.get(DESCRIPTION_TXT_FIELD_NAME),
						RNumVersion.create(version), document.get(PKG_PRIORITY_FIELD_NAME),
						document.get(AUTHORS_TXT_FIELD_NAME),
						document.get(MAINTAINER_TXT_FIELD_NAME),
						document.get(URL_TXT_FIELD_NAME) );
			}
			return null;
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"An error occurred in search: " + getPackageDescriptionDescription(packageName) + ".", e));
			return null;
		}
	}
	
	private String getPackageDescriptionDescription(final String packageName) {
		return "#getPackageDescription '" + packageName + "'";
	}
	
}
