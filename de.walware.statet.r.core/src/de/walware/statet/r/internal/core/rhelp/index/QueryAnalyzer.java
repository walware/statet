/*=============================================================================#
 # Copyright (c) 2014-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.rhelp.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.AnalyzerWrapper;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;


final class QueryAnalyzer extends AnalyzerWrapper {
	
	
	private final Analyzer defaultAnalyzer;
	
	private final Analyzer nameAnalyzers;
	
	
	public QueryAnalyzer() {
		super(PER_FIELD_REUSE_STRATEGY);
		this.defaultAnalyzer= new DefaultAnalyzer();
		this.nameAnalyzers= new WhitespaceAnalyzer(IREnvIndex.LUCENE_VERSION);
	}
	
	
	@Override
	protected Analyzer getWrappedAnalyzer(final String fieldName) {
		if (!(fieldName.endsWith(".txt") || fieldName.endsWith(".html"))) { //$NON-NLS-1$ //$NON-NLS-2$
			return this.nameAnalyzers;
		}
		return this.defaultAnalyzer;
	}
	
}
