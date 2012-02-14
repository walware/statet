/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.model;

import java.util.Comparator;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.IModelElement;

import de.walware.statet.r.core.RSymbolComparator;


public class RElementComparator implements Comparator<IModelElement> {
	
	
	private final Comparator<Object> fNameComparator = RSymbolComparator.R_NAMES_COLLATOR;
	
	
	@Override
	public int compare(final IModelElement o1, final IModelElement o2) {
		final IElementName name1 = o1.getElementName();
		final IElementName name2 = o2.getElementName();
		return fNameComparator.compare(name1.getSegmentName(), name2.getSegmentName());
	}
	
}
