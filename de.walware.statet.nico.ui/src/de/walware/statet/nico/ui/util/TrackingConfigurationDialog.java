/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import de.walware.ecommons.ui.dialogs.DatabindingSupport;
import de.walware.ecommons.ui.dialogs.ExtStatusDialog;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.nico.core.util.TrackingConfiguration;


public class TrackingConfigurationDialog extends ExtStatusDialog {
	
	
	private TrackingConfigurationComposite fConfigComposite;
	
	private TrackingConfiguration fConfig;
	
	
	public TrackingConfigurationDialog(final Shell parent, final TrackingConfiguration config, final boolean isNew) {
		super(parent);
		setTitle(isNew ? "New Tracking Configuration" : "Edit Tracking Configuration");
		
		fConfig = config;
	}
	
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite dialogArea = new Composite(parent, SWT.NONE);
		dialogArea.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		dialogArea.setLayout(LayoutUtil.applyDialogDefaults(new GridLayout(), 2));
		
		fConfigComposite = createConfigComposite(dialogArea);
		fConfigComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		
		fConfigComposite.setInput(fConfig);
		
		final DatabindingSupport databinding = new DatabindingSupport(dialogArea);
		fConfigComposite.addBindings(databinding.getContext(), databinding.getRealm());
		databinding.installStatusListener(new StatusUpdater());
		
		return dialogArea;
	}
	
	protected TrackingConfigurationComposite createConfigComposite(final Composite parent) {
		return new TrackingConfigurationComposite(parent);
	}
	
	protected TrackingConfigurationComposite getConfigComposite() {
		return fConfigComposite;
	}
	
}
