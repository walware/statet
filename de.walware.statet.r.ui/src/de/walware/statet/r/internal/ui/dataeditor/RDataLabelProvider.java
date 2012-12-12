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

package de.walware.statet.r.internal.ui.dataeditor;

import java.util.List;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.ltk.IModelElement;

import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RObject;

import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.internal.ui.dataeditor.RDataEditorOutlinePage.ColumnPropertyItem;
import de.walware.statet.r.ui.RLabelProvider;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.dataeditor.RDataTableColumn;


public class RDataLabelProvider extends StyledCellLabelProvider {
	
	
	private final RLabelProvider fRLabelProvider = new RLabelProvider(
			RLabelProvider.NO_STORE_TYPE | RLabelProvider.COUNT | RLabelProvider.NAMESPACE);
	
	
	public RDataLabelProvider() {
	}
	
	
	public Image getImage(final RDataTableColumn element) {
		switch (element.getColumnType()) {
		case RDataTableColumn.LOGI:
			return RUI.getImage(RUI.IMG_OBJ_COL_LOGI);
		case RDataTableColumn.INT:
			return RUI.getImage(RUI.IMG_OBJ_COL_INT);
		case RDataTableColumn.NUM:
			return RUI.getImage(RUI.IMG_OBJ_COL_NUM);
		case RDataTableColumn.CPLX:
			return RUI.getImage(RUI.IMG_OBJ_COL_CPLX);
		case RDataTableColumn.CHAR:
			return RUI.getImage(RUI.IMG_OBJ_COL_CHAR);
		case RDataTableColumn.RAW:
			return RUI.getImage(RUI.IMG_OBJ_COL_RAW);
		case RDataTableColumn.FACTOR:
			return RUI.getImage(RUI.IMG_OBJ_COL_FACTOR);
		case RDataTableColumn.DATE:
			return RUI.getImage(RUI.IMG_OBJ_COL_DATE);
		case RDataTableColumn.DATETIME:
			return RUI.getImage(RUI.IMG_OBJ_COL_DATETIME);
		default:
			return null;
		}
	}
	
	@Override
	public void update(final ViewerCell cell) {
		Image image;
		final StyledString text = new StyledString();
		final Object element = cell.getElement();
		if (element instanceof RDataTableContentDescription) {
			final RDataTableContentDescription description = (RDataTableContentDescription) element;
			if (description.getRElementStruct() instanceof ICombinedRElement) {
				fRLabelProvider.update(cell, (IModelElement) description.getRElementStruct());
				super.update(cell);
				return;
			}
			switch (description.getRElementStruct().getRObjectType()) {
			case RObject.TYPE_VECTOR:
				image = RUI.getImage(RUI.IMG_OBJ_VECTOR);
				break;
			case RObject.TYPE_ARRAY:
				image = RUI.getImage(RUI.IMG_OBJ_VECTOR);
				break;
			case RObject.TYPE_DATAFRAME:
				image = RUI.getImage(RUI.IMG_OBJ_VECTOR);
				break;
			default:
				image = null;
				break;
			}
			text.append(description.getElementName().toString());
		}
		else if (element instanceof RDataTableColumn) {
			final RDataTableColumn column = (RDataTableColumn) element;
			image = getImage(column);
			text.append(column.getName());
			
			text.append(" : ", StyledString.DECORATIONS_STYLER);
			final List<String> classNames = column.getClassNames();
			text.append(classNames.get(0), StyledString.DECORATIONS_STYLER);
			for (int i = 1; i < classNames.size(); i++) {
				text.append(", ", StyledString.DECORATIONS_STYLER);
				text.append(classNames.get(i), StyledString.DECORATIONS_STYLER);
			}
			if (!classNames.contains(RDataUtil.getStoreClass(column.getDataStore()))) {
				text.append(" (", StyledString.DECORATIONS_STYLER);
				text.append(RDataUtil.getStoreAbbr(column.getDataStore()), StyledString.DECORATIONS_STYLER);
				text.append(")", StyledString.DECORATIONS_STYLER);
			}
		}
		else if (element instanceof ColumnPropertyItem) {
			final ColumnPropertyItem item = (ColumnPropertyItem) element;
			image = null;
			text.append(item.getName());
			final int count = item.getCount();
			if (count >= 0) {
				text.append(" (", StyledString.COUNTER_STYLER);
				text.append(Integer.toString(count), StyledString.COUNTER_STYLER);
				text.append(")", StyledString.COUNTER_STYLER);
			}
		}
		else {
			image = null;
			text.append(element.toString());
		}
		
		cell.setText(text.getString());
		cell.setStyleRanges(text.getStyleRanges());
		cell.setImage(image);
		
		super.update(cell);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		fRLabelProvider.dispose();
	}
	
}
