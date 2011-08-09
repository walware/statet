/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import java.util.Map;
import java.util.TreeMap;

import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.AbstractLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.layer.LayerUtil;
import net.sourceforge.nattable.resize.event.ColumnResizeEvent;

import de.walware.statet.r.internal.ui.dataeditor.AbstractRDataProvider;
import de.walware.statet.r.internal.ui.dataeditor.RDataTableContentDescription;


public class RDataLayer extends AbstractLayer implements IUniqueIndexLayer {
	
	
	private final AbstractRDataProvider<?> fDataProvider;
	
	private final int fHSpacing = 15;
	private final int fVSpacing = 3;
	private int fRowHeight = 10;
	private int fCharWidth = 8;
	
	private final int fColumnFallbackWidth = 10;
	
	private final int fColumnDefaultMinWidth = 8;
	private final int fColumnDefaultMaxWidth = 20;
	
	private final int fColumnAutoMinWidth = 3;
	private final int fColumnAutoMaxWidth = 1000;
	
	private final Map<Integer, Integer> fCustomColumnWidths = new TreeMap<Integer, Integer>();
	
	
	public RDataLayer(final AbstractRDataProvider<?> dataProvider) {
		fDataProvider = dataProvider;
		
		registerCommandHandlers();
	}
	
	
	// Configuration
	
	private void registerCommandHandlers() {
		registerCommandHandler(new ColumnResizeCommandHandler(this));
		registerCommandHandler(new MultiColumnResizeCommandHandler(this));
		registerCommandHandler(new MultiColumnAutoResizeCommandHandler(this));
//		registerCommandHandler(new UpdateDataCommandHandler(this));
	}
	
	
	// Columns
	
	public int getColumnCount() {
		return fDataProvider.getColumnCount();
	}
	
	public int getPreferredColumnCount() {
		return fDataProvider.getColumnCount();
	}
	
	/**
	 * This is the root coordinate system, so the column index is always equal to the column position.
	 */
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
	public int getColumnPositionByIndex(final int columnIndex) {
		if (columnIndex >=0 && columnIndex < getColumnCount()) {
			return columnIndex;
		} else {
			return -1;
		}
	}
	
	public Collection<ILayer> getUnderlyingLayersByColumnPosition(final int columnPosition) {
		return null;
	}
	
	public int localToUnderlyingColumnPosition(final int localColumnPosition) {
		return localColumnPosition;
	}
	
	public int underlyingToLocalColumnPosition(final ILayer sourceUnderlyingLayer, final int underlyingColumnPosition) {
		return underlyingColumnPosition;
	}
	
	public Collection<Range> underlyingToLocalColumnPositions(final ILayer sourceUnderlyingLayer, final Collection<Range> underlyingColumnPositionRanges) {
		return underlyingColumnPositionRanges;
	}
	
	// Width
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
	
	public int getPreferredWidth() {
		return getWidth();
	}
	
	public int getColumnWidthByPosition(final int columnPosition) {
		final Integer columnWidth = fCustomColumnWidths.get(fDataProvider.getAllColumnsEqual() ?
				Integer.valueOf(0) : Integer.valueOf(columnPosition));
		if (columnWidth != null) {
				return columnWidth.intValue();
		}
		return getColumnDefaultWidth(columnPosition);
	}
	
	protected int getColumnFormatterCharWidth(final int columnPosition) {
		if (fDataProvider.getAllColumnsEqual()) {
			final RDataTableContentDescription formatter = fDataProvider.getDescription();
			if (formatter != null
					&& formatter.defaultDataFormatter != null
					&& formatter.defaultDataFormatter.getAutoWidth() >= 0) {
				return formatter.defaultDataFormatter.getAutoWidth();
			}
			return fColumnFallbackWidth;
		}
		else {
			final RDataTableContentDescription description = fDataProvider.getDescription();
			if (description != null) {
				if (columnPosition < description.dataColumns.length
						&& description.dataColumns[columnPosition] != null
						&& description.dataColumns[columnPosition].getDefaultFormat().getAutoWidth() >= 0) {
					return description.dataColumns[columnPosition].getDefaultFormat().getAutoWidth();
				}
				if (description.defaultDataFormatter != null
						&& description.defaultDataFormatter.getAutoWidth() >= 0) {
					return description.defaultDataFormatter.getAutoWidth();
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
	
	public boolean isColumnPositionResizable(final int columnPosition) {
		return true;
	}
	
	
	// Rows
	
	public int getRowCount() {
		return fDataProvider.getRowCount();
	}
	
	public int getPreferredRowCount() {
		return getRowCount();
	}
	
	/**
	 * This is the root coordinate system, so the row index is always equal to the row position.
	 */
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
	public int getRowPositionByIndex(final int rowIndex) {
		if (rowIndex >= 0 && rowIndex < getRowCount()) {
			return rowIndex;
		} else {
			return -1;
		}
	}
	
	public Collection<ILayer> getUnderlyingLayersByRowPosition(final int rowPosition) {
		return null;
	}
	
	public int localToUnderlyingRowPosition(final int localRowPosition) {
		return localRowPosition;
	}
	
	public int underlyingToLocalRowPosition(final ILayer sourceUnderlyingLayer, final int underlyingRowPosition) {
		return underlyingRowPosition;
	}
	
	public Collection<Range> underlyingToLocalRowPositions(final ILayer sourceUnderlyingLayer, final Collection<Range> underlyingRowPositionRanges) {
		return underlyingRowPositionRanges;
	}
	
	// Height
	
	public int getHeight() {
		return getRowCount() * fRowHeight;
	}
	
	public int getPreferredHeight() {
		return getHeight();
	}
	
	public void setSizeConfig(final int height, final int charWidth) {
		fRowHeight = height + fVSpacing;
		fCharWidth = charWidth;
	}
	
	public int getRowHeightByPosition(final int rowPosition) {
		return fRowHeight;
	}
	
	public boolean isRowPositionResizable(final int rowPosition) {
		return false;
	}
	
	
	// Cell features
	
	public Object getDataValueByPosition(final int columnPosition, final int rowPosition) {
		final int columnIndex = getColumnIndexByPosition(columnPosition);
		final int rowIndex = getRowIndexByPosition(rowPosition);
		return fDataProvider.getDataValue(columnIndex, rowIndex);
	}
	
	public int getColumnPositionByX(final int x) {
		return LayerUtil.getColumnPositionByX(this, x);
	}
	
	public int getRowPositionByY(final int y) {
		return LayerUtil.getRowPositionByY(this, y);
	}
	
	public int getStartXOfColumnPosition(final int columnPosition) {
		return aggregateWidth(columnPosition);
	}
	
	public int getStartYOfRowPosition(final int rowPosition) {
		if (rowPosition < 0) {
			return -1;
		}
		return rowPosition * fRowHeight;
	}
	
	public ILayer getUnderlyingLayerByPosition(final int columnPosition, final int rowPosition) {
		return null;
	}
	
}
