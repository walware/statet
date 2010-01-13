/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
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


/**
 * 
 */
public class RSymbolComparator implements Comparator<String> {
	
	
	public static final Collator R_NAMES_COLLATOR = Collator.getInstance(Locale.ENGLISH);
	
	
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
