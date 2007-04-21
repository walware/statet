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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import de.walware.eclipsecommons.ui.dialogs.Layouter;


public abstract class AbstractConfigurationBlock {

	
	private Shell fShell;
	protected boolean fUseProjectSettings = true;
	
	
	public void createContents(Layouter layouter, 
			IWorkbenchPreferenceContainer container, 
			IPreferenceStore preferenceStore) {

		fShell = layouter.composite.getShell();
	}
	
	public void dispose() {
		
	}

	public void performApply() {
		
		performOk();
	}
	
	public abstract boolean performOk();
	
	public abstract void performDefaults();
	
	public void performCancel() {
		
	}

	public void setUseProjectSpecificSettings(boolean enable) {
		
		fUseProjectSettings = enable;
	}
	
	protected Shell getShell() {
		
		return fShell;
	}

	protected void addLinkHeader(Layouter layouter, String text) {
		
		Link link = addLinkControl(layouter.composite, text);
		GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gridData.widthHint = 150; // only expand further if anyone else requires it
		gridData.horizontalSpan = layouter.fNumColumns;
		link.setLayoutData(gridData);

		layouter.addFiller();
	}

	protected Link addLinkControl(Composite composite, String text) {
		
		Link link = new Link(composite, SWT.NONE);
		link.setText(text);
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(getShell(), e.text, null, null); //$NON-NLS-1$
			}
		});
		return link;
	}

}
