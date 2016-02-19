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

import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.miscellaneous.KeywordRepeatFilter;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.CharFilterFactory;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.tartarus.snowball.ext.EnglishStemmer;


final class DefaultAnalyzer extends StopwordAnalyzerBase {
	
	
	public static final CharArraySet STOP_WORDS_SET= StopAnalyzer.ENGLISH_STOP_WORDS_SET;
	
	private final CharFilterFactory charFilterFactory;
	
	
	public DefaultAnalyzer() {
		this(null);
	}
	
	public DefaultAnalyzer(final CharFilterFactory charFilterFactory) {
		super(STOP_WORDS_SET);
		
		this.charFilterFactory= charFilterFactory;
	}
	
	
	@Override
	protected TokenStreamComponents createComponents(final String fieldName, Reader reader) {
		if (this.charFilterFactory != null) {
			reader= this.charFilterFactory.create(reader);
		}
		final Tokenizer source= new StandardTokenizer(reader);
		TokenStream result= source;
		result= new EnglishPossessiveFilter(getVersion(), result);
		result= new LowerCaseFilter(result);
		result= new StopFilter(result, this.stopwords);
		result= new KeywordRepeatFilter(result);
		result= new SnowballFilter(result, new EnglishStemmer());
		result= new RemoveDuplicatesTokenFilter(result);
		return new TokenStreamComponents(source, result);
	}
	
}
