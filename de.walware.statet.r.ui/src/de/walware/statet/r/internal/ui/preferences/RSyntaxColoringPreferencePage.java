/*******************************************************************************
 * Copyright (c) 2005-2006 StatET-Project (www.walware.de/goto/statet).
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

import de.walware.eclipsecommons.ui.preferences.ConfigurationBlockPreferencePage;
import de.walware.eclipsecommons.ui.util.ColorManager;
import de.walware.eclipsecommons.preferences.PreferencesUtil;

import de.walware.statet.ext.ui.editors.StatextSourceViewerConfiguration;
import de.walware.statet.ext.ui.preferences.AbstractSyntaxColoringBlock;
import de.walware.statet.ext.ui.preferences.AbstractSyntaxColoringBlock.SyntaxItem;
import de.walware.statet.r.ui.RUiPlugin;
import de.walware.statet.r.ui.RUiPreferenceConstants;
import de.walware.statet.r.ui.editors.RDocumentSetupParticipant;
import de.walware.statet.r.ui.editors.RSourceViewerConfiguration;


public class RSyntaxColoringPreferencePage extends ConfigurationBlockPreferencePage<AbstractSyntaxColoringBlock> {

		
	public RSyntaxColoringPreferencePage() {

		setPreferenceStore(RUiPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected AbstractSyntaxColoringBlock createConfigurationBlock() {

		AbstractSyntaxColoringBlock syntaxBlock = new AbstractSyntaxColoringBlock() {

			@Override
			protected String getPreviewFileName() {

				return "RSyntaxColoringPreviewCode.txt";
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
		syntaxBlock.setupItems(
			new String[] { "Code", "Comments" },
			new SyntaxItem[][] { {
				new SyntaxItem(Messages.RSyntaxColoring_Default_label, Messages.RSyntaxColoring_Default_description, RUiPreferenceConstants.R.TS_DEFAULT_ROOT),
				new SyntaxItem(Messages.RSyntaxColoring_Undefined_label, Messages.RSyntaxColoring_Undefined_description, RUiPreferenceConstants.R.TS_UNDEFINED_ROOT),
				new SyntaxItem(Messages.RSyntaxColoring_String_label, Messages.RSyntaxColoring_String_description, RUiPreferenceConstants.R.TS_STRING_ROOT),
				new SyntaxItem(Messages.RSyntaxColoring_Numbers_label, Messages.RSyntaxColoring_Numbers_description, RUiPreferenceConstants.R.TS_NUMBERS_ROOT),
				new SyntaxItem(Messages.RSyntaxColoring_SpecialConstants_label, Messages.RSyntaxColoring_SpecialConstants_description, RUiPreferenceConstants.R.TS_SPECIAL_CONSTANTS_ROOT),
				new SyntaxItem(Messages.RSyntaxColoring_LogicalConstants_label, Messages.RSyntaxColoring_LogicalConstants_description, RUiPreferenceConstants.R.TS_LOGICAL_CONSTANTS_ROOT),
				new SyntaxItem(Messages.RSyntaxColoring_Flowcontrol_label, Messages.RSyntaxColoring_Flowcontrol_description, RUiPreferenceConstants.R.TS_FLOWCONTROL_ROOT),
				new SyntaxItem(Messages.RSyntaxColoring_Separators_label, Messages.RSyntaxColoring_Separators_description, RUiPreferenceConstants.R.TS_SEPARATORS_ROOT),
				new SyntaxItem(Messages.RSyntaxColoring_Assignment_label, Messages.RSyntaxColoring_Assignment_description, RUiPreferenceConstants.R.TS_ASSIGNMENT_ROOT),
				new SyntaxItem(Messages.RSyntaxColoring_OtherOperators_label, Messages.RSyntaxColoring_OtherOperators_description, RUiPreferenceConstants.R.TS_OTHER_OPERATORS_ROOT),
				new SyntaxItem(Messages.RSyntaxColoring_Grouping_label, Messages.RSyntaxColoring_Grouping_description, RUiPreferenceConstants.R.TS_GROUPING_ROOT),
				new SyntaxItem(Messages.RSyntaxColoring_Indexing_label, Messages.RSyntaxColoring_Indexing_description, RUiPreferenceConstants.R.TS_INDEXING_ROOT),
		}, {
				new SyntaxItem(Messages.RSyntaxColoring_Comment_label, Messages.RSyntaxColoring_Comment_description, RUiPreferenceConstants.R.TS_COMMENT_ROOT),
				new SyntaxItem(Messages.RSyntaxColoring_taskTag_label, Messages.RSyntaxColoring_Undefined_description, RUiPreferenceConstants.R.TS_TASK_TAG_ROOT)
		} } );

		return syntaxBlock;
	}
	
}
