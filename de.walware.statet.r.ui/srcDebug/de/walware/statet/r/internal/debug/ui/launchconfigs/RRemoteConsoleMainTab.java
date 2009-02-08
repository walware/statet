/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launchconfigs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.launching.RConsoleLaunching;


public class RRemoteConsoleMainTab extends RConsoleMainTab {
	
	
	private Text fAddressControl;
	private WritableValue fAddressValue;
	
	
	public RRemoteConsoleMainTab() {
	}
	
	
	@Override
	protected RConsoleType[] loadTypes() {
		final List<RConsoleType> types = new ArrayList<RConsoleType>();
		types.add(new RConsoleType("RJ (RMI/JRI)", RConsoleLaunching.REMOTE_RJS, false, false)); //$NON-NLS-1$
		return types.toArray(new RConsoleType[types.size()]);
	}
	
	@Override
	protected void createTypeDetails(final Composite container) {
		final Label label = new Label(container, SWT.NONE);
		label.setText("&Address: ");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		fAddressControl = new Text(container, SWT.LEFT | SWT.BORDER);
		fAddressControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		LayoutUtil.addGDDummy(container);
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		super.addBindings(dbc, realm);
		
		fAddressValue = new WritableValue("", String.class); //$NON-NLS-1$
		dbc.bindValue(SWTObservables.observeText(fAddressControl, SWT.Modify),
				fAddressValue, null, null);
	}
	
	
	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		super.setDefaults(configuration);
		configuration.setAttribute(RConsoleLaunching.ATTR_ADDRESS, "rserver"); //$NON-NLS-1$
	}
	
	@Override
	protected void doInitialize(final ILaunchConfiguration configuration) {
		super.doInitialize(configuration);
		
		String address;
		try {
			address = configuration.getAttribute(RConsoleLaunching.ATTR_ADDRESS, ""); //$NON-NLS-1$
		} catch (final CoreException e) {
			address = ""; //$NON-NLS-1$
			logReadingError(e);
		}
		fAddressValue.setValue(address);
	}
	
	@Override
	protected void doSave(final ILaunchConfigurationWorkingCopy configuration) {
		super.doSave(configuration);
		configuration.setAttribute(RConsoleLaunching.ATTR_TYPE, RConsoleLaunching.REMOTE_RJS);
		configuration.setAttribute(RConsoleLaunching.ATTR_ADDRESS, (String) fAddressValue.getValue());
	}
	
}
