/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.util;

import java.util.EnumSet;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.databinding.jface.AbstractSWTObservableValue;
import de.walware.ecommons.preferences.Preference.EnumSetPref;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.nico.core.runtime.SubmitType;


public class SubmitTypeSelectionComposite extends Composite {
	
	
	public static final EnumSetPref<SubmitType> SOURCE_ENCODER = new EnumSetPref<SubmitType>(null, null, SubmitType.class);
	
	private class Observable extends AbstractSWTObservableValue {
		
		public Observable() {
			super(fViewer.getTable());
		}
		
		@Override
		public Object getValueType() {
			return EnumSet.class;
		}
		
		@Override
		protected Object doGetValue() {
			return fCurrentSelection;
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
	
	
	private EnumSet<SubmitType> fCurrentSelection;
	
	private CheckboxTableViewer fViewer;
	
	private Observable fObservable;
	
	
	public SubmitTypeSelectionComposite(final Composite parent) {
		super(parent, SWT.NONE);
		
		create();
	}
	
	
	protected void create() {
		final SubmitType[] input = SubmitType.values();
		final ILabelProvider labelProvider = new LabelProvider() {
			@Override
			public String getText(final Object element) {
				return ((SubmitType) element).getLabel();
			}
		};
		
		setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 1));
		fViewer = CheckboxTableViewer.newCheckList(this, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = LayoutUtil.hintWidth(fViewer.getTable(), input, labelProvider);
		gd.heightHint = LayoutUtil.hintHeight(fViewer.getTable(), input.length);
		fViewer.getControl().setLayoutData(gd);
		fViewer.setContentProvider(new ArrayContentProvider());
		fViewer.setLabelProvider(labelProvider);
		fViewer.setInput(SubmitType.values());
		
		fViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(final CheckStateChangedEvent event) {
				final SubmitType type = (SubmitType) event.getElement();
				final EnumSet<SubmitType> newSet = EnumSet.copyOf(fCurrentSelection);
				final boolean changed;
				if (event.getChecked()) {
					changed = newSet.add(type);
				}
				else {
					changed = newSet.remove(type);
				}
				if (changed) {
					final EnumSet<SubmitType> previousSet = fCurrentSelection;
					fCurrentSelection = newSet;
					if (fObservable != null) {
						fObservable.doValueChanged(previousSet, newSet);
					}
				}
			}
		});
		
		fCurrentSelection = EnumSet.noneOf(SubmitType.class);
	}
	
	@Override
	public boolean setFocus() {
		return fViewer.getTable().setFocus();
	}
	
	public void setSelection(final EnumSet<SubmitType> selectedTypes) {
		final EnumSet<SubmitType> selection = EnumSet.copyOf(selectedTypes);
		fViewer.setCheckedElements(selection.toArray());
		fCurrentSelection = selection;
	}
	
	public EnumSet<SubmitType> getSelection() {
		return EnumSet.copyOf(fCurrentSelection);
	}
	
	
	public Observable getObservable() {
		if (fObservable == null) {
			fObservable = new Observable();
		}
		return fObservable;
	}
	
}
