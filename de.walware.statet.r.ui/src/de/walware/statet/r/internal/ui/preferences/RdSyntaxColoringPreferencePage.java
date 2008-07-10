/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.preferences;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.preference.IPreferenceStore;

import de.walware.eclipsecommons.ui.preferences.ConfigurationBlockPreferencePage;
import de.walware.eclipsecommons.ui.util.ColorManager;

import de.walware.statet.base.ui.sourceeditors.StatextSourceViewerConfiguration;
import de.walware.statet.ext.ui.preferences.AbstractSyntaxColoringBlock;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RUIPreferenceConstants;
import de.walware.statet.r.ui.editors.RSourceViewerConfiguration;
import de.walware.statet.r.ui.editors.RdDocumentSetupParticipant;
import de.walware.statet.r.ui.editors.RdSourceViewerConfiguration;


public class RdSyntaxColoringPreferencePage extends ConfigurationBlockPreferencePage<AbstractSyntaxColoringBlock> {
	
	
	public RdSyntaxColoringPreferencePage() {
		setPreferenceStore(RUIPlugin.getDefault().getPreferenceStore());
	}
	
	@Override
	protected AbstractSyntaxColoringBlock createConfigurationBlock() {
		final AbstractSyntaxColoringBlock syntaxBlock = new AbstractSyntaxColoringBlock() {
			
			@Override
			protected SyntaxNode[] createItems() {
				return new SyntaxNode[] {
						new CategoryNode(Messages.RdSyntaxColoring_CodeCategory_label, new SyntaxNode[] {
								new StyleNode(Messages.RdSyntaxColoring_Default_label, Messages.RdSyntaxColoring_Default_description, 
										RUIPreferenceConstants.Rd.TS_DEFAULT_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
//								new StyleNode(Messages.RdSyntaxColoring_Verbatim_label, Messages.RdSyntaxColoring_Verbatim_description, RUIPreferenceConstants.Rd.TS_VERBATIM_ROOT, false),
								new StyleNode(Messages.RdSyntaxColoring_SectionTag_label, Messages.RdSyntaxColoring_SectionTag_description, 
										RUIPreferenceConstants.Rd.TS_SECTION_TAG_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
								new StyleNode(Messages.RdSyntaxColoring_SubSectionTag_label, Messages.RdSyntaxColoring_SubSectionTag_description, 
										RUIPreferenceConstants.Rd.TS_SUBSECTION_TAG_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
								new StyleNode(Messages.RdSyntaxColoring_OtherTag_label, Messages.RdSyntaxColoring_OtherTag_description, 
										RUIPreferenceConstants.Rd.TS_OTHER_TAG_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
								new StyleNode(Messages.RdSyntaxColoring_UnlistedTag_label, Messages.RdSyntaxColoring_UnlistedTag_description, 
										RUIPreferenceConstants.Rd.TS_UNLISTED_TAG_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
								new StyleNode(Messages.RdSyntaxColoring_Brackets_label, Messages.RdSyntaxColoring_Brackets_description, 
										RUIPreferenceConstants.Rd.TS_BRACKETS_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
									
								new StyleNode(Messages.RdSyntaxColoring_PlatformSpecif_label, Messages.RdSyntaxColoring_PlatformSpecif_description, 
										RUIPreferenceConstants.Rd.TS_PLATFORM_SPECIF_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
						}),
						new CategoryNode(Messages.RdSyntaxColoring_CommentsCategory_label, new SyntaxNode[] {
								new StyleNode(Messages.RdSyntaxColoring_Comment_label, Messages.RdSyntaxColoring_Comment_description, 
										RUIPreferenceConstants.Rd.TS_COMMENT_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
								new StyleNode(Messages.RdSyntaxColoring_TaskTag_label, Messages.RdSyntaxColoring_TaskTag_description, 
										RUIPreferenceConstants.Rd.TS_TASK_TAG_ROOT, new SyntaxNode.UseStyle[] { SyntaxNode.createUseCustomStyle() }, null),
						}),
				};
			}
			
			@Override
			protected String[] getSettingsGroups() {
				return new String[] { RUIPreferenceConstants.Rd.TS_GROUP_ID };
			}
			
			@Override
			protected String getPreviewFileName() {
				return "RdSyntaxColoringPreviewCode.txt"; //$NON-NLS-1$
			}
			
			@Override
			protected StatextSourceViewerConfiguration getSourceViewerConfiguration(
					final ColorManager colorManager, final IPreferenceStore store) {
				return new RdSourceViewerConfiguration(RCore.getDefaultsAccess(),
						RSourceViewerConfiguration.createCombinedPreferenceStore(store), colorManager);
			}
			
			@Override
			protected IDocumentSetupParticipant getDocumentSetupParticipant() {
				return new RdDocumentSetupParticipant();
			}
			
		};
		
		return syntaxBlock;
	}
	
}
