/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.dataeditor;

import java.util.Collections;
import java.util.List;

import de.walware.jcommons.collections.ImCollections;

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
		this.fElementName = elementName;
		this.fStruct = struct;
		this.fLabel = elementName.getDisplayName();
		this.fRHandle = rHandle;
	}
	
	
	public RElementName getElementName() {
		return this.fElementName;
	}
	
	public RObject getRElementStruct() {
		return this.fStruct;
	}
	
	public String getLabel() {
		return this.fLabel;
	}
	
	public ITool getRHandle() {
		return this.fRHandle;
	}
	
	
	void setColumnHeaderRows(final RDataTableColumn... dataColumns) {
		this.fColumnHeaderRows= ImCollections.newList(dataColumns);
	}
	
	void setRowHeaderColumns(final RDataTableColumn... dataColumns) {
		this.fRowHeaderColumns= ImCollections.newList(dataColumns);
	}
	
	void setDataColumns(final RDataTableColumn... dataColumns) {
		this.fDataColumns = ImCollections.newList(dataColumns);
	}
	
	void setVariables(final IRDataTableVariable... variables) {
		this.fDataVariables = variables;
	}
	
	void setDefaultDataFormat(final RDataFormatter format) {
		this.fDefaultDataFormat = format;
	}
	
	
	public List<RDataTableColumn> getColumnHeaderRows() {
		return this.fColumnHeaderRows;
	}
	
	public List<RDataTableColumn> getRowHeaderColumns() {
		return this.fRowHeaderColumns;
	}
	
	public List<RDataTableColumn> getDataColumns() {
		return this.fDataColumns;
	}
	
	IRDataTableVariable[] getVariables() {
		return this.fDataVariables;
	}
	
	
	public RDataFormatter getDefaultDataFormat() {
		return this.fDefaultDataFormat;
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
