/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.internal.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.preferences.Preference.BooleanPref;
import de.walware.eclipsecommons.preferences.Preference.IntPref;
import de.walware.eclipsecommons.ui.databinding.ColorSelectorObservableValue;
import de.walware.eclipsecommons.ui.databinding.NumberValidator;
import de.walware.eclipsecommons.ui.dialogs.IStatusChangeListener;
import de.walware.eclipsecommons.ui.dialogs.Layouter;
import de.walware.eclipsecommons.ui.preferences.ConfigurationBlockPreferencePage;
import de.walware.eclipsecommons.ui.preferences.RGBPref;
import de.walware.eclipsecommons.ui.util.LayoutUtil;

import de.walware.statet.base.internal.ui.StatetUIPlugin;
import de.walware.statet.base.ui.IStatetUIPreferenceConstants;
import de.walware.statet.ext.ui.editors.ContentAssistPreference;
import de.walware.statet.ext.ui.preferences.ManagedConfigurationBlock;


public class EditorsPreferencePage extends ConfigurationBlockPreferencePage<EditorsConfigurationBlock> {

	
	public EditorsPreferencePage() {
		setPreferenceStore(StatetUIPlugin.getDefault().getPreferenceStore());
	}
	
	@Override
	protected EditorsConfigurationBlock createConfigurationBlock() {
		return new EditorsConfigurationBlock(createStatusChangedListener());
	}
	
}


class EditorsConfigurationBlock extends ManagedConfigurationBlock {
	
	private class AppearanceColorsItem {
		
		final String name;
		final RGBPref pref;
		
		AppearanceColorsItem(String label, RGBPref pref) {
			this.name = label;
			this.pref = pref;
		}
	}
	
	
	private ListViewer fColorList;
	private ColorSelector fColorEditor;
	private Button fMatchingBracketsControl;
	private BooleanPref fMatchingBracketsPref;
	private Button fCodeAssistAutoControl;
	private BooleanPref fCodeAssistAutoPref;
	private Text fCodeAssistDelayControl;
	private IntPref fCodeAssistDelayPref;
	
	
	public EditorsConfigurationBlock(IStatusChangeListener statusListener) {
		super(null, statusListener);
	}
	
	@Override
	public void createContents(Layouter layouter, IWorkbenchPreferenceContainer container, IPreferenceStore preferenceStore) {
		// Preferences
		fMatchingBracketsPref = new BooleanPref(StatetUIPlugin.PLUGIN_ID, IStatetUIPreferenceConstants.EDITOR_MATCHING_BRACKETS);
		List<AppearanceColorsItem> colors = new ArrayList<AppearanceColorsItem>();
		colors.add(new AppearanceColorsItem(Messages.Editors_MatchingBracketsHighlightColor, 
				new RGBPref(StatetUIPlugin.PLUGIN_ID, IStatetUIPreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR)));
		colors.add(new AppearanceColorsItem(Messages.Editors_CodeAssistProposalsForegroundColor, 
				ContentAssistPreference.PROPOSALS_FOREGROUND));
		colors.add(new AppearanceColorsItem(Messages.Editors_CodeAssistProposalsBackgroundColor, 
				ContentAssistPreference.PROPOSALS_BACKGROUND));
		colors.add(new AppearanceColorsItem(Messages.Editors_CodeAssistParametersForegrondColor, 
				ContentAssistPreference.PARAMETERS_FOREGROUND));
		colors.add(new AppearanceColorsItem(Messages.Editors_CodeAssistParametersBackgroundColor, 
				ContentAssistPreference.PARAMETERS_BACKGROUND));
		colors.add(new AppearanceColorsItem(Messages.Editors_CodeAssistReplacementForegroundColor, 
				ContentAssistPreference.REPLACEMENT_FOREGROUND));
		colors.add(new AppearanceColorsItem(Messages.Editors_CodeAssistReplacementBackgroundColor, 
				ContentAssistPreference.REPLACEMENT_BACKGROUND));
		fCodeAssistAutoPref = ContentAssistPreference.AUTOINSERT;
		fCodeAssistDelayPref = ContentAssistPreference.AUTOACTIVATION_DELAY;

		List<Preference> prefs = new ArrayList<Preference>();
		prefs.add(fMatchingBracketsPref);
		for (AppearanceColorsItem color : colors) {
			prefs.add(color.pref);
		}
		prefs.add(fCodeAssistAutoPref);
		prefs.add(fCodeAssistDelayPref);
		setupPreferenceManager(container, prefs.toArray(new Preference[prefs.size()]),
				new String[] { ContentAssistPreference.CONTEXT_ID });
		
		// Controls
		addLinkHeader(layouter, Messages.Editors_link);
		createAppearanceSection(layouter);
		layouter.addSmallFiller();
		createCodeAssistSection(layouter);

		// Binding
		fColorList.setInput(colors.toArray(new AppearanceColorsItem[colors.size()]));
		fColorList.setSelection(new StructuredSelection(colors.get(0)));
		createDbc();
		updateControls();
	}
	
	private void createAppearanceSection(Layouter parent) {
		Group group = parent.addGroup(Messages.Editors_Appearance);
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 2));
		Label label;
		GridData gd;
		
		fMatchingBracketsControl = new Button(group, SWT.CHECK | SWT.LEFT);
		fMatchingBracketsControl.setText(Messages.Editors_HighlightMatchingBrackets);
		fMatchingBracketsControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		LayoutUtil.addSmallFiller(group);
		Composite colorComposite = new Composite(group, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		colorComposite.setLayoutData(gd);
		colorComposite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
		label = new Label(colorComposite, SWT.LEFT);
		label.setText(Messages.Editors_AppearanceColors);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		fColorList = new ListViewer(colorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		fColorList.setContentProvider(new ArrayContentProvider());
		fColorList.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				AppearanceColorsItem item = (AppearanceColorsItem) element;
				return item.name;
			}
		});
		fColorList.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		Composite colorOptions = new Composite(colorComposite, SWT.NONE);
		colorOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		colorOptions.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
		label = new Label(colorOptions, SWT.LEFT);
		label.setText(Messages.Editors_Color);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fColorEditor = new ColorSelector(colorOptions);
		fColorEditor.getButton().setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
	}
	
	private void createCodeAssistSection(Layouter parent) {
		Group group = parent.addGroup(Messages.Editors_CodeAssist);
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 2));
		Label label;
		GridData gd;

		fCodeAssistAutoControl = new Button(group, SWT.CHECK);
		fCodeAssistAutoControl.setText(Messages.Editors_CodeAssist_AutoInsert);
		gd = new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1);
		fCodeAssistAutoControl.setLayoutData(gd);
		
		label = new Label(group, SWT.LEFT);
		label.setText(Messages.Editors_CodeAssist_AutoTriggerDelay_label);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		fCodeAssistDelayControl = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		gd.widthHint = LayoutUtil.hintWidth(fCodeAssistDelayControl, 4);
		fCodeAssistDelayControl.setLayoutData(gd);
	}
	
	@Override
	protected void addBindings(DataBindingContext dbc, Realm realm) {
		dbc.bindValue(SWTObservables.observeSelection(fMatchingBracketsControl),
				createObservable(fMatchingBracketsPref),
				null, null);
		IObservableValue colorItem = ViewersObservables.observeSingleSelection(fColorList);
		dbc.bindValue(new ColorSelectorObservableValue(fColorEditor), 
				MasterDetailObservables.detailValue(colorItem, new IObservableFactory() {
					public IObservable createObservable(Object target) {
						AppearanceColorsItem item = (AppearanceColorsItem) target;
						return EditorsConfigurationBlock.this.createObservable(item.pref);
					}
				}, RGB.class),	
				null, null);
		dbc.bindValue(SWTObservables.observeSelection(fCodeAssistAutoControl),
				createObservable(fCodeAssistAutoPref),
				null, null);
		dbc.bindValue(SWTObservables.observeText(fCodeAssistDelayControl, SWT.Modify),
				createObservable(fCodeAssistDelayPref),
				new UpdateValueStrategy().setAfterGetValidator(new NumberValidator(10, 2000, Messages.Editors_CodeAssist_AutoTriggerDelay_error_message)), null);
	}

}
