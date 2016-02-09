/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.ui.util;

import java.util.EnumSet;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import de.walware.ecommons.databinding.jface.AbstractSWTObservableValue;
import de.walware.ecommons.preferences.core.Preference.EnumSetPref;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.SubmitType;


public class SubmitTypeSelectionComposite extends Composite {
	
	
	public static final EnumSetPref<SubmitType> SOURCE_ENCODER= new EnumSetPref<>(null, null, SubmitType.class);
	
	private class Observable extends AbstractSWTObservableValue {
		
		public Observable() {
			super(SubmitTypeSelectionComposite.this.viewer.getTable());
		}
		
		@Override
		public Object getValueType() {
			return EnumSet.class;
		}
		
		@Override
		protected Object doGetValue() {
			return SubmitTypeSelectionComposite.this.selectedTypes;
		}
		
		@Override
		protected void doSetValue(final Object value) {
			if (value instanceof EnumSet<?>) {
				setSelection((EnumSet<SubmitType>) value);
			}
		}
		
		void doValueChanged(final EnumSet<SubmitType> oldValue, final EnumSet<SubmitType> newValue) {
			fireValueChange(Diffs.createValueDiff(oldValue, newValue));
		}
		
	}
	
	
	private final SubmitType[] types;
	
	private EnumSet<SubmitType> editableTypes;
	
	private EnumSet<SubmitType> selectedTypes;
	
	private TableViewer viewer;
	
	private Observable observable;
	
	
	public SubmitTypeSelectionComposite(final Composite parent) {
		super(parent, SWT.NONE);
		
		this.types= SubmitType.values();
		this.editableTypes= EnumSet.allOf(SubmitType.class);
		this.selectedTypes= EnumSet.noneOf(SubmitType.class);
		
		create();
	}
	
	
	protected void create() {
		setLayout(LayoutUtil.createCompositeGrid(1));
		
		final Table table= new Table(this, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.NO_SCROLL);
		final TableViewer viewer= new TableViewer(table);
		
		final Listener buttonListener= new Listener() {
			@Override
			public void handleEvent(final Event event) {
				final Button button= (Button) event.widget;
				final SubmitType type= (SubmitType) button.getData();
				
				switch (event.type) {
				
				case SWT.Selection:
					if (!SubmitTypeSelectionComposite.this.editableTypes.contains(type)) {
						event.doit= false;
						return;
					}
					setSelected(type, button.getSelection());
					return;
				case SWT.MouseDown:
					if (!SubmitTypeSelectionComposite.this.editableTypes.contains(type)) {
						return;
					}
					viewer.setSelection(new StructuredSelection(type));
					button.setSelection(!button.getSelection());
					setSelected(type, button.getSelection());
					recheckSelection();
					return;
					
				case SWT.FocusIn:
					viewer.getTable().forceFocus();
					viewer.setSelection(new StructuredSelection(type));
					return;
				}
			}
		};
		
		{	final TableViewerColumn column= new TableViewerColumn(viewer, SWT.CENTER);
			column.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					final SubmitType type= (SubmitType) cell.getElement();
					final TableItem item= (TableItem) cell.getItem();
					
					TableEditor editor= (TableEditor) item.getData("editor"); //$NON-NLS-1$
					final Button button;
					if (editor == null
							|| editor.getEditor() == null || editor.getEditor().isDisposed()) {
						button= new Button(table, SWT.CHECK);
						editor= new TableEditor(table);
						button.pack();
						final Point buttonSize= button.getSize();
						editor.horizontalAlignment= SWT.CENTER;
						editor.minimumWidth= buttonSize.x;
						editor.minimumHeight= buttonSize.y;
						editor.setEditor(button, item, 0);
						item.setData("editor", editor); //$NON-NLS-1$
						
						button.addListener(SWT.Selection, buttonListener);
						button.addListener(SWT.MouseDown, buttonListener);
						button.addListener(SWT.FocusIn, buttonListener);
					}
					else {
						button= (Button) editor.getEditor();
					}
					
					button.setData(type);
					
					button.setEnabled(SubmitTypeSelectionComposite.this.editableTypes.contains(type));
					button.setSelection(SubmitTypeSelectionComposite.this.selectedTypes.contains(type));
					editor.setEditor(button, item, cell.getColumnIndex());
				};
			});
		}
		{	final TableViewerColumn column= new TableViewerColumn(viewer, SWT.NONE);
			column.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					final SubmitType type= (SubmitType) cell.getElement();
					
					cell.setText(type.getLabel());
					cell.setForeground(SubmitTypeSelectionComposite.this.editableTypes.contains(type) ? null :
					cell.getItem().getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
				}
			});
		}
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(this.types);
		
		table.getColumn(0).pack();
		table.getColumn(1).pack();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.viewer= viewer;
		
		table.setTabList(new Control[0]);
		final Listener tableListener= new Listener() {
			@Override
			public void handleEvent(final Event event) {
				switch (event.type) {
				case SWT.KeyDown:
					if (event.keyCode == SWT.SPACE) {
						final SubmitType type= (SubmitType) ((StructuredSelection) viewer.getSelection()).getFirstElement();
						if (type == null
								|| !SubmitTypeSelectionComposite.this.editableTypes.contains(type)) {
							return;
						}
						toggle(type);
					}
					return;
					
				case SWT.MouseDown:
					final TableItem item= table.getItem(new Point(event.x, event.y));
					if (item != null) {
						final SubmitType type= (SubmitType) item.getData();
						if (type == null 
								|| !SubmitTypeSelectionComposite.this.editableTypes.contains(type)) {
							return;
						}
						final TableEditor editor= (TableEditor) item.getData("editor");
						final Button button= (Button) editor.getEditor();
						button.setSelection(!button.getSelection());
						setSelected(type, button.getSelection());
					}
					return;
				}
			}
		};
		table.addListener(SWT.KeyDown, tableListener);
		table.addListener(SWT.MouseDown, tableListener);
	}
	
	private void toggle(final SubmitType type) {
		final TableItem item= getItem(type);
		if (item != null) {
			final TableEditor editor= (TableEditor) item.getData("editor");
			final Button button= (Button) editor.getEditor();
			button.setSelection(!button.getSelection());
			setSelected(type, button.getSelection());
		}
		else {
			setSelected(type, !this.selectedTypes.contains(type));
		}
	}
	
	private void setSelected(final SubmitType type, final boolean selected) { 
		final EnumSet<SubmitType> newSet= EnumSet.copyOf(SubmitTypeSelectionComposite.this.selectedTypes);
		final boolean changed;
		if (selected) {
			changed= newSet.add(type);
		}
		else {
			changed= newSet.remove(type);
		}
		if (changed) {
			final EnumSet<SubmitType> previousSet= this.selectedTypes;
			this.selectedTypes= newSet;
			if (SubmitTypeSelectionComposite.this.observable != null) {
				SubmitTypeSelectionComposite.this.observable.doValueChanged(previousSet, newSet);
			}
		}
	}
	
	private int indexOf(final SubmitType type) {
		for (int i= 0; i < this.types.length; i++) {
			if (this.types[i] == type) {
				return i;
			}
		}
		return -1;
	}
	
	private TableItem getItem(final SubmitType type) {
		final Table table= this.viewer.getTable();
		final int idx= indexOf(type);
		if (idx >= 0 && idx < table.getItemCount()) {
			return table.getItem(idx);
		}
		return null;
	}
	
	private void recheckSelection() {
		this.viewer.getTable().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (UIAccess.isOkToUse(SubmitTypeSelectionComposite.this.viewer)) {
					final SubmitType type= (SubmitType) ((StructuredSelection) SubmitTypeSelectionComposite.this.viewer.getSelection()).getFirstElement();
					final int idx= indexOf(type);
					if (idx >= 0) {
						SubmitTypeSelectionComposite.this.viewer.getTable().setSelection(idx);
					}
				}
			}
		});
	}
	
	@Override
	public boolean setFocus() {
		return this.viewer.getControl().setFocus();
	}
	
	
	public void setEditable(final EnumSet<SubmitType> editableTypes) {
		if (editableTypes == null) {
			throw new NullPointerException();
		}
		this.editableTypes= editableTypes;
		this.viewer.update(this.types, null);
	}
	
	public void setSelection(final EnumSet<SubmitType> selectedTypes) {
		if (selectedTypes == null) {
			throw new NullPointerException();
		}
		this.selectedTypes= EnumSet.copyOf(selectedTypes);
		this.viewer.update(this.types, null);
	}
	
	public EnumSet<SubmitType> getSelection() {
		return EnumSet.copyOf(this.selectedTypes);
	}
	
	
	public Observable getObservable() {
		if (this.observable == null) {
			this.observable= new Observable();
		}
		return this.observable;
	}
	
}
