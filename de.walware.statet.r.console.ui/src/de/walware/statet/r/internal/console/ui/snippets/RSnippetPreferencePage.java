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

import de.walware.jcommons.collections.ImCollections;

import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ltk.ui.templates.EditTemplateDialog;
import de.walware.ecommons.ltk.ui.templates.config.CodeTemplateConfigurationBlock;
import de.walware.ecommons.ltk.ui.templates.config.ITemplateCategoryConfiguration;
import de.walware.ecommons.ltk.ui.templates.config.ITemplateContribution;
import de.walware.ecommons.ltk.ui.templates.config.TemplateCategory;
import de.walware.ecommons.ltk.ui.templates.config.TemplateStoreContribution;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.ui.ConfigurationBlockPreferencePage;
import de.walware.ecommons.templates.TemplateVariableProcessor;
import de.walware.ecommons.ui.components.ButtonGroup;
import de.walware.ecommons.ui.components.CustomizableVariableSelectionDialog;
import de.walware.ecommons.ui.util.DialogUtil;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.console.ui.RConsoleUIPlugin;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.sourceediting.RTemplateSourceViewerConfigurator;


public class RSnippetPreferencePage extends ConfigurationBlockPreferencePage {
	
	
	public RSnippetPreferencePage() {
	}
	
	
	@Override
	protected CodeTemplateConfigurationBlock createConfigurationBlock() throws CoreException {
		return new SnippetConfigurationBlock();
	}
	
}


class SnippetConfigurationBlock extends CodeTemplateConfigurationBlock {
	
	
	private static final String R_SNIPPET_CATEGORY_ID = "r.ConsoleSnippet"; //$NON-NLS-1$
	
	
	private static class RSnippetTemplateConfiguration implements ITemplateCategoryConfiguration {
		
		
		private final RSnippets snippets;
		
		
		public RSnippetTemplateConfiguration(final RSnippets snippets) {
			this.snippets= snippets;
		}
		
		
		@Override
		public ITemplateContribution getTemplates() {
			return new TemplateStoreContribution(this.snippets.getTemplateStore());
		}
		
		@Override
		public Preference<String> getDefaultPref() {
			return null;
		}
		
		@Override
		public ContextTypeRegistry getContextTypeRegistry() {
			return this.snippets.getTemplateContextRegistry();
		}
		
		@Override
		public String getDefaultContextTypeId() {
			return RSnippetTemplatesContextType.TYPE_ID;
		}
		
		@Override
		public String getViewerConfigId(final TemplatePersistenceData data) {
			return RSnippetTemplatesContextType.TYPE_ID;
		}
		
		@Override
		public SourceEditorViewerConfigurator createViewerConfiguator(final String viewerConfigId,
				final TemplatePersistenceData data,
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
	
	
	private final ICommandService fCommandService;
	
	private final RSnippets fSnippets;
	
	
	public SnippetConfigurationBlock() throws CoreException {
		super(Messages.SnippetTemplates_title, ADD_ITEM, null);
		
		fSnippets= RConsoleUIPlugin.getDefault().getRSnippets();
		setCategories(	ImCollections.newList(
						new TemplateCategory(R_SNIPPET_CATEGORY_ID,
								RConsoleUIPlugin.getDefault().getImageRegistry().getDescriptor(RConsoleUIPlugin.IMG_OBJ_SNIPPETS),
								Messages.SnippetTemplates_RSnippet_label,
								RUI.getImageDescriptor(RUI.IMG_OBJ_R_SCRIPT),
								new RSnippetTemplateConfiguration(fSnippets) )
				));
		
		fCommandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
	}
	
	@Override
	protected String getListLabel() {
		return "Code Snippe&ts";
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
					final List<TemplateItem> templates = getTemplates(getCategories().get(0));
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
