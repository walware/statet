/*=============================================================================#
 # Copyright (c) 2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.rhelp.index;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeParseException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.Operator;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rhelp.RHelpSearchQuery;
import de.walware.statet.r.internal.core.RCorePlugin;


public final class SearchQuery implements IREnvIndex {
	
	
	private static final String[] TOPIC_SEARCH_FIELDS= new String[] {
			ALIAS_FIELD_NAME };
	
	private static final String[] DOC_SEARCH_FIELDS= new String[] {
			TITLE_TXT_FIELD_NAME,
			DESCRIPTION_TXT_FIELD_NAME,
			DOC_TXT_FIELD_NAME,
			EXAMPLES_TXT_FIELD_NAME };
	
	private static final String[] NO_FIELDS= new String[0];
	
	
	private final static Analyzer QUERY_ANALYZER= new DefaultAnalyzer();
	
	
	public static SearchQuery compile(final RHelpSearchQuery query) throws CoreException {
		try {
			final BooleanQuery q= new BooleanQuery();
			q.add(REnvIndexReader.DOCTYPE_PAGE_QUERY, Occur.MUST);
			String[] fieldNames= NO_FIELDS;
			if (query.getSearchString().length() > 0) {
				switch (query.getSearchType()) {
				case RHelpSearchQuery.TOPIC_SEARCH:
					fieldNames= TOPIC_SEARCH_FIELDS;
					q.add(createMainQuery(fieldNames, query.getSearchString()), Occur.MUST);
					break;
				case RHelpSearchQuery.FIELD_SEARCH:
					fieldNames= sortFields(query.getEnabledFields());
					if (fieldNames.length == 0) {
						break;
					}
					q.add(createMainQuery(fieldNames, query.getSearchString()), Occur.MUST);
					break;
				case RHelpSearchQuery.DOC_SEARCH:
					fieldNames= DOC_SEARCH_FIELDS;
					q.add(createMainQuery(fieldNames, query.getSearchString()), Occur.MUST);
					break;
				default:
					break;
				}
			}
			
			final List<String> keywords= query.getKeywords();
			if (!keywords.isEmpty()) {
				q.add(createOrQuery(KEYWORD_FIELD_NAME, keywords), Occur.MUST);
			}
			final List<String> packages= query.getPackages();
			if (!packages.isEmpty()) {
				q.add(createOrQuery(PACKAGE_FIELD_NAME, packages), Occur.MUST);
			}
			
			return new SearchQuery(fieldNames, q);
		}
		catch (final QueryNodeParseException e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					NLS.bind("An error occurred when creating the Lucene query for: {0}.", //$NON-NLS-1$
							query.toString() ),
					e ));
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"The search string is invalid: " + e.getLocalizedMessage(), null));
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					NLS.bind("An error occurred when creating the Lucene query for {0}.", //$NON-NLS-1$
							query.toString() ),
					e ));
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"An error occurred when preparing the R help query.", null));
		}
	}
	
	private static String[] sortFields(final List<String> fieldNames) {
		final String[] array= fieldNames.toArray(new String[fieldNames.size()]);
		Arrays.sort(array, new Comparator<String>() {
			@Override
			public int compare(final String o1, final String o2) {
				return getRank(o1) - getRank(o2);
			}
		});
		return array;
	}
	
	static Query createMainQuery(final String fields[], final String queryText) throws QueryNodeException {
		final StandardQueryParser p= new StandardQueryParser(QUERY_ANALYZER);
		p.setDefaultOperator(Operator.AND);
		p.setAllowLeadingWildcard(true);
		p.setMultiFields(fields);
		return p.parse(queryText, null);
	}
	
	private static Query createOrQuery(final String field, final List<String> terms) {
		if (terms.size() == 1) {
			return new TermQuery(new Term(field, terms.get(0)));
		}
		else if (terms.size() > 1) {
			final BooleanQuery q= new BooleanQuery();
			for (final String keyword : terms) {
				q.add(new TermQuery(new Term(field, keyword)), Occur.SHOULD);
			}
			return q;
		}
		return null;
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
	
	
	public final String[] fieldNames;
	
	public final BooleanQuery luceneQuery;
	
	private SearchQuery(final String[] fieldNames, final BooleanQuery luceneQuery) {
		this.fieldNames= fieldNames;
		this.luceneQuery= luceneQuery;
	}
	
}
