/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.dialogs;

import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.ConstList;
import de.walware.ecommons.ui.SharedMessages;
import de.walware.ecommons.ui.util.LayoutUtil;


/**
 * Composite with buttons to manipulate list or tree items.
 */
public class ButtonGroup<ItemType> extends Composite {
	
	
	private static int ADD_NEW = 1;
	private static int ADD_COPY = 2;
	private static int EDIT = 3;
	
	
	private StructuredViewer fViewer;
	private boolean fTreeMode;
	
	private Button fAddButton;
	private Button fCopyButton;
	private Button fEditButton;
	private Button fDeleteButton;
	
	private Button fDefaultButton;
	
	private Button fUpButton;
	private Button fDownButton;
	
	private int fCachedWidthHint;
	
	// Model
	private IObservableList fList;
	private Set<ItemType> fCheckedSet;
	private IObservableValue fDefault;
	
	private boolean fIsDirty;
	
	
	public ButtonGroup(final Composite parent) {
		super(parent, SWT.NONE);
		setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 1));
	}
	
	
	protected void addLayoutData(final Button button) {
		if (fCachedWidthHint == 0) {
			fCachedWidthHint = LayoutUtil.hintWidth(button);
		}
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.widthHint = fCachedWidthHint;
		button.setLayoutData(gd);
	}
	
	public void addAddButton() {
		fAddButton = new Button(this, SWT.PUSH);
		addLayoutData(fAddButton);
		fAddButton.setText(SharedMessages.CollectionEditing_AddItem_label);
		fAddButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final Object item = ((IStructuredSelection) fViewer.getSelection()).getFirstElement();
				edit0(item, ADD_NEW);
			}
		});
	}
	
	public void addCopyButton() {
		fCopyButton = new Button(this, SWT.PUSH);
		addLayoutData(fCopyButton);
		fCopyButton.setText(SharedMessages.CollectionEditing_CopyItem_label);
		fCopyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final Object item = getItemToEdit((IStructuredSelection) fViewer.getSelection());
				if (item != null) {
					edit0(item, ADD_COPY);
				}
			}
		});
	}
	
	public void addEditButton() {
		fEditButton = new Button(this, SWT.PUSH);
		addLayoutData(fEditButton);
		fEditButton.setText(SharedMessages.CollectionEditing_EditItem_label);
		fEditButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final Object item = getItemToEdit((IStructuredSelection) fViewer.getSelection());
				if (item != null) {
					edit0(item, EDIT);
				}
			}
		});
	}
	
	public void addDeleteButton() {
		fDeleteButton = new Button(this, SWT.PUSH);
		addLayoutData(fDeleteButton);
		fDeleteButton.setText(SharedMessages.CollectionEditing_RemoveItem_label);
		fDeleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final List<? extends Object> items = getItemsToDelete((IStructuredSelection) fViewer.getSelection());
				delete0(items);
			}
		});
	}
	
	public void addDefaultButton() {
		fDefaultButton = new Button(this, SWT.PUSH);
		addLayoutData(fDefaultButton);
		fDefaultButton.setText(SharedMessages.CollectionEditing_DefaultItem_label);
		fDefaultButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final Object item = getItemToEdit((IStructuredSelection) fViewer.getSelection());
				if (item != null) {
					setDefault0(item);
				}
			}
		});
	}
	
	public void addUpButton() {
		fUpButton = new Button(this, SWT.PUSH);
		addLayoutData(fUpButton);
		fUpButton.setText(SharedMessages.CollectionEditing_MoveItemUp_label);
		fUpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
				final Object item = getItemToEdit(selection);
				if (item != null) {
					move0(item, -1);
				}
			}
		});
	}
	
	public void addDownButton() {
		fDownButton = new Button(this, SWT.PUSH);
		addLayoutData(fDownButton);
		fDownButton.setText(SharedMessages.CollectionEditing_MoveItemDown_label);
		fDownButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fDownButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final Object item = getItemToEdit((IStructuredSelection) fViewer.getSelection());
				if (item != null) {
					move0(item, 1);
				}
			}
		});
	}
	
	public void addSeparator() {
		LayoutUtil.addSmallFiller(this, false);
	}
	
	public void connectTo(final StructuredViewer viewer, final IObservableList list, final IObservableValue defaultValue) {
		fViewer = viewer;
		fTreeMode = (viewer instanceof TreeViewer);
		if (fDeleteButton != null) {
			fViewer.getControl().addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(final KeyEvent event) {
					if (event.character == SWT.DEL && event.stateMask == 0 && fDeleteButton != null) {
						final List<? extends Object> items = getItemsToDelete((IStructuredSelection) fViewer.getSelection());
						if (items != null) {
							delete0(items);
						}
					} 
				}	
			});
		}
		if (fEditButton != null) {
			fViewer.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(final DoubleClickEvent event) {
					final Object item = getItemToEdit((IStructuredSelection) event.getSelection());
					if (item != null) {
						edit0(item, EDIT);
					}
				}
			});
		}
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				updateState();
			}
		});
		fList = list;
		fDefault = defaultValue;
	}
	
	public void setCheckedModel(final Set<ItemType> set) {
		fCheckedSet = set;
	}
	
	public void updateState() {
		final IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
		if (fAddButton != null) {
			if (fTreeMode) {
				fAddButton.setEnabled(selection.size() == 1
						&& isAddAllowed(selection.getFirstElement()));
			}
			else {
				fAddButton.setEnabled(true);
			}
		}
		final Object item = getItemToEdit(selection);
		if (fCopyButton != null) {
			fCopyButton.setEnabled(item != null);
		}
		if (fEditButton != null) {
			fEditButton.setEnabled(item != null);
		}
		if (fDeleteButton != null) {
			fDeleteButton.setEnabled(getItemsToDelete(selection) != null);
		}
		
		if (fDefaultButton != null) {
			fDefaultButton.setEnabled(item != null);
		}
		
		if (fUpButton != null) {
			fUpButton.setEnabled(item != null);
		}
		if (fDownButton != null) {
			fDownButton.setEnabled(item != null);
		}
	}
	
	protected boolean isAddAllowed(final Object element) {
		return true;
	}
	
	protected boolean isModifyAllowed(final Object element) {
		return true;
	}
	
	protected Object getItemToEdit(final IStructuredSelection selection) {
		if (selection.size() == 1) {
			final Object element = selection.getFirstElement();
			if (!isModifyAllowed(element)) {
				return null;
			}
			return element;
		}
		return null;
	}
	
	protected List<? extends Object> getItemsToDelete(final IStructuredSelection selection) {
		if (!selection.isEmpty()) {
			final Object[] elements = selection.toArray();
			for (final Object element : elements) {
				if (!isModifyAllowed(element)) {
					return null;
				}
			}
			return new ConstList<Object>(elements);
		}
		return null;
	}
	
	protected List<? super ItemType> getChildContainer(final Object element) {
		return fList;
	}
	
	protected Object getAddParent(final Object element) {
		return element;
	}
	
	protected Object getParent(final Object element) {
		if (fTreeMode) {
			return ((ITreeContentProvider) fViewer.getContentProvider()).getParent(element);
		}
		else {
			return null;
		}
	}
	
	protected ItemType getModelItem(final Object element) {
		return (ItemType) element;
	}
	
	protected Object getViewerElement(final ItemType item, final Object parent) {
		return item;
	}
	
	
	private void edit0(final Object element, final int command) {
		final boolean newItem = (command == ADD_NEW || command == ADD_COPY);
		final ItemType orgItem = (command != ADD_NEW && element != null) ? getModelItem(element) : null;
		
		final ItemType editItem = edit1(((command != ADD_NEW) ? orgItem : null), newItem);
		if (editItem == null) {
			return;
		}
		fIsDirty = true;
		Object parent;
		if (newItem) {
			if (fDefault != null && fList.isEmpty()) {
				fDefault.setValue(editItem);
			}
			final List<? super ItemType> list;
			if (command == ADD_NEW) {
				parent = getAddParent(element);
			}
			else {
				parent = getParent(element);
			}
			list = getChildContainer(element);
			list.add(editItem);
		}
		else {
			parent = getParent(element);
			if (orgItem != editItem) { // can be directly manipulated or replaced)
				if (fDefault != null && fDefault.getValue() == orgItem) {
					fDefault.setValue(editItem);
				}
				final List<? super ItemType> list = getChildContainer(element);
				final int idx = list.indexOf(orgItem);
				list.set(idx, editItem);
			}
		}
		final Object editElement = getViewerElement(editItem, parent);
		refresh0(editElement);
		if (fCheckedSet != null) {
			if (newItem) {
				fCheckedSet.add(editItem);
			}
			else {
				if (fCheckedSet.remove(orgItem)) {
					fCheckedSet.add(editItem);
				}
			}
		}
		if (fViewer instanceof ColumnViewer) {
			((ColumnViewer) fViewer).editElement(editElement, 0);
		}
	}
	
	protected ItemType edit1(final ItemType item, final boolean newItem) {
		return null;
	}
	
	private void delete0(final List<? extends Object> elements) {
		fIsDirty = true;
		if (fDefault != null) {
			final Object defaultValue = fDefault.getValue();
			if (defaultValue != null && elements.contains(defaultValue)) {
				fDefault.setValue(null);
			}
		}
		if (fTreeMode) {
			for (final Object element : elements) {
				getChildContainer(element).remove(getModelItem(element));
			}
		}
		else {
			fList.removeAll(elements);
		}
		if (fCheckedSet != null) {
			fCheckedSet.removeAll(elements);
		}
		refresh0(null);
	}
	
	private void setDefault0(final Object element) {
		final ItemType item = getModelItem(element);
		fIsDirty = true;
		if (fDefault != null && item != null) {
			fDefault.setValue(item);
		}
		refresh0(null);
	}
	
	private void move0(final Object element, final int direction) {
		final int oldIdx = fList.indexOf(element);
		if (oldIdx < 0) {
			return;
		}
		final int newIdx = oldIdx+direction;
		if (newIdx < 0 || newIdx >= fList.size()) {
			return;
		}
		move1(oldIdx, newIdx);
		refresh0(element);
	}
	
	protected void move1(final int oldIdx, final int newIdx) {
		fList.move(oldIdx, newIdx);
	}
	
	public void refresh() {
		refresh0(null);
	}
	
	private void refresh0(final Object elementToSelect) {
		fViewer.refresh();
		if (elementToSelect != null) {
//			Display.getCurrent().asyncExec(new Runnable() {
//				public void run() {
					if (fTreeMode) {
						((TreeViewer) fViewer).expandToLevel(elementToSelect, 0);
					}
					fViewer.setSelection(new StructuredSelection(elementToSelect), true);
//				}
//			});
		}
		updateState();
	}
	
	public boolean isDirty() {
		return fIsDirty;
	}
	
}
