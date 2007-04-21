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

package de.walware.statet.base.internal.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
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

import de.walware.eclipsecommons.preferences.Preference.Type;
import de.walware.eclipsecommons.ui.dialogs.Layouter;
import de.walware.eclipsecommons.ui.dialogs.groups.ListedOptionsGroup;
import de.walware.eclipsecommons.ui.preferences.ConfigurationBlockPreferencePage;
import de.walware.eclipsecommons.ui.preferences.OverlayStoreConfigurationBlock;
import de.walware.eclipsecommons.ui.preferences.OverlayStorePreference;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.internal.ui.StatetUIPlugin;
import de.walware.statet.base.ui.IStatetUIPreferenceConstants;


public class EditorsPreferencePage extends ConfigurationBlockPreferencePage<EditorsConfigurationBlock> {

	
	public EditorsPreferencePage() {
		
		setPreferenceStore(StatetUIPlugin.getDefault().getPreferenceStore());
	}
	
	@Override
	protected EditorsConfigurationBlock createConfigurationBlock() {

		return new EditorsConfigurationBlock();
	}
	
}

class EditorsConfigurationBlock extends OverlayStoreConfigurationBlock {

	private class AppearanceColorsItem {
		
		String name;
		String colorKey;
		
		AppearanceColorsItem(String label, String colorKey) {
			
			this.name = label;
			this.colorKey = colorKey;
		}
	}
	
	private class ListedAppearanceColorsGroup extends ListedOptionsGroup<AppearanceColorsItem> {
		
		Composite fStylesComposite;
		ColorSelector fColorEditor;

		ListedAppearanceColorsGroup() {

			super(false, false);
			
			getListModel().add(new AppearanceColorsItem(Messages.Editors_MatchingBracketsHighlightColor, IStatetUIPreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR));
			getListModel().add(new AppearanceColorsItem(Messages.Editors_CodeAssistProposalsForegroundColor, IStatetUIPreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND));
			getListModel().add(new AppearanceColorsItem(Messages.Editors_CodeAssistProposalsBackgroundColor, IStatetUIPreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND));
			getListModel().add(new AppearanceColorsItem(Messages.Editors_CodeAssistParametersForegrondColor, IStatetUIPreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND));
			getListModel().add(new AppearanceColorsItem(Messages.Editors_CodeAssistParametersBackgroundColor, IStatetUIPreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND));
			getListModel().add(new AppearanceColorsItem(Messages.Editors_CodeAssistReplacementForegroundColor, IStatetUIPreferenceConstants.CODEASSIST_REPLACEMENT_FOREGROUND));
			getListModel().add(new AppearanceColorsItem(Messages.Editors_CodeAssistReplacementBackgroundColor, IStatetUIPreferenceConstants.CODEASSIST_REPLACEMENT_BACKGROUND));
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
				RGB rgb = PreferenceConverter.getColor(fOverlayStore, item.colorKey);
				fColorEditor.setEnabled(true);
				fColorEditor.setColorValue(rgb);
			}
			else {
				fColorEditor.setEnabled(false);
			}
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
		keys.add(new OverlayStorePreference(IStatetUIPreferenceConstants.EDITOR_MATCHING_BRACKETS, Type.BOOLEAN));
		keys.add(new OverlayStorePreference(IStatetUIPreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR, Type.STRING));
		for (AppearanceColorsItem color : fAppearanceColors.getListModel()) {
			keys.add(new OverlayStorePreference(color.colorKey, Type.STRING));
		}
		keys.add(new OverlayStorePreference(IStatetUIPreferenceConstants.CODEASSIST_AUTOINSERT, Type.BOOLEAN));
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
				IStatetUIPreferenceConstants.EDITOR_MATCHING_BRACKETS);
		
		layouter.addSmallFiller();
		
		layouter.addLabel(Messages.Editors_AppearanceColors);

		layouter.addGroup(fAppearanceColors);
		
		Layouter stylesLayouter = new Layouter(fAppearanceColors.fStylesComposite, 2);
		stylesLayouter.addLabel(Messages.Editors_Color, 0, 1); 

		fAppearanceColors.fColorEditor = new ColorSelector(stylesLayouter.composite);
		Button foregroundColorButton = fAppearanceColors.fColorEditor.getButton();
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);

		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				AppearanceColorsItem item = fAppearanceColors.getSingleItem(fAppearanceColors.getSelectedItems());
				if (item != null) {
					PreferenceConverter.setValue(fOverlayStore, item.colorKey, fAppearanceColors.fColorEditor.getColorValue());
				}
			}
		});
	}
	private void createCodeAssistSection(Layouter parent) {
		
		Group group = parent.addGroup(Messages.Editors_CodeAssist);
		Layouter layouter = new Layouter(group, 2);
		FieldManager manager = getManager(layouter);

		layouter.addSmallFiller();
		manager.addCheckBox(Messages.Editors_CodeAssist_AutoInsert, 
				IStatetUIPreferenceConstants.CODEASSIST_AUTOINSERT);
	}

	@Override
	protected void updateControls() {
		super.updateControls();
		
		if (UIAccess.isOkToUse(fAppearanceColors.getStructuredViewer())) {
			fAppearanceColors.reselect();
		}
	}
}