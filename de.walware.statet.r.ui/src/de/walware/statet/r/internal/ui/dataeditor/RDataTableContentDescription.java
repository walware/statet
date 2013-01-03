/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.dataeditor;

import java.util.Collections;
import java.util.List;

import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.ts.ITool;

import de.walware.rj.data.RObject;

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.ui.dataeditor.IRDataTableVariable;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public final class RDataTableContentDescription {
	
	
	private static final IRDataTableVariable[] NO_COLUMNS_ARRAY = new IRDataTableVariable[0];
	
	
	private final RElementName fElementName;
	private final RObject fStruct;
	
	private final String fLabel;
	
	private final ITool fRHandle;
	
	private List<RDataTableColumn> fColumnHeaderRows = Collections.emptyList();
	private List<RDataTableColumn> fRowHeaderColumns = Collections.emptyList();
	
	private List<RDataTableColumn> fDataColumns = Collections.emptyList();
	private IRDataTableVariable[] fDataVariables = NO_COLUMNS_ARRAY;
	
	private RDataFormatter fDefaultDataFormat;
	
	
	public RDataTableContentDescription(final RElementName elementName, final RObject struct,
			final ITool rHandle) {
		if (elementName == null) {
			throw new NullPointerException("elementName");
		}
		if (struct == null) {
			throw new NullPointerException("struct");
		}
		fElementName = elementName;
		fStruct = struct;
		fLabel = elementName.getDisplayName();
		fRHandle = rHandle;
	}
	
	
	public RElementName getElementName() {
		return fElementName;
	}
	
	public RObject getRElementStruct() {
		return fStruct;
	}
	
	public String getLabel() {
		return fLabel;
	}
	
	public ITool getRHandle() {
		return fRHandle;
	}
	
	
	void setColumnHeaderRows(final RDataTableColumn... dataColumns) {
		fColumnHeaderRows = new ConstList<RDataTableColumn>(dataColumns);
	}
	
	void setRowHeaderColumns(final RDataTableColumn... dataColumns) {
		fRowHeaderColumns = new ConstList<RDataTableColumn>(dataColumns);
	}
	
	void setDataColumns(final RDataTableColumn... dataColumns) {
		fDataColumns = new ConstList<RDataTableColumn>(dataColumns);
	}
	
	void setVariables(final IRDataTableVariable... variables) {
		fDataVariables = variables;
	}
	
	void setDefaultDataFormat(final RDataFormatter format) {
		fDefaultDataFormat = format;
	}
	
	
	public List<RDataTableColumn> getColumnHeaderRows() {
		return fColumnHeaderRows;
	}
	
	public List<RDataTableColumn> getRowHeaderColumns() {
		return fRowHeaderColumns;
	}
	
	public List<RDataTableColumn> getDataColumns() {
		return fDataColumns;
	}
	
	IRDataTableVariable[] getVariables() {
		return fDataVariables;
	}
	
	
	public RDataFormatter getDefaultDataFormat() {
		return fDefaultDataFormat;
	}
	
	
	@Override
	public int hashCode() {
		return 986986;
	}
	
	@Override
	public boolean equals(final Object obj) {
		return (obj instanceof RDataTableContentDescription);
	}
	
}
