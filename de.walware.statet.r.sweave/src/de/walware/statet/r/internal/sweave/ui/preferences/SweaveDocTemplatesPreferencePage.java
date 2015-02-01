/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.ui.preferences;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;

import de.walware.ecommons.collections.ConstArrayList;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ltk.ui.templates.CodeTemplatesConfigurationBlock;
import de.walware.ecommons.ltk.ui.templates.CodeTemplatesConfigurationBlock.ITemplateContribution;
import de.walware.ecommons.ltk.ui.templates.CodeTemplatesConfigurationBlock.TemplateGroup;
import de.walware.ecommons.ltk.ui.templates.CodeTemplatesConfigurationBlock.TemplateStoreContribution;
import de.walware.ecommons.preferences.ui.ConfigurationBlockPreferencePage;
import de.walware.ecommons.templates.TemplateVariableProcessor;

import de.walware.docmlet.tex.core.TexCore;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.internal.sweave.LtxRweaveTemplateConfigurator;
import de.walware.statet.r.internal.sweave.NewSweaveDocGenerateWizardPage;
import de.walware.statet.r.internal.sweave.SweavePlugin;
import de.walware.statet.r.sweave.TexRweaveCoreAccess;


public class SweaveDocTemplatesPreferencePage extends ConfigurationBlockPreferencePage<CodeTemplatesConfigurationBlock> {
	
	
	private static final String LTK_RWEAVE_NEWDOC_GROUP_ID = "ltx-rweave.NewDoc"; //$NON-NLS-1$
	
	private static final List<String> GROUP_IDS = new ConstArrayList<String>(
			LTK_RWEAVE_NEWDOC_GROUP_ID
	);
	
	
	private static class LtxRweaveTemplateContribution extends TemplateStoreContribution {
		
		
		public LtxRweaveTemplateContribution() {
			super(SweavePlugin.getDefault().getSweaveDocTemplateContextRegistry(),
					SweavePlugin.getDefault().getSweaveDocTemplateStore() );
		}
		
		
		@Override
		public List<String> getGroups() {
			return GROUP_IDS;
		}
		
		@Override
		public TemplatePersistenceData[] getTemplates(final String groupId) {
			if (groupId == LTK_RWEAVE_NEWDOC_GROUP_ID) {
				return super.getTemplates(groupId);
			}
			return null;
		}
		
		@Override
		public String getViewerConfiguraterId(final TemplatePersistenceData data) {
			return LTK_RWEAVE_NEWDOC_GROUP_ID;
		}
		
		@Override
		public SourceEditorViewerConfigurator createViewerConfiguator(final TemplatePersistenceData data,
				final TemplateVariableProcessor templateProcessor, final IProject project) {
			final IRProject rProject = RProjects.getRProject(project);
			return new LtxRweaveTemplateConfigurator(new TexRweaveCoreAccess(
							TexCore.getWorkbenchAccess(),
							(rProject != null) ? rProject : RCore.getWorkbenchAccess() ),
					templateProcessor );
		}
		
	}
	
	
	@Override
	protected CodeTemplatesConfigurationBlock createConfigurationBlock() throws CoreException {
		return new CodeTemplatesConfigurationBlock(Messages.DocTemplates_title, true,
				new TemplateGroup[] {
					new TemplateGroup(LTK_RWEAVE_NEWDOC_GROUP_ID,
							SweavePlugin.getDefault().getImageRegistry().get(SweavePlugin.IMG_TOOL_NEW_LTXRWEAVE),
							Messages.DocTemplates_LtxRweave_label,
							SweavePlugin.getDefault().getImageRegistry().get(SweavePlugin.IMG_OBJ_LTXRWEAVE) ),
				},
				new ITemplateContribution[] {
					new LtxRweaveTemplateContribution(),
				},
				NewSweaveDocGenerateWizardPage.DEFAULT_NEWDOC_PREF );
	}
	
}
