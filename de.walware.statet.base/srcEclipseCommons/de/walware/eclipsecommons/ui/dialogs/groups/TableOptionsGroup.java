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

import java.util.List;

import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


public abstract class TableOptionsGroup<ItemT extends SelectionItem> 
		extends StructuredSelectionOptionsGroup<TableViewer, ItemT> {

	private class ItemContentProvider implements IStructuredContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
		public Object[] getElements(Object inputElement) {
			
			return fSelectionModel.toArray();
		}
	}
	
	private class ItemSorter extends ViewerSorter {
		
		@SuppressWarnings("unchecked")
		public int compare(Viewer viewer, Object e1, Object e2) {
			return collator.compare(((ItemT) e1).fName, ((ItemT) e2).fName);
		}
	}

	
	private String[] fColumnHeaders;

	
	public TableOptionsGroup(boolean grabSelectionHorizontal, boolean grabVertical) {
		super(grabSelectionHorizontal, grabVertical);
	}
	
	
	@Override
	protected Control createSelectionControl(Composite parent, GridData gd) {
		
		Table table = new Table(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		fSelectionViewer = new TableViewer(table);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		ColumnLayoutData[] columnsData = createColumnLayoutData(table);
		for (int i = 0; i < fColumnHeaders.length; i++) {
			TableColumn col = new TableColumn(table, SWT.LEFT);
			col.setText(fColumnHeaders[i]);
			if (columnsData != null)
				layout.addColumnData(columnsData[i]);
		}
		
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});
		
		fSelectionViewer.setContentProvider(new ItemContentProvider());
		fSelectionViewer.setLabelProvider(createTableLabelProvider());
		fSelectionViewer.setSorter(new ItemSorter());
		
		fSelectionViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleListSelection();
			}
		});
		fSelectionViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				ItemT item = getSingleSelectedItem();
				if (item != null)
					handleDoubleClick(item);
			}
		});
		
		return fSelectionViewer.getControl();
	}
	
	protected abstract ITableLabelProvider createTableLabelProvider();
	
	public void setTableColumns(String[] columnHeaders) {
		
		fColumnHeaders = columnHeaders;
	}
	
	protected ColumnLayoutData[] createColumnLayoutData(Table table) {
		
		return null;
	}
		
	
	
	
	/**
	 * Handles key events in the table viewer.
	 * <p>
	 * Standard-Implementierung macht nichts.
	 */
	protected void handleKeyPressed(KeyEvent event) {
	}
	
	
	
	
	public void addItem(ItemT newItem) {
		
		fSelectionModel.add(newItem);
		fSelectionViewer.refresh();
	}
	
	public void replaceItem(ItemT oldItem, ItemT newItem) {
		
		int idx = fSelectionModel.indexOf(oldItem);
		if (idx == -1)
			return; // Error
		
		fSelectionModel.set(idx, newItem);
		fSelectionViewer.refresh();
	}
	
	public void removeItems(List<ItemT> items) {
		
		for (ItemT item : items) {
			fSelectionModel.remove(item);
		}
		fSelectionViewer.refresh();
	}
}
