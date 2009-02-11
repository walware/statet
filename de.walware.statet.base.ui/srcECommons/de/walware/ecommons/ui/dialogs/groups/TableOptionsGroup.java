/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.dialogs.groups;

import java.util.List;

import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;


public abstract class TableOptionsGroup<ItemT extends Object> 
		extends StructuredSelectionOptionsGroup<TableViewer, ItemT> {

	
	public TableOptionsGroup(boolean grabSelectionHorizontal, boolean grabVertical) {
		super(grabSelectionHorizontal, grabVertical);
	}
	
	
	@Override
	public TableViewer createSelectionViewer(Composite parent) {
		
		Table table = new Table(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		TableViewer viewer = new TableViewer(table);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		createTableColumns(viewer, table, layout);
		
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});

		return viewer;
	}
	
	protected abstract void createTableColumns(TableViewer viewer, Table table, TableLayout layout);
	
	
	public void addItem(ItemT newItem) {
		
		getListModel().add(newItem);
		getStructuredViewer().refresh();
	}
	
	public void replaceItem(ItemT oldItem, ItemT newItem) {
		
		int idx = getListModel().indexOf(oldItem);
		if (idx == -1)
			return; // Error
		
		getListModel().set(idx, newItem);
		getStructuredViewer().refresh();
	}
	
	public void removeItems(List<ItemT> items) {
		
		for (ItemT item : items) {
			getListModel().remove(item);
		}
		getStructuredViewer().refresh();
	}
}
