/*=============================================================================#
 # Copyright (c) 2014-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.rhelp.index;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeParseException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.Operator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.alucene.queries.TermsFilter;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rhelp.RHelpSearchQuery;
import de.walware.statet.r.internal.core.RCorePlugin;


public final class SearchQuery implements IREnvIndex {
	
	
	private static final ImList<String> TOPIC_SEARCH_FIELDS= ImCollections.newList(
			ALIAS_FIELD_NAME );
	
	private static final ImList<String> DOC_SEARCH_FIELDS= ImCollections.newList(
			TITLE_TXT_FIELD_NAME,
			DESCRIPTION_TXT_FIELD_NAME,
			DOC_TXT_FIELD_NAME,
			EXAMPLES_TXT_FIELD_NAME );
	
	
	private final static Analyzer QUERY_ANALYZER= new DefaultAnalyzer();
	
	
	public static SearchQuery compile(final RHelpSearchQuery searchQuery) throws CoreException {
		try {
			Query query= null;
			ImList<String> fields= ImCollections.emptyList();
			if (searchQuery.getSearchString().length() > 0) {
				switch (searchQuery.getSearchType()) {
				case RHelpSearchQuery.TOPIC_SEARCH:
					fields= TOPIC_SEARCH_FIELDS;
					query= createMainQuery(fields, searchQuery.getSearchString());
					break;
				case RHelpSearchQuery.FIELD_SEARCH:
					fields= sortFields(searchQuery.getEnabledFields());
					if (fields.isEmpty()) {
						break;
					}
					query= createMainQuery(fields, searchQuery.getSearchString());
					break;
				case RHelpSearchQuery.DOC_SEARCH:
					fields= DOC_SEARCH_FIELDS;
					query= createMainQuery(fields, searchQuery.getSearchString());
					break;
				default:
					break;
				}
			}
			
			final List<Filter> filters= new ArrayList<>(4);
			{	final List<String> packages= searchQuery.getPackages();
				if (!packages.isEmpty()) {
					filters.add(new TermsFilter(PACKAGE_FIELD_NAME,
							REnvIndexReader.toByteRefTerms(packages) ));
				}
			}
			{	final List<String> keywords= searchQuery.getKeywords();
				if (!keywords.isEmpty()) {
					filters.add(new TermsFilter(KEYWORD_FIELD_NAME,
							REnvIndexReader.toByteRefTerms(keywords) ));
				}
			}
			filters.add(REnvIndexReader.DOCTYPE_PAGE_FILTER);
			
			return new SearchQuery(fields, REnvIndexReader.chainFilters(filters), query);
		}
		catch (final QueryNodeParseException e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					NLS.bind("An error occurred when creating the Lucene query for: {0}.", //$NON-NLS-1$
							searchQuery.toString() ),
					e ));
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"The search string is invalid: " + e.getLocalizedMessage(), null));
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					NLS.bind("An error occurred when creating the Lucene query for {0}.", //$NON-NLS-1$
							searchQuery.toString() ),
					e ));
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					"An error occurred when preparing the R help query.", null));
		}
	}
	
	private static ImList<String> sortFields(final List<String> fields) {
		return ImCollections.toList(fields, new Comparator<String>() {
			@Override
			public int compare(final String o1, final String o2) {
				return getRank(o1) - getRank(o2);
			}
		});
	}
	
	
	static Query createMainQuery(final ImList<String> fields, final String queryText) throws QueryNodeException {
		final StandardQueryParser p= new StandardQueryParser(QUERY_ANALYZER);
		p.setDefaultOperator(Operator.AND);
		p.setAllowLeadingWildcard(true);
		p.setMultiFields(fields.toArray(new String[fields.size()]));
		return p.parse(queryText, null);
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
	
	
	public final ImList<String> fieldNames;
	
	public final Filter luceneFilter;
	public final Query luceneQuery;
	
	
	private SearchQuery(final ImList<String> fieldNames,
			final Filter luceneFilter, final Query luceneQuery) {
		this.fieldNames= fieldNames;
		this.luceneFilter= luceneFilter;
		this.luceneQuery= luceneQuery;
	}
	
}
