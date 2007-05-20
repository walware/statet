/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.ui.dialogs.IStatusChangeListener;
import de.walware.eclipsecommons.ui.preferences.ConfigurationBlockPreferencePage;

import de.walware.statet.ext.ui.preferences.ManagedConfigurationBlock;
import de.walware.statet.r.ui.editors.REditorOptions;


/**
 * Preference page for 'R Editor Options'
 */
public class REditorPreferencePage extends ConfigurationBlockPreferencePage<REditorConfigurationBlock> {

	
	public REditorPreferencePage() {
	}
	
	@Override
	protected REditorConfigurationBlock createConfigurationBlock() {
		return new REditorConfigurationBlock(createStatusChangedListener());
	}
	
}


class REditorConfigurationBlock extends ManagedConfigurationBlock {
	
	private Button fSmartInsertControl;
	private Button fSpellEnableControl;
	
	public REditorConfigurationBlock(IStatusChangeListener statusListener) {
		super(null, statusListener);
	}
	
	@Override
	public void createContents(Composite pageComposite, IWorkbenchPreferenceContainer container, IPreferenceStore preferenceStore) {
		super.createContents(pageComposite, container, preferenceStore);
		// Preferences
		List<Preference> prefs = new ArrayList<Preference>();
		prefs.add(REditorOptions.PREF_SMARTINSERT_ASDEFAULT);
		prefs.add(REditorOptions.PREF_SPELLCHECKING_ENABLED);
		setupPreferenceManager(container, prefs.toArray(new Preference[prefs.size()]),
				new String[] { REditorOptions.CONTEXT_ID });
		
		// Controls
		fSmartInsertControl = new Button(pageComposite, SWT.CHECK);
		fSmartInsertControl.setText(Messages.REditorOptions_SmartInsert_label);
		fSmartInsertControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		fSpellEnableControl = new Button(pageComposite, SWT.CHECK);
		fSpellEnableControl.setText(Messages.REditorOptions_SpellChecking_Enable_label);
		fSpellEnableControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Binding
		createDbc();
		updateControls();
	}
	
	@Override
	protected void addBindings(DataBindingContext dbc, Realm realm) {
		dbc.bindValue(SWTObservables.observeSelection(fSmartInsertControl),
				createObservable(REditorOptions.PREF_SMARTINSERT_ASDEFAULT),
				null, null);
		dbc.bindValue(SWTObservables.observeSelection(fSpellEnableControl),
				createObservable(REditorOptions.PREF_SPELLCHECKING_ENABLED),
				null, null);
	}

}
