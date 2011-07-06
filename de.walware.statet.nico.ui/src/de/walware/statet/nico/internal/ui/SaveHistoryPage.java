/*******************************************************************************
 * Copyright (c) 2006-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui;

import java.util.EnumSet;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.actions.AbstractHistoryPage;
import de.walware.statet.nico.ui.util.SubmitTypeSelectionComposite;


public class SaveHistoryPage extends AbstractHistoryPage {
	
	
	private static final String SETTINGS_APPEND = "save.append"; //$NON-NLS-1$
	private static final String SETTINGS_OVERWRITE = "save.overwrite"; //$NON-NLS-1$
	private static final String SETTINGS_SOURCE = "source.include"; //$NON-NLS-1$
	
	public boolean fAppendToFile;
	public boolean fOverwriteFile;
	
	private Button fAppendControl;
	private Button fOverwriteControl;
	private SubmitTypeSelectionComposite fSourceControl;
	
	
	public SaveHistoryPage(final ToolProcess tool) {
		super("SaveHistoryPage", Messages.SaveHistoryPage_title, tool); //$NON-NLS-1$
		setDescription(NLS.bind(Messages.SaveHistoryPage_description, fTool.getLabel(ITool.DEFAULT_LABEL)));
	}
	
	@Override
	protected ResourceInputComposite createResourceInputComposite(final Composite composite) {
		return new ResourceInputComposite(composite, 
				ResourceInputComposite.STYLE_COMBO,
				ResourceInputComposite.MODE_FILE | ResourceInputComposite.MODE_SAVE, 
				Messages.LoadSaveHistoryPage_File_label);
	}
	
	@Override
	protected Composite createContentOptions(final Composite parent) {
		final Group composite = new Group(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
		composite.setText("Content:");
		
		final Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		label.setText("Include &Commands From:");
		fSourceControl = new SubmitTypeSelectionComposite(composite);
		fSourceControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		return composite;
	}
	
	@Override
	protected Composite createSaveOptions(final Composite parent) {
		final Group composite = new Group(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 1));
		composite.setText(Messages.SaveHistoryPage_Options_label);
		
		fAppendControl = new Button(composite, SWT.CHECK);
		fAppendControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fAppendControl.setText(Messages.SaveHistoryPage_AppendToFile_label);
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
		fOverwriteControl = new Button(composite, SWT.CHECK);
		fOverwriteControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fOverwriteControl.setText(Messages.SaveHistoryPage_OverwriteExisting_label);
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
		return composite;
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
		final String sourceSetting = settings.get(SETTINGS_SOURCE);
		if (sourceSetting != null) {
			fSourceControl.setSelection(SubmitTypeSelectionComposite.SOURCE_ENCODER.store2Usage(sourceSetting));
		}
		else {
			fSourceControl.setSelection(EnumSet.range(SubmitType.CONSOLE, SubmitType.TOOLS));
		}
		updateMode();
	}
	
	@Override
	public void saveSettings() {
		super.saveSettings();
		final IDialogSettings settings = getDialogSettings();
		settings.put(SETTINGS_APPEND, fAppendToFile);
		settings.put(SETTINGS_OVERWRITE, fOverwriteFile);
		settings.put(SETTINGS_SOURCE, SubmitTypeSelectionComposite.SOURCE_ENCODER.usage2Store(fSourceControl.getSelection()));
	}
	
	
	public Set<SubmitType> getContentSubmitTypes() {
		return fSourceControl.getSelection();
	}
	
}
