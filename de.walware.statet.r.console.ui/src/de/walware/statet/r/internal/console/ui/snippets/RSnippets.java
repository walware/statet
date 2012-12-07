/*******************************************************************************
 * Copyright (c) 2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui.snippets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ltk.ui.util.LTKWorkbenchUIUtil;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.ui.workbench.ResourceVariablesUtil;
import de.walware.ecommons.variables.core.DynamicVariable;
import de.walware.ecommons.variables.core.StringVariable;
import de.walware.ecommons.variables.core.VariableText2;

import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.internal.console.ui.RConsoleUIPlugin;
import de.walware.statet.r.launching.RCodeLaunching;
import de.walware.statet.r.ui.rtool.RElementNameVariableResolver;
import de.walware.statet.r.ui.rtool.RResourceEncodingVariableResolver;


public class RSnippets {
	
	
	public static final String RUN_SNIPPET_COMMAND_ID = "de.walware.statet.r.commands.RunRSnippet"; //$NON-NLS-1$
	public static final String RUN_LAST_COMMAND_ID = "de.walware.statet.r.commands.RunLastRSnippet"; //$NON-NLS-1$
	
	public static final String SNIPPET_PAR = "snippet"; //$NON-NLS-1$
	
	
	public static final IStringVariable RESOURCE_ENCODING_VARIABLE = new StringVariable(
			"resource_encoding", Messages.Variable_ResourceEncoding_description ); //$NON-NLS-1$
	
	public static final IStringVariable ECHO_ENABLED_VARIABLE = new StringVariable(
			"echo", Messages.Variable_Echo_description ); //$NON-NLS-1$
	
	
	private static final String[] PRECHECKED_NAMES = new String[] {
			"selected_text", //$NON-NLS-1$
			RElementNameVariableResolver.R_OBJECT_NAME_NAME,
	};
		
	private static void add(final Map<String, IStringVariable> map, final IStringVariable var) {
		map.put(var.getName(), var);
	}
	
	
	private ContextTypeRegistry fTemplatesContextTypeRegistry;
	private TemplateStore fTemplatesStore;
	
	private String fLastSnippetName;
	
	private final Runnable fLastSnippetRunnable = new Runnable() {
		@Override
		public void run() {
			final ICommandService service = (ICommandService) PlatformUI.getWorkbench()
					.getService(ICommandService.class);
			service.refreshElements(RUN_LAST_COMMAND_ID, null);
		}
	};
	
	
	public RSnippets() {
	}
	
	
	public List<IStringVariable> getVariables() {
		final IStringVariable[] variables = VariablesPlugin.getDefault()
				.getStringVariableManager().getVariables();
		
		final List<IStringVariable> all = new ArrayList<IStringVariable>(variables.length + 2);
		for (int i = 0; i < variables.length; i++) {
			all.add(variables[i]);
		}
		all.add(RESOURCE_ENCODING_VARIABLE);
		all.add(ECHO_ENABLED_VARIABLE);
		
		return all;
	}
	
	
	private synchronized void initTemplates() {
		if (fTemplatesContextTypeRegistry == null) {
			fTemplatesContextTypeRegistry = new ContributionContextTypeRegistry();
			fTemplatesContextTypeRegistry.addContextType(new RSnippetTemplatesContextType());
			
			fTemplatesStore = new ContributionTemplateStore(fTemplatesContextTypeRegistry,
					RConsoleUIPlugin.getDefault().getPreferenceStore(),
					RSnippetTemplatesContextType.TEMPLATES_KEY );
			try {
				fTemplatesStore.load();
			}
			catch (final IOException e) {
				RConsoleUIPlugin.log(new Status(IStatus.ERROR, RConsoleUIPlugin.PLUGIN_ID, -1,
						"An error occured when loading 'R snippet' template store.", e )); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Returns the template context type registry for the R snippets.
	 * 
	 * @return the template context type registry
	 */
	public synchronized ContextTypeRegistry getTemplateContextRegistry() {
		if (fTemplatesContextTypeRegistry == null) {
			initTemplates();
		}
		return fTemplatesContextTypeRegistry;
	}
	
	/**
	 * Returns the template store for the R snippets.
	 * 
	 * @return the template store
	 */
	public synchronized TemplateStore getTemplateStore() {
		if (fTemplatesStore == null) {
			initTemplates();
		}
		return fTemplatesStore;
	}
	
	
	private Map<String, IStringVariable> createResolveVariables() {
		final ResourceVariablesUtil util = new ResourceVariablesUtil();
		
		final Map<String, IStringVariable> variables = new HashMap<String, IStringVariable>();
		add(variables, new DynamicVariable.ResolverVariable(
				RSnippets.RESOURCE_ENCODING_VARIABLE, new RResourceEncodingVariableResolver(util)));
		add(variables, new EchoEnabledVariable());
		
		return variables;
	}
	
	private void addPrechecked(final Map<String, IStringVariable> variables) {
		final IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		for (int i = 0; i < PRECHECKED_NAMES.length; i++) {
			final IDynamicVariable real = manager.getDynamicVariable(PRECHECKED_NAMES[i]);
			if (real == null) {
				continue;
			}
			try {
				final String value = real.getValue(null);
				add(variables, new DynamicVariable.StaticVariable(real, value));
			}
			catch (final CoreException e) {
				add(variables, new DynamicVariable.UnresolvedVariable(real, e));
			}
		}
	}
	
	public String resolve(final Template template) throws CoreException {
		final VariableText2 text = new VariableText2(createResolveVariables()) {
			@Override
			protected String checkValue(final IStringVariable variable, final String value) {
				if (!"selected_text".equals(variable.getName()) //$NON-NLS-1$
						&& !RElementNameVariableResolver.R_OBJECT_NAME_NAME.equals(variable.getName()) ) {
					return RUtil.escapeCompletely(value);
				}
				return value;
			}
		};
		
		return text.performStringSubstitution(template.getPattern());
	}
	
	
	public IStatus validate(final TemplateContextType contextType, final String template) {
		try {
			final VariableText2 text = new VariableText2(createResolveVariables());
			
			text.validate(template, VariableText2.SYNTAX_SEVERITIES, null);
			return Status.OK_STATUS;
		}
		catch (final CoreException e) {
			return e.getStatus();
		}
	}
	
	public List<Template> validate(final Template[] templates) {
		final Map<String, IStringVariable> variables = createResolveVariables();
		addPrechecked(variables);
		
		final VariableText2 text = new VariableText2(variables); 
		
		final List<Template> tested = new ArrayList<Template>(templates.length);
		for (final Template template : templates) {
			try {
				text.validate(template.getPattern(), VariableText2.RESOLVE_SEVERITIES, null);
				tested.add(template);
			}
			catch (final CoreException e) {}
		}
		
		return tested;
	}
	
	
	public void setLastSnippet(final String name) {
		synchronized (fLastSnippetRunnable) {
			if ((fLastSnippetName != null) ? fLastSnippetName.equals(name) : null == name) {
				return;
			}
			fLastSnippetName = name;
		}
		UIAccess.getDisplay().asyncExec(fLastSnippetRunnable);
	}
	
	public String getLastSnippet() {
		synchronized (fLastSnippetRunnable) {
			return fLastSnippetName;
		}
	}
	
	public void run(final Template template, final ExecutionEvent event) {
		setLastSnippet(template.getName());
		try {
			final String snippet = resolve(template);
			RCodeLaunching.runRCodeDirect(snippet, false);
			
			return;
		}
		catch (final CoreException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR,
					RConsoleUIPlugin.PLUGIN_ID, 0,
					"An error occurred while submitting code snippet to R.\n" +
					"Template pattern:\n" + template.getPattern(), e ));
			LTKWorkbenchUIUtil.indicateStatus(e.getStatus(), event);
		}
	}
	
	
}
