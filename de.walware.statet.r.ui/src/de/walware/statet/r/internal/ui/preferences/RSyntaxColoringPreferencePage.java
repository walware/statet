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

package de.walware.statet.r.internal.ui.preferences;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.preference.IPreferenceStore;

import de.walware.eclipsecommons.preferences.PreferencesUtil;
import de.walware.eclipsecommons.ui.preferences.ConfigurationBlockPreferencePage;
import de.walware.eclipsecommons.ui.util.ColorManager;

import de.walware.statet.ext.ui.editors.StatextSourceViewerConfiguration;
import de.walware.statet.ext.ui.preferences.AbstractSyntaxColoringBlock;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RUIPreferenceConstants;
import de.walware.statet.r.ui.editors.RDocumentSetupParticipant;
import de.walware.statet.r.ui.editors.RSourceViewerConfiguration;


public class RSyntaxColoringPreferencePage extends ConfigurationBlockPreferencePage<AbstractSyntaxColoringBlock> {

		
	public RSyntaxColoringPreferencePage() {

		setPreferenceStore(RUIPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected AbstractSyntaxColoringBlock createConfigurationBlock() {

		AbstractSyntaxColoringBlock syntaxBlock = new AbstractSyntaxColoringBlock() {

			protected SyntaxNode[] createItems() {
				
				return new SyntaxNode[] {
						new CategoryNode(Messages.RSyntaxColoring_CodeCategory_label, new SyntaxNode[] {
								new StyleNode(Messages.RSyntaxColoring_Default_label, Messages.RSyntaxColoring_Default_description, RUIPreferenceConstants.R.TS_DEFAULT_ROOT, false),
								new StyleNode(Messages.RSyntaxColoring_Undefined_label, Messages.RSyntaxColoring_Undefined_description, RUIPreferenceConstants.R.TS_UNDEFINED_ROOT, false),
								new StyleNode(Messages.RSyntaxColoring_String_label, Messages.RSyntaxColoring_String_description, RUIPreferenceConstants.R.TS_STRING_ROOT, false),
								new StyleNode(Messages.RSyntaxColoring_Numbers_label, Messages.RSyntaxColoring_Numbers_description, RUIPreferenceConstants.R.TS_NUMBERS_ROOT, false),
								new StyleNode(Messages.RSyntaxColoring_SpecialConstants_label, Messages.RSyntaxColoring_SpecialConstants_description, RUIPreferenceConstants.R.TS_SPECIAL_CONSTANTS_ROOT, false),
								new StyleNode(Messages.RSyntaxColoring_LogicalConstants_label, Messages.RSyntaxColoring_LogicalConstants_description, RUIPreferenceConstants.R.TS_LOGICAL_CONSTANTS_ROOT, false),
								new StyleNode(Messages.RSyntaxColoring_Flowcontrol_label, Messages.RSyntaxColoring_Flowcontrol_description, RUIPreferenceConstants.R.TS_FLOWCONTROL_ROOT, false),
								new StyleNode(Messages.RSyntaxColoring_Separators_label, Messages.RSyntaxColoring_Separators_description, RUIPreferenceConstants.R.TS_SEPARATORS_ROOT, false),
								new StyleNode(Messages.RSyntaxColoring_Assignment_label, Messages.RSyntaxColoring_Assignment_description, RUIPreferenceConstants.R.TS_ASSIGNMENT_ROOT, false),
								new StyleNode(Messages.RSyntaxColoring_OtherOperators_label, Messages.RSyntaxColoring_OtherOperators_description, RUIPreferenceConstants.R.TS_OTHER_OPERATORS_ROOT, false),
								new StyleNode(Messages.RSyntaxColoring_Grouping_label, Messages.RSyntaxColoring_Grouping_description, RUIPreferenceConstants.R.TS_GROUPING_ROOT, false),
								new StyleNode(Messages.RSyntaxColoring_Indexing_label, Messages.RSyntaxColoring_Indexing_description, RUIPreferenceConstants.R.TS_INDEXING_ROOT, false),
						}),
						new CategoryNode(Messages.RSyntaxColoring_CommentsCategory_label, new SyntaxNode[] {
								new StyleNode(Messages.RSyntaxColoring_Comment_label, Messages.RSyntaxColoring_Comment_description, RUIPreferenceConstants.R.TS_COMMENT_ROOT, false),
								new StyleNode(Messages.RSyntaxColoring_taskTag_label, Messages.RSyntaxColoring_taskTag_description, RUIPreferenceConstants.R.TS_TASK_TAG_ROOT, false),
						}),
				};
			}
			
			@Override
			protected String getPreviewFileName() {

				return "RSyntaxColoringPreviewCode.txt"; //$NON-NLS-1$
			}

			@Override
			protected StatextSourceViewerConfiguration getSourceViewerConfiguration(
					ColorManager colorManager, IPreferenceStore store) {
				
				return new RSourceViewerConfiguration(null, colorManager, 
						RSourceViewerConfiguration.createCombinedPreferenceStore(
								store, PreferencesUtil.getDefaultPrefs())
						);
			}
			
			@Override
			protected IDocumentSetupParticipant getDocumentSetupParticipant() {
				
				return new RDocumentSetupParticipant();
			}
			
		};

		return syntaxBlock;
	}
	
}
