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

import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;


public class TableLayers {
	
	
	public RDataLayer dataLayer;
	
	public SelectionLayer selectionLayer;
	
	public ViewportLayer viewportLayer;
	
	public ILayer topBodyLayer;
	
	public ILayer topColumnHeaderLayer;
	public ILayer topRowHeaderLayer;
	
	
	public void setAnchor(long columnPosition, final long rowPosition,
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
		selectionLayer.setSelectionAnchor(columnPosition, rowPosition, moveIntoViewport);
	}
	
}
