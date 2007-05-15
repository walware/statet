/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public abstract class StructuredSelectionOptionsGroup<SelectionT extends StructuredViewer, ItemT extends Object> 
		extends SelectionOptionsGroup<ItemT> {

	
	private class ItemContentProvider implements IStructuredContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
		public Object[] getElements(Object inputElement) {
			return getListModel().toArray();
		}
	}

	
	private SelectionT fSelectionViewer;
	
	
	public StructuredSelectionOptionsGroup(boolean grabSelectionHorizontal, boolean grabVertical) {
		super(grabSelectionHorizontal, grabVertical);
	}
	
	@Override
	protected Control createSelectionControl(Composite parent) {
		fSelectionViewer = createSelectionViewer(parent);
		fSelectionViewer.setContentProvider(createContentProvider());
		fSelectionViewer.addSelectionChangedListener(createSelectionChangeListener());
		fSelectionViewer.addDoubleClickListener(createDoubleClickListener());
		fSelectionViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});
		return fSelectionViewer.getControl();
	}

	protected abstract SelectionT createSelectionViewer(Composite parent);
	
	protected IContentProvider createContentProvider() {
		return new ItemContentProvider();
	}
	
	protected ISelectionChangedListener createSelectionChangeListener() {
		return new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				handleSelection(getSingleItem(selection), selection);
			}
		};
	}
	
	protected IDoubleClickListener createDoubleClickListener() {
		return new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				handleDoubleClick(getSingleItem(selection), selection);
			}
		};
	}
	
	public SelectionT getStructuredViewer() {
		return fSelectionViewer;
	}

	@Override
	public void initFields() {
		super.initFields();
		fSelectionViewer.setInput(getListModel());
		fSelectionViewer.refresh();
	}

	public void refresh() {
		getStructuredViewer().refresh();
		reselect();
	}

	public void reselect() {
		IStructuredSelection selection = getSelectedItems();
		handleSelection(getSingleItem(selection), selection);
	}
	
	/**
	 * Selection-change in List
	 * <p>
	 * Default-Implementierung macht nichts.
	 */
	protected void handleSelection(ItemT item, IStructuredSelection rawSelection) {
	}

	/**
	 * Double-click on table item.
	 * <p>
	 * Default Implementierung macht nichts.
	 * @param item
	 */
	protected void handleDoubleClick(ItemT item, IStructuredSelection rawSelection) {
	}

	/**
	 * Handles key events in the table viewer.
	 * <p>
	 * Standard-Implementierung macht nichts.
	 */
	protected void handleKeyPressed(KeyEvent event) {
	}

	
	public IStructuredSelection getSelectedItems() {
		return (IStructuredSelection) fSelectionViewer.getSelection();
	}
	
	@SuppressWarnings("unchecked")
	public ItemT getSingleItem(IStructuredSelection selection) {
		if (selection.size() == 1) {
			return (ItemT) selection.getFirstElement(); 
		}
		return null;
	}
	
}
