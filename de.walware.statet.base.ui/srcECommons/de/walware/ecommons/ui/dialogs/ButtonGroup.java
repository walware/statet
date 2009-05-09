/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.dialogs;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.ui.SharedMessages;
import de.walware.ecommons.ui.util.LayoutUtil;


/**
 * Composite with buttons to manipulate list items.
 */
public class ButtonGroup<ItemType> extends Composite {
	
	
	private StructuredViewer fViewer;
	
	private Button fAddButton;
	private Button fCopyButton;
	private Button fEditButton;
	private Button fDeleteButton;
	
	private Button fDefaultButton;
	
	private Button fUpButton;
	private Button fDownButton;
	
	// Model
	private IObservableList fList;
	private IObservableValue fDefault;
	
	private boolean fIsDirty;
	
	
	public ButtonGroup(final Composite parent) {
		super(parent, SWT.NONE);
		setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 1));
	}
	
	
	public void addAddButton() {
		fAddButton = new Button(this, SWT.PUSH);
		fAddButton.setText(SharedMessages.CollectionEditing_AddItem_label);
		fAddButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fAddButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				edit0(null, true);
			}
		});
	}
	
	public void addCopyButton() {
		fCopyButton = new Button(this, SWT.PUSH);
		fCopyButton.setText(SharedMessages.CollectionEditing_CopyItem_label);
		fCopyButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fCopyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final ItemType item = getItemToEdit((IStructuredSelection) fViewer.getSelection());
				if (item != null) {
					edit0(item, true);
				}
			}
		});
	}
	
	public void addEditButton() {
		fEditButton = new Button(this, SWT.PUSH);
		fEditButton.setText(SharedMessages.CollectionEditing_EditItem_label);
		fEditButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fEditButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final ItemType item = getItemToEdit((IStructuredSelection) fViewer.getSelection());
				if (item != null) {
					edit0(item, false);
				}
			}
		});
	}
	
	public void addDeleteButton() {
		fDeleteButton = new Button(this, SWT.PUSH);
		fDeleteButton.setText(SharedMessages.CollectionEditing_RemoveItem_label);
		fDeleteButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fDeleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final List<ItemType> items = getItemsToDelete((IStructuredSelection) fViewer.getSelection());
				delete0(items);
			}
		});
	}
	
	public void addDefaultButton() {
		fDefaultButton = new Button(this, SWT.PUSH);
		fDefaultButton.setText(SharedMessages.CollectionEditing_DefaultItem_label);
		fDefaultButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fDefaultButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final ItemType item = getItemToEdit((IStructuredSelection) fViewer.getSelection());
				if (item != null) {
					setDefault0(item);
				}
			}
		});
	}
	
	public void addUpButton() {
		fUpButton = new Button(this, SWT.PUSH);
		fUpButton.setText(SharedMessages.CollectionEditing_MoveItemUp_label);
		fUpButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fUpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final ItemType item = getItemToEdit((IStructuredSelection) fViewer.getSelection());
				if (item != null) {
					move(item, -1);
				}
			}
		});
	}
	
	public void addDownButton() {
		fDownButton = new Button(this, SWT.PUSH);
		fDownButton.setText(SharedMessages.CollectionEditing_MoveItemDown_label);
		fDownButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fDownButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final ItemType item = getItemToEdit((IStructuredSelection) fViewer.getSelection());
				if (item != null) {
					move(item, 1);
				}
			}
		});
	}
	
	public void addSeparator() {
		LayoutUtil.addSmallFiller(this, false);
	}
	
	public void connectTo(final StructuredViewer viewer, final IObservableList list, final IObservableValue defaultValue) {
		fViewer = viewer;
		if (fDeleteButton != null) {
			fViewer.getControl().addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(final KeyEvent event) {
					if (event.character == SWT.DEL && event.stateMask == 0 && fDeleteButton != null) {
						final List<ItemType> items = getItemsToDelete((IStructuredSelection) fViewer.getSelection());
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
					final ItemType item = getItemToEdit((IStructuredSelection) event.getSelection());
					if (item != null) {
						edit0(item, false);
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
	
	public void updateState() {
		final IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
		if (fAddButton != null) {
			fAddButton.setEnabled(true);
		}
		final ItemType item = getItemToEdit(selection);
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
	
	protected ItemType getItemToEdit(final IStructuredSelection selection) {
		if (selection.size() == 1) {
			return (ItemType) selection.getFirstElement();
		}
		else {
			return null;
		}
	}
	
	protected List<ItemType> getItemsToDelete(final IStructuredSelection selection) {
		if (!selection.isEmpty()) {
			return (List<ItemType>) Arrays.asList(selection.toArray());
		}
		else {
			return null;
		}
	}
	
	public void edit0(final ItemType item, final boolean newItem) {
		final ItemType editItem = edit1(item, newItem);
		if (editItem == null) {
			return;
		}
		fIsDirty = true;
		if (newItem) {
			if (fDefault != null && fList.isEmpty()) {
				fDefault.setValue(editItem);
			}
			fList.add(editItem);
		}
		else if (item != editItem) { // can be directly manipulated or replaced
			if (fDefault != null && fDefault.getValue() == item) {
				fDefault.setValue(editItem);
			}
			final int idx = fList.indexOf(item);
			fList.set(idx, editItem);
		}
		refresh0();
	}
	
	protected ItemType edit1(final ItemType item, final boolean newItem) {
		return null;
	}
	
	public void delete0(final List<ItemType> items) {
		fIsDirty = true;
		if (fDefault != null) {
			final Object defaultValue = fDefault.getValue();
			if (defaultValue != null && items.contains(defaultValue)) {
				fDefault.setValue(null);
			}
		}
		fList.removeAll(items);
		refresh0();
	}
	
	public void setDefault0(final ItemType item) {
		fIsDirty = true;
		if (fDefault != null && item != null) {
			fDefault.setValue(item);
		}
		refresh0();
	}
	
	public void move(final ItemType item, final int direction) {
		final int oldIdx = fList.indexOf(item);
		if (oldIdx < 0) {
			return;
		}
		final int newIdx = oldIdx+direction;
		if (newIdx < 0 || newIdx >= fList.size()) {
			return;
		}
		fList.move(oldIdx, newIdx);
		refresh0();
	}
	
	public void refresh0() {
		fViewer.refresh();
		updateState();
	}
	
	public boolean isDirty() {
		return fIsDirty;
	}
	
}
