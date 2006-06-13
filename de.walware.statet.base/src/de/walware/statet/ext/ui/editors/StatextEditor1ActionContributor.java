/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.editors;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;


/**
 * Action contributors for Statext based editors.
 */
public class StatextEditor1ActionContributor extends TextEditorActionContributor {

	protected static final String M_NAVIGATE_GOTO = IWorkbenchActionConstants.M_NAVIGATE+"/"+IWorkbenchActionConstants.GO_TO;
	

	public StatextEditor1ActionContributor() {
		super();
	}
	

/* API **/

	@Override
	public void init(IActionBars bars) {
		
		super.init(bars);
	}
	
	public void contributeToMenu(IMenuManager menu) {

		super.contributeToMenu(menu);
		
//		IMenuManager editMenu = menu.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
//		if (editMenu != null) {
//		}
//		
//		IMenuManager navigateMenu = menu.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
//		if (navigateMenu != null) {
//		}
//		
//		IMenuManager gotoMenu = menu.findMenuUsingPath(M_NAVIGATE_GOTO);
//		if (gotoMenu != null) {
//		}
	}

	public void setActiveEditor(IEditorPart part) {

		super.setActiveEditor(part);
		
		IActionBars actionBars = getActionBars();
		IStatusLineManager statusLine = actionBars.getStatusLineManager();
		
		ITextEditor textEditor = null;
		if (part instanceof ITextEditor)
			textEditor = (ITextEditor) part;

		doSetActiveEditor(part, actionBars, statusLine, textEditor);
	}

	public void dispose() {

		IActionBars actionBars = getActionBars();
		IStatusLineManager statusLine = actionBars.getStatusLineManager();

		doSetActiveEditor(null, actionBars, statusLine, null);
		super.dispose();
	}

	
/* **/
	
	protected void doSetActiveEditor(IEditorPart part, IActionBars actionBars, IStatusLineManager statusLine, ITextEditor textEditor) {
		
		statusLine.setMessage(null);
		statusLine.setErrorMessage(null);

		actionBars.setGlobalActionHandler(StatextEditor1.ACTION_ID_GOTO_MATCHING_BRACKET, getAction(textEditor, StatextEditor1.ACTION_ID_GOTO_MATCHING_BRACKET));
		actionBars.setGlobalActionHandler(StatextEditor1.ACTION_ID_TOGGLE_COMMENT, getAction(textEditor, StatextEditor1.ACTION_ID_TOGGLE_COMMENT));
	}

}
