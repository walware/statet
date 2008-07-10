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

package de.walware.statet.r.internal.debug.ui.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.ui.databinding.NumberValidator;
import de.walware.eclipsecommons.ui.dialogs.IStatusChangeListener;
import de.walware.eclipsecommons.ui.preferences.ConfigurationBlockPreferencePage;
import de.walware.eclipsecommons.ui.util.LayoutUtil;

import de.walware.statet.ext.ui.preferences.ManagedConfigurationBlock;

import de.walware.statet.r.internal.debug.ui.launchconfigs.RMIUtil;
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
	public void createContents(final Composite pageComposite, final IWorkbenchPreferenceContainer container,
			final IPreferenceStore preferenceStore) {
		super.createContents(pageComposite, container, preferenceStore);
		
		final Map<Preference, String> prefs = new HashMap<Preference, String>();
		
		prefs.put(RMIUtil.PREF_LOCAL_REGISTRY_AUTOSTART_ENABLED, null);
		prefs.put(RMIUtil.PREF_LOCAL_REGISTRY_PORT, null);
		
		setupPreferenceManager(container, prefs);
		
		LayoutUtil.addSmallFiller(pageComposite, false);
		final Composite group = createRmiComponent(pageComposite, container);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		initBindings();
		updateControls();
	}
	
	private Composite createRmiComponent(final Composite parent, final IWorkbenchPreferenceContainer container) {
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
				final IStatus status = RMIUtil.startRegistry(getPreferenceValue(RMIUtil.PREF_LOCAL_REGISTRY_PORT));
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
				final IStatus status = RMIUtil.stopRegistry(getPreferenceValue(RMIUtil.PREF_LOCAL_REGISTRY_PORT));
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
				createObservable(RMIUtil.PREF_LOCAL_REGISTRY_AUTOSTART_ENABLED),
				null, null);
		dbc.bindValue(SWTObservables.observeText(fRegistryPortControl, SWT.Modify),
				createObservable(RMIUtil.PREF_LOCAL_REGISTRY_PORT),
				new UpdateValueStrategy().setAfterGetValidator(new NumberValidator(1, 65535, Messages.RIntegrationExt_LocalRMI_RegistryPort_error_Invalid_message)),
				null);
	}
	
}
