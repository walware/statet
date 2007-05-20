/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.preferences;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import de.walware.eclipsecommons.ui.dialogs.IStatusChangeListener;
import de.walware.eclipsecommons.ui.dialogs.Layouter;
import de.walware.eclipsecommons.ui.dialogs.StatusUtil;

import de.walware.statet.base.ui.util.ExceptionHandler;


/**
 * Abstract preference page which is used to wrap a
 * Configuration Block
 */
public abstract class ConfigurationBlockPreferencePage<Block extends AbstractConfigurationBlock> extends PreferencePage 
		implements IWorkbenchPreferencePage {
	
	
	protected Block fBlock;
	protected Control fBlockControl;
	protected IStatus fBlockStatus;
	

	/**
	 * Creates a new preference page.
	 */
	public ConfigurationBlockPreferencePage() {
	}
	
	protected abstract Block createConfigurationBlock() throws CoreException;
	

	public void init(IWorkbench workbench) {
		try {
			fBlock = createConfigurationBlock();
		} catch (CoreException e) {
			ExceptionHandler.handle(e, getShell(), "Error occured when initializing the configuration block for '" + getTitle() + "').");
		}
	}

	public void dispose() {
		fBlock.dispose();

		super.dispose();
	}

	protected Control createContents(Composite parent) {
		Layouter layouter = new Layouter(new Composite(parent, SWT.NONE), 1);
		fBlockControl = layouter.composite;
			
		fBlock.createContents(layouter.composite, (IWorkbenchPreferenceContainer) getContainer(), getPreferenceStore());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		layouter.composite.setLayoutData(data);
		
		Dialog.applyDialogFont(layouter.composite);
		
		return layouter.composite;
	}
	
	public boolean performOk() {
		if (fBlock != null) {
			if (!fBlock.performOk())
				return false;
		}
		return super.performOk();
	}
	
	public void performApply() {
		if (fBlock != null) {
			fBlock.performApply();
		}
	}

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
			public void statusChanged(IStatus status) {
				fBlockStatus = status;
				updateStatus();
			}
		};		
	}

	protected void updateStatus() {
		updateStatus(fBlockStatus);
	}
	
	protected void updateStatus(IStatus status) {
		setValid(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);
	}

}
