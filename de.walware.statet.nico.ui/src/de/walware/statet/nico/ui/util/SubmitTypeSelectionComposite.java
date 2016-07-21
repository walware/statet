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
import java.util.Objects;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import de.walware.ecommons.databinding.jface.AbstractSWTObservableValue;
import de.walware.ecommons.preferences.core.Preference.EnumSetPref;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.ViewerUtil;
import de.walware.ecommons.ui.util.ViewerUtil.CheckboxColumnControl;

import de.walware.statet.nico.core.runtime.SubmitType;


public class SubmitTypeSelectionComposite extends Composite implements ICheckStateListener {
	
	
	public static final EnumSetPref<SubmitType> SOURCE_ENCODER= new EnumSetPref<>(null, null, SubmitType.class);
	
	private class Observable extends AbstractSWTObservableValue {
		
		private EnumSet<SubmitType> lastValue;
		
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
				setSelection(this.lastValue= (EnumSet<SubmitType>) value);
			}
		}
		
		void doValueChanged(final EnumSet<SubmitType> newValue) {
			if (!Objects.equals(newValue, this.lastValue)) {
				fireValueChange(Diffs.createValueDiff(
						this.lastValue,
						this.lastValue= EnumSet.copyOf(newValue) ));
			}
		}
		
	}
	
	
	private final SubmitType[] types;
	
	private final EnumSet<SubmitType> editableTypes;
	
	private final EnumSet<SubmitType> selectedTypes;
	
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
		
		{	final TableViewerColumn column= new TableViewerColumn(viewer, SWT.CENTER);
			final CheckboxColumnControl<SubmitType> columnControl= new ViewerUtil.CheckboxColumnControl<>(
					viewer, this.selectedTypes, this.editableTypes );
			column.setLabelProvider(columnControl);
			columnControl.configureAsMainColumn();
			columnControl.addCheckStateListener(this);
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
	}
	
	@Override
	public void checkStateChanged(final CheckStateChangedEvent event) {
		if (SubmitTypeSelectionComposite.this.observable != null) {
			SubmitTypeSelectionComposite.this.observable.doValueChanged(this.selectedTypes);
		}
	}
	
	@Override
	public boolean setFocus() {
		return this.viewer.getControl().setFocus();
	}
	
	
	public void setEditable(final EnumSet<SubmitType> editableTypes) {
		if (editableTypes == null) {
			throw new NullPointerException();
		}
		this.editableTypes.clear();
		this.editableTypes.addAll(editableTypes);
		this.viewer.update(this.types, null);
	}
	
	public void setSelection(final EnumSet<SubmitType> selectedTypes) {
		if (selectedTypes == null) {
			throw new NullPointerException();
		}
		this.selectedTypes.clear();
		this.selectedTypes.addAll(selectedTypes);
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
