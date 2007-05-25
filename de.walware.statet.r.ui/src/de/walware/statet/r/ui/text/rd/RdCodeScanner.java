/*******************************************************************************
 * Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
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

import de.walware.eclipsecommons.ui.util.ColorManager;

import de.walware.statet.ext.ui.text.DefaultWhitespaceDetector;
import de.walware.statet.ext.ui.text.OperatorRule;
import de.walware.statet.ext.ui.text.StatextTextScanner;
import de.walware.statet.r.core.rdoc.RdTags;
import de.walware.statet.r.ui.RUIPreferenceConstants;


/**
 * Scanner for common Rd code (in no verbatim-like sections).
 * 
 * @author Stephan Wahlbrink
 */
public class RdCodeScanner extends StatextTextScanner {
	
	private static class TagDetector implements IWordDetector {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.rules.IWordDetector#isWordStart(char)
		 */
		public boolean isWordStart(char c) {
			return (c == '\\');
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.rules.IWordDetector#isWordPart(char)
		 */
		public boolean isWordPart(char c) {
			return Character.isLetter(c);
		}
	}
	
	public RdCodeScanner(ColorManager colorManager, IPreferenceStore preferenceStore) {
		super(colorManager, preferenceStore, RUIPreferenceConstants.Rd.CONTEXT_ID);
		initialize();
	}
	
	protected List<IRule> createRules() {
		
		IToken tDefaultText = getToken(RdTextTokens.DEFAULT);
		
		IToken tSectionTag = getToken(RdTextTokens.SECTION_TAG);
		IToken tSubSectionTag = getToken(RdTextTokens.SUBSECTION_TAG);
		IToken tOtherTag = getToken(RdTextTokens.OTHER_TAG);
		IToken tUnlistedTag = getToken(RdTextTokens.UNLISTED_TAG);
		
		IToken tBrackets = getToken(RdTextTokens.BRACKETS);
				
		setDefaultReturnToken(tDefaultText);

		List<IRule> rules = new ArrayList<IRule>();
		
		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new DefaultWhitespaceDetector()));

		OperatorRule charRule = new OperatorRule(new char[] { '\\', '{', '}' });
		charRule.addOps(RdTags.ESCAPED_CHARS, tOtherTag);
		charRule.addOps(RdTags.BRACKETS, tBrackets);
		rules.add(charRule);
		
		WordRule tagRule = new WordRule(new TagDetector(), tUnlistedTag);
		for (String tag : RdTags.MAIN_SECTIONS) {
			tagRule.addWord(tag, tSectionTag);
		}
		for (String tag : RdTags.SUB_SECTIONS) {
			tagRule.addWord(tag, tSubSectionTag);
		}
		for (String tag : RdTags.TEXT_MARKUP_TAGs) {
			tagRule.addWord(tag, tOtherTag);
		}
		for (String tag : RdTags.LIST_TABLE_TAGS) {
			tagRule.addWord(tag, tOtherTag);
		}
		for (String tag : RdTags.MATH_TAGS) {
			tagRule.addWord(tag, tOtherTag);
		}
		for (String tag : RdTags.INSERTIONS) {
			tagRule.addWord(tag, tOtherTag);
		}
		rules.add(tagRule);
		
		return rules;
	}

}
