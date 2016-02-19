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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

import de.walware.rj.data.RVector;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.utils.dataaccess.LazyRStore;
import de.walware.rj.services.utils.dataaccess.RVectorDataAdapter;

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.ui.dataeditor.IRDataTableInput;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class RVectorDataProvider extends AbstractRDataProvider<RVector<?>> {
	
	
	public RVectorDataProvider(final IRDataTableInput input, final RVector<?> struct) throws CoreException {
		super(input, new RVectorDataAdapter(), struct);
		
		reset();
	}
	
	
	@Override
	protected RDataTableContentDescription loadDescription(final RElementName name,
			final RVector<?> struct, final IRToolService r,
			final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final RDataTableContentDescription description = new RDataTableContentDescription(name, struct, r.getTool());
		
		description.setRowHeaderColumns(
				createNamesColumn("names(" + fInput.getFullName() + ")", fAdapter.getRowCount(struct),
						r, monitor ));
		
		final RDataTableColumn dataColumn = createColumn(struct.getData(),
						fInput.getFullName(), BASE_NAME, 0, fInput.getLastName(),
						r, monitor );
		description.setDataColumns(dataColumn);
		description.setVariables(dataColumn);
		
		description.setDefaultDataFormat(dataColumn.getDefaultFormat());
		
		return description;
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
	protected Object getDataValue(final LazyRStore.Fragment<RVector<?>> fragment,
			final long rowIdx, final long columnIdx) {
		return fragment.getRObject().getData().get(fragment.toLocalRowIdx(rowIdx));
	}
	
	
	@Override
	public boolean hasRealColumns() {
		return false;
	}
	
	@Override
	public IDataProvider createColumnDataProvider() {
		return new ColumnDataProvider() {
			@Override
			public Object getDataValue(final long columnIndex, final long rowIndex) {
				return fInput.getLastName();
			}
		};
	}
	
	@Override
	protected Object getColumnName(final LazyRStore.Fragment<RVector<?>> fragment, final long columnIdx) {
		throw new UnsupportedOperationException();
	}
	
}
