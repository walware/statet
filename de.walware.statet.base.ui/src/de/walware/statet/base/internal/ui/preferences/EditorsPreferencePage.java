/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.base.internal.ui.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import de.walware.ecommons.IStatusChangeListener;
import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.BooleanPref;
import de.walware.ecommons.preferences.ui.ConfigurationBlock;
import de.walware.ecommons.preferences.ui.ConfigurationBlockPreferencePage;
import de.walware.ecommons.preferences.ui.ManagedConfigurationBlock;
import de.walware.ecommons.text.ui.settings.AssistPreferences;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.base.ui.IStatetUIPreferenceConstants;


public class EditorsPreferencePage extends ConfigurationBlockPreferencePage {
	
	
	public EditorsPreferencePage() {
	}
	
	
	@Override
	protected ConfigurationBlock createConfigurationBlock() {
		return new EditorsConfigurationBlock(createStatusChangedListener());
	}
	
}


class EditorsConfigurationBlock extends ManagedConfigurationBlock {
	
	
	private Button fCodeAssistAutoSingleControl;
	private BooleanPref fCodeAssistAutoSinglePref;
	private Button fCodeAssistAutoCommonControl;
	private BooleanPref fCodeAssistAutoCommonPref;
	
	
	public EditorsConfigurationBlock(final IStatusChangeListener statusListener) {
		super(null, statusListener);
	}
	
	
	@Override
	protected void createBlockArea(final Composite pageComposite) {
		final Map<Preference<?>, String> prefs= new HashMap<>();
		
		// Content Assist
		final AssistPreferences assistPreferences = IStatetUIPreferenceConstants.EDITING_ASSIST_PREFERENCES;
		fCodeAssistAutoSinglePref = assistPreferences.getAutoInsertSinglePref();
		prefs.put(fCodeAssistAutoSinglePref, assistPreferences.getGroupId());
		fCodeAssistAutoCommonPref = assistPreferences.getAutoInsertPrefixPref();
		prefs.put(fCodeAssistAutoCommonPref, assistPreferences.getGroupId());
		
		// Register preferences
		setupPreferenceManager(prefs);
		
		// Controls
		addLinkHeader(pageComposite, Messages.Editors_link);
		{	Composite group= createCodeAssistSection(pageComposite);
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		
		// Binding
		initBindings();
		updateControls();
	}
	
	private Composite createCodeAssistSection(final Composite parent) {
		final Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.Editors_CodeAssist);
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 2));
		Label label;
		GridData gd;
		
		fCodeAssistAutoSingleControl = new Button(group, SWT.CHECK);
		fCodeAssistAutoSingleControl.setText(Messages.Editors_CodeAssist_AutoInsertSingle);
		gd = new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1);
		fCodeAssistAutoSingleControl.setLayoutData(gd);
		
		fCodeAssistAutoCommonControl = new Button(group, SWT.CHECK);
		fCodeAssistAutoCommonControl.setText(Messages.Editors_CodeAssist_AutoInsertCommon);
		gd = new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1);
		fCodeAssistAutoCommonControl.setLayoutData(gd);
		
		return group;
	}
	
	@Override
	protected void addBindings(final DataBindingSupport db) {
		db.getContext().bindValue(SWTObservables.observeSelection(fCodeAssistAutoSingleControl),
				createObservable(fCodeAssistAutoSinglePref),
				null, null);
		db.getContext().bindValue(SWTObservables.observeSelection(fCodeAssistAutoCommonControl),
				createObservable(fCodeAssistAutoCommonPref),
				null, null);
	}
	
}
