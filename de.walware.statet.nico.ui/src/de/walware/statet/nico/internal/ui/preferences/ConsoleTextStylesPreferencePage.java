/*=============================================================================#
 # Copyright (c) 2013-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.ui.preferences;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.ui.editors.text.EditorsUI;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfiguration;
import de.walware.ecommons.ltk.ui.util.CombinedPreferenceStore;
import de.walware.ecommons.preferences.ui.ConfigurationBlock;
import de.walware.ecommons.preferences.ui.ConfigurationBlockPreferencePage;
import de.walware.ecommons.preferences.ui.ScopedPreferenceStore;
import de.walware.ecommons.text.PartitionerDocumentSetupParticipant;
import de.walware.ecommons.text.ui.presentation.AbstractTextStylesConfigurationBlock;
import de.walware.ecommons.text.ui.settings.TextStyleManager;

import de.walware.statet.nico.ui.NicoUIPreferenceNodes;


public class ConsoleTextStylesPreferencePage extends ConfigurationBlockPreferencePage {
	
	
	public ConsoleTextStylesPreferencePage() {
		final ScopedPreferenceStore store= new ScopedPreferenceStore(InstanceScope.INSTANCE,
				NicoUIPreferenceNodes.CAT_CONSOLE_QUALIFIER );
		setPreferenceStore(store);
	}
	
	
	@Override
	protected ConfigurationBlock createConfigurationBlock() {
		return new ConsoleTextStylesPreferenceBlock();
	}
	
}

class ConsoleTextStylesPreferenceBlock extends AbstractTextStylesConfigurationBlock {
	
	
	public ConsoleTextStylesPreferenceBlock() {
	}
	
	
	@Override
	protected String getSettingsGroup() {
		return ConsolePreferences.OUTPUT_TEXTSTYLE_GROUP_ID;
	}
	
	@Override
	protected SyntaxNode[] createItems() {
		return new SyntaxNode[] {
			new StyleNode(Messages.TextStyle_Input_label, Messages.TextStyle_Input_description,
					ConsolePreferences.OUTPUT_INPUT_ROOT_KEY, new SyntaxNode.UseStyle[] {
						SyntaxNode.createUseCustomStyle()
					}, null ),
			new StyleNode(Messages.TextStyle_Info_label, Messages.TextStyle_Info_description,
					ConsolePreferences.OUTPUT_INFO_ROOT_KEY, new SyntaxNode.UseStyle[] {
						SyntaxNode.createUseCustomStyle()
					}, null ),
			new StyleNode(Messages.TextStyle_StandardOutput_label, Messages.TextStyle_StandardOutput_description,
					ConsolePreferences.OUTPUT_STANDARD_OUTPUT_ROOT_KEY, new SyntaxNode.UseStyle[] {
						SyntaxNode.createUseCustomStyle()
					}, new SyntaxNode[] {
				new StyleNode(Messages.TextStyle_SystemOutput_label, Messages.TextStyle_SystemOutput_description,
						ConsolePreferences.OUTPUT_SYSTEM_OUTPUT_ROOT_KEY, new SyntaxNode.UseStyle[] {
							SyntaxNode.createUseCustomStyle()
						}, null ),
			}),
			new StyleNode(Messages.TextStyle_StandardError_label, Messages.TextStyle_StandardError_description,
					ConsolePreferences.OUTPUT_STANDARD_ERROR_ROOT_KEY, new SyntaxNode.UseStyle[] {
						SyntaxNode.createUseCustomStyle()
					}, null ),
		};
	}
	
	@Override
	protected String getLinkMessage() {
		return Messages.TextStyle_link;
	}
	
	@Override
	protected boolean isTextAttributesSupported() {
		return false;
	}
	
	@Override
	protected String getPreviewFileName() {
		return "ConsoleTextStylesPreviewCode.txt"; //$NON-NLS-1$
	}
	
	@Override
	protected IDocumentSetupParticipant getDocumentSetupParticipant() {
		return new PartitionerDocumentSetupParticipant() {
			@Override
			public String getPartitioningId() {
				return ConsoleTextStylesPreviewPartitioner.PARTITIONING;
			}
			
			@Override
			protected IDocumentPartitioner createDocumentPartitioner() {
				return new ConsoleTextStylesPreviewPartitioner();
			}
		};
	}
	
	@Override
	protected SourceEditorViewerConfiguration getSourceEditorViewerConfiguration(
			final IPreferenceStore preferenceStore, final TextStyleManager textStyles) {
		return new ConsolePreviewSourceViewerConfiguration(
				CombinedPreferenceStore.createStore(
						preferenceStore,
//						StatetUIServices.getBaseUIPreferenceStore(),
						EditorsUI.getPreferenceStore() ),
				textStyles );
	}
	
}
