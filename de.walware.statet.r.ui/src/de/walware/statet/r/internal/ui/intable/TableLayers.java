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

import de.walware.ecommons.waltable.NatTable;
import de.walware.ecommons.waltable.command.LayerCommandUtil;
import de.walware.ecommons.waltable.coordinate.ColumnPositionCoordinate;
import de.walware.ecommons.waltable.layer.DataLayer;
import de.walware.ecommons.waltable.layer.ILayer;
import de.walware.ecommons.waltable.selection.SelectionLayer;
import de.walware.ecommons.waltable.viewport.ViewportLayer;


public class TableLayers {
	
	
	public RDataLayer dataLayer;
	
	public SelectionLayer selectionLayer;
	
	public ViewportLayer viewportLayer;
	
	public ILayer topBodyLayer;
	
	public DataLayer dataColumnHeaderLayer;
	public ILayer topColumnHeaderLayer;
	public DataLayer dataRowHeaderLayer;
	public ILayer topRowHeaderLayer;
	
	public NatTable table;
	
	
	public void setAnchor(long columnPosition, final long rowPosition,
			final boolean moveIntoViewport) {
		if (columnPosition < 0) {
			final ColumnPositionCoordinate colCoordinate= LayerCommandUtil.convertColumnPositionToTargetContext(
					new ColumnPositionCoordinate(this.viewportLayer, 0), this.selectionLayer);
			if (colCoordinate != null) {
				columnPosition= colCoordinate.getColumnPosition();
			}
		}
		if (columnPosition < 0 || columnPosition > this.selectionLayer.getColumnCount()
				|| rowPosition < 0 || rowPosition > this.selectionLayer.getRowCount() ) {
			return;
		}
		this.selectionLayer.setSelectionAnchor(columnPosition, rowPosition, moveIntoViewport);
	}
	
}
