/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
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

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;


public class AllDocCollector extends Collector {
	
	
	private Scorer scorer;
	
	private final ArrayIntList docs= new ArrayIntList();
	
	private int docBase;
	
	
	public AllDocCollector() {
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
		this.docBase= context.docBase;
	}
	
	@Override
	public void collect(final int doc) throws IOException {
		if (this.scorer.score() > 0.0f) {
			this.docs.add(this.docBase + doc);
		}
	}
	
	
	public IntList getDocs() {
		return this.docs;
	}
	
}
