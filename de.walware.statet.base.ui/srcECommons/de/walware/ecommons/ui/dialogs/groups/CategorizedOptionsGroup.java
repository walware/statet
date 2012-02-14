/*******************************************************************************
 * Copyright (c) 2005-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.dialogs.groups;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;

import de.walware.ecommons.ui.util.PixelConverter;
import de.walware.ecommons.ui.util.UIAccess;


public abstract class CategorizedOptionsGroup<ItemT extends CategorizedOptionsGroup.CategorizedItem> 
		extends StructuredSelectionOptionsGroup<TreeViewer, ItemT> {

	
	public static class CategorizedItem {

		private int fCategoryIndex;
		private String fName;
		
		public CategorizedItem(String name) {
			
			fName = name;
		}
		
		public String getName() {
			
			return fName;
		}

		private void setCategory(int index) {
			
			fCategoryIndex = index;
		}
		
		public int getCategoryIndex() {
			
			return fCategoryIndex;
		}
	}

	private class CategorizedItemLabelProvider extends LabelProvider {

		@Override
		public String getText(Object element) {
			if (element instanceof String) // Category
				return (String) element;
			return ((CategorizedItem) element).getName(); // ItemT
		}
	}

	private class CategorizedItemContentProvider implements ITreeContentProvider {
	
		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			
			return fCategorys;
		}

		@Override
		public boolean hasChildren(Object element) {

			return element instanceof String;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			
			if (parentElement instanceof String) {
				int idx = getIndexOfCategory(parentElement);
				return fCategoryChilds[idx];
			}
			
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			
			if (element instanceof String)
				return null;
			
			int idx = ((CategorizedItem) element).getCategoryIndex();
			return fCategorys[idx];
		}
	}
	
	public String[] fCategorys;
	public ItemT[][] fCategoryChilds;
	
	public CategorizedOptionsGroup(boolean grabSelectionHorizontal, boolean grabVertical) {
		super(grabSelectionHorizontal, grabVertical);
	}
	
	@Override
	protected TreeViewer createSelectionViewer(Composite parent) {
		
		TreeViewer viewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setLabelProvider(new CategorizedItemLabelProvider());

		return viewer;
	}
	
	@Override
	protected GridData createSelectionGridData() {
		
		GridData gd = super.createSelectionGridData();
		
		Control control = getStructuredViewer().getControl();
		PixelConverter pixel = new PixelConverter(control);
		gd.heightHint = pixel.convertHeightInCharsToPixels(9);
		int maxWidth = 0;
		for (CategorizedItem item : getListModel())
			maxWidth = Math.max(maxWidth, pixel.convertWidthInCharsToPixels(item.getName().length()));
		ScrollBar vBar = ((Scrollable) control).getVerticalBar();
		if (vBar != null)
			maxWidth += vBar.getSize().x * 4; // scrollbars and tree indentation guess
		gd.widthHint = maxWidth;
		
		return gd;
	}
	
	@Override
	protected IContentProvider createContentProvider() {
		
		return new CategorizedItemContentProvider();
	}
	
	@Override
	protected IDoubleClickListener createDoubleClickListener() {
		
		return new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection != null && selection.size() == 1) {
					Object item = selection.getFirstElement();
					if (item instanceof String) {
						if (getStructuredViewer().getExpandedState(item))
							getStructuredViewer().collapseToLevel(item, TreeViewer.ALL_LEVELS);
						else
							getStructuredViewer().expandToLevel(item, 1);
					}
					else {
						handleDoubleClick(getSingleItem(selection), selection);
					}
				}
			}
		};
	}
	
	@Override
	public void initFields() {
		super.initFields();
		
		getStructuredViewer().getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				TreeViewer viewer = getStructuredViewer();
				if (viewer != null && UIAccess.isOkToUse(viewer)) {
					viewer.setSelection(new StructuredSelection(fCategorys[0]));
				}
			}
		});
	}
	
	@Override
	public ItemT getSingleItem(IStructuredSelection selection) {
		
		if (selection.getFirstElement() instanceof String) {
			return null;
		}
		return super.getSingleItem(selection);
	}

	
	public void generateListModel() {

		for (int i = 0; i < fCategorys.length; i++) {
			for (int j = 0; j < fCategoryChilds[i].length; j++) {
				fCategoryChilds[i][j].setCategory(i);
				getListModel().add(fCategoryChilds[i][j]);
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
