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

import java.util.Comparator;
import java.util.Locale;

import com.ibm.icu.text.Collator;


/**
 *
 */
public class RNamesComparator implements Comparator<String> {
	
	
	protected static final Collator gCollator = Collator.getInstance(Locale.ENGLISH);

	
	public int compare(String name1, String name2) {
		return gCollator.compare(name1, name2);
	}
	
}
