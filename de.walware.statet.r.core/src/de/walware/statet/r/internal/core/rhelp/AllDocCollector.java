/*******************************************************************************
 * Copyright (c) 2010 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;


public class AllDocCollector extends Collector {
	
	
	private Scorer fScorer;
	
	private final ArrayIntList fDocs = new ArrayIntList();
	
	private int fDocBase;
	
	
	public AllDocCollector() {
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
	public void setNextReader(final IndexReader reader, final int docBase) throws IOException {
		fDocBase = docBase;
	}
	
	@Override
	public void collect(final int doc) throws IOException {
		if (fScorer.score() > 0.0f) {
			fDocs.add(fDocBase + doc);
		}
	}
	
	
	public IntList getDocs() {
		return fDocs;
	}
	
}
