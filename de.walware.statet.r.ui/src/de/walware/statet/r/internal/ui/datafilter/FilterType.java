/*******************************************************************************
 * Copyright (c) 2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.datafilter;

import java.util.List;

import de.walware.ecommons.collections.ConstList;


public class FilterType {
	
	
	public static final FilterType LEVEL = new FilterType(0, Messages.LevelFilter_label);
	public static final FilterType INTERVAL = new FilterType(1, Messages.IntervalFilter_label);
	public static final FilterType TEXT = new FilterType(2, Messages.TextFilter_label);
	
	public static final List<FilterType> TYPES = new ConstList<FilterType>(LEVEL, INTERVAL, TEXT);
	
	
	private final int fId;
	
	private final String fLabel;
	
	
	public FilterType(final int id, final String label) {
		fId = id;
		fLabel = label;
	}
	
	
	public int getId() {
		return fId;
	}
	
	public String getLabel() {
		return fLabel;
	}
	
	
	@Override
	public String toString() {
		return fId + ": " + fLabel.substring(1); //$NON-NLS-1$
	}
	
}
