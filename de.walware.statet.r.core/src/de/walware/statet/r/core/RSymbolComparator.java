/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core;

import java.util.Comparator;
import java.util.Locale;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator;


/**
 * 
 */
public class RSymbolComparator implements Comparator<String> {
	
	
	public static final class PrefixPattern {
		
		private final char[] fPrefix;
		
		
		public PrefixPattern(final String namePrefix) {
			fPrefix = namePrefix.toLowerCase().toCharArray();
		}
		
		
		/**
		 * Tolerant string comparison
		 * 
		 * @param candidate string to test against prefix
		 * @param prefix char array of lowercase prefix
		 * @return if candidate starts with prefix
		 */
		public boolean matches(final String candidate) {
			if (candidate == null || candidate.isEmpty()) {
				return false;
			}
			if (fPrefix.length == 0) {
				return (candidate.charAt(0) != '.');
			}
			int pC = fPrefix[0];
			int cC = Character.toLowerCase(candidate.charAt(0));
			if (cC != pC) {
				return false;
			}
			int pIdx = 0;
			int cIdx = 0;
			while (true) {
				if (pC == cC) {
					if (++pIdx >= fPrefix.length) {
						return true;
					}
					if (++cIdx >= candidate.length()) {
						return false;
					}
					pC = fPrefix[pIdx];
					cC = Character.toLowerCase(candidate.charAt(cIdx));
					continue;
				}
				if (pC == '.' || pC == '_') {
					if (++pIdx >= fPrefix.length) {
						return true;
					}
					pC = fPrefix[pIdx];
					continue;
				}
				if (cC == '.' || cC == '_') {
					if (++cIdx >= candidate.length()) {
						return false;
					}
					cC = Character.toLowerCase(candidate.charAt(cIdx));
					continue;
				}
				return false;
			}
		}
	}
	
	
	public static final Collator R_NAMES_COLLATOR = Collator.getInstance(Locale.ENGLISH);
	static {
		((RuleBasedCollator) R_NAMES_COLLATOR).setUpperCaseFirst(true);
	}
	
	
	@Override
	public int compare(final String name1, final String name2) {
		return R_NAMES_COLLATOR.compare(check(name1), check(name2));
	}
	
	private String check(final String name) {
		if (name.length() > 0 && name.charAt(0) == '`') {
			return name.substring(1);
		}
		return name;
	}
	
}
