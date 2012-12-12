/*******************************************************************************
 * Copyright (c) 2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.pkgmanager;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabItem;

import de.walware.ecommons.preferences.Preference.BooleanPref;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.ui.RUI;


public class OptionsTab extends Composite{
	
	
	private static final BooleanPref INSTALL_SUGGESTED_PREF = new BooleanPref(
			RUI.PLUGIN_ID + "/r.pkgmgr", "InstallSuggested.enabled"); //$NON-NLS-1$ //$NON-NLS-2$
	
	
	private final RPkgManagerDialog fDialog;
	
	private final TabItem fTab;
	
	private Button fSuggestedButton;
	
	
	public OptionsTab(final RPkgManagerDialog dialog, final TabItem tab, final Composite parent) {
		super(parent, SWT.NONE);
		
		fDialog = dialog;
		fTab = tab;
		
		setLayout(LayoutUtil.createTabGrid(1));
		createContent(this);
		
		initOptions();
	}
	
	
	boolean installSuggested() {
		return fSuggestedButton.getSelection();
	}
	
	
	private void createContent(final Composite parent) {
		final Composite filterCol = createInstall(parent);
		filterCol.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}
	
	private Composite createInstall(final Composite parent) {
		final Group composite = new Group(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.createGroupGrid(1));
		composite.setText("Installation");
		
		{	final Button button = new Button(composite, SWT.CHECK);
			fSuggestedButton = button;
			button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			button.setText("Include packages '&suggested' by the selected packages.");
		}
		
		return composite;
	}
	
	private void initOptions() {
		final Boolean value = PreferencesUtil.getInstancePrefs().getPreferenceValue(INSTALL_SUGGESTED_PREF);
		fSuggestedButton.setSelection(value != null && value.booleanValue());
		
		fSuggestedButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				PreferencesUtil.setPrefValue(InstanceScope.INSTANCE, INSTALL_SUGGESTED_PREF,
						fSuggestedButton.getSelection() );
			}
		});
	}
	
}
