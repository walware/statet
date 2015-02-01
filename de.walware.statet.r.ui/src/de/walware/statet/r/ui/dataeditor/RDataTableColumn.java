/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.dataeditor;

import java.util.List;

import de.walware.rj.data.RStore;

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.internal.ui.dataeditor.RDataFormatter;


public class RDataTableColumn implements IRDataTableVariable {
	
	
	private final long fIndex;
	private final String fName;
	
	private final String fRExpression;
	private final RElementName fElementName;
	
	private final int fColumnType;
	private final RStore fDataStore;
	
	private final List<String> fClassNames;
	
	private final RDataFormatter fDefaultFormat;
	
	
	public RDataTableColumn(final long columnIndex, final String name,
			final String rExpression, final RElementName elementName,
			final int columnType, final RStore dataStore, final List<String> classNames,
			final RDataFormatter defaultFormat) {
		fIndex = columnIndex;
		fName = name;
		fRExpression = rExpression;
		fElementName = elementName;
		fColumnType = columnType;
		fDataStore = dataStore;
		fClassNames = classNames;
		
		fDefaultFormat = defaultFormat;
	}
	
	
	@Override
	public int getVarPresentation() {
		return COLUMN;
	}
	
	public long getIndex() {
		return fIndex;
	}
	
	@Override
	public String getName() {
		return fName;
	}
	
	public String getRExpression() {
		return fRExpression;
	}
	
	public RElementName getElementName() {
		return fElementName;
	}
	
	@Override
	public int getVarType() {
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
		final int h = (int) (fIndex ^ (fIndex >>> 32));
		return h ^ (h >>> 7);
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
