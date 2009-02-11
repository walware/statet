/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.databinding;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ICommonStatusConstants;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * Abstract LaunchConfigurationTab with support of a DataBindingContext.
 * You have to implement:
 *  - {@link #addBindings(DataBindingContext, Realm)} (add binding to the context)
 *  - {@link #doInitialize(ILaunchConfiguration)} (load values from config to the model)
 *  - {@link #doSave(ILaunchConfigurationWorkingCopy)} (save values from model to config)
 * 
 * Validation status with severity WARNING are handled like errors, but can be saved.
 */
public abstract class LaunchConfigTabWithDbc extends AbstractLaunchConfigurationTab {
	
	
	protected class SavableErrorValidator implements IValidator {
		
		private IValidator fWrappedValidator;
		
		public SavableErrorValidator(final IValidator validator) {
			assert (validator != null);
			fWrappedValidator = validator;
		}
		
		public IStatus validate(final Object value) {
			final IStatus status = fWrappedValidator.validate(value);
			if (status != null) {
				switch (status.getSeverity()) {
				case IStatus.ERROR:
					return ValidationStatus.warning(status.getMessage());
				case IStatus.WARNING:
					return ValidationStatus.info(status.getMessage());
				}
			}
			return status;
		}
	}
	
	
	private DataBindingContext fDbc;
	private AggregateValidationStatus fAggregateStatus;
	private IStatus fCurrentStatus;
	private boolean fInitializing;
	
	
	protected LaunchConfigTabWithDbc() {
	}
	
	public String getValidationErrorAttr() {
		return getId()+"/validation.hasError"; //$NON-NLS-1$
	}
	
	protected void updateDialogState() {
		if (!isInitializing()) {
			String message = null;
			String errorMessage = null;
			switch (fCurrentStatus.getSeverity()) {
			case IStatus.ERROR:
				errorMessage = fCurrentStatus.getMessage();
				break;
			case IStatus.WARNING:
				errorMessage = fCurrentStatus.getMessage();
				break;
			case IStatus.INFO:
				message = fCurrentStatus.getMessage();
				break;
			default:
				break;
			}
			setMessage(message);
			setErrorMessage(errorMessage);
			updateLaunchConfigurationDialog();
		}
	}
	
	protected void initBindings() {
		final Realm realm = Realm.getDefault();
		fDbc = new DataBindingContext(realm);
		
		addBindings(fDbc, realm);
		
		fAggregateStatus = new AggregateValidationStatus(fDbc.getBindings(),
				AggregateValidationStatus.MAX_SEVERITY);
		fAggregateStatus.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(final ValueChangeEvent event) {
				fCurrentStatus = (IStatus) event.diff.getNewValue();
				updateDialogState();
			}
		});
		fCurrentStatus = ValidationStatus.ok();
		
		new DirtyTracker(fDbc) {
			@Override
			public void handleChange() {
				setDirty(true);
				updateDialogState();
			}
		};
	}
	
	protected DataBindingContext getDataBindingContext() {
		return fDbc;
	}
	
	/**
	 * @param dbc
	 * @param realm
	 */
	protected abstract void addBindings(DataBindingContext dbc, Realm realm);
	
	@Override
	public void dispose() {
		super.dispose();
		
		if (fAggregateStatus != null) {
			fAggregateStatus.dispose();
			fAggregateStatus = null;
		}
		if (fDbc != null) {
			fDbc.dispose();
			fDbc = null;
		}
	}
	
	protected void logReadingError(final CoreException e) {
		StatetUIPlugin.log(new Status(Status.ERROR, StatetUIPlugin.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
				NLS.bind("An error occurred while reading launch configuration (name: ''{0}'', id: ''{1}'')", getName(), getId()), e)); //$NON-NLS-1$
	}
	
	public void initializeFrom(final ILaunchConfiguration configuration) {
		fInitializing = true;
		doInitialize(configuration);
		setDirty(false);
		for (final Object obj : fDbc.getBindings()) {
			((Binding) obj).validateTargetToModel();
		}
		fCurrentStatus = (IStatus) fAggregateStatus.getValue();
		fInitializing = false;
		updateDialogState();
	}
	
	@Override
	public void activated(final ILaunchConfigurationWorkingCopy workingCopy) {
		updateDialogState();
	}
	
	@Override
	public void deactivated(final ILaunchConfigurationWorkingCopy workingCopy) {
	}
	
	public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
		if (!canSave()) {
			configuration.setAttribute(getValidationErrorAttr(), true); // To enable the revert button
			return;
		}
		configuration.setAttribute(getValidationErrorAttr(), (String) null);
		if (isDirty()) {
			doSave(configuration);
			setDirty(false);
		}
	}
	
	protected final boolean isInitializing() {
		return fInitializing;
	}
	
	protected abstract void doInitialize(ILaunchConfiguration configuration);
	protected abstract void doSave(ILaunchConfigurationWorkingCopy configuration);
	
	@Override
	public boolean isValid(final ILaunchConfiguration launchConfig) {
		return (fCurrentStatus.getSeverity() < IStatus.WARNING);
	}
	
	@Override
	public boolean canSave() {
		return (fCurrentStatus.getSeverity() < IStatus.ERROR);
	}
	
}
