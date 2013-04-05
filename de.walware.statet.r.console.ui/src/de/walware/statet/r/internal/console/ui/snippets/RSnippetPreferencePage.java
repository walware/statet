/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui.snippets;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.ui.StringVariableSelectionDialog.VariableFilter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ltk.ui.templates.CodeTemplatesConfigurationBlock;
import de.walware.ecommons.ltk.ui.templates.CodeTemplatesConfigurationBlock.TemplateStoreContribution;
import de.walware.ecommons.ltk.ui.templates.EditTemplateDialog;
import de.walware.ecommons.preferences.ui.ConfigurationBlock;
import de.walware.ecommons.preferences.ui.ConfigurationBlockPreferencePage;
import de.walware.ecommons.templates.TemplateVariableProcessor;
import de.walware.ecommons.ui.components.ButtonGroup;
import de.walware.ecommons.ui.components.CustomizableVariableSelectionDialog;
import de.walware.ecommons.ui.util.DialogUtil;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.console.ui.RConsoleUIPlugin;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.sourceediting.RTemplateSourceViewerConfigurator;


public class RSnippetPreferencePage
		extends ConfigurationBlockPreferencePage<ConfigurationBlock> {
	
	
	private static final String R_SNIPPET_GROUP_ID = "r.ConsoleSnippet"; //$NON-NLS-1$
	
	private static final List<String> GROUP_IDS = new ConstList<String>(
			R_SNIPPET_GROUP_ID
	);
	
	
	private static class RSnippetTemplateContribution extends TemplateStoreContribution {
		
		
		public RSnippetTemplateContribution(final RSnippets snippets) {
			super(snippets.getTemplateContextRegistry(), snippets.getTemplateStore() );
		}
		
		
		@Override
		public List<String> getGroups() {
			return GROUP_IDS;
		}
		
		@Override
		public TemplatePersistenceData[] getTemplates(final String groupId) {
			if (groupId == R_SNIPPET_GROUP_ID) {
				return super.getTemplates(groupId);
			}
			return null;
		}
		
		@Override
		public String getViewerConfiguraterId(final TemplatePersistenceData data) {
			return R_SNIPPET_GROUP_ID;
		}
		
		@Override
		public SourceEditorViewerConfigurator createViewerConfiguator(final TemplatePersistenceData data,
				final TemplateVariableProcessor templateProcessor, final IProject project) {
			return new RTemplateSourceViewerConfigurator(RCore.getWorkbenchAccess(),
					templateProcessor );
		}
		
	}
	
	private static class RSnippetEditDialog extends EditTemplateDialog {
		
		
		private final RSnippets fSnippets;
		
		
		private RSnippetEditDialog(final Shell parent, final Template template, final boolean edit, final int flags,
				final SourceEditorViewerConfigurator configurator,
				final TemplateVariableProcessor processor, final ContextTypeRegistry registry,
				final RSnippets snippets) {
			super(parent, template, edit, flags, configurator, processor, registry);
			fSnippets = snippets;
		}
		
		
		@Override
		protected IStatus validate(final TemplateContextType contextType, final String text) {
			return fSnippets.validate(contextType, text);
		}
		
		@Override
		protected void insertVariablePressed() {
			final CustomizableVariableSelectionDialog dialog = new CustomizableVariableSelectionDialog(getShell());
			final List<VariableFilter> filters = DialogUtil.DEFAULT_INTERACTIVE_FILTERS;
			for (final VariableFilter filter : filters) {
				dialog.addVariableFilter(filter);
			}
			dialog.addAdditional(RSnippets.ECHO_ENABLED_VARIABLE);
			dialog.addAdditional(RSnippets.RESOURCE_ENCODING_VARIABLE);
			
			if (dialog.open() != Dialog.OK) {
				return;
			}
			final String variable = dialog.getVariableExpression();
			if (variable == null) {
				return;
			}
			insertText(variable);
		}
	}
	
	private static class SnippetConfigurationBlock extends CodeTemplatesConfigurationBlock {
		
		
		private final ICommandService fCommandService;
		
		private final RSnippets fSnippets;
		
		
		public SnippetConfigurationBlock() throws CoreException {
			super(Messages.SnippetTemplates_title, true, null);
			
			fSnippets = RConsoleUIPlugin.getDefault().getRSnippets();
			init(new TemplateGroup[] {
					new TemplateGroup(R_SNIPPET_GROUP_ID,
							RConsoleUIPlugin.getDefault().getImageRegistry().get(RConsoleUIPlugin.IMG_OBJ_SNIPPETS),
							Messages.SnippetTemplates_RSnippet_label,
							RUI.getImage(RUI.IMG_OBJ_R_SCRIPT) ),
				},
				new ITemplateContribution[] {
					new RSnippetTemplateContribution(fSnippets),
				});
			
			fCommandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		}
		
		@Override
		protected void createBlockArea(final Composite pageComposite) {
			super.createBlockArea(pageComposite);
			
			final Link keyLink = addLinkControl(pageComposite, Messages.SnippetTemplates_KeysNote_label,
					new LinkSelectionListener() {
				private ParameterizedCommand fCommand;
				@Override
				protected Object getData(final SelectionEvent e) {
					if (fCommand == null) {
						final Command command = fCommandService.getCommand(RSnippets.SUBMIT_SNIPPET_COMMAND_ID);
						final List<TemplateItem> templates = getTemplates(getGroups().get(0));
						if (!templates.isEmpty()) {
							final String name = templates.get(0).getData().getTemplate().getName();
							final Map<String, String> parameters = Collections.singletonMap(RSnippets.SNIPPET_PAR, name);
							fCommand = ParameterizedCommand.generateCommand(command, parameters);
						}
					}
					return fCommand;
				}
			});
			keyLink.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		
		@Override
		protected EditTemplateDialog createEditDialog(final Template template, final int command,
				final SourceEditorViewerConfigurator configurator, final TemplateVariableProcessor processor,
				final ContextTypeRegistry registry) {
			return new RSnippetEditDialog(getShell(), template,
					((command & ButtonGroup.ADD_ANY) != 0), EditTemplateDialog.CUSTOM_TEMPLATE,
					configurator, processor, registry, fSnippets);
		}
		
	}
	
	
	@Override
	protected CodeTemplatesConfigurationBlock createConfigurationBlock() throws CoreException {
		return new SnippetConfigurationBlock();
	}
	
}
