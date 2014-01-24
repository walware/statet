/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.dataeditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.rj.data.RArray;
import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RStore;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.utils.dataaccess.LazyRStore;
import de.walware.rj.services.utils.dataaccess.RMatrixDataAdapter;

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.ui.dataeditor.IRDataTableInput;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class RMatrixDataProvider extends AbstractRDataProvider<RArray<?>> {
	
	
	public RMatrixDataProvider(final IRDataTableInput input, final RArray<?> struct) throws CoreException {
		super(input, new RMatrixDataAdapter(), struct);
		
		reset();
	}
	
	
	@Override
	public boolean getAllColumnsEqual() {
		return true;
	}
	
	@Override
	protected RDataTableContentDescription loadDescription(final RElementName name,
			final RArray<?> struct, final IRToolService r,
			final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final RDataTableContentDescription description = new RDataTableContentDescription(name, struct, r.getTool());
		final long count = getColumnCount();
		
		description.setRowHeaderColumns(
				createNamesColumn("rownames(" + fInput.getFullName() + ")", fAdapter.getRowCount(struct),
						r, monitor ));
		
		final RDataTableColumn template = createColumn(struct.getData(),
						fInput.getFullName(), null, -1, null,
						r, monitor );
		if (count <= 2500) {
			final int l = (int) count;
			RStore names;
			final RObject rObject = r.evalData("colnames(" + fInput.getFullName() + ")", monitor);
			if (rObject != null && rObject.getRObjectType() == RObject.TYPE_VECTOR
					&& rObject.getLength() == l) {
				names = rObject.getData();
			}
			else {
				names = null;
			}
			final RDataTableColumn[] dataColumns = new RDataTableColumn[l];
			for (int i = 0; i < l; i++) {
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
	protected Object getDataValue(final LazyRStore.Fragment<RArray<?>> fragment,
			final long rowIdx, final long columnIdx) {
		return fragment.getRObject().getData().get(RDataUtil.getDataIdx(fragment.getRowCount(),
				fragment.toLocalRowIdx(rowIdx), fragment.toLocalColumnIdx(columnIdx) ));
	}
	
	@Override
	protected Object getColumnName(final LazyRStore.Fragment<RArray<?>> fragment, final long columnIdx) {
		final RStore names = fragment.getRObject().getNames(1);
		if (names != null) {
			return names.get(columnIdx - fragment.getColumnBeginIdx());
		}
		else {
			return Long.toString(columnIdx + 1);
		}
	}
	
}
