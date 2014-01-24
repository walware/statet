/*=============================================================================#
 # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.ui.dialogs.ExtStatusDialog;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.nico.core.util.TrackingConfiguration;


public class TrackingConfigurationDialog extends ExtStatusDialog {
	
	
	private final TrackingConfiguration fConfig;
	
	private TrackingConfigurationComposite fConfigComposite;
	
	
	public TrackingConfigurationDialog(final Shell parent, final TrackingConfiguration config, final boolean isNew) {
		super(parent, (isNew) ? WITH_DATABINDING_CONTEXT :
				(WITH_DATABINDING_CONTEXT | SHOW_INITIAL_STATUS) );
		setTitle(isNew ? "New Tracking Configuration" : "Edit Tracking Configuration");
		
		fConfig = config;
	}
	
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		area.setLayout(LayoutUtil.applyDialogDefaults(new GridLayout(), 2));
		
		fConfigComposite = createConfigComposite(area);
		fConfigComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		
		fConfigComposite.setInput(fConfig);
		
		applyDialogFont(area);
		
		return area;
	}
	
	@Override
	protected void addBindings(final DataBindingSupport db) {
		fConfigComposite.addBindings(db);
	}
	
	protected TrackingConfigurationComposite createConfigComposite(final Composite parent) {
		return new TrackingConfigurationComposite(parent);
	}
	
	protected TrackingConfigurationComposite getConfigComposite() {
		return fConfigComposite;
	}
	
}
