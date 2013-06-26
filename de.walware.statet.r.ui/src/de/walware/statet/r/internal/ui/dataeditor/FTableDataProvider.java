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
		
		
		protected DataCell getCell(final RList vars, final long varIdx, long valueIdx) {
			int span = 1;
			for (long idx = vars.getLength() - 1; idx > varIdx; idx--) {
				span *= vars.get(idx).getLength();
			}
			valueIdx -= valueIdx % span; // to origin
			return createCell(varIdx, valueIdx, span);
		}
		
		protected abstract DataCell createCell(long varPosition, long valuePosition, int valueSpan);
		
		protected Object getDataValue(final RList vars, final long varIdx, long valueIdx) {
			if (vars.getLength() == 0) {
				return InfoString.DUMMY;
			}
			int span = 1;
			for (long idx = vars.getLength() - 1; idx > varIdx; idx--) {
				span *= vars.get(idx).getLength();
			}
			final RStore values = vars.get(varIdx).getData();
			valueIdx %= span * values.getLength(); // remove iteration
			valueIdx /= span; // remove span
			return values.get(valueIdx);
		}
		
		@Override
		public void setDataValue(final long columnIndex, final long rowIndex, final Object newValue) {
			throw new UnsupportedOperationException();
		}
		
	}
	
	protected class FTableColumnDataProvider extends FTableHeaderDataProvider {
		
		
		public FTableColumnDataProvider() {
		}
		
		
		@Override
		public long getColumnCount() {
			return FTableDataProvider.this.getColumnCount();
		}
		
		@Override
		public long getRowCount() {
			return fColVars.getLength();
		}
		
		@Override
		public DataCell getCellByPosition(final long columnIndex, final long rowIndex) {
			return getCell(fColVars, rowIndex, columnIndex);
		}
		
		@Override
		protected DataCell createCell(final long varIndex, final long valueIndex, final int valueSpan) {
			return new DataCell(valueIndex, varIndex, valueSpan, 1);
		}
		
		@Override
		public Object getDataValue(final long columnIndex, final long rowIndex) {
			return getDataValue(fColVars, rowIndex, columnIndex);
		}
		
	}
	
	protected class FTableRowDataProvider extends FTableHeaderDataProvider {
		
		
		public FTableRowDataProvider() {
		}
		
		
		@Override
		public long getColumnCount() {
			return fRowVars.getLength();
		}
		
		@Override
		public long getRowCount() {
			return FTableDataProvider.this.getRowCount();
		}
		
		@Override
		public DataCell getCellByPosition(final long columnIndex, final long rowIndex) {
			return getCell(fRowVars, columnIndex, rowIndex);
		}
		
		@Override
		protected DataCell createCell(final long varPosition, final long valuePosition, final int valueSpan) {
			return new DataCell(varPosition, valuePosition, 1, valueSpan);
		}
		
		@Override
		public Object getDataValue(final long columnIndex, final long rowIndex) {
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
		{	final int cols = (int) fColVars.getLength();
			final int rows = (int) fRowVars.getLength();
			final IRDataTableVariable[] variables = new IRDataTableVariable[cols + rows];
			int i = 0;
			for (int j = 0; j < cols; j++) {
				variables[i++] = new FTableVariable(IRDataTableVariable.COLUMN, fColVars.getName(j),
						fColVars.get(j).getData() );
			}
			for (int j = 0; j < rows; j++) {
				variables[i++] = new FTableVariable(IRDataTableVariable.ROW, fRowVars.getName(j),
						fRowVars.get(j).getData() );
			}
			description.setVariables(variables);
		}
		
		description.setDefaultDataFormat(template.getDefaultFormat());
		
		return description;
	}
	
	private long checkVars(final RList rList) {
		long num = 1;
		final int l = (int) rList.getLength();
		for (int i = 0; i < l; i++) {
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
			public long getColumnCount() {
				return 0;
			}
			
			@Override
			public long getRowCount() {
				return fColVars.getLength();
			}
			
			@Override
			public Object getDataValue(final long columnIndex, final long rowIndex) {
				return fColVars.getName(rowIndex);
			}
			
			@Override
			public void setDataValue(final long columnIndex, final long rowIndex, final Object newValue) {
				throw new UnsupportedOperationException();
			}
			
		};
	}
	
	@Override
	public IDataProvider createRowLabelProvider() {
		return new IDataProvider() {
			
			@Override
			public long getColumnCount() {
				return fRowVars.getLength();
			}
			
			@Override
			public long getRowCount() {
				return 0;
			}
			
			@Override
			public Object getDataValue(final long columnIndex, final long rowIndex) {
				return fRowVars.getName(columnIndex);
			}
			
			@Override
			public void setDataValue(final long columnIndex, final long rowIndex, final Object newValue) {
				throw new UnsupportedOperationException();
			}
			
		};
	}
	
}
