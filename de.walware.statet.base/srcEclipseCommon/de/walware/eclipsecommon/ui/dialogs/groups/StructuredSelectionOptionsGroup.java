/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommon.ui.dialogs.groups;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;


public abstract class StructuredSelectionOptionsGroup<SelectionT extends StructuredViewer, ItemT extends SelectionItem> 
		extends SelectionOptionsGroup<ItemT> {

	
	public SelectionT fSelectionViewer;
	public StructuredSelectionOptionsGroup(boolean grabSelectionHorizontal, boolean grabVertical) {
		super(grabSelectionHorizontal, grabVertical);
	}
	
	@Override
	public void initFields() {

		super.initFields();
		fSelectionViewer.setInput(fSelectionModel);
		fSelectionViewer.refresh();
		handleListSelection();
	}

	/**
	 * Double-click on table item.
	 * <p>
	 * Default Implementierung macht nichts.
	 * @param item
	 */
	protected void handleDoubleClick(ItemT item) {
		
	}

	
	public IStructuredSelection getSelectedItems() {
		
		return (IStructuredSelection) fSelectionViewer.getSelection();
	}
	
	@SuppressWarnings("unchecked")
	public ItemT getSingleItem(IStructuredSelection selection) {
		
		if (selection.size() == 1) {
			Object obj = selection.getFirstElement();
			if (obj instanceof SelectionItem)
				return (ItemT) obj; 
		}
		return null;
	}
	
	public ItemT getSingleSelectedItem() {
		
		return getSingleItem(getSelectedItems());
	}
	
}
