/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.text.rd;

import java.util.List;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import de.walware.ecommons.text.DefaultWhitespaceDetector;
import de.walware.ecommons.text.core.rules.OperatorRule;
import de.walware.ecommons.text.ui.presentation.AbstractRuleBasedScanner;
import de.walware.ecommons.text.ui.settings.TextStyleManager;

import de.walware.statet.r.core.rdoc.RdTags;


/**
 * Scanner for common Rd code (in no verbatim-like sections).
 */
public class RdCodeScanner extends AbstractRuleBasedScanner {
	
	private static class TagDetector implements IWordDetector {
		
		@Override
		public boolean isWordStart(final char c) {
			return (c == '\\');
		}
		
		@Override
		public boolean isWordPart(final char c) {
			return Character.isLetter(c);
		}
	}
	
	
	public RdCodeScanner(final TextStyleManager textStyles) {
		super(textStyles);
		
		initRules();
	}
	
	@Override
	protected void createRules(final List<IRule> rules) {
		final IToken tDefaultText= getToken(RdTextTokens.DEFAULT);
		
		final IToken tSectionTag= getToken(RdTextTokens.SECTION_TAG);
		final IToken tSubSectionTag= getToken(RdTextTokens.SUBSECTION_TAG);
		final IToken tOtherTag= getToken(RdTextTokens.OTHER_TAG);
		final IToken tUnlistedTag= getToken(RdTextTokens.UNLISTED_TAG);
		
		final IToken tBrackets= getToken(RdTextTokens.BRACKETS);
		
		setDefaultReturnToken(tDefaultText);
		
		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new DefaultWhitespaceDetector()));
		
		final OperatorRule charRule= new OperatorRule(new char[] { '\\', '{', '}' });
		charRule.addOps(RdTags.ESCAPED_CHARS, tOtherTag);
		charRule.addOps(RdTags.BRACKETS, tBrackets);
		rules.add(charRule);
		
		final WordRule tagRule= new WordRule(new TagDetector(), tUnlistedTag);
		for (final String tag : RdTags.MAIN_SECTIONS) {
			tagRule.addWord(tag, tSectionTag);
		}
		for (final String tag : RdTags.SUB_SECTIONS) {
			tagRule.addWord(tag, tSubSectionTag);
		}
		for (final String tag : RdTags.TEXT_MARKUP_TAGs) {
			tagRule.addWord(tag, tOtherTag);
		}
		for (final String tag : RdTags.LIST_TABLE_TAGS) {
			tagRule.addWord(tag, tOtherTag);
		}
		for (final String tag : RdTags.MATH_TAGS) {
			tagRule.addWord(tag, tOtherTag);
		}
		for (final String tag : RdTags.INSERTIONS) {
			tagRule.addWord(tag, tOtherTag);
		}
		rules.add(tagRule);
	}
	
}
