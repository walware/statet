/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui.launching;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.nico.core.NicoVariables;


public class RHistoryConfigurationComposite extends RTrackingConfigurationComposite {
	
	
	static final String HISTORY_TRACKING_DEFAULT_PATH = "${"+NicoVariables.SESSION_STARTUP_WD_VARNAME+"}/.Rhistory"; //$NON-NLS-1$ //$NON-NLS-2$
	
	
	private Button fAutoloadControl;
	
	
	public RHistoryConfigurationComposite(final Composite parent) {
		super(parent);
		
		setStreamsEnabled(false);
	}
	
	
	@Override
	protected void configure() {
		addSaveTemplate(new SaveTemplate("'.Rhistory' in R working directory", HISTORY_TRACKING_DEFAULT_PATH));
		addSaveTemplate(new SaveTemplate("'.Rhistory' in user home directory", "${system_property:user.home}/.RHistory")); //$NON-NLS-2$
	}
	
	@Override
	protected Composite createAdditionalOptions(final Composite parent) {
		final Group composite = new Group(parent, SWT.NONE);
		composite.setText("History Actions:");
		composite.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
		
		fAutoloadControl = new Button(composite, SWT.CHECK);
		fAutoloadControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fAutoloadControl.setText("&Load history from file at startup");
		
		addDefaultAdditionalOptions(composite);
		
		return composite;
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		super.addBindings(dbc, realm);
		
		dbc.bindValue(SWTObservables.observeSelection(fAutoloadControl), BeansObservables.observeValue(getInput(), "loadHistory"));
	}
	
}
