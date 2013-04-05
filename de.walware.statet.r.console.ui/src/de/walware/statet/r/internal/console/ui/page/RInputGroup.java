/*******************************************************************************
 * Copyright (c) 2005-2013 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui.page;

import org.eclipse.core.commands.IHandler2;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IServiceLocator;

import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ui.LTKUI;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ui.ISettingsChangedHandler;
import de.walware.ecommons.ui.actions.HandlerCollection;

import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.ui.console.ConsolePageEditor;

import de.walware.statet.r.console.core.ContinuePrompt;
import de.walware.statet.r.console.core.IRBasicAdapter;
import de.walware.statet.r.ui.sourceediting.InsertAssignmentHandler;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfigurator;


/**
 * R Console input line
 */
public class RInputGroup extends ConsolePageEditor implements ISettingsChangedHandler {
	
	
	private RSourceViewerConfigurator fRConfig;
	
	
	public RInputGroup(final RConsolePage page) {
		super(page);
	}
	
	
	@Override
	protected ISourceUnit createSourceUnit() {
		return new RConsoleSourceUnit((RConsolePage) getConsolePage(), fDocument);
	}
	
	
	@Override
	protected void onPromptUpdate(final Prompt prompt) {
		if ((prompt.meta & IRBasicAdapter.META_PROMPT_INCOMPLETE_INPUT) != 0) {
			final ContinuePrompt p = (ContinuePrompt) prompt;
			fDocument.setPrefix(p.getPreviousInput());
		}
		else {
			fDocument.setPrefix(""); //$NON-NLS-1$
		}
	}
	
	@Override
	public Composite createControl(final Composite parent, final SourceEditorViewerConfigurator editorConfig) {
		fRConfig = (RSourceViewerConfigurator) editorConfig;
		final Composite control = super.createControl(parent, editorConfig);
		return control;
	}
	
	@Override
	public void initActions(final IServiceLocator serviceLocator, final HandlerCollection handlers) {
		super.initActions(serviceLocator, handlers);
		
		final IContextService contextService = (IContextService) serviceLocator.getService(IContextService.class);
		final IHandlerService handlerService = (IHandlerService) serviceLocator.getService(IHandlerService.class);
		
		contextService.activateContext("de.walware.statet.r.contexts.REditor"); //$NON-NLS-1$
		
		{	final IHandler2 handler = new InsertAssignmentHandler(this);
			handlerService.activateHandler(LTKUI.INSERT_ASSIGNMENT_COMMAND_ID, handler);
		}
	}
	
}
