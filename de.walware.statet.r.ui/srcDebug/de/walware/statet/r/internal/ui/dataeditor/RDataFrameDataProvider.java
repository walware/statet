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

import de.walware.rj.data.RDataFrame;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RIntegerStore;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RVector;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.services.RService;
import net.sourceforge.nattable.data.IDataProvider;

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
	protected RDataTableContentDescription loadDescription(final RDataFrame struct,
			final RService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final RDataTableContentDescription description = new RDataTableContentDescription(struct);
		description.rowHeaderColumn =
				createNamesColumn("row.names(" + fInput.getFullName() + ")", struct.getRowCount(), r, monitor);
		description.dataColumns = new RDataTableColumn[getColumnCount()];
		for (int i = 0; i < getColumnCount(); i++) {
			description.dataColumns[i] = createColumn(struct.getColumn(i),
					fInput.getFullName() + "[[" + (i+1) + "]]",
					i, struct.getColumnNames().getChar(i), r, monitor);
		}
		return description;
	}
	
	@Override
	protected RDataFrame loadDataFragment(final Store.Fragment<RDataFrame> f,
			final RService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final RVector<RIntegerStore> dim = RDataUtil.checkRIntVector(
				r.evalData("dim(" + fInput.getFullName() + ")", monitor) );
		if (dim.getData().getLength() != 2
				|| dim.getData().getInt(0) != getRowCount()
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
			final RService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final RVector<RIntegerStore> dim = RDataUtil.checkRIntVector(
				r.evalData("dim(" + fInput.getFullName() + ")", monitor) );
		if (dim.getData().getLength() != 2
				|| dim.getData().getInt(0) != getRowCount()
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
