/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.intable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.nebula.widgets.nattable.config.LayoutSizeConfig;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;

import de.walware.statet.r.internal.ui.dataeditor.AbstractRDataProvider;
import de.walware.statet.r.internal.ui.dataeditor.RDataFormatter;
import de.walware.statet.r.internal.ui.dataeditor.RDataTableContentDescription;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class RDataLayer extends AbstractLayer implements IUniqueIndexLayer {
	
	
	private final AbstractRDataProvider<?> fDataProvider;
	
	private final int fHSpacing;
	private final int fRowHeight;
	private final int fCharWidth;
	
	private final int fColumnFallbackWidth = 10;
	
	private final int fColumnDefaultMinWidth = 8;
	private final int fColumnDefaultMaxWidth = 20;
	
	private final int fColumnAutoMinWidth = 3;
	private final int fColumnAutoMaxWidth = 1000;
	
	private final Map<Integer, Integer> fCustomColumnWidths = new TreeMap<Integer, Integer>();
	
	
	public RDataLayer(final AbstractRDataProvider<?> dataProvider, final LayoutSizeConfig sizeConfig) {
		fDataProvider = dataProvider;
		
		fHSpacing = sizeConfig.getDefaultSpace() * 4;
		fRowHeight = sizeConfig.getRowHeight();
		fCharWidth = sizeConfig.getCharWidth();
		
		registerCommandHandlers();
	}
	
	
	// Configuration
	
	@Override
	protected void registerCommandHandlers() {
		registerCommandHandler(new ColumnResizeCommandHandler(this));
		registerCommandHandler(new MultiColumnResizeCommandHandler(this));
		registerCommandHandler(new MultiColumnAutoResizeCommandHandler(this));
//		registerCommandHandler(new UpdateDataCommandHandler(this));
	}
	
	
	// Columns
	
	@Override
	public int getColumnCount() {
		return fDataProvider.getColumnCount();
	}
	
	@Override
	public int getPreferredColumnCount() {
		return fDataProvider.getColumnCount();
	}
	
	/**
	 * This is the root coordinate system, so the column index is always equal to the column position.
	 */
	@Override
	public int getColumnIndexByPosition(final int columnPosition) {
		if (columnPosition >=0 && columnPosition < getColumnCount()) {
			return columnPosition;
		} else {
			return -1;
		}
	}
	
	/**
	 * This is the root coordinate system, so the column position is always equal to the column index.
	 */
	@Override
	public int getColumnPositionByIndex(final int columnIndex) {
		return (columnIndex >= 0 && columnIndex < getColumnCount()) ? columnIndex : -1;
	}
	
	@Override
	public Collection<ILayer> getUnderlyingLayersByColumnPosition(final int columnPosition) {
		return null;
	}
	
	@Override
	public int localToUnderlyingColumnPosition(final int localColumnPosition) {
		return localColumnPosition;
	}
	
	@Override
	public int underlyingToLocalColumnPosition(final ILayer sourceUnderlyingLayer, final int underlyingColumnPosition) {
		return underlyingColumnPosition;
	}
	
	@Override
	public Collection<Range> underlyingToLocalColumnPositions(final ILayer sourceUnderlyingLayer, final Collection<Range> underlyingColumnPositionRanges) {
		return underlyingColumnPositionRanges;
	}
	
	// Width
	@Override
	public int getWidth() {
		return aggregateWidth(getColumnCount());
	}
	
	private int aggregateWidth(final int position) {
		if (position < 0) {
			return -1;
		}
		
		if (fDataProvider.getAllColumnsEqual()) {
			final Integer columnWidth = fCustomColumnWidths.get(Integer.valueOf(0));
			if (columnWidth != null) {
				return columnWidth * position;
			}
			else {
				return getColumnDefaultWidth(0) * position;
			}
		}
		
		int width = 0;
		for (int i = 0; i < position; i++) {
			final Integer columnWidth = fCustomColumnWidths.get(Integer.valueOf(i));
			if (columnWidth != null) {
				width += columnWidth.intValue();
			}
			else {
				width += getColumnDefaultWidth(i);
			}
		}
		return width;
	}
	
	@Override
	public int getPreferredWidth() {
		return getWidth();
	}
	
	@Override
	public int getColumnWidthByPosition(final int columnPosition) {
		final Integer columnWidth = fCustomColumnWidths.get(fDataProvider.getAllColumnsEqual() ?
				Integer.valueOf(0) : Integer.valueOf(columnPosition));
		if (columnWidth != null) {
				return columnWidth.intValue();
		}
		return getColumnDefaultWidth(columnPosition);
	}
	
	protected int getColumnFormatterCharWidth(final int columnPosition) {
		RDataFormatter formatter;
		if (fDataProvider.getAllColumnsEqual()) {
			final RDataTableContentDescription description = fDataProvider.getDescription();
			if (description != null
					&& (formatter = description.getDefaultDataFormat()) != null
					&& formatter.getAutoWidth() >= 0) {
				return formatter.getAutoWidth();
			}
			return fColumnFallbackWidth;
		}
		else {
			final RDataTableContentDescription description = fDataProvider.getDescription();
			if (description != null) {
				final List<RDataTableColumn> columns = description.getDataColumns();
				if (columnPosition < columns.size()
						&& (formatter = columns.get(columnPosition).getDefaultFormat()) != null
						&& formatter.getAutoWidth() >= 0) {
					return formatter.getAutoWidth();
				}
				if ((formatter = description.getDefaultDataFormat()) != null
						&& formatter.getAutoWidth() >= 0) {
					return formatter.getAutoWidth();
				}
			}
			return fColumnFallbackWidth;
		}
	}
	
	protected int getColumnDefaultWidth(final int columnPosition) {
		final int charWidth = getColumnFormatterCharWidth(columnPosition);
		if (charWidth < fColumnDefaultMinWidth) {
			return fColumnDefaultMinWidth * fCharWidth + fHSpacing;
		}
		if (charWidth > fColumnDefaultMaxWidth) {
			return fColumnDefaultMaxWidth * fCharWidth + fHSpacing;
		}
		return charWidth * fCharWidth + fHSpacing;
	}
	
	protected int getColumnAutoWidth(final int columnPosition) {
		final int charWidth = getColumnFormatterCharWidth(columnPosition);
		if (charWidth < fColumnAutoMinWidth) {
			return fColumnAutoMinWidth * fCharWidth + fHSpacing;
		}
		if (charWidth > fColumnAutoMaxWidth) {
			return fColumnAutoMaxWidth * fCharWidth + fHSpacing;
		}
		return charWidth * fCharWidth + fHSpacing;
	}
	
	public void setColumnWidth(final int columnPosition, final int width) {
		fCustomColumnWidths.put(fDataProvider.getAllColumnsEqual() ?
				Integer.valueOf(0) : Integer.valueOf(columnPosition), width);
		fireLayerEvent(new ColumnResizeEvent(this, columnPosition));
	}
	
	public void setColumnWidthToAutoWidth(final int columnPosition) {
		fCustomColumnWidths.put(fDataProvider.getAllColumnsEqual() ?
				Integer.valueOf(0) : Integer.valueOf(columnPosition), getColumnAutoWidth(columnPosition));
		fireLayerEvent(new ColumnResizeEvent(this, columnPosition));
	}
	
	@Override
	public boolean isColumnPositionResizable(final int columnPosition) {
		return true;
	}
	
	
	// Rows
	
	@Override
	public int getRowCount() {
		return fDataProvider.getRowCount();
	}
	
	@Override
	public int getPreferredRowCount() {
		return getRowCount();
	}
	
	/**
	 * This is the root coordinate system, so the row index is always equal to the row position.
	 */
	@Override
	public int getRowIndexByPosition(final int rowPosition) {
		if (rowPosition >=0 && rowPosition < getRowCount()) {
			return rowPosition;
		} else {
			return -1;
		}
	}
	
	/**
	 * This is the root coordinate system, so the row position is always equal to the row index.
	 */
	@Override
	public int getRowPositionByIndex(final int rowIndex) {
		return (rowIndex >= 0 && rowIndex < getRowCount()) ? rowIndex : -1;
	}
	
	@Override
	public Collection<ILayer> getUnderlyingLayersByRowPosition(final int rowPosition) {
		return null;
	}
	
	@Override
	public int localToUnderlyingRowPosition(final int localRowPosition) {
		return localRowPosition;
	}
	
	@Override
	public int underlyingToLocalRowPosition(final ILayer sourceUnderlyingLayer, final int underlyingRowPosition) {
		return underlyingRowPosition;
	}
	
	@Override
	public Collection<Range> underlyingToLocalRowPositions(final ILayer sourceUnderlyingLayer, final Collection<Range> underlyingRowPositionRanges) {
		return underlyingRowPositionRanges;
	}
	
	// Height
	
	@Override
	public int getHeight() {
		return getRowCount() * fRowHeight;
	}
	
	@Override
	public int getPreferredHeight() {
		return getHeight();
	}
	
	@Override
	public int getRowHeightByPosition(final int rowPosition) {
		return fRowHeight;
	}
	
	@Override
	public boolean isRowPositionResizable(final int rowPosition) {
		return false;
	}
	
	
	// Cell features
	
	@Override
	public Object getDataValueByPosition(final int columnPosition, final int rowPosition) {
		final int columnIndex = getColumnIndexByPosition(columnPosition);
		final int rowIndex = getRowIndexByPosition(rowPosition);
		return fDataProvider.getDataValue(columnIndex, rowIndex);
	}
	
	@Override
	public int getColumnPositionByX(final int x) {
		return LayerUtil.getColumnPositionByX(this, x);
	}
	
	@Override
	public int getRowPositionByY(final int y) {
		return LayerUtil.getRowPositionByY(this, y);
	}
	
	@Override
	public int getStartXOfColumnPosition(final int columnPosition) {
		return aggregateWidth(columnPosition);
	}
	
	@Override
	public int getStartYOfRowPosition(final int rowPosition) {
		if (rowPosition < 0) {
			return -1;
		}
		return rowPosition * fRowHeight;
	}
	
	@Override
	public ILayer getUnderlyingLayerByPosition(final int columnPosition, final int rowPosition) {
		return null;
	}
	
}
