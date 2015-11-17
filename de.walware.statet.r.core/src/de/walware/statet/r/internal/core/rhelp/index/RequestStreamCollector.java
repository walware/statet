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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.search.vectorhighlight.CSimpleBoundaryScanner;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.search.vectorhighlight.ScoreOrderFragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.SimpleFragListBuilder;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImSet;

import de.walware.statet.r.core.rhelp.IRHelpPage;
import de.walware.statet.r.core.rhelp.IRHelpSearchMatch;
import de.walware.statet.r.core.rhelp.IRHelpSearchRequestor;
import de.walware.statet.r.core.rhelp.IRPkgHelp;
import de.walware.statet.r.internal.core.rhelp.RHelpSearchMatch;


public class RequestStreamCollector extends DocFieldVisitorCollector.Visitor implements IREnvIndex {
	
	
	private static final FastVectorHighlighter HIGHLIGHTER;
	
	static {
		final SimpleFragListBuilder fragListBuilder= new SimpleFragListBuilder(10);
		final ScoreOrderFragmentsBuilder fragmentsBuilder= new ScoreOrderFragmentsBuilder(
				new CSimpleBoundaryScanner(10) );
		fragmentsBuilder.setDiscreteMultiValueHighlighting(true);
		HIGHLIGHTER= new FastVectorHighlighter(true, true, fragListBuilder, fragmentsBuilder);
	}
	
	
	private static final ImSet<String> LOAD_ID_SELECTOR= ImCollections.newSet(
			PAGE_FIELD_NAME,
			PACKAGE_FIELD_NAME );
	
	
	private final SearchQuery query;
	
	private final Map<String, IRPkgHelp> packageMap;
	
	private final IRHelpSearchRequestor requestor;
	
	private final List<IRHelpSearchMatch.MatchFragment> fragmentCollection= new ArrayList<>();
	
	private final FieldQuery fieldQuery;
	private final int maxNumFragments;
	
	private AtomicReader reader;
	private int doc;
	private float score;
	
	private String pkgName;
	private String pageName;
	
	
	public RequestStreamCollector(final SearchQuery query, final Map<String, IRPkgHelp> packageMap,
			final IRHelpSearchRequestor requestor) throws IOException {
		super(LOAD_ID_SELECTOR);
		
		this.query= query;
		this.packageMap= packageMap;
		this.requestor= requestor;
		
		this.maxNumFragments= requestor.maxFragments();
		if (this.query.fieldNames != null && this.query.fieldNames.size() > 0 && this.maxNumFragments > 0) {
			this.fieldQuery= HIGHLIGHTER.getFieldQuery(query.luceneQuery, this.reader);
		}
		else {
			this.fieldQuery= null;
		}
	}
	
	
	@Override
	public void newDocMatch(final AtomicReader reader, final int doc, final float score) {
		this.reader= reader;
		
		this.doc= doc;
		this.score= score;
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
	public void finalizeDocMatch() throws IOException {
		if (this.pkgName != null && this.pageName != null) {
			final IRPkgHelp pkgHelp= this.packageMap.get(this.pkgName);
			if (pkgHelp != null) {
				final IRHelpPage page= pkgHelp.getHelpPage(this.pageName);
				if (page != null) {
//					System.out.println(packageName + "?" + page.getName() + ": " + score);
					final RHelpSearchMatch match= new RHelpSearchMatch(page, this.score);
					addHighlighting(match);
					this.requestor.matchFound(match);
				}
			}
		}
	}
	
	private void addHighlighting(final RHelpSearchMatch match) throws IOException {
		if (this.fieldQuery == null) {
			return;
		}
		try {
			final AtomicInteger counter= new AtomicInteger();
			for (final String fieldName : this.query.fieldNames) {
				final String[] fragments= HIGHLIGHTER.getBestFragments(this.fieldQuery, this.reader,
						this.doc, fieldName, 80, this.maxNumFragments,
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
