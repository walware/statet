/*******************************************************************************
 * Copyright (c) 2005-2009 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.nico.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;

import de.walware.ecommons.ui.text.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ui.util.ISettingsChangedHandler;

import de.walware.statet.base.ui.IStatetUICommandIds;
import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.ui.console.InputGroup;

import de.walware.statet.r.nico.RTool;
import de.walware.statet.r.nico.IncompleteInputPrompt;
import de.walware.statet.r.nico.ui.RConsolePage;
import de.walware.statet.r.ui.editors.InsertAssignmentAction;
import de.walware.statet.r.ui.editors.RSourceViewerConfigurator;


/**
 * R Console input line
 */
public class RInputGroup extends InputGroup implements ISettingsChangedHandler {
	
	
	private RSourceViewerConfigurator fRConfig;
	
	
	/**
	 */
	public RInputGroup(final RConsolePage page) {
		super(page);
	}
	
	
	@Override
	protected void onPromptUpdate(final Prompt prompt) {
		if ((prompt.meta & RTool.META_PROMPT_INCOMPLETE_INPUT) != 0) {
			final IncompleteInputPrompt p = (IncompleteInputPrompt) prompt;
			fDocument.setPrefix(p.previousInput);
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
	public void configureServices(final IHandlerService commands, final IContextService keys) {
		super.configureServices(commands, keys);
		
		keys.activateContext("de.walware.statet.r.contexts.REditorScope"); //$NON-NLS-1$
		
		IAction action;
		action = new InsertAssignmentAction(this);
		commands.activateHandler(IStatetUICommandIds.INSERT_ASSIGNMENT, new ActionHandler(action));
	}
	
}
