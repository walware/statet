/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.icu.text.Collator;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.FastList;
import de.walware.ecommons.preferences.ui.SettingsUpdater;
import de.walware.ecommons.ui.ISettingsChangedHandler;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.core.renv.IREnvManager;
import de.walware.statet.r.core.renv.REnvUtil;
import de.walware.statet.r.internal.debug.ui.preferences.REnvPreferencePage;


/**
 * Composite to choose a configured R Environment.
 */
public class REnvSelectionComposite extends Composite implements ISettingsChangedHandler {
	
	
	private static final Comparator<IREnv> RENV_COMPARATOR = new Comparator<IREnv>() {
		@Override
		public int compare(final IREnv o1, final IREnv o2) {
			return Collator.getInstance().compare(o1.getName(), o2.getName());
		}
	};
	
	
	public static interface ChangeListener {
		public void settingChanged(REnvSelectionComposite source, String oldValue, String newValue,
				IREnv newREnv);
	}
	
	private class CompositeObservable extends AbstractObservableValue implements ChangeListener {
		
		public CompositeObservable(final Realm realm) {
			super(realm);
			REnvSelectionComposite.this.addChangeListener(CompositeObservable.this);
		}
		
		@Override
		public void settingChanged(final REnvSelectionComposite source, final String oldValue,
				final String newValue, final IREnv newREnv) {
			fireValueChange(Diffs.createValueDiff(oldValue, newValue));
		}
		
		@Override
		protected void doSetValue(final Object value) {
			setEncodedSetting((String) value);
		}
		
		@Override
		protected Object doGetValue() {
			return getEncodedSetting();
		}
		
		@Override
		public Object getValueType() {
			return String.class;
		}
		
		public REnvSelectionComposite getComposite() {
			return REnvSelectionComposite.this;
		}
	}
	
	private class ChooseREnvValidator implements IValidator {
		
		@Override
		public IStatus validate(final Object dummy) {
			if (fInvalidPreference) {
				return ValidationStatus.error(RUIMessages.ChooseREnv_error_InvalidPreferences_message);
			}
			if (fCurrentREnv == null) {
				if (fEnableNone) {
					return ValidationStatus.ok();
				}
				return ValidationStatus.error(RUIMessages.ChooseREnv_error_IncompleteSelection_message);
			}
			final IREnv rEnv = fCurrentREnv.resolve();
			if (rEnv == null
					|| (fValidREnvs != null && !fValidREnvs.contains(rEnv))
					|| rEnv.getConfig() == null) {
				return ValidationStatus.error(RUIMessages.ChooseREnv_error_InvalidSelection_message);
			}
			return ValidationStatus.ok();
		}
		
	}
	
	
	private final boolean fEnableNone;
	
	private boolean fInvalidPreference;
	private List<IREnv> fValidREnvs;
	
	private IREnv fCurrentREnv;
	private String fCurrentEncoded;
	private IREnv fCurrentSpecified;
	private final FastList<ChangeListener> fListeners= new FastList<>(ChangeListener.class);
	
	private DataBindingContext fBindindContexts;
	private Binding fBindings;
	
	private Button fNoneButton;
	private Button fWorkbenchDefaultButton;
	private Text fWorkbenchLabel;
	private Button fSpecificButton;
	private Combo fSpecificCombo;
	private Button fConfigurationButton;
	
	
	public REnvSelectionComposite(final Composite parent) {
		this(parent, false);
	}
	
	public REnvSelectionComposite(final Composite parent, final boolean enableNone) {
		super(parent, SWT.NONE);
		fEnableNone = enableNone;
		
		fInvalidPreference = true;
		
		createControls();
		fWorkbenchDefaultButton.setSelection(true);
		initPreferences();
		updateState(true, false);
	}
	
	
	private void initPreferences() {
		new SettingsUpdater(this, fSpecificCombo, new String[] { IREnvManager.SETTINGS_GROUP_ID });
		loadREnvironments();
	}
	
	private void createControls() {
		final Composite container = this;
		container.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 3));
		
		if (fEnableNone) {
			fNoneButton = new Button(container, SWT.RADIO);
			fNoneButton.setText(RUIMessages.ChooseREnv_None_label);
			fNoneButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		}
		
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
		
		if (fEnableNone) {
			fNoneButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					if (fNoneButton.getSelection()) {
						fCurrentREnv = null;
						updateState(false, false);
					}
				}
			});
		}
		fWorkbenchDefaultButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (fWorkbenchDefaultButton.getSelection()) {
					fCurrentREnv = RCore.getREnvManager().getDefault();
					updateState(false, false);
				}
			}
		});
		fSpecificButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (fSpecificButton.getSelection()) {
					fCurrentREnv = fCurrentSpecified;
					updateState(false, false);
				}
			}
		});
		fSpecificCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				final String name = getSpecifiedName();
				if (name != null) {
					fCurrentREnv = RCore.getREnvManager().get(null, name);
					updateState(false, false);
				}
			}
		});
		fConfigurationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				org.eclipse.ui.dialogs.PreferencesUtil.createPreferenceDialogOn(getShell(),
						REnvPreferencePage.PREF_PAGE_ID, new String[] { REnvPreferencePage.PREF_PAGE_ID },
						null).open();
			}
		});
	}
	
	@Override
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		loadREnvironments();
	}
	
	private void loadREnvironments() {
		fInvalidPreference = true;
		final IREnvManager manager = RCore.getREnvManager();
		manager.getReadLock().lock();
		try {
			// Workbench default
			final IREnv defaultEnv = manager.getDefault();
			final List<IREnvConfiguration> list = manager.getConfigurations();
			fValidREnvs = getValidREnvs(list);
			Collections.sort(fValidREnvs, RENV_COMPARATOR);
			final String[] validNames = new String[fValidREnvs.size()];
			for (int i = 0; i < validNames.length; i++) {
				validNames[i] = fValidREnvs.get(i).getName();
			}
			fWorkbenchLabel.setText(defaultEnv.getName());
			if (!list.isEmpty()) {
				fInvalidPreference = false;
			}
			// Specifics
			fSpecificCombo.setItems(validNames);
			if (fCurrentSpecified != null) {
				final boolean current = (fCurrentREnv == fCurrentSpecified);
				fCurrentSpecified = manager.get(fCurrentSpecified.getId(), fCurrentSpecified.getName());
				if (current) {
					fCurrentREnv = fCurrentSpecified;
				}
			}
		}
		finally {
			manager.getReadLock().unlock();
			updateState(false, true);
		}
	}
	
	protected List<IREnv> getValidREnvs(final List<IREnvConfiguration> configurations) {
		final List<IREnv> list= new ArrayList<>(configurations.size());
		for (final IREnvConfiguration rEnvConfig : configurations) {
			if (isValid(rEnvConfig)) {
				list.add(rEnvConfig.getReference());
			}
		}
		return list;
	}
	
	protected boolean isValid(final IREnvConfiguration rEnvConfig) {
		return (!rEnvConfig.isDeleted());
	}
	
	public void setSetting(final IREnv rEnv) {
		fCurrentREnv = rEnv;
		updateState(true, false);
	}
	
	public String getEncodedSetting() {
		return fCurrentEncoded;
	}
	
	public void setEncodedSetting(final String encodedSetting) {
		setSetting(REnvUtil.decode(encodedSetting, RCore.getREnvManager()));
	}
	
	private String getSpecifiedName() {
		final int idx = fSpecificCombo.getSelectionIndex();
		if (idx >= 0) {
			return fSpecificCombo.getItem(idx);
		}
		return null;
	}
	
	public IREnv getSelection() {
		return fCurrentREnv;
	}
	
	private void updateState(final boolean updateSelection, final boolean force) {
		final boolean isWorkbench = (fCurrentREnv != null
				&& fCurrentREnv.getId().equals(IREnv.DEFAULT_WORKBENCH_ENV_ID) );
		final boolean isSpecific = (fCurrentREnv != null && !isWorkbench);
		if (updateSelection) {
			if (fNoneButton != null) {
				fNoneButton.setSelection(!isWorkbench && !isSpecific);
			}
			fWorkbenchDefaultButton.setSelection(isWorkbench);
			fSpecificButton.setSelection(isSpecific);
		}
		fWorkbenchLabel.setEnabled(fWorkbenchDefaultButton.getSelection());
		fSpecificCombo.setEnabled(fSpecificButton.getSelection());
		
		if (isSpecific) {
			fCurrentSpecified = fCurrentREnv;
		}
		if (fCurrentSpecified != null) {
			fSpecificCombo.select(fSpecificCombo.indexOf(fCurrentSpecified.getName()));
		}
		else {
			fSpecificCombo.deselectAll();
		}
		
		final String oldEncoded = fCurrentEncoded;
		fCurrentEncoded = REnvUtil.encode(fCurrentREnv);
		if (!((fCurrentEncoded != null) ? fCurrentEncoded.equals(oldEncoded) : (null == oldEncoded))) {
			for (final ChangeListener listener : fListeners.toArray()) {
				listener.settingChanged(this, oldEncoded, fCurrentEncoded, fCurrentREnv);
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
			final IObservableList bindings = fBindindContexts.getBindings();
			for (final Object obj : bindings) {
				final Binding binding = (Binding) obj;
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
	
	
	public void addChangeListener(final ChangeListener listener) {
		fListeners.add(listener);
	}
	
	public void removeChangeListener(final ChangeListener listener) {
		fListeners.remove(listener);
	}
		
	/**
	 * Return a new Observable for the encoded setting of selected REnv.
	 * (So type is String)
	 */
	public IObservableValue createObservable(final Realm realm) {
		return new CompositeObservable(realm);
	}
	
	public ChooseREnvValidator createValidator(final DataBindingContext context) {
		fBindindContexts = context;
		return new ChooseREnvValidator();
	}
	
}
