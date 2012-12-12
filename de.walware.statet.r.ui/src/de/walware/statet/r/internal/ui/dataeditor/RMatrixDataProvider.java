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

import de.walware.rj.data.RArray;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RIntegerStore;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RStore;
import de.walware.rj.data.RVector;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.ui.dataeditor.IRDataTableInput;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class RMatrixDataProvider extends AbstractRDataProvider<RArray<?>> {
	
	
	protected RArray<?> fRObject;
	
	
	public RMatrixDataProvider(final IRDataTableInput input, final RArray<?> struct) throws CoreException {
		super(input, struct);
		fRObject = struct;
		
		reset();
	}
	
	
	@Override
	public RArray<?> getRObject() {
		return fRObject;
	}
	
	@Override
	protected int getColumnCount(final RArray<?> struct) {
		return struct.getDim().getInt(1);
	}
	
	@Override
	protected int getRowCount(final RArray<?> struct) {
		return struct.getDim().getInt(0);
	}
	
	@Override
	public boolean getAllColumnsEqual() {
		return true;
	}
	
	@Override
	protected RArray<?> validateObject(final RObject struct) throws UnexpectedRDataException {
		final RArray<?> array = RDataUtil.checkRArray(struct, 2);
		if (array.getDim().getInt(1) != getColumnCount()) {
			throw new UnexpectedRDataException("dim column");
		}
//		RDataUtil.checkData(array.getData(), fRObject.getData().getStoreType());
		fRObject = array;
		return array;
	}
	
	@Override
	protected RDataTableContentDescription loadDescription(final RElementName name,
			final RArray<?> struct, final IRToolService r,
			final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final RDataTableContentDescription description = new RDataTableContentDescription(name, struct, r.getTool());
		final int count = getColumnCount();
		
		description.setRowHeaderColumns(
				createNamesColumn("rownames(" + fInput.getFullName() + ")", getRowCount(struct),
						r, monitor ));
		
		final RDataTableColumn template = createColumn(struct.getData(),
						fInput.getFullName(), null, -1, null,
						r, monitor );
		if (count <= 2500) {
			RStore names;
			final RObject rObject = r.evalData("colnames(" + fInput.getFullName() + ")", monitor);
			if (rObject != null && rObject.getRObjectType() == RObject.TYPE_VECTOR
					&& rObject.getLength() == count) {
				names = rObject.getData();
			}
			else {
				names = null;
			}
			final RDataTableColumn[] dataColumns = new RDataTableColumn[count];
			for (int i = 0; i < count; i++) {
				dataColumns[i] = new RDataTableColumn(i,
						(names != null) ? names.getChar(i) : Integer.toString((i+1)), null, null,
						template.getVarType(), template.getDataStore(), template.getClassNames(),
						template.getDefaultFormat());
			}
			description.setVariables(dataColumns);
		}
		
		description.setDefaultDataFormat(template.getDefaultFormat());
		
		return description;
	}
	
	@Override
	protected RArray<?> loadDataFragment(final Store.Fragment<RArray<?>> f,
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
			cmd.append("rj:::.getDataMatrixValues(");
			cmd.append(fInput.getFullName());
			cmd.append(',');
			appendRowIdxs(cmd, f.beginRowIdx, f.endRowIdx);
			cmd.append(',');
			appendColumnIdxs(cmd, f.beginColumnIdx, f.endColumnIdx);
			cmd.append(')');
			fragment = r.evalData(cmd.toString(), monitor);
		}
		final RArray<?> array = RDataUtil.checkRArray(fragment, 2);
		
		if (array.getDim().getInt(0) != (f.endRowIdx - f.beginRowIdx)
				|| array.getDim().getInt(1) != (f.endColumnIdx - f.beginColumnIdx)) {
			throw new UnexpectedRDataException("dim");
		}
		RDataUtil.checkData(fragment.getData(), fRObject.getData().getStoreType());
		return array;
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
			cmd.append("rj:::.getDataMatrixRowNames(");
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
		cmd.append("[,");
		cmd.append((sortColumn.columnIdx + 1));
		cmd.append("],decreasing=");
		cmd.append(sortColumn.decreasing ? "TRUE" : "FALSE");
		cmd.append(')');
	}
	
	
	@Override
	protected Object getDataValue(final Store.Fragment<RArray<?>> fragment,
			final int rowIdx, final int columnIdx) {
		return fragment.rObject.getData().get(RDataUtil.getDataIdx(
				fragment.endRowIdx - fragment.beginRowIdx,
				rowIdx - fragment.beginRowIdx, columnIdx - fragment.beginColumnIdx ));
	}
	
	@Override
	protected Object getColumnName(final Store.Fragment<RArray<?>> fragment, final int columnIdx) {
		final RStore names = fragment.rObject.getNames(1);
		if (names != null) {
			return names.get(columnIdx - fragment.beginColumnIdx);
		}
		else {
			return Integer.toString(columnIdx + 1);
		}
	}
	
}
