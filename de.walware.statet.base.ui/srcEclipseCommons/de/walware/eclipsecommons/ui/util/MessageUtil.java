/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 */
public class MessageUtil {
	
	
	public static final Pattern MNEMONICS_PATTERN = Pattern.compile("(\\&)[^\\&]"); //$NON-NLS-1$
	public static final Pattern AMPERSAND_PATTERN = Pattern.compile("\\&"); //$NON-NLS-1$
	public static final String AMPERSAND_TOOLTIP_REPLACEMENT = Matcher.quoteReplacement("&&"); //$NON-NLS-1$
	
	public static String removeMnemonics(final String label) {
		final Matcher match = MNEMONICS_PATTERN.matcher(label);
		if (match.find()) {
			return label.substring(0, match.start(0)) + label.substring(match.start(0)+1, label.length());
		}
		return label;
	}
	
	public static String escapeForFormText(final String text) {
		final StringBuilder escaped = new StringBuilder(text.length());
		ITERATE_CHARS : for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			switch (c) {
			case '<':
				escaped.append("&lt;"); //$NON-NLS-1$
				continue ITERATE_CHARS;
			case '>':
				escaped.append("&gt;"); //$NON-NLS-1$
				continue ITERATE_CHARS;
			case '&':
				escaped.append("&amp;"); //$NON-NLS-1$
				continue ITERATE_CHARS;
			case '"':
				escaped.append("&quot;"); //$NON-NLS-1$
				continue ITERATE_CHARS;
			case '\'':
				escaped.append("&apos;"); //$NON-NLS-1$
				continue ITERATE_CHARS;
			default:
				escaped.append(c);
				continue ITERATE_CHARS;
			}
		}
		return escaped.toString();
	}
	
	public static String escapeForTooltip(final CharSequence text) {
		return AMPERSAND_PATTERN.matcher(text).replaceAll(AMPERSAND_TOOLTIP_REPLACEMENT);
	}
	
	public static String escapeForMenu(final String text) {
		final StringBuilder escaped = new StringBuilder(text.length());
		ITERATE_CHARS : for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			switch (c) {
			case '&':
				escaped.append("&&"); //$NON-NLS-1$
				continue ITERATE_CHARS;
			case '\t':
				escaped.append("  "); //$NON-NLS-1$
				continue ITERATE_CHARS;
			case '\n':
				escaped.append("  "); //$NON-NLS-1$
				continue ITERATE_CHARS;
			default:
				escaped.append(c);
				continue ITERATE_CHARS;
			}
		}
		return escaped.toString();
	}
	
	
	private MessageUtil() {
	}
	
}
