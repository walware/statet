/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.internal.actions;

import java.util.LinkedHashSet;

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

import de.walware.eclipsecommons.ui.dialogs.ChooseResourceComposite;
import de.walware.eclipsecommons.ui.dialogs.Layouter;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.internal.Messages;
import de.walware.statet.nico.ui.util.ToolInfoGroup;


/**
 *
 */
public abstract class AbstractHistoryPage extends WizardPage {

	private static final String SETTINGS_HISTORY = "location.history"; //$NON-NLS-1$
	private static final int HISTORY_MAX = 10;

	protected ChooseResourceComposite fLocationGroup;
	private String fResourcePath;
	public IFile fResourceInWorkspace;
	public IFileStore fResourceInEFS;

	public String fEncoding = "UTF-8"; //$NON-NLS-1$
	
	protected ToolProcess fTool;
	protected boolean isIntialized = false;
	
	
	public AbstractHistoryPage(String pageName, String title, ToolProcess tool) {
		
		super(pageName); //$NON-NLS-1$

		fTool = tool;
		setTitle(title);
	}
	
	public void createControl(Composite parent) {
		
		initializeDialogUnits(parent);
		
    	GridLayout layout = new GridLayout();
    	layout.marginHeight = 0;
    	Layouter layouter = new Layouter(new Composite(parent, SWT.NONE), layout);
		layouter.fComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setControl(layouter.fComposite);
		
		createContents(layouter);
		
		Dialog.applyDialogFont(layouter.fComposite);
		initFields();
		isIntialized = true;
		validate();
		setErrorMessage(null);
		setMessage(null);
	}
	
	protected void createContents(Layouter contentLayouter) {
		
		contentLayouter.addLabel(Messages.LoadSaveHistoryPage_File_label);
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

//		contentLayouter.addSmallFiller();
		contentLayouter.addSpaceGrabber();
		contentLayouter.add(new ToolInfoGroup(contentLayouter.fComposite,
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
		IStatus status = fLocationGroup.validate();
		if (status != null && status.getSeverity() == IStatus.ERROR) {
			setMessage(null);
			setErrorMessage(Messages.LoadSaveHistoryPage_error_File_prefix+status.getMessage());
			setPageComplete(false);
			fResourceInWorkspace = null;
			fResourceInEFS = null;
			fResourcePath = null;
		}
		else {
			setErrorMessage(null);
			if (status != null && status.getSeverity() != IStatus.OK) {
				setMessage(status.getMessage(), status.getSeverity());
			}
			else {
				setMessage(null);
			}
			setPageComplete(true);
			fResourceInWorkspace = fLocationGroup.getResourceAsWorkspaceResource();
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
		LinkedHashSet<String> history = new LinkedHashSet<String>(10);
		history.add(fResourcePath);
		String[] oldHistory = settings.getArray(SETTINGS_HISTORY);
		if (oldHistory != null) {
			for (int i = 0; i < oldHistory.length && history.size() < HISTORY_MAX; i++) {
				history.add(oldHistory[i]);
			}
		}
		settings.put(SETTINGS_HISTORY, history.toArray(new String[history.size()]));
	}

}

