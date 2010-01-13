/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.ecommons.ltk.IModelElement;


public class RElementComparator implements Comparator<IModelElement> {
	
	
	private final Comparator fNameComparator = RSymbolComparator.R_NAMES_COLLATOR;
	
	
	public int compare(final IModelElement o1, final IModelElement o2) {
		return fNameComparator.compare(o1.getElementName().getSegmentName(), o2.getElementName().getSegmentName());
	}
	
}
