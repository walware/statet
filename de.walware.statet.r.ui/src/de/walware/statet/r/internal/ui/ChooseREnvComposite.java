/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui;

import java.util.Set;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import de.walware.eclipsecommons.ui.util.LayoutUtil;

import de.walware.statet.base.ui.util.ISettingsChangedHandler;
import de.walware.statet.base.ui.util.SettingsUpdater;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnvManager;
import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.core.renv.REnvSetting;
import de.walware.statet.r.core.renv.REnvSetting.SettingsType;
import de.walware.statet.r.internal.debug.ui.preferences.REnvPreferencePage;


/**
 * Composite to choose a configured R Environment.
 */
public class ChooseREnvComposite extends Composite implements ISettingsChangedHandler {
	
	public static interface ChangeListener {
		public void settingChanged(ChooseREnvComposite source, String oldValue, String newValue);
	}
	
	private class CompositeObservable extends AbstractObservableValue implements ChangeListener {
		
		public CompositeObservable(Realm realm) {
			super(realm);
			ChooseREnvComposite.this.addChangeListener(CompositeObservable.this);
		}
		
		public void settingChanged(ChooseREnvComposite source, String oldValue, String newValue) {
			fireValueChange(Diffs.createValueDiff(oldValue, newValue));
		}
		
		@Override
		protected void doSetValue(Object value) {
			setEncodedSetting((String) value);
		}
		
		@Override
		protected Object doGetValue() {
			return getEncodedSetting();
		}
		
		public Object getValueType() {
			return String.class;
		}
		
		public ChooseREnvComposite getComposite() {
			return ChooseREnvComposite.this;
		}
	}
	
	private class ChooseREnvValidator implements IValidator {
		
		public IStatus validate(Object dummy) {
			if (fInvalidPreference) {
				return ValidationStatus.error(RUIMessages.ChooseREnv_error_InvalidPreferences_message);
			}
			if (fCurrentSetting == null) {
				return ValidationStatus.error(RUIMessages.ChooseREnv_error_IncompleteSelection_message);
			}
			REnvConfiguration config = REnvSetting.resolveREnv(REnvSetting.decodeType(fCurrentSetting, true));
			if (config == null || config.isDisposed()) {
				return ValidationStatus.error(RUIMessages.ChooseREnv_error_InvalidSelection_message);
			}
			return ValidationStatus.ok();
		}
		
	}
	
	private boolean fInvalidPreference;
	private String fCurrentSetting;
	private REnvConfiguration fCurrentSpecified;
	private ListenerList fListeners;
	
	private DataBindingContext fBindindContexts;
	private Binding fBindings;
	
	private Button fWorkbenchDefaultButton;
	private Text fWorkbenchLabel;
	private Button fSpecificButton;
	private Combo fSpecificCombo;
	private Button fConfigurationButton;
	
	
	public ChooseREnvComposite(Composite parent) {
		super(parent, SWT.NONE);
		fInvalidPreference = true;
		fListeners = new ListenerList();
		
		createControls();
		fWorkbenchDefaultButton.setSelection(true);
		initPreferences();
		updateState(false);
	}
	
	
	
	private void initPreferences() {
		new SettingsUpdater(this, fSpecificCombo, new String[] { IREnvManager.SETTINGS_GROUP_ID });
		loadREnvironments();
	}
	
	private void createControls() {
		Composite container = this;
		container.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 3));
		
		fWorkbenchDefaultButton = new Button(container, SWT.RADIO);
		fWorkbenchDefaultButton.setText(RUIMessages.ChooseREnv_WorkbenchDefault_label);
		fWorkbenchDefaultButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		fWorkbenchLabel = new Text(container, SWT.BORDER | SWT.LEFT | SWT.SINGLE | SWT.READ_ONLY);
		fWorkbenchLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		LayoutUtil.addGDDummy(container);
		
		fSpecificButton = new Button(container, SWT.RADIO);
		fSpecificButton.setText(RUIMessages.ChooseREnv_Selected_label);
		fSpecificButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		fSpecificCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		fSpecificCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		fConfigurationButton = new Button(container, SWT.PUSH);
		fConfigurationButton.setText(RUIMessages.ChooseREnv_Configure_label);
		fConfigurationButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		fWorkbenchDefaultButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateState(false);
			}
		});
		fSpecificCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				String name = getSpecifiedName();
				if (name != null) {
					fCurrentSpecified = RCore.getREnvManager().get(null, name);
					updateState(false);
				}
			}
		});
		fConfigurationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				org.eclipse.ui.dialogs.PreferencesUtil.createPreferenceDialogOn(getShell(),
						REnvPreferencePage.PREF_PAGE_ID, new String[] { REnvPreferencePage.PREF_PAGE_ID },
						null).open();
			}
		});
	}
	
	public boolean handleSettingsChanged(Set<String> groupIds, Object options) {
		loadREnvironments();
		return true;
	}
	
	private void loadREnvironments() {
		fInvalidPreference = true;
		IREnvManager manager = RCore.getREnvManager();
		manager.getReadLock().lock();
		try {
			// Workbench default
			REnvConfiguration defaultConfig = manager.getDefault();
			String[] newNames = manager.getNames();
			fWorkbenchLabel.setText(defaultConfig.getName());
			if (newNames.length > 0) {
				fInvalidPreference = false;
			}
			// Specifics
			fSpecificCombo.setItems(newNames);
			if (fCurrentSpecified != null) {
				if (!fCurrentSpecified.isDisposed()) {
					setSpecified(fCurrentSpecified);
				} else {
					setSpecified(manager.get(null, fCurrentSpecified.getName()));
				}
			}
		}
		finally {
			manager.getReadLock().unlock();
			updateState(true);
		}
	}
	
	public void setSetting(REnvSetting setting) {
		SettingsType type = (setting != null) ? setting.getType() : null;
		if (type == SettingsType.SPECIFIC) {
			fWorkbenchDefaultButton.setSelection(false);
			fSpecificButton.setSelection(true);
			setSpecified(RCore.getREnvManager().get(setting.getDetails()[0], setting.getDetails()[1]));
		}
		else if (type == SettingsType.WORKBENCH) {
			fWorkbenchDefaultButton.setSelection(true);
			fSpecificButton.setSelection(false);
		}
		else {
			fWorkbenchDefaultButton.setSelection(false);
			fSpecificButton.setSelection(false);
		}
		updateState(false);
	}
	
	public String getEncodedSetting() {
		return fCurrentSetting;
	}
	
	public void setEncodedSetting(String encodedSetting) {
		REnvSetting setting = REnvSetting.decodeType(encodedSetting, false);
		setSetting(setting);
	}
	
	private void setSpecified(REnvConfiguration config) {
		fCurrentSpecified = config;
		if (fCurrentSpecified != null) {
			fSpecificCombo.select(fSpecificCombo.indexOf(fCurrentSpecified.getName()));
		}
		else {
			fSpecificCombo.deselectAll();
		}
	}
	
	private String getSpecifiedName() {
		int idx = fSpecificCombo.getSelectionIndex();
		if (idx >= 0) {
			return fSpecificCombo.getItem(idx);
		}
		return null;
	}
	
	private void updateState(boolean force) {
		SettingsType type = getSettingsType();
		fWorkbenchLabel.setEnabled(type == SettingsType.WORKBENCH);
		fSpecificCombo.setEnabled(type == SettingsType.SPECIFIC);
		String oldSetting = fCurrentSetting;
		fCurrentSetting = REnvSetting.encodeREnv(type, fCurrentSpecified, false);
		if ((fCurrentSetting == null && oldSetting != null)
				|| (fCurrentSetting != null && !fCurrentSetting.equals(oldSetting)) ) {
			for (Object listener : fListeners.getListeners()) {
				((ChangeListener) listener).settingChanged(this, oldSetting, fCurrentSetting);
			}
		}
		else if (force) {
			checkBindings();
			if (fBindings != null) {
				fBindings.validateTargetToModel();
			}
		}
	}
	
	private void checkBindings() {
		if (fBindindContexts != null) {
			IObservableList bindings = fBindindContexts.getBindings();
			for (Object obj : bindings) {
				Binding binding = (Binding) obj;
				if (binding.getTarget() instanceof CompositeObservable) {
					if (((CompositeObservable) binding.getTarget()).getComposite() == this) {
						fBindings = binding;
						fBindindContexts = null;
						return;
					}
				}
			}
		}
	}
	
	
	public void addChangeListener(ChangeListener listener) {
		fListeners.add(listener);
	}
	
	public void removeChangeListener(ChangeListener listener) {
		fListeners.remove(listener);
	}
		
	public SettingsType getSettingsType() {
		if (fWorkbenchDefaultButton.getSelection()) {
			return SettingsType.WORKBENCH;
		}
		if (fSpecificButton.getSelection()) {
			return SettingsType.SPECIFIC;
		}
		throw new IllegalStateException();
	}
	
	/**
	 * Return a new Observable for the encoded setting of selected REnv.
	 * (So type is String)
	 */
	public IObservableValue createObservable(Realm realm) {
		return new CompositeObservable(realm);
	}
	
	public ChooseREnvValidator createValidator(DataBindingContext context) {
		fBindindContexts = context;
		return new ChooseREnvValidator();
	}
	
}
