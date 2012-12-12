/*******************************************************************************
 * Copyright (c) 2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.datafilterview;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Font;

import de.walware.statet.r.internal.ui.intable.InfoString;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class ColumnLabelProvider extends CellLabelProvider {
	
	
	private final RDataTableColumn fColumn;
	
	private Font fInfoFont;
	
	
	public ColumnLabelProvider(final RDataTableColumn column) {
		fColumn = column;
	}
	
	
	@Override
	public void update(final ViewerCell cell) {
		final Object data = fColumn.getDefaultFormat().modelToDisplayValue(cell.getElement());
		cell.setFont((data instanceof InfoString) ? getInfoFont() : null);
		cell.setText(data.toString());
	}
	
	protected Font getInfoFont() {
		if (fInfoFont == null) {
			fInfoFont = JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
		}
		return fInfoFont;
	}
	
}
