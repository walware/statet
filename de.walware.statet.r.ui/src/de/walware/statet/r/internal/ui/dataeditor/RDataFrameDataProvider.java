/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
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

import de.walware.jcommons.collections.ImCollections;

import de.walware.rj.data.RDataFrame;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.eclient.IRToolService;
import de.walware.rj.services.utils.dataaccess.LazyRStore;
import de.walware.rj.services.utils.dataaccess.RDataFrameDataAdapter;

import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.ui.dataeditor.IRDataTableInput;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class RDataFrameDataProvider extends AbstractRDataProvider<RDataFrame> {
	
	
	public RDataFrameDataProvider(final IRDataTableInput input, final RDataFrame struct) throws CoreException {
		super(input, new RDataFrameDataAdapter(), struct);
		
		reset();
	}
	
	
	@Override
	protected RDataTableContentDescription loadDescription(final RElementName name,
			final RDataFrame struct, final IRToolService r,
			final IProgressMonitor monitor) throws CoreException, UnexpectedRDataException {
		final RDataTableContentDescription description = new RDataTableContentDescription(name, struct, r.getTool());
		final int columnCount = (int) getColumnCount();
		
		description.setRowHeaderColumns(
				createNamesColumn("attr(" + fInput.getFullName() + ", 'row.names', exact= TRUE)", //$NON-NLS-1$ //$NON-NLS-2$
						struct.getRowCount(), r, monitor ));
		final RDataTableColumn[] dataColumns = new RDataTableColumn[columnCount];
		for (int i = 0; i < columnCount; i++) {
			final String columnName = struct.getColumnNames().getChar(i);
			final RElementName elementName= RElementName.concat(ImCollections.newList(BASE_NAME,
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
	protected void appendOrderCmd(final StringBuilder cmd, final SortColumn sortColumn) {
		cmd.append("order(");
		cmd.append(fInput.getFullName());
		cmd.append("[[");
		cmd.append((sortColumn.columnIdx + 1));
		cmd.append("]], decreasing=");
		cmd.append(sortColumn.decreasing ? "TRUE" : "FALSE");
		cmd.append(')');
	}
	
	
	@Override
	protected Object getDataValue(final LazyRStore.Fragment<RDataFrame> fragment,
			final long rowIdx, final long columnIdx) {
		return fragment.getRObject().getColumn(fragment.toLocalColumnIdx(columnIdx))
				.get(fragment.toLocalRowIdx(rowIdx) );
	}
	
	@Override
	public IDataProvider createColumnDataProvider() {
		return new ColumnDataProvider() {
			@Override
			public Object getDataValue(final long columnIndex, final long rowIndex) {
				return getRObject().getName(columnIndex);
			}
		};
	}
	
	@Override
	protected Object getColumnName(final LazyRStore.Fragment<RDataFrame> fragment, final long columnIdx) {
		return fragment.getRObject().getName(fragment.toLocalColumnIdx(columnIdx));
	}
	
}
