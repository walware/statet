/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.IStatusChangeListener;
import de.walware.ecommons.databinding.NumberValidator;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.ui.ConfigurationBlockPreferencePage;
import de.walware.ecommons.preferences.ui.ManagedConfigurationBlock;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.internal.ui.RUIPreferenceInitializer;
import de.walware.statet.r.internal.ui.editors.DefaultRFoldingPreferences;
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
	private Button[] fSmartInsertOnPasteControl;
	private Button[] fSmartInsertCloseCurlyBracketsControl;
	private Button[] fSmartInsertCloseRoundBracketsControl;
	private Button[] fSmartInsertCloseSquareBracketsControl;
	private Button[] fSmartInsertCloseSpecialControl;
	private Button[] fSmartInsertCloseStringsControl;
	private Button fFoldingEnableControl;
	private Button fFoldingDefaultAllBlocksControl;
	private Text fFoldingDefaultMinLines;
	private Button fFoldingDefaultRoxygenControl;
	private Button fFoldingDefaultRoxygenInitiallyControl;
	private Text fFoldingDefaultRoxygenMinLines;
	private Button fMarkOccurrencesControl;
	private Button fProblemsEnableControl;
	private Button fSpellEnableControl;
	
	
	public REditorConfigurationBlock(final IStatusChangeListener statusListener) {
		super(null, statusListener);
	}
	
	
	@Override
	protected void createBlockArea(final Composite pageComposite) {
		// Preferences
		final Map<Preference, String> prefs = new HashMap<Preference, String>();
		prefs.put(REditorOptions.PREF_SMARTINSERT_BYDEFAULT_ENABLED, REditorOptions.GROUP_ID);
		prefs.put(REditorOptions.PREF_SMARTINSERT_ONPASTE_ENABLED, REditorOptions.GROUP_ID);
		prefs.put(REditorOptions.PREF_SMARTINSERT_CLOSECURLY_ENABLED, REditorOptions.GROUP_ID);
		prefs.put(REditorOptions.PREF_SMARTINSERT_CLOSEROUND_ENABLED, REditorOptions.GROUP_ID);
		prefs.put(REditorOptions.PREF_SMARTINSERT_CLOSESQUARE_ENABLED, REditorOptions.GROUP_ID);
		prefs.put(REditorOptions.PREF_SMARTINSERT_CLOSESPECIAL_ENABLED, REditorOptions.GROUP_ID);
		prefs.put(REditorOptions.PREF_SMARTINSERT_CLOSESTRINGS_ENABLED, REditorOptions.GROUP_ID);
		
		prefs.put(RUIPreferenceInitializer.CONSOLE_SMARTINSERT_CLOSECURLY_ENABLED, REditorOptions.GROUP_ID);
		prefs.put(RUIPreferenceInitializer.CONSOLE_SMARTINSERT_CLOSEROUND_ENABLED, REditorOptions.GROUP_ID);
		prefs.put(RUIPreferenceInitializer.CONSOLE_SMARTINSERT_CLOSESQUARE_ENABLED, REditorOptions.GROUP_ID);
		prefs.put(RUIPreferenceInitializer.CONSOLE_SMARTINSERT_CLOSESPECIAL_ENABLED, REditorOptions.GROUP_ID);
		prefs.put(RUIPreferenceInitializer.CONSOLE_SMARTINSERT_CLOSESTRINGS_ENABLED, REditorOptions.GROUP_ID);
		
		prefs.put(REditorOptions.PREF_FOLDING_ENABLED, null);
		prefs.put(DefaultRFoldingPreferences.PREF_OTHERBLOCKS_ENABLED, DefaultRFoldingPreferences.GROUP_ID);
		prefs.put(DefaultRFoldingPreferences.PREF_MINLINES_NUM, DefaultRFoldingPreferences.GROUP_ID);
		prefs.put(DefaultRFoldingPreferences.PREF_ROXYGEN_ENABLED, DefaultRFoldingPreferences.GROUP_ID);
		prefs.put(DefaultRFoldingPreferences.PREF_ROXYGEN_COLLAPSE_INITIALLY_ENABLED, DefaultRFoldingPreferences.GROUP_ID);
		prefs.put(DefaultRFoldingPreferences.PREF_ROXYGEN_MINLINES_NUM, DefaultRFoldingPreferences.GROUP_ID);
		
		prefs.put(REditorOptions.PREF_MARKOCCURRENCES_ENABLED, null);
		
		prefs.put(REditorOptions.PREF_PROBLEMCHECKING_ENABLED, null);
		prefs.put(REditorOptions.PREF_SPELLCHECKING_ENABLED, REditorOptions.GROUP_ID);
		
		setupPreferenceManager(prefs);
		
		// Controls
		Group group;
		int n;
		
		group = new Group(pageComposite, SWT.NONE);
		group.setText(Messages.REditorOptions_SmartInsert_label+':');
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		n = 5;
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), n));
		fSmartInsertControl = new Button(group, SWT.CHECK);
		fSmartInsertControl.setText(Messages.REditorOptions_SmartInsert_AsDefault_label);
		fSmartInsertControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, n, 1));
		{	final Link link = addLinkControl(group, Messages.REditorOptions_SmartInsert_description);
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, n, 1);
			gd.widthHint = 300;
			link.setLayoutData(gd);
		}
		
		LayoutUtil.addGDDummy(group);
		LayoutUtil.addGDDummy(group);
		{	Label label = new Label(group, SWT.CENTER);
			label.setText(Messages.REditorOptions_SmartInsert_ForEditor_header);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			label = new Label(group, SWT.CENTER);
			label.setText(Messages.REditorOptions_SmartInsert_ForConsole_header);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		}
		final Label dummy = new Label(group, SWT.NONE);
		dummy.setVisible(false);
		dummy.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 7));
		
		fSmartInsertOnPasteControl = createOption(group, Messages.REditorOptions_SmartInsert_OnPaste_label, null, false);
		fSmartInsertCloseCurlyBracketsControl = createOption(group, Messages.REditorOptions_SmartInsert_CloseAuto_label, Messages.REditorOptions_SmartInsert_CloseCurly_label, true);
		fSmartInsertCloseRoundBracketsControl = createOption(group, null, Messages.REditorOptions_SmartInsert_CloseRound_label, true);
		fSmartInsertCloseSquareBracketsControl = createOption(group, null, Messages.REditorOptions_SmartInsert_CloseSquare_label, true);
		fSmartInsertCloseSpecialControl = createOption(group, null, Messages.REditorOptions_SmartInsert_ClosePercent_label, true);
		fSmartInsertCloseStringsControl = createOption(group, null, Messages.REditorOptions_SmartInsert_CloseString_label, true);
		
		// Code Folding
		LayoutUtil.addSmallFiller(pageComposite, false);
		
		{	fFoldingEnableControl = new Button(pageComposite, SWT.CHECK);
			fFoldingEnableControl.setText(Messages.REditorOptions_Folding_Enable_label);
			fFoldingEnableControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		final Composite foldingOptions = new Composite(pageComposite, SWT.NONE);
		{	final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			foldingOptions.setLayoutData(gd);
			foldingOptions.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
		}
		
		{	fFoldingDefaultAllBlocksControl = new Button(foldingOptions, SWT.CHECK);
			fFoldingDefaultAllBlocksControl.setText(Messages.REditorOptions_Folding_EnableForAllBlocks_label);
			fFoldingDefaultAllBlocksControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		}
		{	final Label label = new Label(foldingOptions, SWT.LEFT);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			label.setLayoutData(gd);
			label.setText(Messages.REditorOptions_Folding_MinNumOfLines_label);
		}
		{	fFoldingDefaultMinLines = new Text(foldingOptions, SWT.SINGLE | SWT.BORDER);
			final GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			gd.widthHint = LayoutUtil.hintWidth(fFoldingDefaultMinLines, 2);
			fFoldingDefaultMinLines.setLayoutData(gd);
		}
		{	fFoldingDefaultRoxygenControl = new Button(foldingOptions, SWT.CHECK);
			fFoldingDefaultRoxygenControl.setText(Messages.REditorOptions_Folding_EnableForRoxygen_label);
			fFoldingDefaultRoxygenControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		}
		{	final Label label = new Label(foldingOptions, SWT.LEFT);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			label.setLayoutData(gd);
			label.setText(Messages.REditorOptions_Folding_MinNumOfLines_label);
		}
		{	fFoldingDefaultRoxygenMinLines = new Text(foldingOptions, SWT.SINGLE | SWT.BORDER);
			final GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			gd.widthHint = LayoutUtil.hintWidth(fFoldingDefaultRoxygenMinLines, 2);
			fFoldingDefaultRoxygenMinLines.setLayoutData(gd);
		}
		{	fFoldingDefaultRoxygenInitiallyControl = new Button(foldingOptions, SWT.CHECK);
			fFoldingDefaultRoxygenInitiallyControl.setText(Messages.REditorOptions_Folding_EnableForRoxygen_Initially_label);
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			fFoldingDefaultRoxygenInitiallyControl.setLayoutData(gd);
		}
		
		// Annotation
		LayoutUtil.addSmallFiller(pageComposite, false);
		
		{	fMarkOccurrencesControl = new Button(pageComposite, SWT.CHECK);
			fMarkOccurrencesControl.setText(Messages.REditorOptions_MarkOccurrences_Enable_label);
			fMarkOccurrencesControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			final Link link = addLinkControl(pageComposite, Messages.REditorOptions_MarkOccurrences_Appearance_info);
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.widthHint = 300;
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			link.setLayoutData(gd);
		}
		
		LayoutUtil.addSmallFiller(pageComposite, false);
		
		{	fProblemsEnableControl = new Button(pageComposite, SWT.CHECK);
			fProblemsEnableControl.setText(Messages.REditorOptions_ProblemChecking_Enable_label);
			fProblemsEnableControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		
		LayoutUtil.addSmallFiller(pageComposite, false);
		
		{	fSpellEnableControl = new Button(pageComposite, SWT.CHECK);
			fSpellEnableControl.setText(Messages.REditorOptions_SpellChecking_Enable_label);
			fSpellEnableControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			final Link link = addLinkControl(pageComposite, Messages.REditorOptions_SpellChecking_note);
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.widthHint = 300;
			gd.horizontalIndent = LayoutUtil.defaultIndent();
			link.setLayoutData(gd);
		}
		
		// Binding
		initBindings();
		updateControls();
	}
	
	private Button[] createOption(final Composite composite, final String text1, final String text2, final boolean console) {
		GridData gd;
		if (text1 != null) {
			final Label label = new Label(composite, SWT.NONE);
			if (text2 == null) {
				label.setText(text1+':');
				gd = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
			}
			else {
				label.setText(text1);
				gd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			}
			label.setLayoutData(gd);
		}
		else {
			LayoutUtil.addGDDummy(composite);
		}
		if (text2 != null) {
			final Label label = new Label(composite, SWT.NONE);
			label.setText(text2+':');
			gd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			label.setLayoutData(gd);
		}
		final Button button0 = new Button(composite, SWT.CHECK);
		gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		button0.setLayoutData(gd);
		
		final Button button1 = new Button(composite, SWT.CHECK);
		gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		button1.setLayoutData(gd);
		if (!console) {
			button1.setEnabled(false);
		}
		return new Button[] { button0, button1 };
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		dbc.bindValue(SWTObservables.observeSelection(fSmartInsertControl),
				createObservable(REditorOptions.PREF_SMARTINSERT_BYDEFAULT_ENABLED),
				null, null);
		dbc.bindValue(SWTObservables.observeSelection(fSmartInsertOnPasteControl[0]),
				createObservable(REditorOptions.PREF_SMARTINSERT_ONPASTE_ENABLED),
				null, null);
		dbc.bindValue(SWTObservables.observeSelection(fSmartInsertCloseCurlyBracketsControl[0]),
				createObservable(REditorOptions.PREF_SMARTINSERT_CLOSECURLY_ENABLED),
				null, null);
		dbc.bindValue(SWTObservables.observeSelection(fSmartInsertCloseRoundBracketsControl[0]),
				createObservable(REditorOptions.PREF_SMARTINSERT_CLOSEROUND_ENABLED),
				null, null);
		dbc.bindValue(SWTObservables.observeSelection(fSmartInsertCloseSquareBracketsControl[0]),
				createObservable(REditorOptions.PREF_SMARTINSERT_CLOSESQUARE_ENABLED),
				null, null);
		dbc.bindValue(SWTObservables.observeSelection(fSmartInsertCloseSpecialControl[0]),
				createObservable(REditorOptions.PREF_SMARTINSERT_CLOSESPECIAL_ENABLED),
				null, null);
		dbc.bindValue(SWTObservables.observeSelection(fSmartInsertCloseStringsControl[0]),
				createObservable(REditorOptions.PREF_SMARTINSERT_CLOSESTRINGS_ENABLED),
				null, null);
		
		dbc.bindValue(SWTObservables.observeSelection(fSmartInsertCloseCurlyBracketsControl[1]),
				createObservable(RUIPreferenceInitializer.CONSOLE_SMARTINSERT_CLOSECURLY_ENABLED),
				null, null);
		dbc.bindValue(SWTObservables.observeSelection(fSmartInsertCloseRoundBracketsControl[1]),
				createObservable(RUIPreferenceInitializer.CONSOLE_SMARTINSERT_CLOSEROUND_ENABLED),
				null, null);
		dbc.bindValue(SWTObservables.observeSelection(fSmartInsertCloseSquareBracketsControl[1]),
				createObservable(RUIPreferenceInitializer.CONSOLE_SMARTINSERT_CLOSESQUARE_ENABLED),
				null, null);
		dbc.bindValue(SWTObservables.observeSelection(fSmartInsertCloseSpecialControl[1]),
				createObservable(RUIPreferenceInitializer.CONSOLE_SMARTINSERT_CLOSESPECIAL_ENABLED),
				null, null);
		dbc.bindValue(SWTObservables.observeSelection(fSmartInsertCloseStringsControl[1]),
				createObservable(RUIPreferenceInitializer.CONSOLE_SMARTINSERT_CLOSESTRINGS_ENABLED),
				null, null);
		
		dbc.bindValue(SWTObservables.observeSelection(fFoldingEnableControl),
				createObservable(REditorOptions.PREF_FOLDING_ENABLED),
				null, null);
		dbc.bindValue(SWTObservables.observeSelection(fFoldingDefaultAllBlocksControl),
				createObservable(DefaultRFoldingPreferences.PREF_OTHERBLOCKS_ENABLED),
				null, null);
		dbc.bindValue(SWTObservables.observeText(fFoldingDefaultMinLines, SWT.Modify),
				createObservable(DefaultRFoldingPreferences.PREF_MINLINES_NUM),
				new UpdateValueStrategy().setAfterGetValidator(new NumberValidator(2, 1000, Messages.REditorOptions_Folding_MinNumOfLines_error_message)), null);
		dbc.bindValue(SWTObservables.observeSelection(fFoldingDefaultRoxygenControl),
				createObservable(DefaultRFoldingPreferences.PREF_ROXYGEN_ENABLED),
				null, null);
		dbc.bindValue(SWTObservables.observeSelection(fFoldingDefaultRoxygenInitiallyControl),
				createObservable(DefaultRFoldingPreferences.PREF_ROXYGEN_COLLAPSE_INITIALLY_ENABLED),
				null, null);
		dbc.bindValue(SWTObservables.observeText(fFoldingDefaultRoxygenMinLines, SWT.Modify),
				createObservable(DefaultRFoldingPreferences.PREF_ROXYGEN_MINLINES_NUM),
				new UpdateValueStrategy().setAfterGetValidator(new NumberValidator(2, 1000, Messages.REditorOptions_Folding_MinNumOfLines_error_message)), null);
		
		dbc.bindValue(SWTObservables.observeSelection(fMarkOccurrencesControl),
				createObservable(REditorOptions.PREF_MARKOCCURRENCES_ENABLED),
				null, null);
		
		dbc.bindValue(SWTObservables.observeSelection(fProblemsEnableControl),
				createObservable(REditorOptions.PREF_PROBLEMCHECKING_ENABLED),
				null, null);
		
		dbc.bindValue(SWTObservables.observeSelection(fSpellEnableControl),
				createObservable(REditorOptions.PREF_SPELLCHECKING_ENABLED),
				null, null);
	}
	
}
