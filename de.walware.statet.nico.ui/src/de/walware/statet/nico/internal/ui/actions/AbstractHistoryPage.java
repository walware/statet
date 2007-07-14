/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui.actions;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import de.walware.eclipsecommons.FileValidator;
import de.walware.eclipsecommons.ui.dialogs.ChooseResourceComposite;
import de.walware.eclipsecommons.ui.dialogs.Layouter;
import de.walware.eclipsecommons.ui.util.DialogUtil;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.Messages;
import de.walware.statet.nico.ui.util.ToolInfoGroup;


/**
 *
 */
public abstract class AbstractHistoryPage extends WizardPage {

	private static final String SETTINGS_HISTORY = "statet:location.history"; //$NON-NLS-1$

	protected ChooseResourceComposite fLocationGroup;
	private String fResourcePath;
	public IFile fResourceInWorkspace;
	public IFileStore fResourceInEFS;

	public String fEncoding = "UTF-8"; //$NON-NLS-1$
	
	protected ToolProcess fTool;
	protected boolean isIntialized = false;
	
	
	public AbstractHistoryPage(String pageName, String title, ToolProcess tool) {
		super(pageName);

		fTool = tool;
		setTitle(title);
	}
	
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		
    	GridLayout layout = new GridLayout();
    	Layouter layouter = new Layouter(new Composite(parent, SWT.NONE), layout);
		layouter.composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setControl(layouter.composite);
		
		createContents(layouter);
		
		Dialog.applyDialogFont(layouter.composite);
		initFields();
		isIntialized = true;
		validate();
		setErrorMessage(null);
		setMessage(null);
	}
	
	protected void createContents(Layouter contentLayouter) {
		fLocationGroup = createResourceComposite(contentLayouter);
		fLocationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fLocationGroup.setHistory(getDialogSettings().getArray(SETTINGS_HISTORY));
		fLocationGroup.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		addAdditionalContent1(contentLayouter);
		
		Group encodingGroup = contentLayouter.addGroup(Messages.LoadSaveHistoryPage_Encoding_label);
		Layouter encoding = new Layouter(encodingGroup, 1);
		encoding.addLabel("Not yet implemented, UTF-8 is used, if no BOM is detected."); //$NON-NLS-1$
		addAdditionalContent2(contentLayouter);

		contentLayouter.addSpaceGrabber();
		contentLayouter.add(new ToolInfoGroup(contentLayouter.composite,
				fTool).getControl());
	}
	
	protected abstract ChooseResourceComposite createResourceComposite(Layouter layouter);
	
	protected void addAdditionalContent1(Layouter layouter) {
	}
	protected void addAdditionalContent2(Layouter layouter) {
	}
	
	protected void initFields() {
	}
	
	protected void validate() {
		if (!isIntialized) {
			return;
		}
		FileValidator validator = fLocationGroup.getValidator();
		IStatus status = validator.getStatus();
		if (status != null && status.getSeverity() == IStatus.ERROR) {
			setMessage(status.getMessage(), IStatus.ERROR);
			setPageComplete(false);
			fResourceInWorkspace = null;
			fResourceInEFS = null;
			fResourcePath = null;
		}
		else {
			if (status != null && status.getSeverity() != IStatus.OK) {
				setMessage(status.getMessage(), status.getSeverity());
			}
			else {
				setMessage(null);
			}
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
		IDialogSettings settings = getDialogSettings();
		DialogUtil.saveHistorySettings(settings, SETTINGS_HISTORY, fResourcePath);
	}

}

