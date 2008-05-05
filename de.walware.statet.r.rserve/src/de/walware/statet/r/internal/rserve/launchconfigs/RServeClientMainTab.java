/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * v2.1 or newer, which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.rserve.launchconfigs;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import de.walware.eclipsecommons.ui.databinding.LaunchConfigTabWithDbc;
import de.walware.eclipsecommons.ui.databinding.NumberValidator;
import de.walware.eclipsecommons.ui.dialogs.Layouter;

import de.walware.statet.base.ui.StatetImages;


/**
 * Tab to configure the connection to the RServe-server.
 */
public class RServeClientMainTab extends LaunchConfigTabWithDbc {
	
	
	private ConnectionConfig fConnectionConfig;
	
	private Text fServerAddress;
	private Text fServerPort;
	
	
	public RServeClientMainTab() {
		super();
		fConnectionConfig = new ConnectionConfig();
	}
	
	
	public String getName() {
		return "&Main";
	}
	
	@Override
	public Image getImage() {
		return StatetImages.getImage(StatetImages.LAUNCHCONFIG_MAIN);
	}
	
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Layouter main = new Layouter(mainComposite, new GridLayout());
		Layouter layouter = new Layouter(main.addGroup("Connection:"), 2);
		
		fServerAddress = layouter.addLabeledTextControl("Server Address:");
		fServerPort = layouter.addLabeledTextControl("Server Port:");
		
		Dialog.applyDialogFont(parent);
		initBindings();
	}
	
	@Override
	protected void addBindings(DataBindingContext dbc, Realm realm) {
		dbc.bindValue(SWTObservables.observeText(fServerAddress, SWT.Modify),
				BeansObservables.observeValue(fConnectionConfig, ConnectionConfig.PROP_SERVERADDRESS),
				null, null);
		dbc.bindValue(SWTObservables.observeText(fServerPort, SWT.Modify),
				BeansObservables.observeValue(fConnectionConfig, ConnectionConfig.PROP_SERVERPORT),
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE)
						.setAfterGetValidator(new NumberValidator(0, 65535, "The valid port range is 0-65535.")),
				null);
	}
	
	
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		ConnectionConfig.writeDefaultsTo(configuration);
	}
	
	@Override
	protected void doInitialize(ILaunchConfiguration configuration) {
		fConnectionConfig.load(configuration);
	}
	
	@Override
	protected void doSave(ILaunchConfigurationWorkingCopy configuration) {
		fConnectionConfig.save(configuration);
	}
	
}
