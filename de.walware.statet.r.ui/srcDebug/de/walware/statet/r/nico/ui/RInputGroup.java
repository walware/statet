/*******************************************************************************
 * Copyright (c) 2005-2007 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;

import de.walware.statet.base.ui.IStatetUICommandIds;
import de.walware.statet.base.ui.util.ISettingsChangedHandler;
import de.walware.statet.ext.ui.editors.IEditorAdapter;
import de.walware.statet.ext.ui.editors.SourceViewerConfigurator;
import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.ui.console.InputGroup;
import de.walware.statet.r.nico.BasicR;
import de.walware.statet.r.nico.IncompleteInputPrompt;
import de.walware.statet.r.ui.editors.InsertAssignmentAction;
import de.walware.statet.r.ui.editors.RSourceViewerConfigurator;


/**
 * R Console input line
 */
class RInputGroup extends InputGroup implements ISettingsChangedHandler {
	
	
	private RSourceViewerConfigurator fRConfig;
	
	
	/**
	 * @param page
	 */
	RInputGroup(RConsolePage page) {
		super(page);
	}
	
	@Override
	protected void onPromptUpdate(Prompt prompt) {
		if ((prompt.meta & BasicR.META_PROMPT_INCOMPLETE_INPUT) != 0) {
			IncompleteInputPrompt p = (IncompleteInputPrompt) prompt;
			fDocument.setPrefix(p.previousInput);
		}
		else {
			fDocument.setPrefix(""); //$NON-NLS-1$
		}
	}
	
	@Override
	public Composite createControl(Composite parent, SourceViewerConfigurator editorConfig) {
		fRConfig = (RSourceViewerConfigurator) editorConfig;
		Composite control = super.createControl(parent, editorConfig);
		return control;
	}
	
	@Override
	public void configureServices(IHandlerService commands, IContextService keys) {
		super.configureServices(commands, keys);
		
		keys.activateContext("de.walware.statet.r.contexts.REditorScope");
		
		IAction action;
		action = new InsertAssignmentAction((IEditorAdapter) getConsolePage().getAdapter(IEditorAdapter.class));
		commands.activateHandler(IStatetUICommandIds.INSERT_ASSIGNMENT, new ActionHandler(action));
	}
	
}