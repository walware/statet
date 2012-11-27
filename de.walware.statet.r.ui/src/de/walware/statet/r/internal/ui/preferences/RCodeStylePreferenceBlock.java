/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.IStatusChangeListener;
import de.walware.ecommons.databinding.IntegerValidator;
import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.ui.ManagedConfigurationBlock;
import de.walware.ecommons.text.ui.settings.IndentSettingsUI;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.core.RCodeStyleSettings;


/**
 * A PreferenceBlock for RCodeStyleSettings (code formatting preferences).
 */
public class RCodeStylePreferenceBlock extends ManagedConfigurationBlock {
	// in future supporting multiple profiles?
	// -> we bind to bean not to preferences
	
	
	private RCodeStyleSettings fModel;
	
	private IndentSettingsUI fStdIndentSettings;
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
		final Map<Preference<?>, String> prefs = new HashMap<Preference<?>, String>();
		
		prefs.put(RCodeStyleSettings.INDENT_DEFAULT_TYPE_PREF, RCodeStyleSettings.INDENT_GROUP_ID);
		prefs.put(RCodeStyleSettings.TAB_SIZE_PREF, RCodeStyleSettings.INDENT_GROUP_ID);
		prefs.put(RCodeStyleSettings.INDENT_SPACES_COUNT_PREF, RCodeStyleSettings.INDENT_GROUP_ID);
		prefs.put(RCodeStyleSettings.REPLACE_TABS_WITH_SPACES_PREF, RCodeStyleSettings.INDENT_GROUP_ID);
		prefs.put(RCodeStyleSettings.REPLACE_CONVERSATIVE_PREF, RCodeStyleSettings.INDENT_GROUP_ID);
		
		prefs.put(RCodeStyleSettings.INDENT_BLOCK_DEPTH_PREF, RCodeStyleSettings.INDENT_GROUP_ID);
		prefs.put(RCodeStyleSettings.INDENT_GROUP_DEPTH_PREF, RCodeStyleSettings.INDENT_GROUP_ID);
		prefs.put(RCodeStyleSettings.INDENT_WRAPPED_COMMAND_DEPTH_PREF, RCodeStyleSettings.INDENT_GROUP_ID);
		
		prefs.put(RCodeStyleSettings.WS_ARGASSIGN_BEFORE_PREF, RCodeStyleSettings.WS_GROUP_ID);
		prefs.put(RCodeStyleSettings.WS_ARGASSIGN_BEHIND_PREF, RCodeStyleSettings.WS_GROUP_ID);
		prefs.put(RCodeStyleSettings.NL_FDEF_BODYBLOCK_BEFORE_PREF, RCodeStyleSettings.WS_GROUP_ID);
		
		setupPreferenceManager(prefs);
		
		fModel = new RCodeStyleSettings(0);
		fStdIndentSettings = new IndentSettingsUI();
		
		final Composite mainComposite = new Composite(pageComposite, SWT.NONE);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout((LayoutUtil.applyCompositeDefaults(new GridLayout(), 2)));
		
		final TabFolder folder = new TabFolder(mainComposite, SWT.NONE);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		{	final TabItem item = new TabItem(folder, SWT.NONE);
			item.setText(fStdIndentSettings.getGroupLabel());
			item.setControl(createIndentControls(folder));
		}
		{	final TabItem item = new TabItem(folder, SWT.NONE);
			item.setText("&More");
			item.setControl(createMoreControls(folder));
		}
		
		initBindings();
		updateControls();
	}
	
	private Control createIndentControls(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.createTabGrid(2));
		
		fStdIndentSettings.createControls(composite);
		LayoutUtil.addSmallFiller(composite, false);
		
		final Composite depthComposite = new Composite(composite, SWT.NONE);
		depthComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		depthComposite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 3));
		fIndentBlockDepth = createIndentDepthLine(depthComposite, Messages.RCodeStyle_Indent_IndentInBlocks_label);
		fIndentGroupDepth = createIndentDepthLine(depthComposite, Messages.RCodeStyle_Indent_IndentInGroups_label);
		fIndentWrappedCommandDepth = createIndentDepthLine(depthComposite, Messages.RCodeStyle_Indent_IndentOfWrappedCommands_label);
		
		return composite;
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
		typeControl.setText(fStdIndentSettings.getLevelUnitLabel());
		typeControl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		return textControl;
	}
	
	private Control createMoreControls(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(LayoutUtil.createTabGrid(2));
		
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
		
		return composite;
	}
	
	@Override
	protected void addBindings(final DataBindingSupport db) {
		fStdIndentSettings.addBindings(db, fModel);
		
		db.getContext().bindValue(
				SWTObservables.observeText(fIndentBlockDepth, SWT.Modify),
				BeansObservables.observeValue(db.getRealm(), fModel, RCodeStyleSettings.INDENT_BLOCK_DEPTH_PROP),
				new UpdateValueStrategy().setAfterGetValidator(new IntegerValidator(1, 10, Messages.RCodeStyle_Indent_IndentInBlocks_error_message)),
				null );
		db.getContext().bindValue(
				SWTObservables.observeText(fIndentGroupDepth, SWT.Modify),
				BeansObservables.observeValue(db.getRealm(), fModel, RCodeStyleSettings.INDENT_GROUP_DEPTH_PROP),
				new UpdateValueStrategy().setAfterGetValidator(new IntegerValidator(1, 10, Messages.RCodeStyle_Indent_IndentInGroups_error_message)),
				null );
		db.getContext().bindValue(
				SWTObservables.observeText(fIndentWrappedCommandDepth, SWT.Modify),
				BeansObservables.observeValue(db.getRealm(), fModel, RCodeStyleSettings.INDENT_WRAPPED_COMMAND_DEPTH_PROP),
				new UpdateValueStrategy().setAfterGetValidator(new IntegerValidator(1, 10, Messages.RCodeStyle_Indent_IndentOfWrappedCommands_error_message)),
				null );
		
		db.getContext().bindValue(
				SWTObservables.observeSelection(fWSArgAssignBefore),
				BeansObservables.observeValue(db.getRealm(), fModel, RCodeStyleSettings.WS_ARGASSIGN_BEFORE_PROP) );
		db.getContext().bindValue(
				SWTObservables.observeSelection(fWSArgAssignBehind),
				BeansObservables.observeValue(db.getRealm(), fModel, RCodeStyleSettings.WS_ARGASSIGN_BEHIND_PROP) );
		db.getContext().bindValue(
				SWTObservables.observeSelection(fNLFDefBodyBlockBefore),
				BeansObservables.observeValue(db.getRealm(), fModel, RCodeStyleSettings.NL_FDEF_BODYBLOCK_BEFOREP_PROP) );
	}
	
	@Override
	protected void updateControls() {
		fModel.load(this);
		fModel.resetDirty();
		getDataBinding().getContext().updateTargets();  // required for invalid target values
	}
	
	@Override
	protected void updatePreferences() {
		if (fModel.isDirty()) {
			fModel.resetDirty();
			setPrefValues(fModel.toPreferencesMap());
		}
	}
	
}
