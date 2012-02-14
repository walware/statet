/*******************************************************************************
 * Copyright (c) 2008-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launcher;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import de.walware.ecommons.ltk.ISourceStructElement;
import de.walware.ecommons.ltk.ui.util.LTKSelectionUtil;
import de.walware.ecommons.ltk.ui.util.WorkbenchUIUtil;

import de.walware.statet.r.core.refactoring.RRefactoringAdapter;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.RCodeLaunching;


/**
 * Launch shortcut, which submits 
 *  - the current line/selection directly to R (text editor)
 *  - code of selected model element (outline etc.)
 * and does not change the focus by default.
 * 
 * Low requirements: ITextSelection is sufficient
 */
public class RunSelectionHandler extends AbstractHandler implements IElementUpdater {
	
	
	private final boolean fGotoConsole;
	private RRefactoringAdapter fModelUtil;
	
	
	public RunSelectionHandler() {
		this(false); 
	}
	
	protected RunSelectionHandler(final boolean gotoConsole) {
		fGotoConsole = gotoConsole;
	}
	
	
	@Override
	public void updateElement(final UIElement element, final Map parameters) {
		element.setText(appendVariant(RLaunchingMessages.RunCode_OtherSelection_label));
	}
	
	protected String appendVariant(final String label) {
		return label;
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ISelection selection = WorkbenchUIUtil.getCurrentSelection(event.getApplicationContext());
		
		try {
			// text selection
			if (selection instanceof ITextSelection) {
				final String code = LaunchShortcutUtil.getSelectedCode(event);
				if (code != null) {
					RCodeLaunching.runRCodeDirect(code, fGotoConsole);
					return null;
				}
			}
			else
			// selection of model elements
			if (selection instanceof IStructuredSelection) {
				final ISourceStructElement[] elements = LTKSelectionUtil.getSelectedSourceStructElements(selection);
				if (elements != null) {
					if (fModelUtil == null) {
						fModelUtil = new RRefactoringAdapter();
					}
					final String code = fModelUtil.getSourceCodeStringedTogether(elements, null);
					
					RCodeLaunching.runRCodeDirect(code, fGotoConsole);
					return null;
				}
				
				final IFile[] files = LTKSelectionUtil.getSelectedFiles(selection);
				if (files != null) {
					final int last = files.length-1;
					for (int i = 0; i <= last; i++) {
						final String[] lines = LaunchShortcutUtil.getCodeLines(files[i]);
						RCodeLaunching.runRCodeDirect(lines[i], (i < last) && fGotoConsole);
					}
					return null;
				}
			}
		}
		catch (final CoreException e) {
			LaunchShortcutUtil.handleRLaunchException(e,
					RLaunchingMessages.RSelectionLaunch_error_message, event);
			return null;
		}
		
		LaunchShortcutUtil.handleUnsupportedExecution(event);
		return null;
	}
	
}
