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

import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RIntegerStore;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RVector;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.ui.dataeditor.IRDataTableInput;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class RVectorDataProvider extends AbstractRDataProvider<RVector<?>> {
	
	
	protected RVector<?> fRObject;
	
	
	public RVectorDataProvider(final IRDataTableInput input, final RVector<?> struct) throws CoreException {
		super(input, struct);
		fRObject = struct;
		
		reset();
	}
	
	
	@Override
	public RVector<?> getRObject() {
		return fRObject;
	}
	
	@Override
	protected int getColumnCount(final RVector<?> struct) {
		return 1;
	}
	
	@Override
	protected int getRowCount(final RVector<?> struct) {
		return struct.getLength();
	}
	
	@Override
	protected RVector<?> validateObject(final RObject struct) throws UnexpectedRDataException {
		final RVector<?> vector = RDataUtil.checkRVector(struct);
//		RDataUtil.checkData(vector.getData(), fRObject.getData().getStoreType());
		fRObject = vector;
		return vector;
	}
	
	@Override
	protected RDataTableContentDescription loadDescription(final RElementName name,
			final RVector<?> struct, final IRToolService r,
			final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final RDataTableContentDescription description = new RDataTableContentDescription(name, struct, r.getTool());
		
		description.setRowHeaderColumns(
				createNamesColumn("names(" + fInput.getFullName() + ")", getRowCount(struct),
						r, monitor ));
		
		final RDataTableColumn dataColumn = createColumn(struct.getData(),
						fInput.getFullName(), BASE_NAME, 0, fInput.getLastName(),
						r, monitor );
		description.setDataColumns(dataColumn);
		
		description.setDefaultDataFormat(dataColumn.getDefaultFormat());
		
		return description;
	}
	
	@Override
	protected RVector<?> loadDataFragment(final Store.Fragment<RVector<?>> f,
			final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final RVector<RIntegerStore> dim = RDataUtil.checkRIntVector(
				r.evalData("length(" + fInput.getFullName() + ")", monitor) );
		if (dim.getData().getLength() != 1
				|| dim.getData().getInt(0) != getFullRowCount() ) {
			throw new UnexpectedRDataException("dim");
		}
		
		final RObject fragment;
		{	final StringBuilder cmd = getRCmdStringBuilder();
			cmd.append("rj:::.getDataVectorValues(");
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
		RDataUtil.checkData(fragment.getData(), fRObject.getData().getStoreType());
		return vector;
	}
	
	@Override
	protected RVector<?> loadRowNamesFragment(final Store.Fragment<RVector<?>> f,
			final IRToolService r, final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final RVector<RIntegerStore> dim = RDataUtil.checkRIntVector(
				r.evalData("length(" + fInput.getFullName() + ")", monitor) );
		if (dim.getData().getLength() != 1
				|| dim.getData().getInt(0) != getFullRowCount() ) {
			throw new UnexpectedRDataException("dim");
		}
		
		final RObject fragment;
		{	final StringBuilder cmd = getRCmdStringBuilder();
			cmd.append("rj:::.getDataVectorRowNames(");
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
		cmd.append(",decreasing=");
		cmd.append(sortColumn.decreasing ? "TRUE" : "FALSE");
		cmd.append(')');
	}
	
	
	@Override
	protected Object getDataValue(final Store.Fragment<RVector<?>> fragment,
			final int rowIdx, final int columnIdx) {
		return fragment.rObject.getData().get(rowIdx - fragment.beginRowIdx);
	}
	
	@Override
	public IDataProvider createColumnDataProvider() {
		return new ColumnDataProvider() {
			@Override
			public Object getDataValue(final int columnIndex, final int rowIndex) {
				return fInput.getLastName();
			}
		};
	}
	
	@Override
	protected Object getColumnName(final Store.Fragment<RVector<?>> fragment, final int columnIdx) {
		throw new UnsupportedOperationException();
	}
	
}
