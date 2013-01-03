/*******************************************************************************
 * Copyright (c) 2005-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.editors.text.EditorsUI;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfiguration;
import de.walware.ecommons.ltk.ui.util.CombinedPreferenceStore;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.Preference.StringArrayPref;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.ui.ConfigurationBlock;
import de.walware.ecommons.preferences.ui.ConfigurationBlockPreferencePage;
import de.walware.ecommons.text.ui.presentation.AbstractTextStylesConfigurationBlock;
import de.walware.ecommons.ui.ColorManager;

import de.walware.statet.base.ui.StatetUIServices;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.RUIPreferenceConstants;
import de.walware.statet.r.ui.editors.RDocumentSetupParticipant;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfiguration;
import de.walware.statet.r.ui.text.r.IRTextTokens;


public class RTextStylesPreferencePage extends ConfigurationBlockPreferencePage<ConfigurationBlock> {
	
	
	public RTextStylesPreferencePage() {
		setPreferenceStore(RUIPlugin.getDefault().getPreferenceStore());
	}
	
	@Override
	protected AbstractTextStylesConfigurationBlock createConfigurationBlock() {
		return new RTextStylesPreferenceBlock();
	}
	
}

class RTextStylesPreferenceBlock extends AbstractTextStylesConfigurationBlock {
	
	@Override
	protected SyntaxNode[] createItems() {
		final List<StyleNode> identifierChilds = new ArrayList<StyleNode>(5);
		identifierChilds.add(
				new StyleNode(Messages.RSyntaxColoring_Identifier_Assignment_label, getIdentifierItemsDescription(RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_ASSIGNMENT_ITEMS),
						RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_ASSIGNMENT_ROOT, new SyntaxNode.UseStyle[] {
						SyntaxNode.createUseNoExtraStyle(RUIPreferenceConstants.R.TS_DEFAULT_ROOT),
						SyntaxNode.createUseOtherStyle(RUIPreferenceConstants.R.TS_ASSIGNMENT_ROOT, Messages.RSyntaxColoring_Assignment_label),
						SyntaxNode.createUseCustomStyle() }, null));
		identifierChilds.add(
				new StyleNode(Messages.RSyntaxColoring_Identifier_Logical_label, getIdentifierItemsDescription(RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_LOGICAL_ITEMS),
						RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_LOGICAL_ROOT, new SyntaxNode.UseStyle[] {
						SyntaxNode.createUseNoExtraStyle(RUIPreferenceConstants.R.TS_DEFAULT_ROOT),
						SyntaxNode.createUseOtherStyle(RUIPreferenceConstants.R.TS_OPERATORS_SUB_LOGICAL_ROOT, Messages.RSyntaxColoring_Operators_Logical_label),
						SyntaxNode.createUseCustomStyle() }, null));
		identifierChilds.add(
				new StyleNode(Messages.RSyntaxColoring_Identifier_Flowcontrol_label, getIdentifierItemsDescription(RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_FLOWCONTROL_ITEMS),
						RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_FLOWCONTROL_ROOT, new SyntaxNode.UseStyle[] {
						SyntaxNode.createUseNoExtraStyle(RUIPreferenceConstants.R.TS_DEFAULT_ROOT),
						SyntaxNode.createUseOtherStyle(RUIPreferenceConstants.R.TS_FLOWCONTROL_ROOT, Messages.RSyntaxColoring_Flowcontrol_label),
						SyntaxNode.createUseCustomStyle() }, null));
//		if (getPreferenceStore().getString(RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_CUSTOM1_ITEMS).length() > 0) {
			identifierChilds.add(
					new StyleNode(Messages.RSyntaxColoring_Identifier_Custom1_label, getIdentifierItemsDescription(RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_CUSTOM1_ITEMS),
							RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_CUSTOM1_ROOT, new SyntaxNode.UseStyle[] {
						SyntaxNode.createUseNoExtraStyle(RUIPreferenceConstants.R.TS_DEFAULT_ROOT),
						SyntaxNode.createUseCustomStyle() }, null));
//		}
//		if (getPreferenceStore().getString(RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_CUSTOM2_ITEMS).length() > 0) {
			identifierChilds.add(
					new StyleNode(Messages.RSyntaxColoring_Identifier_Custom2_label, getIdentifierItemsDescription(RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_CUSTOM2_ITEMS),
							RUIPreferenceConstants.R.TS_IDENTIFIER_SUB_CUSTOM2_ROOT, new SyntaxNode.UseStyle[] {
						SyntaxNode.createUseNoExtraStyle(RUIPreferenceConstants.R.TS_DEFAULT_ROOT),
						SyntaxNode.createUseCustomStyle() }, null));
//		}
		
		return new SyntaxNode[] {
				new CategoryNode(Messages.RSyntaxColoring_CodeCategory_label, new SyntaxNode[] {
						new StyleNode(Messages.RSyntaxColoring_Default_label, Messages.RSyntaxColoring_Default_description,
								RUIPreferenceConstants.R.TS_DEFAULT_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, identifierChilds.toArray(new SyntaxNode[identifierChilds.size()])),
						new StyleNode(Messages.RSyntaxColoring_Undefined_label, Messages.RSyntaxColoring_Undefined_description,
								RUIPreferenceConstants.R.TS_UNDEFINED_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
						new StyleNode(Messages.RSyntaxColoring_String_label, Messages.RSyntaxColoring_String_description,
								RUIPreferenceConstants.R.TS_STRING_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
						new StyleNode(Messages.RSyntaxColoring_Numbers_label, Messages.RSyntaxColoring_Numbers_description,
								RUIPreferenceConstants.R.TS_NUMBERS_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, new SyntaxNode[] {
							new StyleNode(Messages.RSyntaxColoring_Numbers_Integer_label, Messages.RSyntaxColoring_Numbers_Integer_description,
									RUIPreferenceConstants.R.TS_NUMBERS_SUB_INT_ROOT, new SyntaxNode.UseStyle[] {
										SyntaxNode.createUseNoExtraStyle(RUIPreferenceConstants.R.TS_NUMBERS_ROOT),
										SyntaxNode.createUseCustomStyle() }, null),
							new StyleNode(Messages.RSyntaxColoring_Numbers_Complex_label, Messages.RSyntaxColoring_Numbers_Complex_description,
									RUIPreferenceConstants.R.TS_NUMBERS_SUB_CPLX_ROOT, new SyntaxNode.UseStyle[] {
										SyntaxNode.createUseNoExtraStyle(RUIPreferenceConstants.R.TS_NUMBERS_ROOT),
										SyntaxNode.createUseCustomStyle() }, null),
						}),
						new StyleNode(Messages.RSyntaxColoring_SpecialConstants_label, addListToTooltip(Messages.RSyntaxColoring_SpecialConstants_description, IRTextTokens.SPECIALCONST),
								RUIPreferenceConstants.R.TS_SPECIAL_CONSTANTS_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
						new StyleNode(Messages.RSyntaxColoring_LogicalConstants_label, addListToTooltip(Messages.RSyntaxColoring_LogicalConstants_description, IRTextTokens.LOGICALCONST),
								RUIPreferenceConstants.R.TS_LOGICAL_CONSTANTS_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
						new StyleNode(Messages.RSyntaxColoring_Flowcontrol_label, addListToTooltip(Messages.RSyntaxColoring_Flowcontrol_description, IRTextTokens.FLOWCONTROL),
								RUIPreferenceConstants.R.TS_FLOWCONTROL_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
						new StyleNode(Messages.RSyntaxColoring_Separators_label, addListToTooltip(Messages.RSyntaxColoring_Separators_description, IRTextTokens.SEPARATOR),
								RUIPreferenceConstants.R.TS_SEPARATORS_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
						new StyleNode(Messages.RSyntaxColoring_Assignment_label, addListToTooltip(Messages.RSyntaxColoring_Assignment_description, new String[] { "<-", "->", "<<-", "->>", addExtraStyleNoteToTooltip("= ({0})") }), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
								RUIPreferenceConstants.R.TS_ASSIGNMENT_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, new SyntaxNode[] {
							new StyleNode(Messages.RSyntaxColoring_Assignment_Equalsign_label, addListToTooltip(Messages.RSyntaxColoring_Assignment_Equalsign_description, new String[] { "=" }),  //$NON-NLS-1$
									RUIPreferenceConstants.R.TS_ASSIGNMENT_SUB_EQUALSIGN_ROOT, new SyntaxNode.UseStyle[] {
										SyntaxNode.createUseNoExtraStyle(RUIPreferenceConstants.R.TS_ASSIGNMENT_ROOT),
										SyntaxNode.createUseCustomStyle() }, null),
						}),
						new StyleNode(Messages.RSyntaxColoring_Operators_label, Messages.RSyntaxColoring_Operators_description,
								RUIPreferenceConstants.R.TS_OTHER_OPERATORS_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, new SyntaxNode[] {
							new StyleNode(Messages.RSyntaxColoring_Operators_Logical_label, addListToTooltip(Messages.RSyntaxColoring_Operators_Logical_description, IRTextTokens.OP_SUB_LOGICAL),
									RUIPreferenceConstants.R.TS_OPERATORS_SUB_LOGICAL_ROOT, new SyntaxNode.UseStyle[] {
									SyntaxNode.createUseNoExtraStyle(RUIPreferenceConstants.R.TS_OTHER_OPERATORS_ROOT),
									SyntaxNode.createUseCustomStyle() }, null),
							new StyleNode(Messages.RSyntaxColoring_Operators_Relational_label, addListToTooltip(Messages.RSyntaxColoring_Operators_Relational_description, IRTextTokens.OP_SUB_RELATIONAL),
									RUIPreferenceConstants.R.TS_OPERATORS_SUB_RELATIONAL_ROOT, new SyntaxNode.UseStyle[] {
									SyntaxNode.createUseNoExtraStyle(RUIPreferenceConstants.R.TS_OTHER_OPERATORS_ROOT),
									SyntaxNode.createUseCustomStyle() }, null),
							new StyleNode(Messages.RSyntaxColoring_Operators_Userdefined_label, addListToTooltip(Messages.RSyntaxColoring_Operators_Userdefined_description, new String[] { "%\u2026%" }),  //$NON-NLS-1$
									RUIPreferenceConstants.R.TS_OPERATORS_SUB_USERDEFINED_ROOT, new SyntaxNode.UseStyle[] {
									SyntaxNode.createUseNoExtraStyle(RUIPreferenceConstants.R.TS_OTHER_OPERATORS_ROOT),
									SyntaxNode.createUseCustomStyle() }, null),
						}),
						new StyleNode(Messages.RSyntaxColoring_Grouping_label, Messages.RSyntaxColoring_Grouping_description,
								RUIPreferenceConstants.R.TS_GROUPING_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
						new StyleNode(Messages.RSyntaxColoring_Indexing_label, Messages.RSyntaxColoring_Indexing_description,
								RUIPreferenceConstants.R.TS_INDEXING_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
				}),
				new CategoryNode(Messages.RSyntaxColoring_CommentsCategory_label, new SyntaxNode[] {
						new StyleNode(Messages.RSyntaxColoring_Comment_label, Messages.RSyntaxColoring_Comment_description,
								RUIPreferenceConstants.R.TS_COMMENT_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
						new StyleNode(Messages.RSyntaxColoring_TaskTag_label, Messages.RSyntaxColoring_TaskTag_description,
								RUIPreferenceConstants.R.TS_TASK_TAG_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
						new StyleNode(Messages.RSyntaxColoring_Roxygen_label, Messages.RSyntaxColoring_Roxygen_description,
								RUIPreferenceConstants.R.TS_ROXYGEN_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
						new StyleNode(Messages.RSyntaxColoring_RoxygenTag_label, Messages.RSyntaxColoring_RoxygenTag_description,
								RUIPreferenceConstants.R.TS_ROXYGEN_TAG_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
				}),
		};
	}
	
	private String getIdentifierItemsDescription(final String key) {
		final Preference<String[]> pref = new StringArrayPref(RUI.PLUGIN_ID, key);
		final String[] value = PreferencesUtil.getInstancePrefs().getPreferenceValue(pref);
		return addListToTooltip(Messages.RSyntaxColoring_Identifier_Items_description, value);
	}
	
	@Override
	protected String[] getSettingsGroups() {
		return new String[] { RUIPreferenceConstants.R.TS_GROUP_ID };
	}
	
	@Override
	protected String getPreviewFileName() {
		return "RTextStylesPreviewCode.txt"; //$NON-NLS-1$
	}
	
	@Override
	protected SourceEditorViewerConfiguration getSourceViewerConfiguration(
			final ColorManager colorManager, final IPreferenceStore store) {
		return new RSourceViewerConfiguration(null,
				RCore.getDefaultsAccess(),
				CombinedPreferenceStore.createStore(
						store,
						StatetUIServices.getBaseUIPreferenceStore(),
						EditorsUI.getPreferenceStore() ),
				colorManager);
	}
	
	@Override
	protected IDocumentSetupParticipant getDocumentSetupParticipant() {
		return new RDocumentSetupParticipant();
	}
	
	protected String addListToTooltip(final String tooltip, final RTerminal[] listItems) {
		return addListToTooltip(tooltip, RTerminal.textArray(listItems));
	}
	
}