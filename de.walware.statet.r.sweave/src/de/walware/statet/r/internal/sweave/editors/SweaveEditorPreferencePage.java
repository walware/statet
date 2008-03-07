/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.editors;

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
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.ui.dialogs.IStatusChangeListener;
import de.walware.eclipsecommons.ui.preferences.ConfigurationBlockPreferencePage;
import de.walware.eclipsecommons.ui.util.LayoutUtil;

import de.walware.statet.ext.ui.preferences.ManagedConfigurationBlock;


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
	protected String[] getChangedGroups() {
		return new String[] {
				SweaveEditorOptions.GROUP_ID,
		};
	}
	
	@Override
	public void createContents(final Composite pageComposite, final IWorkbenchPreferenceContainer container, final IPreferenceStore preferenceStore) {
		super.createContents(pageComposite, container, preferenceStore);
		// Preferences
		final List<Preference> prefs = new ArrayList<Preference>();
		prefs.add(SweaveEditorOptions.PREF_SPELLCHECKING_ENABLED);
		
		setupPreferenceManager(container, prefs.toArray(new Preference[prefs.size()]));
		
		// Controls
		Link link;
		GridData gd;
		
		link = addLinkControl(pageComposite, "Sweave editor preferences. Note that some settings are inherited from R editor and LaTeX editor.");
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.widthHint = 300;
		link.setLayoutData(gd);
		
		link = addLinkControl(pageComposite, "The markers '<<' and '>>=' are displayed according to the <a href=\"de.walware.statet.r.preferencePages.RSyntaxColoring\">coloring</a> options for 'Invalid / Other' tokens.");
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.widthHint = 300;
		gd.horizontalIndent = LayoutUtil.defaultIndent();
		link.setLayoutData(gd);
		
		LayoutUtil.addSmallFiller(pageComposite, false);
		fSpellEnableControl = new Button(pageComposite, SWT.CHECK);
		fSpellEnableControl.setText("Enable s&pell checking.");
		fSpellEnableControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		link = addLinkControl(pageComposite, "Note: On the <a href=\"org.eclipse.ui.editors.preferencePages.Spelling\">Spelling</a> preference page, the spell checking must be enabled and properly configured.");
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
