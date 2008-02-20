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
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.IDocumentModelProvider;
import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ltk.text.TextUtil;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.RCodeLaunchRegistry;


/**
 * Launch shortcut, which submits the commands (touched by selection)
 * and does not change the focus.
 * 
 * Supports only text editors with input supporting R AST.
 * 
 * TODO: error reporting
 */
public class RCommandDirectLaunchShortcut implements ILaunchShortcut {
	
	
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
	
	private void doLaunch(final ITextEditor textEditor, final ITextSelection selection, final IProgressMonitor monitor)
			throws CoreException, InvocationTargetException {
		final IEditorInput editorInput = textEditor.getEditorInput();
		final IDocumentProvider documentProvider = textEditor.getDocumentProvider();
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
		if (astInfo == null || monitor.isCanceled()) {
			return;
		}
		final RAstNode[] nodes = RAst.findDeepestCommands(astInfo.root, selection.getOffset(), selection.getOffset()+selection.getLength());
		if (nodes == null || nodes.length == 0) {
			return;
		}
		
		try {
			final int offset = checkStart(doc, nodes[0].getStartOffset());
			final int length = nodes[nodes.length-1].getStopOffset() - offset;
			final ArrayList<String> lines = new ArrayList<String>(doc.getNumberOfLines(offset, length));
			TextUtil.getLines(doc, offset, length, lines);
			
			if (lines == null || monitor.isCanceled()) {
				return;
			}
			
			monitor.subTask(RLaunchingMessages.RCodeLaunch_SubmitCode_task);
			if (RCodeLaunchRegistry.runRCodeDirect(lines.toArray(new String[lines.size()]), fGotoConsole)) {
				postLaunch(textEditor, doc, nodes);
			}
		}
		catch (final BadLocationException e) {
			throw new InvocationTargetException(e);
		}
	}
	
	private int checkStart(final IDocument doc, final int offset) throws BadLocationException {
		final int startLine = doc.getLineOfOffset(offset);
		final int lineOffset = doc.getLineOffset(startLine);
		if (offset == lineOffset) {
			return offset;
		}
		final String s = doc.get(lineOffset, offset-lineOffset);
		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			if (c != ' ' && c != '\t') {
				return offset;
			}
		}
		return lineOffset;
	}
	
	protected void postLaunch(final ITextEditor textEditor, final IDocument doc, final RAstNode[] nodes) {
	}
	
}
