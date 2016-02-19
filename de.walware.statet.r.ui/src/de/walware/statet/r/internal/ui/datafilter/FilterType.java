/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
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


public class FilterType {
	
	
	public static final FilterType LEVEL = new FilterType(0, Messages.LevelFilter_label);
	public static final FilterType INTERVAL = new FilterType(1, Messages.IntervalFilter_label);
	public static final FilterType TEXT = new FilterType(2, Messages.TextFilter_label);
	
	public static final ImList<FilterType> TYPES= ImCollections.newList(LEVEL, INTERVAL, TEXT);
	
	
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
