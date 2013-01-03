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
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;

import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRHelpSearchMatch;
import de.walware.statet.r.core.rhelp.IRHelpSearchRequestor;
import de.walware.statet.r.core.rhelp.IRPackageHelp;
import de.walware.statet.r.internal.core.rhelp.REnvIndexReader.InternalQuery;


public class RequestStreamCollector extends Collector implements IREnvIndex {
	
	
	private static class CollectorSelector implements FieldSelector {
		
		private static final long serialVersionUID = 1L;
		
		private final String[] fFieldNamesToLoad;
		
		public CollectorSelector(final String[] fieldNamesToLoad) {
			fFieldNamesToLoad = fieldNamesToLoad;
		}
		
		@Override
		public FieldSelectorResult accept(final String fieldName) {
			if (fieldName == PACKAGE_FIELD_NAME
					|| fieldName == PAGE_FIELD_NAME) {
				return FieldSelectorResult.LOAD;
			}
			for (int i = 0; i < fFieldNamesToLoad.length; i++) {
				if (fFieldNamesToLoad[i] == fieldName) {
					return FieldSelectorResult.LOAD;
				}
			}
			return FieldSelectorResult.NO_LOAD;
		}
		
	};
	
	
	private final InternalQuery fQuery;
	
	private Scorer fScorer;
	
	private final Map<String, IRPackageHelp> fPackageMap;
	
	private final IRHelpSearchRequestor fRequestor;
	
	private IndexReader fReader;
	private int fDocBase;
	private final FieldSelector fFieldSelector;
	
	private FastVectorHighlighter fHighlighter;
	private final List<IRHelpSearchMatch.MatchFragment> fFragmentCollection = new ArrayList<IRHelpSearchMatch.MatchFragment>();
	
	
	public RequestStreamCollector(final Map<String, IRPackageHelp> packageMap,
			final InternalQuery query, final IRHelpSearchRequestor requestor) {
		fQuery = query;
		fPackageMap = packageMap;
		fRequestor = requestor;
		fFieldSelector = new CollectorSelector(fQuery.fieldNames);
		
		final int max = requestor.maxFragments();
		if (fQuery.fieldNames != null && fQuery.fieldNames.length > 0 && max > 0) {
			fHighlighter = new FastVectorHighlighter( true, true );
			fHighlighter.setTags(IRHelpSearchMatch.PRE_TAGS, IRHelpSearchMatch.POST_TAGS);
			fHighlighter.setQuery(query.luceneQuery);
			fHighlighter.setMaxNumFragments(max);
		}
	}
	
	
	@Override
	public void setScorer(final Scorer scorer) throws IOException {
		fScorer = scorer;
	}
	
	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}
	
	@Override
	public void setNextReader(final IndexReader reader, final int docBase)
			throws IOException {
		fReader = reader;
		fDocBase = docBase;
	}
	
	@Override
	public void collect(final int doc) throws IOException {
		final float score = fScorer.score();
		if (score > 0.0f) {
			final int docId = fDocBase + doc;
			final Document document = fReader.document(docId, fFieldSelector);
			final String packageName = document.get(PACKAGE_FIELD_NAME);
			final IRPackageHelp packageHelp = fPackageMap.get(packageName);
			if (packageHelp != null) {
				final IRHelpPage page = packageHelp.getHelpPage(document.get(PAGE_FIELD_NAME));
				if (page != null) {
					final RHelpSearchMatch match = new RHelpSearchMatch(page, score);
					addHighlighting(docId, document, match);
					fRequestor.matchFound(match);
				}
			}
		}
	}
	
	private void addHighlighting(final int docId, final Document document, final RHelpSearchMatch match) throws IOException {
		if (fHighlighter == null) {
			return;
		}
		try {
			for (final String fieldName : fQuery.fieldNames) {
				final String[] fragments = fHighlighter.getBestFragments( fReader, docId, fieldName, 80);
				if (fragments != null) {
					for (int j = 0; j < fragments.length; j++) {
						fFragmentCollection.add(new RHelpSearchMatch.Fragment(match, fieldName, fragments[j]));
					}
				}
			}
			match.setTotalMatches(fHighlighter.getTotalMatches());
			match.setBestFragments(fFragmentCollection.toArray(new IRHelpSearchMatch.MatchFragment[fFragmentCollection.size()]));
		}
		finally {
			fHighlighter.clear();
			fFragmentCollection.clear();
		}
	}
	
}
