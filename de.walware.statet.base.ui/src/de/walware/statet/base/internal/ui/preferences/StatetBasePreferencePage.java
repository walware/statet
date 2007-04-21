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

package de.walware.statet.base.internal.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


public class StatetBasePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	
	public StatetBasePreferencePage() {
		
		super(GRID);
		
		setPreferenceStore(StatetUIPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.StatetBase_description);
	}

	
	/*
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	public void createFieldEditors() {
//		addField(new DirectoryFieldEditor(PreferenceConstants.R_DIRECTORY, 
//				NicoMessages.getString(I.RES, "RootPage.rDirectory"), getFieldEditorParent()));
//
//		Label label = new Label(getFieldEditorParent(), SWT.LEFT);
//		label.setText(NicoMessages.getString(I.RES, "RootPage.rDirectory.info"));
//		GridData gd = new GridData();
//		gd.horizontalSpan = 2;
//		label.setLayoutData(gd);

//		addField(
//			new BooleanFieldEditor(
//				P_BOOLEAN,
//				"&An example of a boolean preference",
//				getFieldEditorParent()));
//
//		addField(new RadioGroupFieldEditor(
//			P_CHOICE,
//			"An example of a multiple-choice preference",
//			1,
//			new String[][] { { "&Choice 1", "choice1" }, {
//				"C&hoice 2", "choice2" }
//		}, getFieldEditorParent()));
//		addField(
//			new StringFieldEditor(P_STRING, "A &text preference:", getFieldEditorParent()));
	}
	
}