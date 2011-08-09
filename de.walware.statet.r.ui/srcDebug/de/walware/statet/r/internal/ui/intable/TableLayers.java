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

import net.sourceforge.nattable.command.LayerCommandUtil;
import net.sourceforge.nattable.coordinate.ColumnPositionCoordinate;
import net.sourceforge.nattable.layer.AbstractLayer;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.event.CellSelectionEvent;
import net.sourceforge.nattable.sort.SortHeaderLayer;
import net.sourceforge.nattable.viewport.ViewportLayer;


public class TableLayers {
	
	
	public RDataLayer dataLayer;
	
	public SelectionLayer selectionLayer;
	
	public ViewportLayer viewportLayer;
	
	public AbstractLayer topBodyLayer;
	
	
	public SortHeaderLayer<?> sortColumnHeaderLayer;
	
	
	public void setAnchor(int columnPosition, final int rowPosition,
			final boolean moveIntoViewport) {
		if (columnPosition < 0) {
			final ColumnPositionCoordinate colCoordinate = LayerCommandUtil.convertColumnPositionToTargetContext(
					new ColumnPositionCoordinate(viewportLayer, 0), selectionLayer);
			if (colCoordinate != null) {
				columnPosition = colCoordinate.getColumnPosition();
			}
		}
		if (columnPosition < 0 || columnPosition > selectionLayer.getColumnCount()
				|| rowPosition < 0 || rowPosition > selectionLayer.getRowCount() ) {
			return;
		}
		selectionLayer.getSelectionAnchor().set(rowPosition, columnPosition);
		
		selectionLayer.fireLayerEvent(new CellSelectionEvent(selectionLayer,
				columnPosition, rowPosition, moveIntoViewport ));
	}
	
}
