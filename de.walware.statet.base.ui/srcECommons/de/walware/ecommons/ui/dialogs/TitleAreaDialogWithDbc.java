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

package de.walware.ecommons.ui.dialogs;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * Dialog with connected databinding context.
 * <p><ul>
 *   <li>use {@link #initBindings()} to create the context</li>
 *   <li>overwrite {@link #addBindings(DataBindingContext, Realm)} to register the bindings</li>
 * </ul>
 */
public abstract class TitleAreaDialogWithDbc extends TitleAreaDialog {
	
	
	private DataBindingContext fDbc;
	private AggregateValidationStatus fAggregateStatus;
	private IStatus fCurrentStatus;
	private boolean fInitializing = false; // currently not used
	
	private IDialogSettings fDialogSettings;
	
	
	public TitleAreaDialogWithDbc(final Shell shell) {
		super(shell);
	}
	
	
	protected void setDialogSettings(final IDialogSettings settings) {
		fDialogSettings = settings;
	}
	
	protected IDialogSettings getDialogSettings() {
		return fDialogSettings;
	}
	
	protected void updateDialogState() {
		if (!fInitializing) {
//			setErrorMessage(null);
			switch (fCurrentStatus.getSeverity()) {
			case IStatus.ERROR:
				setMessage(fCurrentStatus.getMessage(), IMessageProvider.ERROR);
				break;
			case IStatus.WARNING:
				setMessage(fCurrentStatus.getMessage(), IMessageProvider.WARNING);
				break;
			case IStatus.INFO:
				setMessage(fCurrentStatus.getMessage(), IMessageProvider.INFORMATION);
				break;
			case IStatus.OK:
				setMessage(null);
				break;
			default:
				break;
			}
		}
	}
	
	protected void initBindings() {
		final Realm realm = Realm.getDefault();
		fDbc = new DataBindingContext(realm);
		
		addBindings(fDbc, realm);
		
		fAggregateStatus = new AggregateValidationStatus(fDbc, AggregateValidationStatus.MAX_SEVERITY);
		fAggregateStatus.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(final ValueChangeEvent event) {
				fCurrentStatus = (IStatus) event.diff.getNewValue();
				updateDialogState();
			}
		});
		fCurrentStatus = ValidationStatus.ok();
		
//		new DirtyTracker(fDbc) {
//			@Override
//			public void handleChange() {
//				setDirty(true);
//				updateDialogState();
//			}
//		};
	}
	
	/**
	 * @param dbc
	 * @param realm
	 */
	protected abstract void addBindings(DataBindingContext dbc, Realm realm);
	
	@Override
	public boolean close() {
		try {
			disposeBindings();
		}
		catch (final Throwable e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, StatetUIPlugin.PLUGIN_ID,
					"An error occurend when dispose databinding", e)); //$NON-NLS-1$
		}
		return super.close();
	}
	
	private void disposeBindings() {
		if (fAggregateStatus != null) {
			fAggregateStatus.dispose();
			fAggregateStatus = null;
		}
		if (fDbc != null) {
			fDbc.dispose();
			fDbc = null;
		}
	}
	
}
