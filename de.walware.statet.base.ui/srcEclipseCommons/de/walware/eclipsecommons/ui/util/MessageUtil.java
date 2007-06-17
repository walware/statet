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

package de.walware.eclipsecommons.ui.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 */
public class MessageUtil {

	
	public static final Pattern MNEMONICS_PATTERN = Pattern.compile("(\\&)[^\\&]"); //$NON-NLS-1$
	
	public static String removeMnemonics(String label) {
		Matcher match = MNEMONICS_PATTERN.matcher(label);
		if (match.find()) {
			return label.substring(0, match.start(0)) + label.substring(match.start(0)+1, label.length());
		}
		return label;
	}

	
	private MessageUtil() { 
	}
	
}
