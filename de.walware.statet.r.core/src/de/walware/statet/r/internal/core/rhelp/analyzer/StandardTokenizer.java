/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.walware.statet.r.internal.core.rhelp.analyzer;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;


/**
 * Tokenzizer for R help pages constructed with JFlex
 *
 * <p> This should be a good tokenizer for most European-language documents:
 *
 * <ul>
 *   <li>Splits words at punctuation characters, removing punctuation.
 *   <li>Splits words at hyphens, unless there's a number in the token, in which case
 *     the whole token is interpreted as a product number and is not split.
 *   <li>Recognizes email addresses and internet hostnames as one token.
 * </ul>
 */
public final class StandardTokenizer extends Tokenizer {
	
	/** A private instance of the JFlex-constructed scanner */
	private final StandardTokenizerImpl scanner;
	
	public static final int ALPHANUM          = 0;
	public static final int APOSTROPHE        = 1;
	public static final int ACRONYM           = 2;
	public static final int COMPANY           = 3;
	public static final int EMAIL             = 4;
	public static final int HOST              = 5;
	public static final int NUM               = 6;
	public static final int CJ                = 7;
	
	/** String token types that correspond to token type int constants */
	public static final String [] TOKEN_TYPES = new String [] {
			"<ALPHANUM>",
			"<APOSTROPHE>",
			"<ACRONYM>",
			"<COMPANY>",
			"<EMAIL>",
			"<HOST>",
			"<NUM>",
			"<CJ>",
	};
	
	
	private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;
	
	
	/** Set the max allowed token length.  Any token longer
	 *  than this is skipped. */
	public void setMaxTokenLength(final int length) {
		this.maxTokenLength = length;
	}
	
	/** @see #setMaxTokenLength */
	public int getMaxTokenLength() {
		return maxTokenLength;
	}
	
	/**
	 * Creates a new instance of the {@link org.apache.lucene.analysis.standard.StandardTokenizer}.  Attaches
	 * the <code>input</code> to the newly created JFlex scanner.
	 *
	 * @param input The input reader
	 *
	 * See http://issues.apache.org/jira/browse/LUCENE-1068
	 */
	public StandardTokenizer(final Version matchVersion, final Reader input) {
		super();
		this.scanner = new StandardTokenizerImpl(input);
		init(input, matchVersion);
	}
	
	/**
	 * Creates a new StandardTokenizer with a given {@link AttributeSource}. 
	 */
	public StandardTokenizer(final Version matchVersion, final AttributeSource source, final Reader input) {
		super(source);
		this.scanner = new StandardTokenizerImpl(input);
		init(input, matchVersion);
	}
	
	/**
	 * Creates a new StandardTokenizer with a given {@link org.apache.lucene.util.AttributeSource.AttributeFactory} 
	 */
	public StandardTokenizer(final Version matchVersion, final AttributeFactory factory, final Reader input) {
		super(factory);
		this.scanner = new StandardTokenizerImpl(input);
		init(input, matchVersion);
	}
	
	private void init(final Reader input, final Version matchVersion) {
		this.input = input;
		termAtt = addAttribute(TermAttribute.class);
		offsetAtt = addAttribute(OffsetAttribute.class);
		posIncrAtt = addAttribute(PositionIncrementAttribute.class);
		typeAtt = addAttribute(TypeAttribute.class);
	}
	
	// this tokenizer generates three attributes:
	// offset, positionIncrement and type
	private TermAttribute termAtt;
	private OffsetAttribute offsetAtt;
	private PositionIncrementAttribute posIncrAtt;
	private TypeAttribute typeAtt;
	
	
	@Override
	public final boolean incrementToken() throws IOException {
		clearAttributes();
		int posIncr = 1;
		
		while(true) {
			final int tokenType = scanner.getNextToken();
			
			if (tokenType == StandardTokenizerImpl.YYEOF) {
				return false;
			}
			
			if (scanner.yylength() <= maxTokenLength) {
				posIncrAtt.setPositionIncrement(posIncr);
				scanner.getText(termAtt);
				final int start = scanner.yychar();
				offsetAtt.setOffset(correctOffset(start), correctOffset(start+termAtt.termLength()));
				typeAtt.setType(StandardTokenizerImpl.TOKEN_TYPES[tokenType]);
				return true;
			} else
				// When we skip a too-long term, we still increment the
				// position increment
				posIncr++;
		}
	}
	
	@Override
	public final void end() {
		// set final offset
		final int finalOffset = correctOffset(scanner.yychar() + scanner.yylength());
		offsetAtt.setOffset(finalOffset, finalOffset);
	}
	
	@Override
	public void reset(final Reader reader) throws IOException {
		super.reset(reader);
		scanner.reset(reader);
	}
	
}
