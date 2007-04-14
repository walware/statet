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

package de.walware.statet.ui.internal.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import de.walware.eclipsecommons.ui.dialogs.Layouter;
import de.walware.eclipsecommons.ui.dialogs.groups.ListedOptionsGroup;
import de.walware.eclipsecommons.ui.dialogs.groups.SelectionItem;
import de.walware.eclipsecommons.ui.preferences.ConfigurationBlockPreferencePage;
import de.walware.eclipsecommons.ui.preferences.OverlayStoreConfigurationBlock;
import de.walware.eclipsecommons.ui.preferences.OverlayStorePreference;
import de.walware.eclipsecommons.preferences.Preference.Type;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.ui.StatetUiPreferenceConstants;


public class EditorsPreferencePage extends ConfigurationBlockPreferencePage<EditorsConfigurationBlock> {

	
	public EditorsPreferencePage() {
		
		setPreferenceStore(StatetPlugin.getDefault().getPreferenceStore());
	}
	
	@Override
	protected EditorsConfigurationBlock createConfigurationBlock() {

		return new EditorsConfigurationBlock();
	}
	
}

class EditorsConfigurationBlock extends OverlayStoreConfigurationBlock {

	private class AppearanceColorsItem extends SelectionItem {
		
		public String fColorKey;
		
		AppearanceColorsItem(String label, String colorKey) {
			
			super(label);
			fColorKey = colorKey;
		}
		
	}
	
	private class ListedAppearanceColorsGroup extends ListedOptionsGroup<AppearanceColorsItem> {
		
		Composite fStylesComposite;
		ColorSelector fColorEditor;

		ListedAppearanceColorsGroup() {

			super();
			
			fSelectionModel.add(new AppearanceColorsItem(Messages.Editors_MatchingBracketsHighlightColor, StatetUiPreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR));
			fSelectionModel.add(new AppearanceColorsItem(Messages.Editors_CodeAssistProposalsForegroundColor, StatetUiPreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND));
			fSelectionModel.add(new AppearanceColorsItem(Messages.Editors_CodeAssistProposalsBackgroundColor, StatetUiPreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND));
			fSelectionModel.add(new AppearanceColorsItem(Messages.Editors_CodeAssistParametersForegrondColor, StatetUiPreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND));
			fSelectionModel.add(new AppearanceColorsItem(Messages.Editors_CodeAssistParametersBackgroundColor, StatetUiPreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND));
			fSelectionModel.add(new AppearanceColorsItem(Messages.Editors_CodeAssistReplacementForegroundColor, StatetUiPreferenceConstants.CODEASSIST_REPLACEMENT_FOREGROUND));
			fSelectionModel.add(new AppearanceColorsItem(Messages.Editors_CodeAssistReplacementBackgroundColor, StatetUiPreferenceConstants.CODEASSIST_REPLACEMENT_BACKGROUND));
		}
		
		@Override
		protected Control createOptionsControl(Composite parent, GridData gd) {
			
			fStylesComposite = new Composite(parent, SWT.NONE);
			return fStylesComposite;
		}
		
		@Override
		public void handleListSelection() {
			int i = fListControl.getSelectionIndex();
			if (i == -1)
				return;
			String key = fSelectionModel.get(i).fColorKey;
			RGB rgb = PreferenceConverter.getColor(fOverlayStore, key);
			fColorEditor.setColorValue(rgb);		
		}
	};


	private ListedAppearanceColorsGroup fAppearanceColors;
	
	
	public EditorsConfigurationBlock() {
		super();
		
		fAppearanceColors = new ListedAppearanceColorsGroup();
	}
	
	@Override
	public void createContents(Layouter layouter, IWorkbenchPreferenceContainer container, IPreferenceStore preferenceStore) {

		super.createContents(layouter, container, preferenceStore);
		
		List<OverlayStorePreference> keys = new ArrayList<OverlayStorePreference>();
		keys.add(new OverlayStorePreference(StatetUiPreferenceConstants.EDITOR_MATCHING_BRACKETS, Type.BOOLEAN));
		keys.add(new OverlayStorePreference(StatetUiPreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR, Type.STRING));
		for (AppearanceColorsItem color : fAppearanceColors.fSelectionModel) {
			keys.add(new OverlayStorePreference(color.fColorKey, Type.STRING));
		}
		keys.add(new OverlayStorePreference(StatetUiPreferenceConstants.CODEASSIST_AUTOINSERT, Type.BOOLEAN));
		setupPreferenceManager(preferenceStore, keys.toArray(new OverlayStorePreference[keys.size()]));

		addLinkHeader(layouter, Messages.Editors_link);
		
		createAppearanceSection(layouter);
		createCodeAssistSection(layouter);

		fAppearanceColors.initFields();
		updateControls();
	}
	
	private void createAppearanceSection(Layouter parent) {
		
		Group group = parent.addGroup(Messages.Editors_Appearance);
		Layouter layouter = new Layouter(group, 2);
		FieldManager manager = getManager(layouter);
		
		layouter.addSmallFiller();

		manager.addCheckBox(Messages.Editors_HighlightMatchingBrackets, 
				StatetUiPreferenceConstants.EDITOR_MATCHING_BRACKETS);
		
		layouter.addSmallFiller();
		
		layouter.addLabel(Messages.Editors_AppearanceColors);

		layouter.addGroup(fAppearanceColors);
		
		Layouter stylesLayouter = new Layouter(fAppearanceColors.fStylesComposite, 2);
		stylesLayouter.addLabel(Messages.Editors_Color, 0, 1); 

		fAppearanceColors.fColorEditor = new ColorSelector(stylesLayouter.fComposite);
		Button foregroundColorButton = fAppearanceColors.fColorEditor.getButton();
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);

		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				int i = fAppearanceColors.fListControl.getSelectionIndex();
				if (i == -1)
					return;

				String key = fAppearanceColors.fSelectionModel.get(i).fColorKey;
				PreferenceConverter.setValue(fOverlayStore, key, fAppearanceColors.fColorEditor.getColorValue());
			}
		});
	}
	private void createCodeAssistSection(Layouter parent) {
		
		Group group = parent.addGroup(Messages.Editors_CodeAssist);
		Layouter layouter = new Layouter(group, 2);
		FieldManager manager = getManager(layouter);

		layouter.addSmallFiller();
		manager.addCheckBox(Messages.Editors_CodeAssist_AutoInsert, 
				StatetUiPreferenceConstants.CODEASSIST_AUTOINSERT);
	}

	@Override
	protected void updateControls() {
		super.updateControls();
		
		if (Layouter.isOkToUse(fAppearanceColors.fListControl))
			fAppearanceColors.handleListSelection();
	}
}