/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;

import de.walware.ecommons.ui.dialogs.Layouter;
import de.walware.ecommons.ui.workbench.ChooseResourceComposite;

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
	
	
	public SaveHistoryPage(final ToolProcess tool) {
		super("SaveHistoryPage", Messages.SaveHistoryPage_title, tool); //$NON-NLS-1$
		setDescription(NLS.bind(Messages.SaveHistoryPage_description, fTool.getToolLabel(false)));
	}
	
	@Override
	protected ChooseResourceComposite createResourceComposite(final Layouter layouter) {
		return new ChooseResourceComposite(layouter.composite, 
				ChooseResourceComposite.STYLE_COMBO,
				ChooseResourceComposite.MODE_FILE | ChooseResourceComposite.MODE_SAVE, 
				Messages.LoadSaveHistoryPage_File_label);
	}
	
	@Override
	protected void addAdditionalContent1(final Layouter parent) {
		final Group group = parent.addGroup(Messages.SaveHistoryPage_Options_label);
		final Layouter options = new Layouter(group, 1);
		fAppendControl = options.addCheckBox(Messages.SaveHistoryPage_AppendToFile_label);
		fAppendControl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
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
			@Override
			public void widgetSelected(final SelectionEvent e) {
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
		int severity;
		if (fAppendToFile) {
			severity = IStatus.INFO;
		}
		else if (fOverwriteFile) {
			severity = IStatus.WARNING;
		}
		else {
			severity = IStatus.ERROR;
		}
		fLocationGroup.getValidator().setOnExisting(severity);
		validate();
	}
	
	@Override
	protected void initFields() {
		super.initFields();
		final IDialogSettings settings = getDialogSettings();
		
		fAppendToFile = settings.getBoolean(SETTINGS_APPEND);
		fAppendControl.setSelection(fAppendToFile);
		fOverwriteFile = settings.getBoolean(SETTINGS_OVERWRITE);
		fOverwriteControl.setSelection(fOverwriteFile);
		updateMode();
	}
	
	@Override
	public void saveSettings() {
		super.saveSettings();
		final IDialogSettings settings = getDialogSettings();
		settings.put(SETTINGS_APPEND, fAppendToFile);
		settings.put(SETTINGS_OVERWRITE, fOverwriteFile);
	}
	
}
