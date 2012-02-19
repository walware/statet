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
public final class StandardAnalyzer extends Analyzer {
	
	/**
	 * An unmodifiable set containing some common English words that are usually not useful for searching.
	*/
	public static final Set<?> STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
	
	private static final class TokenStreamComponents {
		protected StandardTokenizer defaultSource;
		protected TokenStream defaultSink;
		protected StandardTokenizer htmlSource;
		protected TokenStream htmlSink;
	}
	
	
	private final Version fMatchVersion;
	
	private final int fMaxTokenLength = StandardTokenizer.DEFAULT_MAX_TOKEN_LENGTH;
	
	private final boolean fIndexMode;
	
	
	/** Builds an analyzer with the default stop words ({@link
	 * #STOP_WORDS_SET}).
	 * @param matchVersion Lucene version to match See {@link
	 * <a href="#version">above</a>}
	 */
	public StandardAnalyzer(final boolean indexMode) {
		fMatchVersion = Version.LUCENE_30;
		fIndexMode = indexMode;
	}
	
	
	@Override
	public TokenStream tokenStream(final String fieldName, Reader reader) {
		if (enableHtmlStrip(fieldName)) {
			reader = new HTMLStripCharFilter(reader);
		}
		final StandardTokenizer source = new StandardTokenizer(fMatchVersion, reader);
		final TokenStream sink = createSink(source);
		return sink;
	}
	
	@Override
	public TokenStream reusableTokenStream(final String fieldName, Reader reader) throws IOException {
		TokenStreamComponents components = (TokenStreamComponents) getPreviousTokenStream();
		if (components == null) {
			components = new TokenStreamComponents();
			setPreviousTokenStream(components);
		}
		if (enableHtmlStrip(fieldName)) {
			reader = new HTMLStripCharFilter(reader);
			if (components.htmlSource == null) {
				components.htmlSource = new StandardTokenizer(fMatchVersion, reader);
				components.htmlSink = createSink(components.htmlSource);
			}
			else {
				components.htmlSource.reset(reader);
			}
			return components.htmlSink;
		}
		else {
			if (components.defaultSource == null) {
				components.defaultSource = new StandardTokenizer(fMatchVersion, reader);
				components.defaultSink = createSink(components.defaultSource);
			}
			else {
				components.defaultSource.reset(reader);
			}
			return components.defaultSink;
		}
	}
	
	protected boolean enableHtmlStrip(final String fieldName) {
		return (fIndexMode && fieldName != null && fieldName.endsWith(".html"));
	}
	
	protected TokenStream createSink(final StandardTokenizer source) {
		source.setMaxTokenLength(fMaxTokenLength);
		TokenStream result = new StandardFilter(fMatchVersion, source);
		result = new LowerCaseFilter(fMatchVersion, result);
		result = new PorterStemFilter(result);
		result = new StopFilter(fMatchVersion, result, STOP_WORDS_SET);
		return result;
	}
	
}
