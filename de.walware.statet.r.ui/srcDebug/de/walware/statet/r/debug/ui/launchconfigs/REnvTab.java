/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.debug.ui.launchconfigs;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import de.walware.ecommons.FileValidator;
import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.ui.databinding.LaunchConfigTabWithDbc;
import de.walware.ecommons.ui.dialogs.ChooseResourceComposite;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.MessageUtil;

import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.core.renv.REnvSetting;
import de.walware.statet.r.core.renv.REnvSetting.SettingsType;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.internal.ui.ChooseREnvComposite;
import de.walware.statet.r.ui.RUI;


/**
 * 
 */
public class REnvTab extends LaunchConfigTabWithDbc {
	
	
	public static final String NS = "de.walware.statet.r.debug/REnv/"; //$NON-NLS-1$
	private static final String NEW_RENV_ID = NS + "code"; //$NON-NLS-1$
	
	public static REnvSetting readREnv(final ILaunchConfiguration configuration) throws CoreException {
		final String setting = configuration.getAttribute(RLaunchConfigurations.ATTR_RENV_SETTING, (String) null);
		return REnvSetting.decodeType(setting, false);
	}
	
	/**
	 * Reads the setting from the configuration, resolves the REnvironment and validates the configuration.
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public static REnvConfiguration getREnv(final ILaunchConfiguration configuration)
			throws CoreException {
		final REnvConfiguration config = REnvSetting.resolveREnv(readREnv(configuration));
		if (config == null) {
			throw new CoreException(new Status(Status.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					RLaunchingMessages.REnv_Runtime_error_CouldNotFound_message, null));
		}
		final IStatus status = config.validate();
		if (status.getSeverity() == IStatus.ERROR) {
			throw new CoreException(new Status(Status.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					RLaunchingMessages.REnv_Runtime_error_Invalid_message+' '+status.getMessage(), null));
		}
		return config;
	}
	
	public static String readWorkingDirectory(final ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(RLaunchConfigurations.ATTR_WORKING_DIRECTORY, ""); //$NON-NLS-1$
	}
	
	/**
	 * Reads the setting from the configuration, resolves the path and validates the directory.
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public static IFileStore getWorkingDirectory(final ILaunchConfiguration configuration) throws CoreException {
		return getWorkingDirectoryValidator(configuration, true).getFileStore();
	}
	
	public static FileValidator getWorkingDirectoryValidator(final ILaunchConfiguration configuration, final boolean validate) throws CoreException {
		String path = readWorkingDirectory(configuration);
		if (path == null || path.trim().length() == 0) {
			path = System.getProperty("user.dir"); //$NON-NLS-1$
		}
		final FileValidator validator = new FileValidator(true);
		validator.setOnDirectory(IStatus.OK);
		validator.setOnFile(IStatus.ERROR);
		validator.setResourceLabel(MessageUtil.removeMnemonics(RLaunchingMessages.REnv_Tab_WorkingDir_label));
		if (validate && validator.validate(path).getSeverity() == IStatus.ERROR) {
			throw new CoreException(validator.getStatus());
		}
		else {
			validator.setExplicit(path);
		}
		return validator;
	}
	
	
/*-- --*/
	
	
	private ChooseREnvComposite fREnvControl;
	private ChooseResourceComposite fWorkingDirectoryControl;
	
	private WritableValue fREnvSettingValue;
	private WritableValue fWorkingDirectoryValue;
	private Binding fREnvBinding;
	
	
	public REnvTab() {
		super();
	}
	
	
	public String getName() {
		return RLaunchingMessages.REnv_Tab_title;
	}
	
	@Override
	public Image getImage() {
		return RUI.getImage(RUI.IMG_OBJ_R_ENVIRONMENT);
	}
	
	public void createControl(final Composite parent) {
		final Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(GridLayoutFactory.swtDefaults().create());
		
		Group group;
		group = new Group(mainComposite, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setText(RLaunchingMessages.REnv_Tab_REnvConfig_label+':');
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
		fREnvControl = new ChooseREnvComposite(group);
		fREnvControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		fWorkingDirectoryControl = new ChooseResourceComposite(mainComposite,
				ChooseResourceComposite.STYLE_GROUP | ChooseResourceComposite.STYLE_TEXT,
				ChooseResourceComposite.MODE_DIRECTORY | ChooseResourceComposite.MODE_OPEN,
				RLaunchingMessages.REnv_Tab_WorkingDir_label);
		fWorkingDirectoryControl.showInsertVariable(true);
		fWorkingDirectoryControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Dialog.applyDialogFont(parent);
		initBindings();
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		fREnvSettingValue = new WritableValue(realm, null, String.class);
		fWorkingDirectoryValue = new WritableValue(realm, null, String.class);
		
		fREnvBinding = dbc.bindValue(fREnvControl.createObservable(realm), fREnvSettingValue,
				new UpdateValueStrategy().setAfterGetValidator(
						new SavableErrorValidator(fREnvControl.createValidator(dbc))),
				null);
		fWorkingDirectoryControl.getValidator().setOnEmpty(IStatus.OK);
		dbc.bindValue(fWorkingDirectoryControl.createObservable(), fWorkingDirectoryValue,
				new UpdateValueStrategy().setAfterGetValidator(
						new SavableErrorValidator(fWorkingDirectoryControl.getValidator())),
				null);
	}
	
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		final String code = REnvSetting.encodeREnv(SettingsType.WORKBENCH, null, false);
		configuration.setAttribute(RLaunchConfigurations.ATTR_RENV_SETTING, code);
		configuration.setAttribute(NEW_RENV_ID, code);
		configuration.setAttribute(RLaunchConfigurations.ATTR_WORKING_DIRECTORY, ""); //$NON-NLS-1$
	}
	
	@Override
	protected void doInitialize(final ILaunchConfiguration configuration) {
		try {
			fREnvSettingValue.setValue(configuration.getAttribute(RLaunchConfigurations.ATTR_RENV_SETTING, (String) null));
		} catch (final CoreException e) {
			fREnvSettingValue.setValue(null);
			logReadingError(e);
		}
		
		try {
			fWorkingDirectoryValue.setValue(configuration.getAttribute(RLaunchConfigurations.ATTR_WORKING_DIRECTORY, "")); //$NON-NLS-1$
		} catch (final CoreException e) {
			fWorkingDirectoryValue.setValue(null);
			logReadingError(e);
		}
	}
	
	@Override
	protected void doSave(final ILaunchConfigurationWorkingCopy configuration) {
		final String code = (String) fREnvSettingValue.getValue();
		configuration.setAttribute(RLaunchConfigurations.ATTR_RENV_SETTING, code);
		configuration.setAttribute(NEW_RENV_ID, code);
		configuration.setAttribute(RLaunchConfigurations.ATTR_WORKING_DIRECTORY, (String) fWorkingDirectoryValue.getValue());
	}
	
	
	public REnvConfiguration getSelectedEnv() {
		if (fREnvBinding != null) {
			final IStatus validationStatus = (IStatus) fREnvBinding.getValidationStatus().getValue();
			if (validationStatus != null && validationStatus.getSeverity() < IStatus.WARNING) { // note: warning means error which can be saved
				final REnvSetting setting = REnvSetting.decodeType((String) fREnvSettingValue.getValue(), false);
				if (setting != null) {
					return REnvSetting.resolveREnv(setting);
				}
			}
		}
		return null;
	}
	
}
