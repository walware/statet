/*******************************************************************************
 * Copyright (c) 2006-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui.actions;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.io.FileValidator;
import de.walware.ecommons.ui.components.StatusInfo;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.util.ToolInfoGroup;


/**
 * 
 */
public abstract class AbstractHistoryPage extends WizardPage {
	
	private static final String SETTINGS_HISTORY = "statet:location.history"; //$NON-NLS-1$
	
	
	protected ResourceInputComposite fLocationGroup;
	private String fResourcePath;
	public IFile fResourceInWorkspace;
	public IFileStore fResourceInEFS;
	
	public String fEncoding = "UTF-8"; //$NON-NLS-1$
	
	protected ToolProcess fTool;
	protected boolean isIntialized = false;
	
	
	public AbstractHistoryPage(final String pageName, final String title, final ToolProcess tool) {
		super(pageName);
		
		fTool = tool;
		setTitle(title);
	}
	
	
	@Override
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(LayoutUtil.createContentGrid(1));
		
		fLocationGroup = createResourceInputComposite(composite);
		fLocationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fLocationGroup.setHistory(getDialogSettings().getArray(SETTINGS_HISTORY));
		fLocationGroup.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				validate();
			}
		});
		final Composite contentOptions = createContentOptions(composite);
		if (contentOptions != null) {
			contentOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		final Composite saveOptions = createSaveOptions(composite);
		if (saveOptions != null) {
			saveOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		
		LayoutUtil.addSmallFiller(composite, true);
		final ToolInfoGroup info = new ToolInfoGroup(composite, fTool);
		info.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Dialog.applyDialogFont(composite);
		setControl(composite);
		
		initFields();
		isIntialized = true;
		validate();
		setErrorMessage(null);
		setMessage(null);
	}
	
	protected abstract ResourceInputComposite createResourceInputComposite(Composite composite);
	
	protected Composite createContentOptions(final Composite parent) {
		return null;
	}
	
	protected Composite createSaveOptions(final Composite parent) {
		return null;
	}
	
	protected void initFields() {
	}
	
	protected void validate() {
		if (!isIntialized) {
			return;
		}
		final FileValidator validator = fLocationGroup.getValidator();
		IStatus status = validator.getStatus();
		if (status == null) {
			status = Status.OK_STATUS;
		}
		StatusInfo.applyToStatusLine(this, status);
		if (status.getSeverity() == IStatus.ERROR) {
			setPageComplete(false);
			fResourceInWorkspace = null;
			fResourceInEFS = null;
			fResourcePath = null;
		}
		else {
			setPageComplete(true);
			fResourceInWorkspace = (IFile) fLocationGroup.getResourceAsWorkspaceResource();
			fResourceInEFS = fLocationGroup.getResourceAsFileStore();
			fResourcePath = fLocationGroup.getResourceString();
		}
	}
	
	public Object getFile() {
		if (fResourceInWorkspace != null) {
			return fResourceInWorkspace;
		}
		return fResourceInEFS;
	}
	
	public void saveSettings() {
		final IDialogSettings settings = getDialogSettings();
		DialogUtil.saveHistorySettings(settings, SETTINGS_HISTORY, fResourcePath);
	}
	
}
