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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.vectorhighlight.CSimpleBoundaryScanner;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.search.vectorhighlight.ScoreOrderFragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.SimpleFragListBuilder;

import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRHelpSearchMatch;
import de.walware.statet.r.core.rhelp.IRHelpSearchRequestor;
import de.walware.statet.r.core.rhelp.IRPkgHelp;
import de.walware.statet.r.internal.core.rhelp.RHelpSearchMatch;


public class RequestStreamCollector extends Collector implements IREnvIndex {
	
	
	private static final FastVectorHighlighter HIGHLIGHTER;
	
	static {
		final SimpleFragListBuilder fragListBuilder= new SimpleFragListBuilder(10);
		final ScoreOrderFragmentsBuilder fragmentsBuilder= new ScoreOrderFragmentsBuilder(
				new CSimpleBoundaryScanner(10) );
		fragmentsBuilder.setDiscreteMultiValueHighlighting(true);
		HIGHLIGHTER= new FastVectorHighlighter(true, true, fragListBuilder, fragmentsBuilder);
	}
	
	
	private final SearchQuery query;
	
	private Scorer scorer;
	
	private final Map<String, IRPkgHelp> packageMap;
	
	private final IRHelpSearchRequestor requestor;
	
	private AtomicReader reader;
	private int docBase;
	private final Set<String> fieldSelector;
	
	private final List<IRHelpSearchMatch.MatchFragment> fragmentCollection= new ArrayList<>();
	
	private final FieldQuery fieldQuery;
	private final int maxNumFragments;
	
	
	public RequestStreamCollector(final Map<String, IRPkgHelp> packageMap,
			final SearchQuery query, final IndexReader reader,
			final IRHelpSearchRequestor requestor) throws IOException {
		this.query= query;
		this.packageMap= packageMap;
		this.requestor= requestor;
		
		this.fieldSelector= new HashSet<>(this.query.fieldNames.length + 2);
		this.fieldSelector.add(PACKAGE_FIELD_NAME);
		this.fieldSelector.add(PAGE_FIELD_NAME);
		for (int i= 0; i < this.query.fieldNames.length; i++) {
			this.fieldSelector.add(this.query.fieldNames[i]);
		}
		
		this.maxNumFragments= requestor.maxFragments();
		if (this.query.fieldNames != null && this.query.fieldNames.length > 0 && this.maxNumFragments > 0) {
			this.fieldQuery= HIGHLIGHTER.getFieldQuery(query.luceneQuery, reader);
		}
		else {
			this.fieldQuery= null;
		}
	}
	
	
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
		final float score= this.scorer.score();
		if (score > 0) {
			// TODO: reader#document not recommend
			final Document document= this.reader.document(doc, this.fieldSelector);
			final String packageName= document.get(PACKAGE_FIELD_NAME);
			final IRPkgHelp pkgHelp= this.packageMap.get(packageName);
			if (pkgHelp != null) {
				final IRHelpPage page= pkgHelp.getHelpPage(document.get(PAGE_FIELD_NAME));
				if (page != null) {
//					System.out.println(packageName + "/" + page.getName() + ": " + score);
					final RHelpSearchMatch match= new RHelpSearchMatch(page, score);
					addHighlighting(doc, document, match);
					this.requestor.matchFound(match);
				}
			}
		}
	}
	
	private void addHighlighting(final int doc, final Document document, final RHelpSearchMatch match) throws IOException {
		if (this.fieldQuery == null) {
			return;
		}
		try {
			final AtomicInteger counter= new AtomicInteger();
			for (final String fieldName : this.query.fieldNames) {
				final String[] fragments= HIGHLIGHTER.getBestFragments(this.fieldQuery, this.reader,
						doc, fieldName, 80, this.maxNumFragments,
						IRHelpSearchMatch.PRE_TAGS, IRHelpSearchMatch.POST_TAGS, REnvIndexReader.DEFAULT_ENCODER,
						counter );
				if (fragments != null) {
					for (int j= 0; j < fragments.length; j++) {
						this.fragmentCollection.add(new RHelpSearchMatch.Fragment(match, fieldName, fragments[j]));
					}
				}
			}
			match.setTotalMatches(counter.get());
			match.setBestFragments(this.fragmentCollection.toArray(new IRHelpSearchMatch.MatchFragment[this.fragmentCollection.size()]));
		}
		finally {
			this.fragmentCollection.clear();
		}
	}
	
}
