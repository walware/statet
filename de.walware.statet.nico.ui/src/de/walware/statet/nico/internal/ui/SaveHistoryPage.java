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

package de.walware.statet.nico.internal.ui;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;

import de.walware.eclipsecommons.ui.dialogs.ChooseResourceComposite;
import de.walware.eclipsecommons.ui.dialogs.Layouter;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.actions.AbstractHistoryPage;


/**
 *
 */
public class SaveHistoryPage extends AbstractHistoryPage {

	
	private static final String SETTINGS_APPEND = "save.append"; //$NON-NLS-1$
	private static final String SETTINGS_OVERWRITE = "save.overwrite"; //$NON-NLS-1$
	
	public boolean fAppendToFile;
	public boolean fOverwriteFile;
	
	private Button fAppendControl;
	private Button fOverwriteControl;
	
	
	public SaveHistoryPage(ToolProcess tool) {
		
		super("SaveHistoryPage", Messages.SaveHistoryPage_title, tool); //$NON-NLS-1$
		setDescription(NLS.bind(Messages.SaveHistoryPage_description, fTool.getToolLabel(false)));
	}
	
	protected ChooseResourceComposite createResourceComposite(Layouter layouter) {
		
		return new ChooseResourceComposite(layouter.fComposite, 0, 
				Messages.SaveHistoryPage_FileTask_label);
	}
	
	protected void addAdditionalContent1(Layouter parent) {
		
		Group group = parent.addGroup(Messages.SaveHistoryPage_Options_label);
		Layouter options = new Layouter(group, 1);
		fAppendControl = options.addCheckBox(Messages.SaveHistoryPage_AppendToFile_label);
		fAppendControl.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fAppendToFile = fAppendControl.getSelection();
				if (fAppendToFile) {
					fOverwriteFile = false;
					fOverwriteControl.setSelection(false);
				}
				updateMode();
			}
		});
		fOverwriteControl = options.addCheckBox(Messages.SaveHistoryPage_OverwriteExisting_label);
		fOverwriteControl.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fOverwriteFile = fOverwriteControl.getSelection();
				if (fOverwriteFile) {
					fAppendToFile = false;
					fAppendControl.setSelection(false);
				}
				updateMode();
			}
		});
	}
	
	protected void updateMode() {
		
		int mode = ChooseResourceComposite.MODE_EXISTING_ERROR;
		if (fAppendToFile || fOverwriteFile) {
			mode = ChooseResourceComposite.MODE_EXISTING_WARNING;
		}
		fLocationGroup.setMode(mode);
		validate();
	}
	
	protected void initFields() {
		
		super.initFields();
		IDialogSettings settings = getDialogSettings();

		fAppendToFile = settings.getBoolean(SETTINGS_APPEND);
		fAppendControl.setSelection(fAppendToFile);
		fOverwriteFile = settings.getBoolean(SETTINGS_OVERWRITE);
		fOverwriteControl.setSelection(fOverwriteFile);
		updateMode();
	}
	
	@Override
	public void saveSettings() {
		
		super.saveSettings();
		IDialogSettings settings = getDialogSettings();
		settings.put(SETTINGS_APPEND, fAppendToFile);
		settings.put(SETTINGS_OVERWRITE, fOverwriteFile);
	}
}
