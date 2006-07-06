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

package de.walware.eclipsecommons.ui.dialogs.groups;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;

import de.walware.eclipsecommons.ui.dialogs.Layouter;
import de.walware.eclipsecommons.ui.util.PixelConverter;



public abstract class CategorizedOptionsGroup<ItemT extends CategorizedItem> 
		extends StructuredSelectionOptionsGroup<TreeViewer, ItemT> {

	
	private class ItemLabelProvider extends LabelProvider {

		public String getText(Object element) {
			if (element instanceof String) // Category
				return (String) element;
			return ((CategorizedItem) element).fName; // ItemT
		}
	}

	private class ItemContentProvider implements ITreeContentProvider {
	
		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getElements(Object inputElement) {
			
			return fCategorys;
		}

		public boolean hasChildren(Object element) {

			return element instanceof String;
		}

		public Object[] getChildren(Object parentElement) {
			
			if (parentElement instanceof String) {
				int idx = getIndexOfCategory(parentElement);
				return fCategoryChilds[idx];
			}
			
			return new Object[0];
		}

		public Object getParent(Object element) {
			
			if (element instanceof String)
				return null;
			
			int idx = ((CategorizedItem) element).fCategoryIndex;
			return fCategorys[idx];
		}
	}
	
	public String[] fCategorys;
	public ItemT[][] fCategoryChilds;
	
	public CategorizedOptionsGroup(boolean grabSelectionHorizontal, boolean grabVertical) {
		super(grabSelectionHorizontal, grabVertical);
	}
	
	protected Control createSelectionControl(Composite parent, GridData gd) {
		
		fSelectionViewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		PixelConverter pixel = new PixelConverter(parent);
		gd.heightHint = pixel.convertHeightInCharsToPixels(9);
		int maxWidth = 0;
		for (CategorizedItem item : fSelectionModel)
			maxWidth = Math.max(maxWidth, pixel.convertWidthInCharsToPixels(item.fName.length()));
		ScrollBar vBar = ((Scrollable) fSelectionViewer.getControl()).getVerticalBar();
		if (vBar != null)
			maxWidth += vBar.getSize().x * 4; // scrollbars and tree indentation guess
		gd.widthHint = maxWidth;
		
		fSelectionViewer.setLabelProvider(new ItemLabelProvider());
		fSelectionViewer.setContentProvider(new ItemContentProvider());

		fSelectionViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleListSelection();
			}
		});
		fSelectionViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection != null && selection.size() == 1) {
					Object item = selection.getFirstElement();
					if (item instanceof String) {
						if (fSelectionViewer.getExpandedState(item))
							fSelectionViewer.collapseToLevel(item, TreeViewer.ALL_LEVELS);
						else
							fSelectionViewer.expandToLevel(item, 1);
					}
					else
						handleDoubleClick(getSingleItem(selection));
				}
			}
		});
		
		return fSelectionViewer.getControl();
	}
	
	@Override
	public void initFields() {
		super.initFields();
		
		fComposite.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fSelectionViewer != null && Layouter.isOkToUse(fSelectionViewer.getControl()))
					fSelectionViewer.setSelection(new StructuredSelection(fCategorys[0]));
			}
		});
	}

	
	public void generateListModel() {

		for (int i = 0; i < fCategorys.length; i++) {
			for (int j = 0; j < fCategoryChilds[i].length; j++) {
				fCategoryChilds[i][j].fCategoryIndex = i;
				fSelectionModel.add(fCategoryChilds[i][j]);
			}
		}
	}
	
	public int getIndexOfCategory(Object category) {
		
		for (int i = 0; i < fCategorys.length; i++) {
			if (fCategorys[i] == category)
				return i;
		}
		return -1;
	}
	
}
