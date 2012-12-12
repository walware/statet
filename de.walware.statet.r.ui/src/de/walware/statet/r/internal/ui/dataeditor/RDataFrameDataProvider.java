/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.ecommons.collections.ConstList;

import de.walware.rj.data.RDataFrame;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RIntegerStore;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RVector;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.ui.dataeditor.IRDataTableInput;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class RDataFrameDataProvider extends AbstractRDataProvider<RDataFrame> {
	
	
	protected RDataFrame fRObject;
	
	
	public RDataFrameDataProvider(final IRDataTableInput input, final RDataFrame struct) throws CoreException {
		super(input, struct);
		fRObject = struct;
		
		reset();
	}
	
	
	@Override
	public RDataFrame getRObject() {
		return fRObject;
	}
	
	@Override
	protected int getColumnCount(final RDataFrame struct) {
		return struct.getColumnCount();
	}
	
	@Override
	protected int getRowCount(final RDataFrame struct) {
		return struct.getRowCount();
	}
	
	@Override
	protected RDataFrame validateObject(final RObject struct) throws UnexpectedRDataException {
		final RDataFrame dataFrame = RDataUtil.checkRDataFrame(struct, getColumnCount());
//		for (int i = 0; i < getColumnCount(); i++) {
//			RDataUtil.checkData(dataFrame.getColumn(i), fRObject.getColumn(i).getStoreType());
//		}
		fRObject = dataFrame;
		return dataFrame;
	}
	
	@Override
	protected RDataTableContentDescription loadDescription(final RElementName name,
			final RDataFrame struct, final IRToolService r,
			final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final RDataTableContentDescription description = new RDataTableContentDescription(name, struct, r.getTool());
		final int columnCount = getColumnCount();
		
		description.setRowHeaderColumns(
				createNamesColumn("row.names(" + fInput.getFullName() + ")", struct.getRowCount(),
						r, monitor ));
		final RDataTableColumn[] dataColumns = new RDataTableColumn[columnCount];
		for (int i = 0; i < columnCount; i++) {
			final String columnName = struct.getColumnNames().getChar(i);
			final RElementName elementName = RElementName.concat(new ConstList<RElementName>(BASE_NAME,
					RElementName.create(RElementName.SUB_NAMEDPART, columnName, i+1 )));
			dataColumns[i] = createColumn(struct.getColumn(i),
					fInput.getFullName() + "[[" + (i+1) + "]]", elementName, i, columnName,
					r, monitor);
		}
		description.setDataColumns(dataColumns);
		description.setVariables(dataColumns);
		
		return description;
	}
	
	@Override
	protected RDataFrame loadDataFragment(final Store.Fragment<RDataFrame> f,
			final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final RVector<RIntegerStore> dim = RDataUtil.checkRIntVector(
				r.evalData("dim(" + fInput.getFullName() + ")", monitor) );
		if (dim.getData().getLength() != 2
				|| dim.getData().getInt(0) != getFullRowCount()
				|| dim.getData().getInt(1) != getColumnCount() ) {
			throw new UnexpectedRDataException("dim");
		}
		
		final RObject fragment;
		{	final StringBuilder cmd = getRCmdStringBuilder();
			cmd.append("rj:::.getDataFrameValues(");
			cmd.append(fInput.getFullName());
			cmd.append(',');
			appendRowIdxs(cmd, f.beginRowIdx, f.endRowIdx);
			cmd.append(',');
			appendColumnIdxs(cmd, f.beginColumnIdx, f.endColumnIdx);
			cmd.append(')');
			fragment = r.evalData(cmd.toString(), monitor);
		}
		final RDataFrame dataframe = RDataUtil.checkRDataFrame(fragment,
				f.endColumnIdx - f.beginColumnIdx);
		if (dataframe.getRowCount() != (f.endRowIdx - f.beginRowIdx)) {
			throw new UnexpectedRDataException("row count");
		}
		for (int i = f.beginColumnIdx; i < f.endColumnIdx; i++) {
			RDataUtil.checkData(dataframe.getColumn(i - f.beginColumnIdx),
					fRObject.getColumn(i).getStoreType());
		}
		return dataframe;
	}
	
	@Override
	protected RVector<?> loadRowNamesFragment(final Store.Fragment<RVector<?>> f,
			final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final RVector<RIntegerStore> dim = RDataUtil.checkRIntVector(
				r.evalData("dim(" + fInput.getFullName() + ")", monitor) );
		if (dim.getData().getLength() != 2
				|| dim.getData().getInt(0) != getFullRowCount()
				|| dim.getData().getInt(1) != getColumnCount() ) {
			throw new UnexpectedRDataException("dim");
		}
		
		final RObject fragment;
		{	final StringBuilder cmd = getRCmdStringBuilder();
			cmd.append("rj:::.getDataFrameRowNames(");
			cmd.append(fInput.getFullName());
			cmd.append(',');
			appendRowIdxs(cmd, f.beginRowIdx, f.endRowIdx);
			cmd.append(')');
			fragment = r.evalData(cmd.toString(), monitor);
		}
		final RVector<?> vector = RDataUtil.checkRVector(fragment);
		if (vector.getLength() != (f.endRowIdx - f.beginRowIdx)) {
			throw new UnexpectedRDataException("length");
		}
		return vector;
	}
	
	@Override
	protected void appendOrderCmd(final StringBuilder cmd, final SortColumn sortColumn) {
		cmd.append("order(");
		cmd.append(fInput.getFullName());
		cmd.append("[[");
		cmd.append((sortColumn.columnIdx + 1));
		cmd.append("]],decreasing=");
		cmd.append(sortColumn.decreasing ? "TRUE" : "FALSE");
		cmd.append(')');
	}
	
	
	@Override
	protected Object getDataValue(final Store.Fragment<RDataFrame> fragment,
			final int rowIdx, final int columnIdx) {
		return fragment.rObject.getColumn(columnIdx - fragment.beginColumnIdx).get(
				rowIdx - fragment.beginRowIdx);
	}
	
	@Override
	public IDataProvider createColumnDataProvider() {
		return new ColumnDataProvider() {
			@Override
			public Object getDataValue(final int columnIndex, final int rowIndex) {
				return fRObject.getName(columnIndex);
			}
		};
	}
	
	@Override
	protected Object getColumnName(final Store.Fragment<RDataFrame> fragment, final int columnIdx) {
		return fragment.rObject.getName(columnIdx - fragment.beginColumnIdx);
	}
	
}
