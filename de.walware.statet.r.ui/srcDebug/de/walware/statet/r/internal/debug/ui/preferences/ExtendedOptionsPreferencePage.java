/*******************************************************************************
 * Copyright (c) 2008-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.preferences;

import static de.walware.statet.r.launching.RRunDebugPreferenceConstants.PREF_LOCAL_REGISTRY_AUTOSTART_ENABLED;
import static de.walware.statet.r.launching.RRunDebugPreferenceConstants.PREF_LOCAL_REGISTRY_PORT;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.IStatusChangeListener;
import de.walware.ecommons.databinding.NumberValidator;
import de.walware.ecommons.net.RMIUtil;
import de.walware.ecommons.net.RMIUtil.StopRule;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.ui.ConfigurationBlockPreferencePage;
import de.walware.ecommons.preferences.ui.ManagedConfigurationBlock;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.internal.ui.RUIPlugin;


/**
 * Preference page for advanced options
 *  - RMI
 */
public class ExtendedOptionsPreferencePage extends ConfigurationBlockPreferencePage<ExtendedOptionsConfigurationBlock> {
	
	
	public ExtendedOptionsPreferencePage() {
		setPreferenceStore(RUIPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.RIntegrationExt_description);
	}
	
	@Override
	protected ExtendedOptionsConfigurationBlock createConfigurationBlock() {
		return new ExtendedOptionsConfigurationBlock(createStatusChangedListener());
	}
	
}

class ExtendedOptionsConfigurationBlock extends ManagedConfigurationBlock {
	
	
	private Button fRegistryAutostartControl;
	private Text fRegistryPortControl;
	
	
	public ExtendedOptionsConfigurationBlock(final IStatusChangeListener statusListener) {
		super(null, statusListener);
	}
	
	
	@Override
	protected void createBlockArea(final Composite pageComposite) {
		final Map<Preference, String> prefs = new HashMap<Preference, String>();
		
		prefs.put(PREF_LOCAL_REGISTRY_AUTOSTART_ENABLED, null);
		prefs.put(PREF_LOCAL_REGISTRY_PORT, null);
		
		setupPreferenceManager(prefs);
		
		LayoutUtil.addSmallFiller(pageComposite, false);
		final Composite group = createRmiComponent(pageComposite);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		initBindings();
		updateControls();
	}
	
	private Composite createRmiComponent(final Composite parent) {
		Label label;
		GridData gd;
		final Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.RIntegrationExt_LocalRMI_label);
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 4));
		
		fRegistryAutostartControl = new Button(group, SWT.CHECK);
		fRegistryAutostartControl.setText(Messages.RIntegrationExt_LocalRMI_RegistryAutostart_label);
		fRegistryAutostartControl.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 4, 1));
		
		label = new Label(group, SWT.NONE);
		label.setText(Messages.RIntegrationExt_LocalRMI_RegistryPort_label+':');
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		fRegistryPortControl = new Text(group, SWT.BORDER);
		gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		gd.widthHint = LayoutUtil.hintWidth(fRegistryPortControl, 6);
		fRegistryPortControl.setLayoutData(gd);
		
		final Button start = new Button(group, SWT.PUSH);
		start.setText(Messages.RIntegrationExt_LocalRMI_RegistryAction_Start_label);
		gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd.widthHint = LayoutUtil.hintWidth(start);
		start.setLayoutData(gd);
		start.addSelectionListener(new SelectionAdapter() {
			 @Override
			public void widgetSelected(final SelectionEvent e) {
				final IStatus status = RMIUtil.INSTANCE.startSeparateRegistry(getPreferenceValue(PREF_LOCAL_REGISTRY_PORT), StopRule.IF_EMPTY);
				if (status.getSeverity() != IStatus.OK) {
					StatusManager.getManager().handle(status, StatusManager.LOG | StatusManager.SHOW);
				}
			}
		});
		final Button stop = new Button(group, SWT.PUSH);
		stop.setText(Messages.RIntegrationExt_LocalRMI_RegistryAction_Stop_label);
		gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd.widthHint = LayoutUtil.hintWidth(stop);
		stop.setLayoutData(gd);
		stop.addSelectionListener(new SelectionAdapter() {
			 @Override
			public void widgetSelected(final SelectionEvent e) {
				final IStatus status = RMIUtil.INSTANCE.stopSeparateRegistry(getPreferenceValue(PREF_LOCAL_REGISTRY_PORT));
				if (status.getSeverity() != IStatus.OK) {
					StatusManager.getManager().handle(status, StatusManager.LOG | StatusManager.SHOW);
				}
			}
		});
		
		return group;
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		dbc.bindValue(SWTObservables.observeSelection(fRegistryAutostartControl),
				createObservable(PREF_LOCAL_REGISTRY_AUTOSTART_ENABLED),
				null, null);
		dbc.bindValue(SWTObservables.observeText(fRegistryPortControl, SWT.Modify),
				createObservable(PREF_LOCAL_REGISTRY_PORT),
				new UpdateValueStrategy().setAfterGetValidator(new NumberValidator(1, 65535, Messages.RIntegrationExt_LocalRMI_RegistryPort_error_Invalid_message)),
				null);
	}
	
}
