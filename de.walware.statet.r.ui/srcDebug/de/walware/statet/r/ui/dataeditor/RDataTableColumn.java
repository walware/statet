/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.dataeditor;

import java.util.List;

import de.walware.rj.data.RStore;

import de.walware.statet.r.internal.ui.dataeditor.RDataFormatter;


public class RDataTableColumn {
	
	
	public static final int LOGI = RStore.LOGICAL;
	public static final int INT = RStore.INTEGER;
	public static final int NUM = RStore.NUMERIC;
	public static final int CPLX = RStore.COMPLEX;
	public static final int CHAR = RStore.CHARACTER;
	public static final int RAW = RStore.RAW;
	public static final int FACTOR = RStore.FACTOR;
	public static final int DATE = 0x11;
//	public static final int TIME = 0x12;
	public static final int DATETIME = 0x13;
	
	
	private final int fIndex;
	private final String fName;
	
	private final int fColumnType;
	private final RStore fDataStore;
	
	private final List<String> fClassNames;
	
	private final RDataFormatter fDefaultFormat;
	
	
	public RDataTableColumn(final int columnIndex, final String name,
			final int columnType, final RStore dataStore, final List<String> classNames,
			final RDataFormatter defaultFormat) {
		fIndex = columnIndex;
		fName = name;
		fColumnType = columnType;
		fDataStore = dataStore;
		fClassNames = classNames;
		
		fDefaultFormat = defaultFormat;
	}
	
	
	public int getIndex() {
		return fIndex;
	}
	
	public String getName() {
		return fName;
	}
	
	public int getColumnType() {
		return fColumnType;
	}
	
	public RStore getDataStore() {
		return fDataStore;
	}
	
	public List<String> getClassNames() {
		return fClassNames;
	}
	
	public RDataFormatter getDefaultFormat() {
		return fDefaultFormat;
	}
	
	
	@Override
	public int hashCode() {
		return fIndex * 253;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RDataTableColumn)) {
			return false;
		}
		final RDataTableColumn other = (RDataTableColumn) obj;
		return (fIndex == other.fIndex);
	}
	
}
