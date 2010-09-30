/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.editors;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;

import de.walware.ecommons.IStatusChangeListener;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.ui.ConfigurationBlockPreferencePage;
import de.walware.ecommons.preferences.ui.ManagedConfigurationBlock;
import de.walware.ecommons.ui.util.LayoutUtil;


public class SweaveEditorPreferencePage extends ConfigurationBlockPreferencePage<SweaveEditorConfigurationBlock> {
	
	
	public SweaveEditorPreferencePage() {
	}
	
	@Override
	protected SweaveEditorConfigurationBlock createConfigurationBlock() {
		return new SweaveEditorConfigurationBlock(createStatusChangedListener());
	}
	
}


class SweaveEditorConfigurationBlock extends ManagedConfigurationBlock {
	
	
	private Button fSpellEnableControl;
	
	
	public SweaveEditorConfigurationBlock(final IStatusChangeListener statusListener) {
		super(null, statusListener);
	}
	
	
	@Override
	public void createBlockArea(final Composite pageComposite) {
		// Preferences
		final Map<Preference, String> prefs = new HashMap<Preference, String>();
		
		prefs.put(SweaveEditorOptions.PREF_SPELLCHECKING_ENABLED, SweaveEditorOptions.GROUP_ID);
		
		setupPreferenceManager(prefs);
		
		// Controls
		Link link;
		GridData gd;
		
		link = addLinkControl(pageComposite, Messages.SweaveEditorOptions_RAndLatexRef_note);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.widthHint = 300;
		link.setLayoutData(gd);
		
		link = addLinkControl(pageComposite, Messages.SweaveEditorOptions_SyntaxColoring_note);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.widthHint = 300;
		gd.horizontalIndent = LayoutUtil.defaultIndent();
		link.setLayoutData(gd);
		
		LayoutUtil.addSmallFiller(pageComposite, false);
		fSpellEnableControl = new Button(pageComposite, SWT.CHECK);
		fSpellEnableControl.setText(Messages.SweaveEditorOptions_SpellChecking_Enable_label);
		fSpellEnableControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		link = addLinkControl(pageComposite, Messages.SweaveEditorOptions_SpellChecking_note);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.widthHint = 300;
		gd.horizontalIndent = LayoutUtil.defaultIndent();
		link.setLayoutData(gd);
		
		// Binding
		initBindings();
		updateControls();
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		dbc.bindValue(SWTObservables.observeSelection(fSpellEnableControl),
				createObservable(SweaveEditorOptions.PREF_SPELLCHECKING_ENABLED),
				null, null);
	}
	
}
