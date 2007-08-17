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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import de.walware.eclipsecommons.preferences.Preference;
import de.walware.eclipsecommons.ui.databinding.NumberValidator;
import de.walware.eclipsecommons.ui.dialogs.IStatusChangeListener;
import de.walware.eclipsecommons.ui.util.LayoutUtil;

import de.walware.statet.ext.ui.preferences.ManagedConfigurationBlock;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCodeStyleSettings.IndentationType;


/**
 * A PreferenceBlock for RCodeStyleSettings (code formatting preferences).
 */
public class RCodeStylePreferenceBlock extends ManagedConfigurationBlock {
	
	
	private RCodeStyleSettings fModel;
	
	private Text fTabSize;
	private ComboViewer fIndentPolicy;
	private Button fConserveIndent;
	private Label fIndentSpaceCountLabel;
	private Text fIndentSpaceCount;
	private Button fReplaceOtherTabs;
	private Text fIndentBlockDepth;
	private Text fIndentGroupDepth;
	private Text fIndentWrappedCommandDepth;

	
	public RCodeStylePreferenceBlock(IProject project, IStatusChangeListener statusListener) {
		super(project, statusListener);
	}
	
	@Override
	protected String[] getChangedContexts() {
		return new String[] {
				RCodeStyleSettings.CONTEXT_ID,
		};
	}
	
	@Override
	public void createContents(Composite pageComposite, IWorkbenchPreferenceContainer container,
			IPreferenceStore preferenceStore) {
		super.createContents(pageComposite, container, preferenceStore);
		setupPreferenceManager(container, new Preference[] {
				RCodeStyleSettings.PREF_TAB_SIZE,
				RCodeStyleSettings.PREF_INDENT_DEFAULT_TYPE,
				RCodeStyleSettings.PREF_INDENT_SPACES_COUNT,
				RCodeStyleSettings.PREF_INDENT_BLOCK_DEPTH,
				RCodeStyleSettings.PREF_INDENT_GROUP_DEPTH,
				RCodeStyleSettings.PREF_INDENT_WRAPPED_COMMAND_DEPTH,
				RCodeStyleSettings.PREF_REPLACE_TABS_WITH_SPACES,
				RCodeStyleSettings.PREF_REPLACE_CONVERSATIVE,
		});
		fModel = new RCodeStyleSettings(true);
		
		Composite mainComposite = new Composite(pageComposite, SWT.NONE);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout((LayoutUtil.applyCompositeDefaults(new GridLayout(), 2)));
		
		Group group = new Group(mainComposite, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		group.setText(Messages.RCodeStyle_Indent_group);
		createIndentControls(group);
		
		createDbc();
		updateControls();
	}
	
	private void createIndentControls(Composite group) {
		group.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 2));
		GridData gd;
		Label label;
		
		label = new Label(group, SWT.NONE);
		label.setText(Messages.RCodeStyle_Indent_DefaultType_label);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fIndentPolicy = new ComboViewer(group, SWT.DROP_DOWN | SWT.READ_ONLY);
		fIndentPolicy.setContentProvider(new ArrayContentProvider());
		IndentationType[] items = new IndentationType[] { IndentationType.TAB, IndentationType.SPACES };
		final String[] itemLabels = new String[] { Messages.RCodeStyle_Indent_Type_UseTabs_name, Messages.RCodeStyle_Indent_Type_UseSpaces_name };
		fIndentPolicy.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				IndentationType t = (IndentationType) element;
				switch (t) {
				case TAB:
					return itemLabels[0];
				case SPACES:
					return itemLabels[1];
				}
				return null;
			}
		});
		fIndentPolicy.setInput(items);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = LayoutUtil.hintWidth(fIndentPolicy.getCombo(), itemLabels);
		fIndentPolicy.getCombo().setLayoutData(gd);
		fIndentPolicy.setSelection(new StructuredSelection(IndentationType.TAB));
		
		fConserveIndent = new Button(group, SWT.CHECK);
		fConserveIndent.setText(Messages.RCodeStyle_Indent_ConserveExisting_label);
		fConserveIndent.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		
		label = new Label(group, SWT.NONE);
		label.setText(Messages.RCodeStyle_TabSize_label);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fTabSize = new Text(group, SWT.RIGHT | SWT.BORDER | SWT.SINGLE);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = LayoutUtil.hintWidth(fTabSize, 2);
		fTabSize.setLayoutData(gd);

		label = fIndentSpaceCountLabel = new Label(group, SWT.NONE);
		label.setText(Messages.RCodeStyle_Indent_NumOfSpaces_label);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fIndentSpaceCount = new Text(group, SWT.RIGHT | SWT.SINGLE | SWT.BORDER);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = LayoutUtil.hintWidth(fIndentSpaceCount, 2);
		fIndentSpaceCount.setLayoutData(gd);
		
		fReplaceOtherTabs = new Button(group, SWT.CHECK);
		fReplaceOtherTabs.setText(Messages.RCodeStyle_Indent_ReplaceOtherTabs_label);
		fReplaceOtherTabs.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		
		LayoutUtil.addSmallFiller(group, false);
		
		Composite depthComposite = new Composite(group, SWT.NONE);
		depthComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		depthComposite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 3));
		fIndentBlockDepth = createIndentDepthLine(depthComposite, Messages.RCodeStyle_Indent_IndentInBlocks_label);
		fIndentGroupDepth = createIndentDepthLine(depthComposite, Messages.RCodeStyle_Indent_IndentInGroups_label);
		fIndentWrappedCommandDepth = createIndentDepthLine(depthComposite, Messages.RCodeStyle_Indent_IndentOfWrappedCommands_label);
	}
	
	private Text createIndentDepthLine(Composite composite, String label) {
		Label labelControl = new Label(composite, SWT.LEFT);
		labelControl.setText(label);
		labelControl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		Text textControl = new Text(composite, SWT.RIGHT | SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = LayoutUtil.hintWidth(textControl, 2);
		textControl.setLayoutData(gd);
		Label typeControl = new Label(composite, SWT.LEFT);
		typeControl.setText(Messages.RCodeStyle_Indent_Strategy_Levels_label);
		typeControl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		return textControl;
	}

	@Override
	protected void addBindings(DataBindingContext dbc, Realm realm) {
		dbc.bindValue(SWTObservables.observeText(fTabSize, SWT.Modify),
				BeansObservables.observeValue(realm, fModel, RCodeStyleSettings.PROP_TAB_SIZE),
				new UpdateValueStrategy().setAfterGetValidator(new NumberValidator(1, 32, Messages.RCodeStyle_TabSize_error_message)),
				null);
		
		IObservableValue indentObservable = ViewersObservables.observeSingleSelection(fIndentPolicy);
		indentObservable.setValue(null);
		indentObservable.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {
				IndentationType t = (IndentationType) event.diff.getNewValue();
				fIndentSpaceCountLabel.setEnabled(t == IndentationType.SPACES);
				fIndentSpaceCount.setEnabled(t == IndentationType.SPACES);
				fReplaceOtherTabs.setEnabled(t == IndentationType.SPACES);
			}
		});
		dbc.bindValue(indentObservable, BeansObservables.observeValue(realm, fModel, RCodeStyleSettings.PROP_INDENT_DEFAULT_TYPE),
				null, null);
		dbc.bindValue(SWTObservables.observeSelection(fConserveIndent),
				BeansObservables.observeValue(realm, fModel, RCodeStyleSettings.PROP_REPLACE_CONVERSATIVE),
				null, null);
		dbc.bindValue(SWTObservables.observeText(fIndentSpaceCount, SWT.Modify),
				BeansObservables.observeValue(realm, fModel, RCodeStyleSettings.PROP_INDENT_SPACES_COUNT),
				new UpdateValueStrategy().setAfterGetValidator(new NumberValidator(1, 32, Messages.RCodeStyle_Indent_NumOfSpaces_error_message)),
				null);
		dbc.bindValue(SWTObservables.observeText(fIndentBlockDepth, SWT.Modify),
				BeansObservables.observeValue(realm, fModel, RCodeStyleSettings.PROP_INDENT_BLOCK_DEPTH),
				new UpdateValueStrategy().setAfterGetValidator(new NumberValidator(1, 10, Messages.RCodeStyle_Indent_IndentInBlocks_error_message)),
				null);
		dbc.bindValue(SWTObservables.observeText(fIndentGroupDepth, SWT.Modify),
				BeansObservables.observeValue(realm, fModel, RCodeStyleSettings.PROP_INDENT_GROUP_DEPTH),
				new UpdateValueStrategy().setAfterGetValidator(new NumberValidator(1, 10, Messages.RCodeStyle_Indent_IndentInGroups_error_message)),
				null);
		dbc.bindValue(SWTObservables.observeText(fIndentWrappedCommandDepth, SWT.Modify),
				BeansObservables.observeValue(realm, fModel, RCodeStyleSettings.PROP_INDENT_WRAPPED_COMMAND_DEPTH),
				new UpdateValueStrategy().setAfterGetValidator(new NumberValidator(1, 10, Messages.RCodeStyle_Indent_IndentOfWrappedCommands_error_message)),
				null);
		dbc.bindValue(SWTObservables.observeSelection(fReplaceOtherTabs),
				BeansObservables.observeValue(realm, fModel, RCodeStyleSettings.PROP_REPLACE_TABS_WITH_SPACES),
				null, null);
	}
	
	@Override
	protected void updateControls() {
		fModel.load(this);
		fModel.resetDirty();
		getDbc().updateTargets();  // required for invalid target values
	}
	
	@Override
	protected void onBeforeSave() {
		if (fModel.isDirty()) {
			fModel.resetDirty();
			setPrefValues(fModel.toPreferencesMap());
		}
	}
}
