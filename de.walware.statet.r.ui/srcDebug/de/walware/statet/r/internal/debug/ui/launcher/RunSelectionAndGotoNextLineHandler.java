/*******************************************************************************
 * Copyright (c) 2008-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launcher;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import de.walware.ecommons.workbench.ui.WorkbenchUIUtil;

import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.launching.RCodeLaunching;


public class RunSelectionAndGotoNextLineHandler extends AbstractHandler {
	
	
	public RunSelectionAndGotoNextLineHandler() {
		super();
	}
	
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart workbenchPart = HandlerUtil.getActivePart(event);
		final ISelection selection = WorkbenchUIUtil.getCurrentSelection(event.getApplicationContext());
		
		try {
			if (workbenchPart instanceof ITextEditor && selection instanceof ITextSelection) {
				final ITextEditor editor = (ITextEditor) workbenchPart;
				final IDocumentProvider documentProvider = editor.getDocumentProvider();
				if (documentProvider == null) {
					return null;
				}
				final IDocument document = documentProvider.getDocument(editor.getEditorInput());
				if (document == null) {
					return null;
				}
				final String code = LaunchShortcutUtil.getSelectedCode(event);
				if (code != null) {
					RCodeLaunching.runRCodeDirect(code, false);
					final int newOffset = getNextLineOffset(document, ((ITextSelection) selection).getEndLine());
					if (newOffset >= 0) {
						editor.selectAndReveal(newOffset, 0);
					}
				}
				return null;
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
	
	private int getNextLineOffset(final IDocument doc, final int endLine) {
		try {
			if (endLine >= 0 && endLine+1 < doc.getNumberOfLines()) {
				return doc.getLineOffset(endLine+1);
			}
			else {
				return -1;
			}
		}
		catch (final BadLocationException e) {
			// don't show an error
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "Error while find next line.", e); //$NON-NLS-1$
			return -1;
		}
	}
	
}
