/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.DefaultEncoder;
import org.apache.lucene.search.highlight.Encoder;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.NumericUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;
import de.walware.jcommons.collections.ImSet;

import de.walware.alucene.queries.ChainedFilter;
import de.walware.alucene.queries.PrefixTermsFilter;
import de.walware.alucene.queries.TermFilter;
import de.walware.alucene.queries.TermsFilter;

import de.walware.rj.renv.IRPkgDescription;
import de.walware.rj.renv.RNumVersion;
import de.walware.rj.renv.RPkgDescription;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.rhelp.IREnvHelp;
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
	
	private static final ImSet<String> LOAD_TOPICS_SELECTOR= ImCollections.newSet(
			PACKAGE_FIELD_NAME,
			ALIAS_FIELD_NAME );
	
	private static final ImSet<String> LOAD_PKG_DESCRIPTION_SELECTOR= ImCollections.newSet(
			DESCRIPTION_TXT_FIELD_NAME,
			AUTHORS_TXT_FIELD_NAME,
			MAINTAINER_TXT_FIELD_NAME,
			URL_TXT_FIELD_NAME );
	
	
	private static final ImList<String> DOC_HTML_SEARCH_FIELDS= ImCollections.newList(
			DOC_HTML_FIELD_NAME );
	
	
	static final BytesRef[] toByteRefTerms(final List<String> stringValues) {
		final BytesRef[] terms= new BytesRef[stringValues.size()];
		for (int i = 0; i < terms.length; i++) {
			terms[i]= new BytesRef(stringValues.get(i));
		}
		return terms;
	}
	
	static final BytesRef toByteRefTerm(final String stringValue) {
		final BytesRef term= new BytesRef(stringValue);
		return term;
	}
	
	static final BytesRef toByteRefTerm(final int typeValue) {
		final BytesRefBuilder bytes= new BytesRefBuilder();
		NumericUtils.intToPrefixCoded(typeValue, 0, bytes);
		return bytes.toBytesRef();
	}
	
	static final Filter chainFilters(final List<Filter> filters) {
		final int count= filters.size();
		switch (count) {
		case 0:
			return null;
		case 1:
			return filters.get(0);
		default:
			return new ChainedFilter(filters.toArray(new Filter[count]), ChainedFilter.AND);
		}
	}
	static final Filter chainFilters(final Filter... filters) {
		final int count= filters.length;
		switch (count) {
		case 0:
			return null;
		case 1:
			return filters[0];
		default:
			return new ChainedFilter(filters, ChainedFilter.AND);
		}
	}
	
	
	static final Filter DOCTYPE_PKG_DESCRIPTION_FILTER= new TermFilter(DOCTYPE_FIELD_NAME,
			toByteRefTerm(PKG_DESCRIPTION_DOCTYPE) );
	
	static final Filter DOCTYPE_PAGE_FILTER= new TermFilter(DOCTYPE_FIELD_NAME,
			toByteRefTerm(PAGE_DOCTYPE) );
	
	
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
			final Filter filter= chainFilters(
					new TermFilter(PACKAGE_FIELD_NAME, toByteRefTerm(pkgHelp.getName())),
					new TermFilter(ALIAS_FIELD_NAME, toByteRefTerm(topic)),
					DOCTYPE_PAGE_FILTER );
			
			final TopDocs docs= this.indexSearcher.search(new MatchAllDocsQuery(), filter, 1);
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
			final Filter filter= chainFilters(
					new TermFilter(ALIAS_FIELD_NAME, toByteRefTerm(topic)),
					DOCTYPE_PAGE_FILTER );
			
			final List<IRHelpPage> pages= new ArrayList<>();
			this.indexSearcher.search(new MatchAllDocsQuery(), filter, new DocFieldVisitorCollector(
					new DocFieldVisitorCollector.Visitor(LOAD_ID_SELECTOR) {
				
				private String pkgName;
				private String pageName;
				
				@Override
				public void newDocMatch(final AtomicReader reader, final int doc, final float score) {
					this.pkgName= null;
					this.pageName= null;
				}
				@Override
				public void stringField(final FieldInfo fieldInfo, final String value) throws IOException {
					switch (fieldInfo.name) {
					case PACKAGE_FIELD_NAME:
						this.pkgName= value;
						return;
					case PAGE_FIELD_NAME:
						this.pageName= value;
						return;
					default:
						return;
					}
				}
				@Override
				public void finalizeDocMatch() {
					if (this.pkgName != null && this.pageName != null) {
						final IRPkgHelp pkgHelp= packageMap.get(this.pkgName);
						if (pkgHelp != null) {
							final IRHelpPage page= pkgHelp.getHelpPage(this.pageName);
							if (page != null) {
								pages.add(page);
							}
						}
						else {
							RCorePlugin.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, RCore.PLUGIN_ID,
									"Unexpected search result: page '" + this.pkgName + "?" + this.pageName + "' object not found;" +
									"in search: " + getPagesForTopicDescription(topic) + "." ));
						}
					}
				}
			} ));
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
			final Filter filter= chainFilters(
					new TermFilter(PACKAGE_FIELD_NAME, toByteRefTerm(packageName)),
					new TermFilter(PAGE_FIELD_NAME, toByteRefTerm(pageName)),
					DOCTYPE_PAGE_FILTER );
			
			final TopDocs docs= this.indexSearcher.search(new MatchAllDocsQuery(), filter, 1);
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
	
	
	public boolean search(final RHelpSearchQuery.Compiled searchQuery,
			final List<IRPkgHelp> packageList, final Map<String, IRPkgHelp> packageMap,
			final IRHelpSearchRequestor requestor) {
		check();
		try {
			final SearchQuery internal= (SearchQuery) searchQuery.compiled();
			final Filter filter= internal.luceneFilter;
			final Query query= internal.luceneQuery;
			
			if (query == null && filter == DOCTYPE_PAGE_FILTER) {
				for (final IRPkgHelp pkgHelp : packageList) {
					for (final IRHelpPage page : pkgHelp.getHelpPages()) {
						requestor.matchFound(new RHelpSearchMatch(page, 1.0f));
					}
				}
			}
			else {
				this.indexSearcher.search(query, filter, new DocFieldVisitorCollector(
						new RequestStreamCollector(internal, packageMap, requestor) ));
			}
			return true;
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"An error occurred in search: " + searchQuery.toString() + ".", e));
			return false;
		}
	}
	
	
	public List<RHelpTopicEntry> getPackageTopics(final IRPkgHelp pkgHelp) {
		final List<RHelpTopicEntry> list= new ArrayList<>(64);
		try {
			final Filter filter= chainFilters(
					new TermFilter(PACKAGE_FIELD_NAME, toByteRefTerm(pkgHelp.getName())),
					DOCTYPE_PAGE_FILTER );
			
			this.indexSearcher.search(new MatchAllDocsQuery(), filter, new DocFieldVisitorCollector(
					new DocFieldVisitorCollector.Visitor(LOAD_PKG_TOPICS_SELECTOR) {
				
				private String pageName;
				private final List<String> topics= new ArrayList<>();
				
				@Override
				public void newDocMatch(final AtomicReader reader, final int doc, final float score) {
					this.pageName= null;
					this.topics.clear();
				}
				@Override
				public void stringField(final FieldInfo fieldInfo, final String value) throws IOException {
					switch (fieldInfo.name) {
					case PAGE_FIELD_NAME:
						this.pageName= value;
						return;
					case ALIAS_FIELD_NAME:
						this.topics.add(value);
						return;
					default:
						return;
					}
				}
				@Override
				public void finalizeDocMatch() {
					if (this.pageName != null) {
						final IRHelpPage page= pkgHelp.getHelpPage(this.pageName);
						for (int i= 0; i < this.topics.size(); i++) {
							list.add(new RHelpTopicEntry(this.topics.get(i), page));
						}
					}
				}
			} ));
			
			Collections.sort(list);
			return list;
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					NLS.bind("An error occurred in search: {0}.", //$NON-NLS-1$
							getPackageTopics(pkgHelp.getName()) ),
					e ));
			throw new RuntimeException("R help index search error.");
		}
	}
	
	private String getPackageTopics(final String packageName) {
		return "#getPackageTopics '" + packageName + "'"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public boolean searchTopics(final String prefix, final String topicType,
			final List<String> packages, final IREnvHelp.ITopicSearchRequestor requestor) {
		try {
			final String uCasePrefix= (prefix != null) ? prefix.toUpperCase() : null;
			final String topicEnd= (topicType != null) ? ("-" + topicType) : null;
			
			final List<Filter> filters= new ArrayList<>(4);
			if (packages != null) {
				filters.add(new TermsFilter(PACKAGE_FIELD_NAME, toByteRefTerms(packages)));
			}
			if (prefix != null && !prefix.isEmpty()) {
				final char upperCase= uCasePrefix.charAt(0);
				final char lowerCase= Character.toLowerCase(upperCase);
				filters.add(new PrefixTermsFilter(ALIAS_FIELD_NAME, toByteRefTerms(
						(lowerCase != upperCase) ?
								ImCollections.newList(String.valueOf(lowerCase), String.valueOf(upperCase)) :
								ImCollections.newList(String.valueOf(lowerCase)) )));
			}
			
			filters.add(DOCTYPE_PAGE_FILTER);
			
			this.indexSearcher.search(new MatchAllDocsQuery(), chainFilters(filters), new DocFieldVisitorCollector(
					new DocFieldVisitorCollector.Visitor(LOAD_TOPICS_SELECTOR) {
						
						private String pkgName;
						private final List<String> topics= new ArrayList<>();
						
						@Override
						public void newDocMatch(final AtomicReader reader, final int doc, final float score) {
							this.pkgName= null;
							this.topics.clear();
						}
						private boolean matchesPrefix(final String value, final String uCasePrefix) {
							if (value.length() < uCasePrefix.length()) {
								return false;
							}
							for (int i= 0; i < uCasePrefix.length(); i++) {
								if (Character.toUpperCase(value.charAt(i)) != uCasePrefix.charAt(i)) {
									return false;
								}
							}
							return true;
						}
						@Override
						public void stringField(final FieldInfo fieldInfo, String value) throws IOException {
							switch (fieldInfo.name) {
							case PACKAGE_FIELD_NAME:
								this.pkgName= value;
								return;
							case ALIAS_FIELD_NAME:
								if (topicEnd != null) {
									if (!value.endsWith(topicEnd)) {
										return;
									}
									value= value.substring(0, value.length() - topicEnd.length());
								}
								if (uCasePrefix != null && !matchesPrefix(value, uCasePrefix)) {
									return;
								}
								this.topics.add(value);
								return;
							default:
								return;
							}
						}
						@Override
						public void finalizeDocMatch() {
							if (this.pkgName != null) {
								for (int i= 0; i < this.topics.size(); i++) {
									requestor.matchFound(this.topics.get(i), this.pkgName);
								}
							}
						}
					} ));
			return true;
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					NLS.bind("An error occurred in search: {0}.", //$NON-NLS-1$
							getTopicsSearchDescription(prefix, topicType) ),
					e ));
			return false;
		}
	}
	
	private String getTopicsSearchDescription(final String prefix, final String topicType) {
		final StringBuilder sb= new StringBuilder("#searchTopics '"); //$NON-NLS-1$
		if (prefix != null) {
			sb.append(prefix);
		}
		sb.append('*');
		if (topicType != null) {
			sb.append('-');
			sb.append(topicType);
		}
		sb.append('\'');
		return sb.toString();
	}
	
	public IRPkgDescription getPkgDescription(final IRPkgHelp pkgHelp) {
		try {
			final Filter filter= chainFilters(
					new TermFilter(PACKAGE_FIELD_NAME, toByteRefTerm(pkgHelp.getName())),
					DOCTYPE_PKG_DESCRIPTION_FILTER );
			
			final TopDocs docs= this.indexSearcher.search(new MatchAllDocsQuery(), filter, 1);
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
