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
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.extra.analysis.HTMLStripCharFilter;
import org.apache.lucene.util.Version;


/**
 * Filters 
 *   <li>R-Help {@link StandardTokenizer} with {@link StandardFilter},</li>
 *   <li>{@link LowerCaseFilter},</li>
 *   <li>{@link PorterStemFilter} and</li>
 *   <li>{@link StopFilter}, using a list of English stop words.</li>
 */
public class StandardAnalyzer extends Analyzer {
	
	/**
	 * An unmodifiable set containing some common English words that are usually not useful for searching.
	*/
	public static final Set<?> STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
	
	
	private final Version fMatchVersion;
	
	private final Set<?> fStopSet;
	
	private final boolean fIndexMode;
	
	
	/** Builds an analyzer with the default stop words ({@link
	 * #STOP_WORDS_SET}).
	 * @param matchVersion Lucene version to match See {@link
	 * <a href="#version">above</a>}
	 */
	public StandardAnalyzer(final Version matchVersion, final boolean indexMode) {
		fStopSet = STOP_WORDS_SET;
		fMatchVersion = matchVersion;
		fIndexMode = indexMode;
	}
	
	
	@Override
	public TokenStream tokenStream(final String fieldName, final Reader reader) {
		final StandardTokenizer tokenStream = new StandardTokenizer(fMatchVersion, reader);
		tokenStream.setMaxTokenLength(fMaxTokenLength);
		TokenStream result = new StandardFilter(tokenStream);
		result = new LowerCaseFilter(result);
		result = new PorterStemFilter(result);
		result = new StopFilter(true, result, fStopSet);
		return result;
	}
	
	private static final class SavedStreams {
		StandardTokenizer defaultTokenStream;
		TokenStream defaultFilteredTokenStream;
		StandardTokenizer htmlTokenStream;
		TokenStream htmlFilteredTokenStream;
	}
	
	/** Default maximum allowed token length */
	public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;
	
	private int fMaxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;
	
	/**
	 * Set maximum allowed token length.  If a token is seen
	 * that exceeds this length then it is discarded.  This
	 * setting only takes effect the next time tokenStream or
	 * reusableTokenStream is called.
	 */
	public void setMaxTokenLength(final int length) {
		fMaxTokenLength = length;
	}
	
	/**
	 * @see #setMaxTokenLength
	 */
	public int getMaxTokenLength() {
		return fMaxTokenLength;
	}
	
	@Override
	public TokenStream reusableTokenStream(final String fieldName, Reader reader) throws IOException {
		SavedStreams streams = (SavedStreams) getPreviousTokenStream();
		if (streams == null) {
			streams = new SavedStreams();
			setPreviousTokenStream(streams);
		}
		if (fIndexMode && fieldName != null && fieldName.endsWith(".html")) {
			reader = new HTMLStripCharFilter(reader);
			if (streams.htmlTokenStream == null) {
				streams.htmlTokenStream = new StandardTokenizer(fMatchVersion, reader);
				TokenStream result = new StandardFilter(streams.htmlTokenStream);
				result = new LowerCaseFilter(result);
				result = new PorterStemFilter(result);
				result = new StopFilter(true, result, fStopSet);
				streams.htmlFilteredTokenStream = result;
			} else {
				streams.htmlTokenStream.reset(reader);
			}
			streams.htmlTokenStream.setMaxTokenLength(fMaxTokenLength);
			
			return streams.htmlFilteredTokenStream;
		}
		else {
			if (streams.defaultTokenStream == null) {
				streams = new SavedStreams();
				setPreviousTokenStream(streams);
				streams.defaultTokenStream = new StandardTokenizer(fMatchVersion, reader);
				TokenStream result = new StandardFilter(streams.defaultTokenStream);
				result = new LowerCaseFilter(result);
				result = new PorterStemFilter(result);
				result = new StopFilter(true, result, fStopSet);
				streams.defaultFilteredTokenStream = result;
			} else {
				streams.defaultTokenStream.reset(reader);
			}
			streams.defaultTokenStream.setMaxTokenLength(fMaxTokenLength);
			
			return streams.defaultFilteredTokenStream;
		}
	}
	
}
