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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.IDocumentModelProvider;
import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.RCodeLaunching;
import de.walware.statet.r.ui.RUI;


/**
 * Launch shortcut, which submits the commands (touched by selection)
 * and does not change the focus.
 * 
 * Supports only text editors with input supporting R AST.
 */
public class RunEntireCommandHandler extends AbstractHandler {
	
	
	protected static class TextData {
		
		ITextEditor editor;
		ITextSelection selection;
		IDocument document;
		AstInfo astInfo;
		Object modelElements;
		
	}
	
	
	private boolean fGotoConsole;
	
	
	public RunEntireCommandHandler() {
		this(false);
	}
	
	protected RunEntireCommandHandler(final boolean gotoConsole) {
		fGotoConsole = gotoConsole;
	}
	
	
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart workbenchPart = HandlerUtil.getActivePart(event);
		
		try {
			if (workbenchPart instanceof ITextEditor) {
				final ITextEditor textEditor = (ITextEditor) workbenchPart;
				final ISelection selection = textEditor.getSelectionProvider().getSelection();
				if (selection instanceof ITextSelection && 
						textEditor.getDocumentProvider() instanceof IDocumentModelProvider) {
					final TextData data = new TextData();
					data.editor = textEditor;
					data.selection = (ITextSelection) selection;
					
					final AtomicReference<Boolean> success = new AtomicReference<Boolean>();
					((IProgressService) workbenchPart.getSite().getService(IProgressService.class))
							.busyCursorWhile(new IRunnableWithProgress() {
						public void run(final IProgressMonitor monitor)
								throws InvocationTargetException {
							try {
								success.set(doLaunch(data, monitor));
							}
							catch (final CoreException e) {
								throw new InvocationTargetException(e);
							}
						}
					});
					if (success.get()) {
						return null;
					}
				}
			}
		}
		catch (final InvocationTargetException e) {
			LaunchShortcutUtil.handleRLaunchException(e.getTargetException(),
					getErrorMessage(), event);
			return null;
		}
		catch (final InterruptedException e) {
			Thread.interrupted();
			return null;
		}
		
		LaunchShortcutUtil.handleUnsupportedExecution(event);
		return null;
	}
	
	protected String getErrorMessage() {
		return RLaunchingMessages.RCommandLaunch_error_message;
	}
	
	protected boolean doLaunch(final TextData data, final IProgressMonitor monitor)
			throws CoreException {
		final IDocumentProvider documentProvider = data.editor.getDocumentProvider();
		final IEditorInput editorInput = data.editor.getEditorInput();
		if (!(documentProvider instanceof IDocumentModelProvider)) {
			return false;
		}
		final ISourceUnit unit = ((IDocumentModelProvider) documentProvider).getWorkingCopy(editorInput);
		if (unit == null) {
			return false;
		}
		data.document = documentProvider.getDocument(editorInput);
		
		monitor.subTask(RLaunchingMessages.RCodeLaunch_UpdateStructure_task);
		data.astInfo = unit.getAstInfo(RModel.TYPE_ID, true, monitor);
		if (monitor.isCanceled() || data.astInfo == null) {
			return false;
		}
		
		final String code = getCode(data);
		if (code != null) {
			monitor.subTask(RLaunchingMessages.RCodeLaunch_SubmitCode_task);
			if (RCodeLaunching.runRCodeDirect(code, fGotoConsole)) {
				postLaunch(data);
			}
		}
		return true;
	}
	
	protected String getCode(final TextData data)
			throws CoreException {
		final RAstNode[] nodes = RAst.findDeepestCommands(data.astInfo.root, data.selection.getOffset(), data.selection.getOffset()+data.selection.getLength());
		if (nodes == null || nodes.length == 0) {
			final RAstNode next = RAst.findNextCommands(data.astInfo.root, data.selection.getOffset()+data.selection.getLength());
			if (next != null) {
				UIAccess.getDisplay().syncExec(new Runnable() {
					public void run() {
						data.editor.selectAndReveal(next.getOffset(), 0);
					}
				});
			}
			return null;
		}
		try {
			final int start = checkStart(data.document, nodes[0].getOffset());
			final int end = nodes[nodes.length-1].getOffset()+nodes[nodes.length-1].getLength();
			final String code = data.document.get(start, end-start);
			data.modelElements = nodes;
			return code;
		}
		catch (final BadLocationException e) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					RLaunchingMessages.RunCode_error_WhenAnalyzingAndCollecting_message, e));
		}
	}
	
	protected int checkStart(final IDocument doc, final int offset) throws BadLocationException {
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
	
	protected void postLaunch(final TextData data) {
	}
	
}
