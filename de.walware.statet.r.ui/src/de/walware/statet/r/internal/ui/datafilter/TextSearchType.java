/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.datafilter;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;


public abstract class TextSearchType {
	
	public static final TextSearchType ECLIPSE = new Eclipse();
	public static final TextSearchType REGEX = new Regex();
	public static final TextSearchType EXACT = new Exact();
	
	public static final ImList<TextSearchType> TYPES= ImCollections.newList(ECLIPSE, REGEX, EXACT);
	
	
	private static class Eclipse extends TextSearchType {
		
		private Eclipse() {
			super(0, Messages.TextSearch_Eclipse_label);
		}
		
		@Override
		public String getRPattern(final String pattern) {
			if (pattern.length() == 0) {
				return pattern;
			}
			final StringBuilder sb = new StringBuilder(pattern.length());
			int index = 0;
			if (pattern.charAt(0) == '*') {
				index++;
			}
			else {
				sb.append('^');
			}
			while (index < pattern.length()) {
				final char c = pattern.charAt(index++);
				switch (c) {
				case '\\':
					if (index < pattern.length()) {
						final char c2 = pattern.charAt(index++);
						sb.append('\\');
						sb.append(c2);
					}
					continue;
				case '*':
					sb.append(".*"); //$NON-NLS-1$
					continue;
				case '?':
					sb.append(".?"); //$NON-NLS-1$
					continue;
				case '.':
				case '|':
				case '(':
				case ')':
				case '[':
				case '{':
				case '^':
				case '$':
				case '+':
					sb.append('\\');
					sb.append(c);
					continue;
				default:
					sb.append(c);
					continue;
				}
			}
			return sb.toString();
		}
		
	}
	
	private static class Regex extends TextSearchType {
		
		private Regex() {
			super(1, Messages.TextSearch_Regex_label);
		}
		
		@Override
		public String getRPattern(final String pattern) {
			return pattern;
		}
		
	}
	
	private static class Exact extends TextSearchType {
		
		private Exact() {
			super(2, Messages.TextSearch_Exact_label);
		}
		
		@Override
		public String getRPattern(final String pattern) {
			return pattern;
		}
		
	}
	
	
	private final int fId;
	
	private final String fLabel;
	
	
	private TextSearchType(final int id, final String label) {
		fId = id;
		fLabel = label;
	}
	
	
	public int getId() {
		return fId;
	}
	
	public String getLabel() {
		return fLabel;
	}
	
	public abstract String getRPattern(String pattern);
	
	
	@Override
	public String toString() {
		return getClass().getName();
	}
	
}
