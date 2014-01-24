/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.breakpoints;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPropertyListener;

import de.walware.ecommons.FastList;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.debug.core.breakpoints.IRMethodBreakpoint;


public class RMethodBreakpointDetailEditor extends RLineBreakpointDetailEditor {
	
	
	private IRMethodBreakpoint fBreakpoint;
	
	private Button fEntryControl;
	private Button fExitControl;
	
	private WritableValue fEntryValue;
	private WritableValue fExitValue;
	
	
	public RMethodBreakpointDetailEditor(final boolean mnemonics, final boolean autosave,
			final FastList<IPropertyListener> listeners) {
		super(mnemonics, autosave, listeners);
	}
	
	
	@Override
	protected void addContent(final Composite composite) {
		{	final Composite methodOptions = createMethodOptions(composite);
			methodOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		
		super.addContent(composite);
	}
	
	protected Composite createMethodOptions(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 3));
		
		fEntryControl = new Button(composite, SWT.CHECK);
		fEntryControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		fEntryControl.setText(checkLabel("&Entry"));
		
		fExitControl = new Button(composite, SWT.CHECK);
		fExitControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		fExitControl.setText(checkLabel("&Exit"));
		
		return composite;
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		super.addBindings(dbc, realm);
		
		fEntryValue = new WritableValue(realm, Boolean.FALSE, Boolean.class);
		fExitValue = new WritableValue(realm, Boolean.FALSE, Boolean.class);
		
		enableAutosave(dbc.bindValue(SWTObservables.observeSelection(fEntryControl), fEntryValue));
		enableAutosave(dbc.bindValue(SWTObservables.observeSelection(fExitControl), fExitValue));
	}
	
	
	@Override
	public void doSetInput(final Object input) {
		super.doSetInput(input);
		
		fBreakpoint = null;
		boolean control = false;
		boolean entry = false;
		boolean exit = false;
		
		if (input instanceof IRMethodBreakpoint) {
			fBreakpoint = (IRMethodBreakpoint) input;
			
			try {
				entry = fBreakpoint.isEntry();
				exit = fBreakpoint.isExit();
			}
			catch (final CoreException e) {
				logLoadError(e);
			}
			
			control = true;
		}
		
		fEntryControl.setEnabled(control);
		fExitControl.setEnabled(control);
		
		fEntryValue.setValue(entry);
		fExitValue.setValue(exit);
	}
	
	@Override
	public void doSave() {
		super.doSave();
		
		if (fBreakpoint != null) {
			try {
				fBreakpoint.setEntry((Boolean) fEntryValue.getValue());
				fBreakpoint.setExit((Boolean) fExitValue.getValue());
			}
			catch (final CoreException e) {
				logSaveError(e);
			}
		}
	}
	
}
