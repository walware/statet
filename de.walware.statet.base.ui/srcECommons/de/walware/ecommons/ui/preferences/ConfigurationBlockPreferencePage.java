/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.preferences;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.internal.ui.preferences.Messages;
import de.walware.ecommons.ui.dialogs.IStatusChangeListener;
import de.walware.ecommons.ui.dialogs.StatusInfo;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * Abstract preference page which is used to wrap a
 * Configuration Block
 */
public abstract class ConfigurationBlockPreferencePage<Block extends AbstractConfigurationBlock> extends PreferencePage
		implements IWorkbenchPreferencePage {
	
	
	protected Block fBlock;
	protected Composite fBlockControl;
	protected IStatus fBlockStatus;
	
	
	/**
	 * Creates a new preference page.
	 */
	public ConfigurationBlockPreferencePage() {
		fBlockStatus = new StatusInfo();
	}
	
	
	protected abstract Block createConfigurationBlock() throws CoreException;
	
	public void init(final IWorkbench workbench) {
		try {
			fBlock = createConfigurationBlock();
		} catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(Status.ERROR, StatetUIPlugin.PLUGIN_ID, -1,
					NLS.bind(Messages.ConfigurationPage_error_message, getTitle()), e),
					StatusManager.LOG | StatusManager.SHOW);
		}
	}
	
	@Override
	public void dispose() {
		fBlock.dispose();
		
		super.dispose();
	}
	
	@Override
	protected Control createContents(final Composite parent) {
		fBlockControl = new Composite(parent, SWT.NONE);
		fBlockControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		fBlockControl.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 1));
		fBlock.createContents(fBlockControl, (IWorkbenchPreferenceContainer) getContainer(), getPreferenceStore());
		
		Dialog.applyDialogFont(fBlockControl);
		
		final String helpContext = getHelpContext();
		if (helpContext != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(fBlockControl, helpContext);
		}
		
		return fBlockControl;
	}
	
	/**
	 * Overwrite to enable a help context
	 * 
	 * @return the help context for the page or <code>null</code>
	 */
	protected String getHelpContext() {
		return null;
 	}
	
	
	@Override
	public boolean performOk() {
		if (fBlock != null) {
			if (!fBlock.performOk())
				return false;
		}
		return super.performOk();
	}
	
	@Override
	public void performApply() {
		if (fBlock != null) {
			fBlock.performApply();
		}
	}
	
	@Override
	public void performDefaults() {
		if (fBlock != null) {
			fBlock.performDefaults();
		}
		super.performDefaults();
	}
	
	/**
	 * Returns a new status change listener
	 * @return The new listener
	 */
	protected IStatusChangeListener createStatusChangedListener() {
		return new IStatusChangeListener() {
			public void statusChanged(final IStatus status) {
				fBlockStatus = status;
				updateStatus();
			}
		};
	}
	
	protected void updateStatus() {
		updateStatus(fBlockStatus);
	}
	
	protected void updateStatus(final IStatus status) {
		setValid(!status.matches(IStatus.ERROR));
		StatusInfo.applyToStatusLine(this, status);
	}
	
}
