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

import java.util.List;

import de.walware.ecommons.waltable.config.IConfigRegistry;
import de.walware.ecommons.waltable.coordinate.Orientation;
import de.walware.ecommons.waltable.coordinate.PositionId;
import de.walware.ecommons.waltable.data.convert.IDisplayConverter;
import de.walware.ecommons.waltable.layer.cell.ILayerCell;

import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class RDataFormatterConverter implements IDisplayConverter {
	
	
	public static class RowHeader extends RDataFormatterConverter {
		
		public RowHeader(final AbstractRDataProvider<?> dataProvider) {
			super(dataProvider);
		}
		
		@Override
		protected RDataFormatter getFormatter(final RDataTableContentDescription description,
				final ILayerCell cell) {
			if ((cell.getDim(Orientation.VERTICAL).getId() & PositionId.CAT_MASK) == PositionId.BODY_CAT) {
				final List<RDataTableColumn> columns= description.getRowHeaderColumns();
				final long index= getColumnIndex(cell);
				if (columns != null && index >= 0 && index < columns.size()) {
					return columns.get((int) index).getDefaultFormat();
				}
			}
			return null;
		}
		
	}
	
	
	private final AbstractRDataProvider<?> fDataProvider;
	
	
	RDataFormatter fFallbackFormatter;
	
	
	public RDataFormatterConverter(final AbstractRDataProvider<?> dataProvider) {
		this.fDataProvider= dataProvider;
	}
	
	
	private RDataFormatter getFormatter(final ILayerCell cell) {
		final RDataTableContentDescription description= this.fDataProvider.getDescription();
		if (description != null) {
			final RDataFormatter formatter= getFormatter(description, cell);
			if (formatter != null) {
				return formatter;
			}
		}
		if (this.fFallbackFormatter == null) {
			this.fFallbackFormatter= new RDataFormatter();
		}
		return this.fFallbackFormatter;
	}
	
	protected RDataFormatter getFormatter(final RDataTableContentDescription description,
			final ILayerCell cell) {
		if ((cell.getDim(Orientation.HORIZONTAL).getId() & PositionId.CAT_MASK) == PositionId.BODY_CAT
				&& (cell.getDim(Orientation.VERTICAL).getId() & PositionId.CAT_MASK) == PositionId.BODY_CAT) {
			final List<RDataTableColumn> columns= description.getDataColumns();
			final long index= getColumnIndex(cell);
			if (index >= 0 && index < columns.size()) {
				return columns.get((int) index).getDefaultFormat();
			}
			return description.getDefaultDataFormat();
		}
		return null;
	}
	
	protected long getColumnIndex(final ILayerCell cell) {
		return (cell.getDim(Orientation.HORIZONTAL).getId() & PositionId.NUM_MASK);
	}
	
	
	@Override
	public Object canonicalToDisplayValue(final ILayerCell cell, final IConfigRegistry configRegistry,
			final Object canonicalValue) {
		return getFormatter(cell).modelToDisplayValue(canonicalValue);
	}
	
	@Override
	public Object displayToCanonicalValue(final ILayerCell cell, final IConfigRegistry configRegistry,
			final Object displayValue) {
		throw new UnsupportedOperationException();
	}
	
}
