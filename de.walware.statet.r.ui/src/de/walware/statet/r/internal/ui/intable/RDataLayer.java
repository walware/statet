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

package de.walware.statet.r.internal.ui.intable;

import static de.walware.ecommons.waltable.coordinate.Orientation.HORIZONTAL;
import static de.walware.ecommons.waltable.coordinate.Orientation.VERTICAL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.walware.ecommons.waltable.config.LayoutSizeConfig;
import de.walware.ecommons.waltable.coordinate.Orientation;
import de.walware.ecommons.waltable.coordinate.PositionId;
import de.walware.ecommons.waltable.coordinate.PositionOutOfBoundsException;
import de.walware.ecommons.waltable.layer.AbstractLayer;
import de.walware.ecommons.waltable.layer.DataDim;
import de.walware.ecommons.waltable.layer.ILayer;
import de.walware.ecommons.waltable.layer.ILayerDim;
import de.walware.ecommons.waltable.layer.cell.ILayerCell;
import de.walware.ecommons.waltable.layer.cell.LayerCell;
import de.walware.ecommons.waltable.layer.cell.LayerCellDim;
import de.walware.ecommons.waltable.resize.ColumnResizeEvent;

import de.walware.statet.r.internal.ui.dataeditor.AbstractRDataProvider;
import de.walware.statet.r.internal.ui.dataeditor.RDataFormatter;
import de.walware.statet.r.internal.ui.dataeditor.RDataTableContentDescription;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class RDataLayer extends AbstractLayer {
	
	
	private static int minmax(final int value, final int min, final int max) {
		return (value <= min) ? min : ((value >= max) ? max : value);
	}
	
	
	private static class ColumnsDim extends DataDim<RDataLayer> {
		
		
		private final int defaultMinChars= 8;
		private final int defaultMaxChars= 20;
		
		private final int autoMinChars= 3;
		private final int autoMaxChars= 1000;
		
		private final int fallbackPositionSize= 10;
		
		private final Map<Long, Integer> customPositionSizes= new HashMap<>();
		
		private int charSize;
		private int spacing;
		
		
		public ColumnsDim(final RDataLayer layer, final int charSize, final int spacing) {
			super(layer, Orientation.HORIZONTAL, PositionId.BODY_CAT);
			
			this.charSize= charSize;
			this.spacing= spacing;
		}
		
		
		@Override
		public long getPositionCount() {
			return this.layer.dataProvider.getColumnCount();
		}
		
		
		private long aggregateSize(final long position) {
			if (position < 0) {
				return -1;
			}
			
			if (this.layer.dataProvider.getAllColumnsEqual()) {
				final Integer columnWidth= this.customPositionSizes.get(Long.valueOf(0));
				if (columnWidth != null) {
					return columnWidth * position;
				}
				else {
					return getDefaultSize(0) * position;
				}
			}
			
			long width= 0;
			for (long i= 0; i < position; i++) {
				final Integer columnWidth= this.customPositionSizes.get(Long.valueOf(i));
				if (columnWidth != null) {
					width += columnWidth.intValue();
				}
				else {
					width += getDefaultSize(i);
				}
			}
			return width;
		}
		
		@Override
		public long getSize() {
			return aggregateSize(getPositionCount());
		}
		
		@Override
		public long getPositionStart(final long position) {
			if (position < 0 || position >= getPositionCount()) {
				throw PositionOutOfBoundsException.position(position, getOrientation());
			}
			return aggregateSize(position);
		}
		
		@Override
		public int getPositionSize(final long position) {
			if (position < 0 || position >= getPositionCount()) {
				throw PositionOutOfBoundsException.position(position, getOrientation());
			}
			final Integer columnWidth= this.customPositionSizes.get(
					this.layer.dataProvider.getAllColumnsEqual() ?
							Long.valueOf(0) : Long.valueOf(position));
			if (columnWidth != null) {
					return columnWidth.intValue();
			}
			return getDefaultSize(position);
		}
		
		@Override
		public boolean isPositionResizable(final long position) {
			return true;
		}
		
		
		protected int getFormatterCharWidth(final long position) {
			RDataFormatter formatter;
			if (this.layer.dataProvider.getAllColumnsEqual()) {
				final RDataTableContentDescription description= this.layer.dataProvider.getDescription();
				if (description != null
						&& (formatter= description.getDefaultDataFormat()) != null
						&& formatter.getAutoWidth() >= 0) {
					return formatter.getAutoWidth();
				}
				return this.fallbackPositionSize;
			}
			else {
				final RDataTableContentDescription description= this.layer.dataProvider.getDescription();
				if (description != null) {
					final List<RDataTableColumn> columns= description.getDataColumns();
					if (position < columns.size()
							&& (formatter= columns.get((int) position).getDefaultFormat()) != null
							&& formatter.getAutoWidth() >= 0) {
						return formatter.getAutoWidth();
					}
					if ((formatter= description.getDefaultDataFormat()) != null
							&& formatter.getAutoWidth() >= 0) {
						return formatter.getAutoWidth();
					}
				}
				return this.fallbackPositionSize;
			}
		}
		
		protected int getDefaultSize(final long position) {
			final int charWidth= minmax(getFormatterCharWidth(position),
					this.defaultMinChars, this.defaultMaxChars );
			return charWidth * this.charSize + this.spacing;
		}
		
		protected int getAutoSize(final long position) {
			final int charWidth= minmax(getFormatterCharWidth(position),
					this.autoMinChars, this.autoMaxChars );
			return charWidth * this.charSize + this.spacing;
		}
		
		public void setCustomSize(final long position, final int size) {
			this.customPositionSizes.put(
					this.layer.dataProvider.getAllColumnsEqual() ?
							Long.valueOf(0) : Long.valueOf(position),
					size );
		}
		
		public void setAutoSize(final long position) {
			this.customPositionSizes.put(
					this.layer.dataProvider.getAllColumnsEqual() ?
							Long.valueOf(0) : Long.valueOf(position),
					getAutoSize(position) );
		}
		
	}
	
	private static class RowsDim extends DataDim<RDataLayer> {
		
		
		private final int positionSize;
		
		
		public RowsDim(final RDataLayer layer, final int positionSize) {
			super(layer, Orientation.VERTICAL, PositionId.BODY_CAT);
			
			this.positionSize= positionSize;
		}
		
		
		@Override
		public long getPositionCount() {
			return this.layer.dataProvider.getRowCount();
		}
		
		@Override
		public long getSize() {
			return getPositionCount() * this.positionSize;
		}
		
		@Override
		public long getPositionStart(final long position) {
			if (position < 0 || position >= getPositionCount()) {
				throw new IndexOutOfBoundsException("position: " + position); //$NON-NLS-1$
			}
			return position * this.positionSize;
		}
		
		@Override
		public int getPositionSize(final long position) {
			if (position < 0 || position >= getPositionCount()) {
				throw new IndexOutOfBoundsException("position: " + position); //$NON-NLS-1$
			}
			return this.positionSize;
		}
		
		@Override
		public boolean isPositionResizable(final long position) {
			return false;
		}
		
	}
	
	
	private final AbstractRDataProvider<?> dataProvider;
	
	private LayoutSizeConfig sizeConfig;
	
	
	public RDataLayer(final AbstractRDataProvider<?> dataProvider, final LayoutSizeConfig sizeConfig) {
		this.dataProvider= dataProvider;
		this.sizeConfig= sizeConfig;
		initDims();
		
		registerCommandHandlers();
	}
	
	
	@Override
	protected void initDims() {
		if (this.sizeConfig == null) {
			return;
		}
		
		setDim(new ColumnsDim(this,
				this.sizeConfig.getCharWidth(), this.sizeConfig.getDefaultSpace() * 4 ));
		setDim(new RowsDim(this,
				this.sizeConfig.getRowHeight() ));
	}
	
	private ColumnsDim getColumnDim() {
		return (ColumnsDim) getDim(HORIZONTAL);
	}
	
	private RowsDim getRowDim() {
		return (RowsDim) getDim(VERTICAL);
	}
	
	
	// Configuration
	
	@Override
	protected void registerCommandHandlers() {
		registerCommandHandler(new DimPositionResizeCommandHandler(this));
		registerCommandHandler(new MultiColumnResizeCommandHandler(this));
		registerCommandHandler(new MultiColumnAutoResizeCommandHandler(this));
//		registerCommandHandler(new UpdateDataCommandHandler(this));
	}
	
	
	// Columns
	
	public void setColumnWidth(final long columnPosition, final int width) {
		getColumnDim().setCustomSize(columnPosition, width);
		fireLayerEvent(new ColumnResizeEvent(this, columnPosition));
	}
	
	public void setColumnWidthToAutoWidth(final long columnPosition) {
		getColumnDim().setAutoSize(columnPosition);
		fireLayerEvent(new ColumnResizeEvent(this, columnPosition));
	}
	
	
	// Cell features
	
	@Override
	public ILayerCell getCellByPosition(final long columnPosition, final long rowPosition) {
		final ILayerDim hDim= getDim(HORIZONTAL);
		final ILayerDim vDim= getDim(VERTICAL);
		final long columnId= hDim.getPositionId(columnPosition, columnPosition);
		final long rowId= vDim.getPositionId(rowPosition, rowPosition);
		
		return new LayerCell(this,
				new LayerCellDim(HORIZONTAL, columnId, columnPosition),
				new LayerCellDim(VERTICAL, rowId, rowPosition) ) {
			
			@Override
			public Object getDataValue(final int flags) {
				return RDataLayer.this.dataProvider.getDataValue(getColumnPosition(), getRowPosition(),
						flags );
			}
			
		};
	}
	
	@Override
	public ILayer getUnderlyingLayerByPosition(final long columnPosition, final long rowPosition) {
		return null;
	}
	
}
