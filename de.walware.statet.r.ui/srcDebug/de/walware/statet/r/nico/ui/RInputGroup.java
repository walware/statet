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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;

import de.walware.eclipsecommons.preferences.PreferencesUtil;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.core.preferences.PreferenceManageListener;
import de.walware.statet.base.ui.IStatetUICommandIds;
import de.walware.statet.ext.ui.editors.IEditorAdapter;
import de.walware.statet.ext.ui.editors.IEditorConfiguration;
import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.ui.console.InputGroup;
import de.walware.statet.nico.ui.console.NIConsolePage;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.nico.BasicR;
import de.walware.statet.r.nico.IncompleteInputPrompt;
import de.walware.statet.r.ui.editors.InsertAssignmentAction;


/**
 * R Console input line
 */
class RInputGroup extends InputGroup {
	
	
	private RCodeStyleSettings fRCodeStyle;
	private PreferenceManageListener fPreferenceManageListener;
	
	
	/**
	 * @param page
	 */
	RInputGroup(NIConsolePage page) {
		super(page);

		fRCodeStyle = new RCodeStyleSettings();
		fPreferenceManageListener = new PreferenceManageListener(fRCodeStyle, PreferencesUtil.getInstancePrefs(), RCodeStyleSettings.CONTEXT_ID);
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
	protected void createSourceViewer(IEditorConfiguration editorConfig) {
		super.createSourceViewer(editorConfig);
		
		fRCodeStyle.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (RCodeStyleSettings.PROP_TAB_SIZE.equals(event.getPropertyName())) {
					UIAccess.getDisplay().syncExec(new Runnable() {
						public void run() {
							getSourceViewer().setTabWidth(fRCodeStyle.getTabSize());
						}
					});
				}
			}
		});
		getSourceViewer().setTabWidth(fRCodeStyle.getTabSize());
	}
	
	@Override
	public void configureServices(IHandlerService commands, IContextService keys) {
		super.configureServices(commands, keys);
		
		IAction action;
		action = new InsertAssignmentAction((IEditorAdapter) getConsolePage().getAdapter(IEditorAdapter.class));
		commands.activateHandler(IStatetUICommandIds.INSERT_ASSIGNMENT, new ActionHandler(action));
	}
	
	@Override
	public void dispose() {
		if (fPreferenceManageListener != null) {
			fPreferenceManageListener.dispose();
			fPreferenceManageListener = null;
		}
		super.dispose();
	}
	
}