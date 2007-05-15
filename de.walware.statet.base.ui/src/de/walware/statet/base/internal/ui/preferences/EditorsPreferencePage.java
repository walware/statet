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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
import de.walware.eclipsecommons.ui.dialogs.groups.ListedOptionsGroup;
import de.walware.eclipsecommons.ui.preferences.ConfigurationBlockPreferencePage;
import de.walware.eclipsecommons.ui.preferences.RGBPref;
import de.walware.eclipsecommons.ui.util.LayoutUtil;
import de.walware.eclipsecommons.ui.util.UIAccess;

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
	
	private class ListedAppearanceColorsGroup extends ListedOptionsGroup<AppearanceColorsItem> {
		
		Composite fStylesComposite;

		ListedAppearanceColorsGroup() {
			super(false, false);
			getListModel().add(new AppearanceColorsItem(Messages.Editors_MatchingBracketsHighlightColor, 
					new RGBPref(StatetUIPlugin.PLUGIN_ID, IStatetUIPreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR)));
			getListModel().add(new AppearanceColorsItem(Messages.Editors_CodeAssistProposalsForegroundColor, 
					new RGBPref(IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER, ContentAssistPreference.PROPOSALS_FOREGROUND)));
			getListModel().add(new AppearanceColorsItem(Messages.Editors_CodeAssistProposalsBackgroundColor, 
					new RGBPref(IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER, ContentAssistPreference.PROPOSALS_BACKGROUND)));
			getListModel().add(new AppearanceColorsItem(Messages.Editors_CodeAssistParametersForegrondColor, 
					new RGBPref(IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER, ContentAssistPreference.PARAMETERS_FOREGROUND)));
			getListModel().add(new AppearanceColorsItem(Messages.Editors_CodeAssistParametersBackgroundColor, 
					new RGBPref(IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER, ContentAssistPreference.PARAMETERS_BACKGROUND)));
			getListModel().add(new AppearanceColorsItem(Messages.Editors_CodeAssistReplacementForegroundColor, 
					new RGBPref(IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER, ContentAssistPreference.REPLACEMENT_FOREGROUND)));
			getListModel().add(new AppearanceColorsItem(Messages.Editors_CodeAssistReplacementBackgroundColor, 
					new RGBPref(IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER, ContentAssistPreference.REPLACEMENT_BACKGROUND)));
		}
		
		@Override
		protected ILabelProvider createLabelProvider() {
			return new LabelProvider() {
				@Override
				public String getText(Object element) {
					AppearanceColorsItem item = (AppearanceColorsItem) element;
					return item.name;
				}
			};
		}
		
		@Override
		protected Control createOptionsControl(Composite parent) {
			fStylesComposite = new Composite(parent, SWT.NONE);
			return fStylesComposite;
		}
		
		@Override
		protected void handleSelection(AppearanceColorsItem item, IStructuredSelection rawSelection) {
			if (item != null) {
				fColorEditor.setEnabled(true);
			}
			else {
				fColorEditor.setEnabled(false);
			}
		}
	};


	private ListedAppearanceColorsGroup fAppearanceColors;
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
		fAppearanceColors = new ListedAppearanceColorsGroup();
		fMatchingBracketsPref = new BooleanPref(StatetUIPlugin.PLUGIN_ID, IStatetUIPreferenceConstants.EDITOR_MATCHING_BRACKETS);
		fCodeAssistAutoPref = new BooleanPref(IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER, ContentAssistPreference.AUTOINSERT);
		fCodeAssistDelayPref = new IntPref(IStatetUIPreferenceConstants.CAT_CODEASSIST_QUALIFIER, ContentAssistPreference.AUTOACTIVATION_DELAY);

		List<Preference> prefs = new ArrayList<Preference>();
		prefs.add(fMatchingBracketsPref);
		for (AppearanceColorsItem color : fAppearanceColors.getListModel()) {
			prefs.add(color.pref);
		}
		prefs.add(fCodeAssistAutoPref);
		prefs.add(fCodeAssistDelayPref);
		setupPreferenceManager(container, prefs.toArray(new Preference[prefs.size()]));
		
		addLinkHeader(layouter, Messages.Editors_link);
		
		createAppearanceSection(layouter);
		createCodeAssistSection(layouter);

		fAppearanceColors.initFields();
		createDbc();
		updateControls();
	}
	
	private void createAppearanceSection(Layouter parent) {
		Group group = parent.addGroup(Messages.Editors_Appearance);
		Layouter layouter = new Layouter(group, 2);
		
		LayoutUtil.addSmallFiller(layouter.composite);
		fMatchingBracketsControl = layouter.addCheckBox(Messages.Editors_HighlightMatchingBrackets); 

		LayoutUtil.addSmallFiller(layouter.composite);
		layouter.addLabel(Messages.Editors_AppearanceColors);
		layouter.addGroup(fAppearanceColors);
		Layouter stylesLayouter = new Layouter(fAppearanceColors.fStylesComposite, 2);
		stylesLayouter.addLabel(Messages.Editors_Color, 0, 1); 
		fColorEditor = new ColorSelector(stylesLayouter.composite);
		Button foregroundColorButton = fColorEditor.getButton();
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);
	}
	
	private void createCodeAssistSection(Layouter parent) {
		Group group = parent.addGroup(Messages.Editors_CodeAssist);
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 2));
		GridData gd;

		LayoutUtil.addSmallFiller(group);
		fCodeAssistAutoControl = new Button(group, SWT.CHECK);
		fCodeAssistAutoControl.setText(Messages.Editors_CodeAssist_AutoInsert);
		gd = new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1);
		fCodeAssistAutoControl.setLayoutData(gd);
		
		Label label = new Label(group, SWT.LEFT);
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
		IObservableValue colorItem = ViewersObservables.observeSingleSelection(fAppearanceColors.getStructuredViewer());
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

	protected void updateControls() {
		super.updateControls();
		if (UIAccess.isOkToUse(fAppearanceColors.getStructuredViewer())) {
			fAppearanceColors.reselect();
		}
	}

}