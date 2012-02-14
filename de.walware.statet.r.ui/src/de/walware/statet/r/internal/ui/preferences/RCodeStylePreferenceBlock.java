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
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.IStatusChangeListener;
import de.walware.ecommons.databinding.IntegerValidator;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.ui.ManagedConfigurationBlock;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCodeStyleSettings.IndentationType;


/**
 * A PreferenceBlock for RCodeStyleSettings (code formatting preferences).
 */
public class RCodeStylePreferenceBlock extends ManagedConfigurationBlock {
	// in future supporting multiple profiles?
	// -> we bind to bean not to preferences
	
	
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
	
	private Button fWSArgAssignBefore;
	private Button fWSArgAssignBehind;
	private Button fNLFDefBodyBlockBefore;
	
	
	public RCodeStylePreferenceBlock(final IProject project, final IStatusChangeListener statusListener) {
		super(project, statusListener);
	}
	
	
	@Override
	protected void createBlockArea(final Composite pageComposite) {
		final Map<Preference, String> prefs = new HashMap<Preference, String>();
		
		prefs.put(RCodeStyleSettings.PREF_TAB_SIZE, RCodeStyleSettings.INDENT_GROUP_ID);
		prefs.put(RCodeStyleSettings.PREF_INDENT_DEFAULT_TYPE, RCodeStyleSettings.INDENT_GROUP_ID);
		prefs.put(RCodeStyleSettings.PREF_INDENT_SPACES_COUNT, RCodeStyleSettings.INDENT_GROUP_ID);
		prefs.put(RCodeStyleSettings.PREF_INDENT_BLOCK_DEPTH, RCodeStyleSettings.INDENT_GROUP_ID);
		prefs.put(RCodeStyleSettings.PREF_INDENT_GROUP_DEPTH, RCodeStyleSettings.INDENT_GROUP_ID);
		prefs.put(RCodeStyleSettings.PREF_INDENT_WRAPPED_COMMAND_DEPTH, RCodeStyleSettings.INDENT_GROUP_ID);
		prefs.put(RCodeStyleSettings.PREF_REPLACE_TABS_WITH_SPACES, RCodeStyleSettings.INDENT_GROUP_ID);
		prefs.put(RCodeStyleSettings.PREF_REPLACE_CONVERSATIVE, RCodeStyleSettings.INDENT_GROUP_ID);
		
		prefs.put(RCodeStyleSettings.PREF_WS_ARGASSIGN_BEFORE, RCodeStyleSettings.WS_GROUP_ID);
		prefs.put(RCodeStyleSettings.PREF_WS_ARGASSIGN_BEHIND, RCodeStyleSettings.WS_GROUP_ID);
		prefs.put(RCodeStyleSettings.PREF_NL_FDEF_BODYBLOCK_BEFORE, RCodeStyleSettings.WS_GROUP_ID);
		
		setupPreferenceManager(prefs);
		
		fModel = new RCodeStyleSettings(true);
		
		final Composite mainComposite = new Composite(pageComposite, SWT.NONE);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout((LayoutUtil.applyCompositeDefaults(new GridLayout(), 2)));
		
		final TabFolder folder = new TabFolder(mainComposite, SWT.NONE);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		{	final TabItem item = new TabItem(folder, SWT.NONE);
			item.setText(Messages.RCodeStyle_Indent_group);
			final Composite composite = new Composite(folder, SWT.NONE);
			item.setControl(composite);
			createIndentControls(composite);
		}
		{	final TabItem item = new TabItem(folder, SWT.NONE);
			item.setText("&More");
			final Composite composite = new Composite(folder, SWT.NONE);
			item.setControl(composite);
			createMoreControls(composite);
		}
		
		initBindings();
		updateControls();
	}
	
	private void createIndentControls(final Composite composite) {
		composite.setLayout(LayoutUtil.applyTabDefaults(new GridLayout(), 2));
		GridData gd;
		Label label;
		
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.RCodeStyle_Indent_DefaultType_label);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fIndentPolicy = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		fIndentPolicy.setContentProvider(new ArrayContentProvider());
		final IndentationType[] items = new IndentationType[] { IndentationType.TAB, IndentationType.SPACES };
		final String[] itemLabels = new String[] { Messages.RCodeStyle_Indent_Type_UseTabs_name, Messages.RCodeStyle_Indent_Type_UseSpaces_name };
		fIndentPolicy.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(final Object element) {
				final IndentationType t = (IndentationType) element;
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
		
		fConserveIndent = new Button(composite, SWT.CHECK);
		fConserveIndent.setText(Messages.RCodeStyle_Indent_ConserveExisting_label);
		fConserveIndent.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.RCodeStyle_TabSize_label);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fTabSize = new Text(composite, SWT.RIGHT | SWT.BORDER | SWT.SINGLE);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = LayoutUtil.hintWidth(fTabSize, 2);
		fTabSize.setLayoutData(gd);
		
		label = fIndentSpaceCountLabel = new Label(composite, SWT.NONE);
		label.setText(Messages.RCodeStyle_Indent_NumOfSpaces_label);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fIndentSpaceCount = new Text(composite, SWT.RIGHT | SWT.SINGLE | SWT.BORDER);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = LayoutUtil.hintWidth(fIndentSpaceCount, 2);
		fIndentSpaceCount.setLayoutData(gd);
		
		fReplaceOtherTabs = new Button(composite, SWT.CHECK);
		fReplaceOtherTabs.setText(Messages.RCodeStyle_Indent_ReplaceOtherTabs_label);
		fReplaceOtherTabs.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		
		LayoutUtil.addSmallFiller(composite, false);
		
		final Composite depthComposite = new Composite(composite, SWT.NONE);
		depthComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		depthComposite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 3));
		fIndentBlockDepth = createIndentDepthLine(depthComposite, Messages.RCodeStyle_Indent_IndentInBlocks_label);
		fIndentGroupDepth = createIndentDepthLine(depthComposite, Messages.RCodeStyle_Indent_IndentInGroups_label);
		fIndentWrappedCommandDepth = createIndentDepthLine(depthComposite, Messages.RCodeStyle_Indent_IndentOfWrappedCommands_label);
	}
	
	private Text createIndentDepthLine(final Composite composite, final String label) {
		final Label labelControl = new Label(composite, SWT.LEFT);
		labelControl.setText(label);
		labelControl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		final Text textControl = new Text(composite, SWT.RIGHT | SWT.SINGLE | SWT.BORDER);
		final GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = LayoutUtil.hintWidth(textControl, 2);
		textControl.setLayoutData(gd);
		final Label typeControl = new Label(composite, SWT.LEFT);
		typeControl.setText(Messages.RCodeStyle_Indent_Strategy_Levels_label);
		typeControl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		return textControl;
	}
	
	private void createMoreControls(final Composite composite) {
		composite.setLayout(LayoutUtil.applyTabDefaults(new GridLayout(), 2));
		
		fWSArgAssignBefore = new Button(composite, SWT.CHECK);
		fWSArgAssignBefore.setText(Messages.RCodeStyle_Whitespace_ArgAssign_Before_message);
		fWSArgAssignBefore.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		fWSArgAssignBehind = new Button(composite, SWT.CHECK);
		fWSArgAssignBehind.setText(Messages.RCodeStyle_Whitespace_ArgAssign_Behind_message);
		fWSArgAssignBehind.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		LayoutUtil.addSmallFiller(composite, false);
		
		fNLFDefBodyBlockBefore = new Button(composite, SWT.CHECK);
		fNLFDefBodyBlockBefore.setText(Messages.RCodeStyle_Newline_FDefBodyBlock_Before_message);
		fNLFDefBodyBlockBefore.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
	}
	
	@Override
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
		dbc.bindValue(SWTObservables.observeText(fTabSize, SWT.Modify),
				BeansObservables.observeValue(realm, fModel, RCodeStyleSettings.PROP_TAB_SIZE),
				new UpdateValueStrategy().setAfterGetValidator(new IntegerValidator(1, 32, Messages.RCodeStyle_TabSize_error_message)),
				null);
		
		final IObservableValue indentObservable = ViewersObservables.observeSingleSelection(fIndentPolicy);
		indentObservable.setValue(null);
		indentObservable.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(final ValueChangeEvent event) {
				final IndentationType t = (IndentationType) event.diff.getNewValue();
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
				new UpdateValueStrategy().setAfterGetValidator(new IntegerValidator(1, 32, Messages.RCodeStyle_Indent_NumOfSpaces_error_message)),
				null);
		dbc.bindValue(SWTObservables.observeText(fIndentBlockDepth, SWT.Modify),
				BeansObservables.observeValue(realm, fModel, RCodeStyleSettings.PROP_INDENT_BLOCK_DEPTH),
				new UpdateValueStrategy().setAfterGetValidator(new IntegerValidator(1, 10, Messages.RCodeStyle_Indent_IndentInBlocks_error_message)),
				null);
		dbc.bindValue(SWTObservables.observeText(fIndentGroupDepth, SWT.Modify),
				BeansObservables.observeValue(realm, fModel, RCodeStyleSettings.PROP_INDENT_GROUP_DEPTH),
				new UpdateValueStrategy().setAfterGetValidator(new IntegerValidator(1, 10, Messages.RCodeStyle_Indent_IndentInGroups_error_message)),
				null);
		dbc.bindValue(SWTObservables.observeText(fIndentWrappedCommandDepth, SWT.Modify),
				BeansObservables.observeValue(realm, fModel, RCodeStyleSettings.PROP_INDENT_WRAPPED_COMMAND_DEPTH),
				new UpdateValueStrategy().setAfterGetValidator(new IntegerValidator(1, 10, Messages.RCodeStyle_Indent_IndentOfWrappedCommands_error_message)),
				null);
		dbc.bindValue(SWTObservables.observeSelection(fReplaceOtherTabs),
				BeansObservables.observeValue(realm, fModel, RCodeStyleSettings.PROP_REPLACE_TABS_WITH_SPACES),
				null, null);
		
		dbc.bindValue(SWTObservables.observeSelection(fWSArgAssignBefore),
				BeansObservables.observeValue(realm, fModel, RCodeStyleSettings.PROP_WS_ARGASSIGN_BEFORE),
				null, null);
		dbc.bindValue(SWTObservables.observeSelection(fWSArgAssignBehind),
				BeansObservables.observeValue(realm, fModel, RCodeStyleSettings.PROP_WS_ARGASSIGN_BEHIND),
				null, null);
		dbc.bindValue(SWTObservables.observeSelection(fNLFDefBodyBlockBefore),
				BeansObservables.observeValue(realm, fModel, RCodeStyleSettings.PROP_NL_FDEF_BODYBLOCK_BEFORE),
				null, null);
	}
	
	@Override
	protected void updateControls() {
		fModel.load(this);
		fModel.resetDirty();
		getDbc().updateTargets();  // required for invalid target values
	}
	
	@Override
	protected void updatePreferences() {
		if (fModel.isDirty()) {
			fModel.resetDirty();
			setPrefValues(fModel.toPreferencesMap());
		}
	}
	
}
