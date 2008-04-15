/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launcher;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.IDocumentModelProvider;
import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.RCodeLaunchRegistry;


/**
 * Launch shortcut, which submits the lowest enclosing function (assign of fdef)
 * and does not change the focus.
 * 
 * Supports only text editors with input supporting R AST.
 */
public class RFunctionDirectLaunchShortcut implements ILaunchShortcut {
	
	
	protected boolean fGotoConsole = false;
	
	
	public void launch(final ISelection selection, final String mode) {
		
		// not supported
	}
	
	public void launch(final IEditorPart editor, final String mode) {
		assert mode.equals("run"); //$NON-NLS-1$
		
		try {
			final ITextEditor textEditor = (ITextEditor) editor;
			final AtomicReference<ITextSelection> textSelection = new AtomicReference<ITextSelection>();
			UIAccess.getDisplay().syncExec(new Runnable() {
				public void run() {
					 textSelection.set((ITextSelection) textEditor.getSelectionProvider().getSelection());
				}
			});
			
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor)
						throws InvocationTargetException {
					try {
						doLaunch(textEditor, textSelection.get(), monitor);
					}
					catch (final CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (final InvocationTargetException e) {
			LaunchShortcutUtil.handleRLaunchException(e.getTargetException(),
					RLaunchingMessages.RFunctionLaunch_error_message);
		} catch (final InterruptedException e) {
			Thread.interrupted();
		}
	}
	
	private void doLaunch(final ITextEditor editor, final ITextSelection selection, final IProgressMonitor monitor)
			throws CoreException {
		final IDocumentProvider documentProvider = editor.getDocumentProvider();
		final IEditorInput editorInput = editor.getEditorInput();
		if (!(documentProvider instanceof IDocumentModelProvider)) {
			return;
		}
		final ISourceUnit unit = ((IDocumentModelProvider) documentProvider).getWorkingCopy(editorInput);
		if (unit == null) {
			return;
		}
		final IDocument doc = documentProvider.getDocument(editorInput);
		
		monitor.subTask(RLaunchingMessages.RCodeLaunch_UpdateStructure_task);
		final AstInfo astInfo = unit.getAstInfo("r", true, monitor); //$NON-NLS-1$
		if (monitor.isCanceled() || astInfo == null) {
			return;
		}
		final RAstNode node = RAst.findLowestFDefAssignment(astInfo.root, selection.getOffset());
		if (node == null) {
			return;
		}
		final String[] lines = LaunchShortcutUtil.listLines(doc, new TextSelection(doc, node.getOffset(), node.getStopOffset()-node.getOffset()));
		
		if (lines == null || monitor.isCanceled()) {
			return;
		}
		
		monitor.subTask(RLaunchingMessages.RCodeLaunch_SubmitCode_task);
		RCodeLaunchRegistry.runRCodeDirect(lines, fGotoConsole);
	}
	
}
