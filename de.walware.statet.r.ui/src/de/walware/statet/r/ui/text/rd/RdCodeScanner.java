/*******************************************************************************
 * Copyright (c) 2005-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.text.rd;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import de.walware.ecommons.text.DefaultWhitespaceDetector;
import de.walware.ecommons.text.ui.OperatorRule;
import de.walware.ecommons.text.ui.presentation.AbstractRuleBasedScanner;
import de.walware.ecommons.ui.ColorManager;

import de.walware.statet.r.core.rdoc.RdTags;
import de.walware.statet.r.ui.RUIPreferenceConstants;


/**
 * Scanner for common Rd code (in no verbatim-like sections).
 */
public class RdCodeScanner extends AbstractRuleBasedScanner {
	
	private static class TagDetector implements IWordDetector {
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.rules.IWordDetector#isWordStart(char)
		 */
		@Override
		public boolean isWordStart(final char c) {
			return (c == '\\');
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.rules.IWordDetector#isWordPart(char)
		 */
		@Override
		public boolean isWordPart(final char c) {
			return Character.isLetter(c);
		}
	}
	
	public RdCodeScanner(final ColorManager colorManager, final IPreferenceStore preferenceStore) {
		super(colorManager, preferenceStore, RUIPreferenceConstants.Rd.TS_GROUP_ID);
		initialize();
	}
	
	@Override
	protected List<IRule> createRules() {
		
		final IToken tDefaultText = getToken(RdTextTokens.DEFAULT);
		
		final IToken tSectionTag = getToken(RdTextTokens.SECTION_TAG);
		final IToken tSubSectionTag = getToken(RdTextTokens.SUBSECTION_TAG);
		final IToken tOtherTag = getToken(RdTextTokens.OTHER_TAG);
		final IToken tUnlistedTag = getToken(RdTextTokens.UNLISTED_TAG);
		
		final IToken tBrackets = getToken(RdTextTokens.BRACKETS);
				
		setDefaultReturnToken(tDefaultText);
		
		final List<IRule> rules = new ArrayList<IRule>();
		
		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new DefaultWhitespaceDetector()));
		
		final OperatorRule charRule = new OperatorRule(new char[] { '\\', '{', '}' });
		charRule.addOps(RdTags.ESCAPED_CHARS, tOtherTag);
		charRule.addOps(RdTags.BRACKETS, tBrackets);
		rules.add(charRule);
		
		final WordRule tagRule = new WordRule(new TagDetector(), tUnlistedTag);
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
		
		return rules;
	}
	
}
