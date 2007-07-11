/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core;

import java.util.regex.Pattern;


/**
 *
 */
public class RUtil {

	public static final Pattern BACKSLASH_PATTERN = Pattern.compile("\\\\"); //$NON-NLS-1$
	public static final String BACKSLASH_REPLACEMENT = "\\\\\\\\"; //$NON-NLS-1$
	
	
	public static String escapeDoubleQuote(String s) {
		if (s.indexOf('"') < 0) {
			return s;
		}
		StringBuilder escaped = new StringBuilder(s.length()+1);
		boolean nextEscaped = false;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\\':
				nextEscaped = !nextEscaped;
				escaped.append(c);
				break;
			case '"':
				if (!nextEscaped) {
					escaped.append('\\');
				}
				// no break
			default:
				nextEscaped = false;
				escaped.append(c);
				break;
			}
		}
		return escaped.toString();
	}
	
	public static String escapeCompletly(String s) {
		StringBuilder escaped = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\\':
			case '\'':
			case '"':
				escaped.append('\\');
				// no break
			default:
				escaped.append(c);
				break;
			}
		}
		return escaped.toString();
	}

}
