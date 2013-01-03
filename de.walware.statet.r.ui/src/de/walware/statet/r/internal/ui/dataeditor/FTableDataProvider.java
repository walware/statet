/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.dataeditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ISpanningDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.DataCell;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;

import de.walware.rj.data.RArray;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RList;
import de.walware.rj.data.RStore;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.FunctionCall;

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.internal.ui.intable.InfoString;
import de.walware.statet.r.ui.dataeditor.IRDataTableInput;
import de.walware.statet.r.ui.dataeditor.IRDataTableVariable;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class FTableDataProvider extends RMatrixDataProvider {
	
	
	protected abstract class FTableHeaderDataProvider implements ISpanningDataProvider {
		
		
		public FTableHeaderDataProvider() {
		}
		
		
		protected DataCell getCell(final RList vars, final int varIdx, int valueIdx) {
			int span = 1;
			for (int idx = vars.getLength() - 1; idx > varIdx; idx--) {
				span *= vars.get(idx).getLength();
			}
			valueIdx -= valueIdx % span; // to origin
			return createCell(varIdx, valueIdx, span);
		}
		
		protected abstract DataCell createCell(int varPosition, int valuePosition, int valueSpan);
		
		protected Object getDataValue(final RList vars, final int varIdx, int valueIdx) {
			if (vars.getLength() == 0) {
				return InfoString.DUMMY;
			}
			int span = 1;
			for (int idx = vars.getLength() - 1; idx > varIdx; idx--) {
				span *= vars.get(idx).getLength();
			}
			final RStore values = vars.get(varIdx).getData();
			valueIdx %= span * values.getLength(); // remove iteration
			valueIdx /= span; // remove span
			return values.get(valueIdx);
		}
		
		@Override
		public void setDataValue(final int columnIndex, final int rowIndex, final Object newValue) {
			throw new UnsupportedOperationException();
		}
		
	}
	
	protected class FTableColumnDataProvider extends FTableHeaderDataProvider {
		
		
		public FTableColumnDataProvider() {
		}
		
		
		@Override
		public int getColumnCount() {
			return FTableDataProvider.this.getColumnCount();
		}
		
		@Override
		public int getRowCount() {
			return fColVars.getLength();
		}
		
		@Override
		public DataCell getCellByPosition(final int columnIndex, final int rowIndex) {
			return getCell(fColVars, rowIndex, columnIndex);
		}
		
		@Override
		protected DataCell createCell(final int varIndex, final int valueIndex, final int valueSpan) {
			return new DataCell(valueIndex, varIndex, valueSpan, 1);
		}
		
		@Override
		public Object getDataValue(final int columnIndex, final int rowIndex) {
			return getDataValue(fColVars, rowIndex, columnIndex);
		}
		
	}
	
	protected class FTableRowDataProvider extends FTableHeaderDataProvider {
		
		
		public FTableRowDataProvider() {
		}
		
		
		@Override
		public int getColumnCount() {
			return fRowVars.getLength();
		}
		
		@Override
		public int getRowCount() {
			return FTableDataProvider.this.getRowCount();
		}
		
		@Override
		public DataCell getCellByPosition(final int columnIndex, final int rowIndex) {
			return getCell(fRowVars, columnIndex, rowIndex);
		}
		
		@Override
		protected DataCell createCell(final int varPosition, final int valuePosition, final int valueSpan) {
			return new DataCell(varPosition, valuePosition, 1, valueSpan);
		}
		
		@Override
		public Object getDataValue(final int columnIndex, final int rowIndex) {
			return getDataValue(fRowVars, columnIndex, rowIndex);
		}
		
	}
	
	
	private RList fColVars;
	private RList fRowVars;
	
	
	public FTableDataProvider(final IRDataTableInput input, final RArray<?> struct) throws CoreException {
		super(input, struct);
	}
	
	
	@Override
	protected RDataTableContentDescription loadDescription(final RElementName name,
			final RArray<?> struct, final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final RDataTableContentDescription description = new RDataTableContentDescription(name, struct, r.getTool());
		
//		description.rowHeaderColumn = createNamesColumn("rownames(" + fInput.getFullName() + ")", getRowCount(struct), r, monitor);
		
		final RDataTableColumn template = createColumn(struct.getData(),
				fInput.getFullName(), null, -1, null,
				r, monitor );
		
		{	final FunctionCall call = r.createFunctionCall("attr"); //$NON-NLS-1$
			call.add(fInput.getFullName());
			call.addChar("col.vars"); //$NON-NLS-1$
			fColVars = RDataUtil.checkRList(call.evalData(monitor));
			
			if (checkVars(fColVars) != getColumnCount()) {
				fColVars = null;
				throw new UnexpectedRDataException("col.vars"); //$NON-NLS-1$
			}
		}
		{	final FunctionCall call = r.createFunctionCall("attr"); //$NON-NLS-1$
			call.add(fInput.getFullName());
			call.addChar("row.vars"); //$NON-NLS-1$
			fRowVars = RDataUtil.checkRList(call.evalData(monitor));
			
			if (checkVars(fRowVars) != getFullRowCount()) {
				fColVars = null;
				fRowVars = null;
				throw new UnexpectedRDataException("row.vars"); //$NON-NLS-1$
			}
		}
		{	final IRDataTableVariable[] variables = new IRDataTableVariable[fColVars.getLength() + fRowVars.getLength()];
			int i = 0;
			for (int j = 0; j < fColVars.getLength(); j++) {
				variables[i++] = new FTableVariable(IRDataTableVariable.COLUMN, fColVars.getName(j),
						fColVars.get(j).getData() );
			}
			for (int j = 0; j < fRowVars.getLength(); j++) {
				variables[i++] = new FTableVariable(IRDataTableVariable.ROW, fRowVars.getName(j),
						fRowVars.get(j).getData() );
			}
			description.setVariables(variables);
		}
		
		description.setDefaultDataFormat(template.getDefaultFormat());
		
		return description;
	}
	
	private int checkVars(final RList rList) {
		int num = 1;
		for (int i = 0; i < rList.getLength(); i++) {
			num *= rList.get(i).getLength();
		}
		return num;
	}
	
	@Override
	protected ISortModel createSortModel() {
		return null;
	}
	
	@Override
	protected IDataProvider createColumnDataProvider() {
		return new FTableColumnDataProvider();
	}
	
	@Override
	protected IDataProvider createRowDataProvider() {
		return new FTableRowDataProvider();
	}
	
	@Override
	public IDataProvider createColumnLabelProvider() {
		return new IDataProvider() {
			
			@Override
			public int getColumnCount() {
				return 0;
			}
			
			@Override
			public int getRowCount() {
				return fColVars.getLength();
			}
			
			@Override
			public Object getDataValue(final int columnIndex, final int rowIndex) {
				return fColVars.getName(rowIndex);
			}
			
			@Override
			public void setDataValue(final int columnIndex, final int rowIndex, final Object newValue) {
				throw new UnsupportedOperationException();
			}
			
		};
	}
	
	@Override
	public IDataProvider createRowLabelProvider() {
		return new IDataProvider() {
			
			@Override
			public int getColumnCount() {
				return fRowVars.getLength();
			}
			
			@Override
			public int getRowCount() {
				return 0;
			}
			
			@Override
			public Object getDataValue(final int columnIndex, final int rowIndex) {
				return fRowVars.getName(columnIndex);
			}
			
			@Override
			public void setDataValue(final int columnIndex, final int rowIndex, final Object newValue) {
				throw new UnsupportedOperationException();
			}
			
		};
	}
	
}
