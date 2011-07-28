/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.util.WorkbenchUIUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRModelManager;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.RCodeLaunching;
import de.walware.statet.r.launching.RCodeLaunching.SourceRegion;
import de.walware.statet.r.ui.RUI;


/**
 * Launch shortcut, which submits the commands (touched by selection)
 * and does not change the focus.
 * 
 * Supports only text editors with input supporting R AST.
 */
public class RunEntireCommandHandler extends AbstractHandler {
	
	
	protected static class Data {
		
		ISourceEditor editor;
		ITextSelection selection;
		AbstractDocument document;
		IRModelInfo model;
		AstInfo<?> ast;
		RAstNode[] nodes;
		IRSourceUnit su;
		
		List<SourceRegion> regions;
		
	}
	
	
	private final boolean fGotoConsole;
	
	
	public RunEntireCommandHandler() {
		this(false);
	}
	
	protected RunEntireCommandHandler(final boolean gotoConsole) {
		fGotoConsole = gotoConsole;
	}
	
	
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart workbenchPart = HandlerUtil.getActivePart(event);
		final AtomicReference<IStatus> success = new AtomicReference<IStatus>();
		
		try {
			if (workbenchPart instanceof IEditorPart) {
				final Data data = new Data();
				data.editor = (ISourceEditor) workbenchPart.getAdapter(ISourceEditor.class);
				if (data.editor != null) {
					data.selection = (ITextSelection) data.editor.getViewer().getSelection();
					if (data.selection != null) {
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
			return null;
		}
		
		final IStatus status = success.get();
		if (status != null
				&& status.getSeverity() != IStatus.OK && status.getSeverity() != IStatus.CANCEL) {
			WorkbenchUIUtil.indicateStatus(status, event);
		}
		return null;
	}
	
	protected String getErrorMessage() {
		return RLaunchingMessages.RCommandLaunch_error_message;
	}
	
	protected IStatus doLaunch(final Data data, final IProgressMonitor monitor)
			throws CoreException {
		{	final ISourceUnit su = data.editor.getSourceUnit();
			if (su instanceof IRSourceUnit) {
				data.su = (IRSourceUnit) su;
			}
			else {
				return LaunchShortcutUtil.createUnsupported();
			}
		}
		assert (data.su.getDocument(monitor) == data.editor.getViewer().getDocument());
		data.document = data.su.getDocument(monitor);
		
		monitor.subTask(RLaunchingMessages.RCodeLaunch_UpdateStructure_task);
		synchronized ((data.document instanceof ISynchronizable) ?
				((ISynchronizable) data.document).getLockObject() : data.document) {
			data.model = (IRModelInfo) data.su.getModelInfo(RModel.TYPE_ID,
					IRModelManager.MODEL_FILE, monitor );
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			if (data.model != null) {
				data.ast = data.model.getAst();
			}
			else {
				data.ast = data.su.getAstInfo(null, true, monitor);
			}
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			if (data.ast == null) {
				return LaunchShortcutUtil.createUnsupported();
			}
			final IStatus status = getRegions(data);
			if (!status.isOK() || data.regions == null) {
				return status;
			}
		}
		
		monitor.subTask(RLaunchingMessages.RCodeLaunch_SubmitCode_task);
		if (RCodeLaunching.runRCodeDirect(data.regions, fGotoConsole)) {
			postLaunch(data);
		}
		return Status.OK_STATUS;
	}
	
	protected IStatus getRegions(final Data data)
			throws CoreException {
		final RAstNode[] nodes = RAst.findDeepestCommands(data.ast.root,
				data.selection.getOffset(), data.selection.getOffset()+data.selection.getLength() );
		if (nodes == null || nodes.length == 0) {
			final RAstNode next = RAst.findNextCommands(data.ast.root,
					data.selection.getOffset()+data.selection.getLength() );
			if (next != null) {
				UIAccess.getDisplay().asyncExec(new Runnable() {
					public void run() {
						data.editor.selectAndReveal(next.getOffset(), 0);
					}
				});
			}
			return Status.OK_STATUS;
		}
		try {
			data.nodes = nodes;
			final List<SourceRegion> list = new ArrayList<RCodeLaunching.SourceRegion>(nodes.length);
			for (int i = 0; i < nodes.length; i++) {
				if (RAst.hasErrors(nodes[i])) {
					return new Status(IStatus.ERROR, RUI.PLUGIN_ID,
							RLaunchingMessages.RunCode_info_SyntaxError_message );
				}
				
				final SourceRegion region = new SourceRegion(data.su, data.document);
				region.setBegin(checkStart(data.document, nodes[i].getOffset()));
				region.setEnd(nodes[i].getOffset()+nodes[i].getLength());
				region.setCode(data.document.get(region.getOffset(), region.getLength()));
				region.setNode(nodes[i]);
				list.add(region);
			}
			data.regions = list;
			return Status.OK_STATUS;
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
	
	protected void postLaunch(final Data data) {
	}
	
}
