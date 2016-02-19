/*=============================================================================#
 # Copyright (c) 2015-2016 Stephan Wahlbrink (WalWare.de) and others.
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
import java.util.Set;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.StoredFieldVisitor;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;


public class DocFieldVisitorCollector extends Collector {
	
	
	public abstract static class Visitor extends StoredFieldVisitor {
		
		
		private final Set<String> fieldNames;
		
		
		public Visitor(final Set<String> fieldNames) {
			this.fieldNames= fieldNames;
		}
		
		
		@Override
		public Status needsField(final FieldInfo fieldInfo) throws IOException {
			return this.fieldNames.contains(fieldInfo.name) ? Status.YES : Status.NO;
		}
		
		public abstract void newDocMatch(AtomicReader reader, int doc, float score) throws IOException;
		
		public abstract void finalizeDocMatch() throws IOException;
		
	}
	
	
	private final Visitor visitor;
	
	private Scorer scorer;
	
	private AtomicReader reader;
	
	
	public DocFieldVisitorCollector(final Visitor visitor) {
		this.visitor= visitor;
	}
	
	
	@Override
	public void setScorer(final Scorer scorer) throws IOException {
		this.scorer= scorer;
	}
	
	@Override
	public void setNextReader(final AtomicReaderContext context) throws IOException {
		this.reader= context.reader();
	}
	
	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}
	
	@Override
	public void collect(final int doc) throws IOException {
		final float score= this.scorer.score();
		if (score > 0.0f) {
			this.visitor.newDocMatch(this.reader, doc, score);
			this.reader.document(doc, this.visitor);
			this.visitor.finalizeDocMatch();
		}
	}
	
}
