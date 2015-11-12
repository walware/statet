/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
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
import de.walware.ecommons.debug.ui.breakpoints.AbstractBreakpointDetailEditor;
import de.walware.ecommons.ltk.ui.sourceediting.SnippetEditor;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.debug.core.breakpoints.IRLineBreakpoint;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfiguration;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfigurator;


public class RLineBreakpointDetailEditor extends AbstractBreakpointDetailEditor {
	
	
	private IRLineBreakpoint fBreakpoint;
	
	private Button fConditionEnabledControl;
	private SnippetEditor fConditionCodeEditor;
	
	private WritableValue fConditionEnabledValue;
	private WritableValue fConditionCodeValue;
	
	
	public RLineBreakpointDetailEditor(final boolean mnemonics, final boolean autosave,
			final FastList<IPropertyListener> listeners) {
		super(mnemonics, autosave, listeners);
	}
	
	
	@Override
	protected void addContent(final Composite composite) {
		{	final Composite options = createConditionOptions(composite);
			options.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}
	}
	
	protected Composite createConditionOptions(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 1));
		
		fConditionEnabledControl = new Button(composite, SWT.CHECK);
		fConditionEnabledControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		fConditionEnabledControl.setText(checkLabel("&Conditional / Expression:"));
		
		final RSourceViewerConfigurator viewerConfigurator = new RSourceViewerConfigurator(
				RCore.WORKBENCH_ACCESS,
				new RSourceViewerConfiguration(null, SharedUIResources.getColors()) );
		fConditionCodeEditor = new SnippetEditor(viewerConfigurator);
		fConditionCodeEditor.create(composite, SWT.BORDER | SWT.H_SCROLL | SWT.MULTI | SWT.LEFT_TO_RIGHT);
		fConditionCodeEditor.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		return composite;
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		super.addBindings(dbc, realm);
		
		fConditionEnabledValue = new WritableValue(realm, Boolean.FALSE, Boolean.class);
		fConditionCodeValue = new WritableValue(realm, "", String.class);
		
		enableAutosave(dbc.bindValue(SWTObservables.observeSelection(fConditionEnabledControl),
				fConditionEnabledValue ));
		dbc.bindValue(SWTObservables.observeText(fConditionCodeEditor.getTextControl(), SWT.Modify),
				fConditionCodeValue );
		dbc.bindValue(SWTObservables.observeEnabled(fConditionCodeEditor.getTextControl()),
				fConditionEnabledValue );
	}
	
	
	@Override
	public void doSetInput(final Object input) {
		super.doSetInput(input);
		
		fBreakpoint = null;
		boolean control = false;
		boolean enabled = false;
		String code = "";
		if (input instanceof IRLineBreakpoint) {
			fBreakpoint = (IRLineBreakpoint) input;
			
			int type = IRLineBreakpoint.R_TOPLEVEL_COMMAND_ELEMENT_TYPE;
			try {
				type = fBreakpoint.getElementType();
				enabled = fBreakpoint.isConditionEnabled();
				code = fBreakpoint.getConditionExpr();
				if (code == null) {
					code = "";
				}
			}
			catch (final CoreException e) {
				logLoadError(e);
			}
			
			if (type == IRLineBreakpoint.R_TOPLEVEL_COMMAND_ELEMENT_TYPE) {
				enabled = false;
				control = false;
			}
			else {
				control = true;
			}
		}
		
		fConditionEnabledControl.setEnabled(control);
		
		fConditionEnabledValue.setValue(enabled);
		fConditionCodeValue.setValue(code);
	}
	
	@Override
	public void doSave() {
		super.doSave();
		
		if (fBreakpoint != null) {
			try {
				fBreakpoint.setConditionEnabled((Boolean) fConditionEnabledValue.getValue());
				fBreakpoint.setConditionExpr((String) fConditionCodeValue.getValue());
			}
			catch (final CoreException e) {
				logSaveError(e);
			}
		}
	}
	
}
