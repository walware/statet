/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.ui.sourceeditors.IEditorAdapter;
import de.walware.statet.nico.core.runtime.IRequireSynch;
import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.Queue;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.core.rsource.RIndentUtil;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.nico.IBasicRAdapter;
import de.walware.statet.r.ui.RUI;


/**
 * 
 */
public class RSelectionDirectAndPasteOutputLaunchShortcut implements ILaunchShortcut {
	
	
	private static class R implements IToolRunnable<IBasicRAdapter>, Runnable {
		
		private IEditorAdapter fEditor;
		private IDocument fDocument;
		private String[] fLines;
		private Position fPosition;
		private StringBuilder fOutput;
		
		
		public R(final IEditorAdapter editor) {
			fEditor = editor;
		}
		
		private boolean setupSource(final ITextSelection selection) {
			final SourceViewer viewer = fEditor.getSourceViewer();
			fDocument = viewer.getDocument();
			fLines = LaunchShortcutUtil.listLines(fDocument, selection);
			try {
				if (selection.getLength() > 0) {
					final int start = selection.getOffset();
					int end = start + selection.getLength();
					final char c = fDocument.getChar(end-1);
					if (c == '\n') {
						end--;
						if (end > 0 && fDocument.getChar(end-1) == '\r') {
							end--;
						}
					}
					else if (c == '\r') {
						end--;
					}
					fPosition = new Position(start, end - start);
				}
				else {
					final IRegion line = fDocument.getLineInformationOfOffset(selection.getOffset());
					fPosition = new Position(line.getOffset(), line.getLength());
				}
				fDocument.addPosition(fPosition);
				return true;
			}
			catch (final BadLocationException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1, "An error occurred preparing Run and Paste Output", e)); //$NON-NLS-1$
				return false;
			}
		}
		
		public void dispose() {
			if (fDocument != null && fPosition != null) {
				fDocument.removePosition(fPosition);
			}
			fEditor = null;
			fDocument = null;
			fPosition = null;
			fOutput = null;
			fLines = null;
		}
		
		public String getTypeId() {
			return "editor/run-and-paste"; //$NON-NLS-1$
		}
		
		public String getLabel() {
			return RLaunchingMessages.RunAndPasteLaunch_RTask_label;
		}
		
		public SubmitType getSubmitType() {
			return SubmitType.TOOLS;
		}
		
		public void changed(final int event) {
			if (event == Queue.ENTRIES_DELETE || event == Queue.ENTRIES_ABANDONED) {
				UIAccess.getDisplay().asyncExec(new Runnable() {
					public void run() {
						dispose();
					}
				});
			}
		}
		
		public void run(final IBasicRAdapter tools, final IProgressMonitor monitor)
				throws InterruptedException, CoreException {
			fOutput = new StringBuilder(200);
			final IStreamListener listener = new IStreamListener() {
				public void streamAppended(final String text, final IStreamMonitor monitor) {
					fOutput.append(text);
				}
			};
			final ToolController controller = tools.getController();
			try {
				controller.getStreams().getOutputStreamMonitor().addListener(listener);
				controller.getStreams().getErrorStreamMonitor().addListener(listener);
				for (int i = 0; i < fLines.length; i++) {
					if (monitor.isCanceled()) {
						return;
					}
					monitor.subTask(fLines[i]);
					tools.submitToConsole(fLines[i], monitor);
					if (tools instanceof IRequireSynch) {
						final Pattern pattern = ((IRequireSynch) tools).synch(monitor);
						if (pattern != null) {
							final Matcher matcher = pattern.matcher(fOutput);
							int idx = -1;
							while (matcher.find()) {
								idx = matcher.start();
							}
							if (idx >= 0) {
								fOutput.delete(idx, fOutput.length());
							}
						}
					}
				}
			}
			finally {
				controller.getStreams().getOutputStreamMonitor().removeListener(listener);
				controller.getStreams().getErrorStreamMonitor().removeListener(listener);
				UIAccess.getDisplay().asyncExec(this);
			}
		}
		
		public void run() {
			// After R in display
			final SourceViewer viewer = fEditor.getSourceViewer();
			if (!UIAccess.isOkToUse(viewer)
					|| (viewer.getDocument() != fDocument)
					|| fPosition.isDeleted()) {
				return;
			}
			
			final IWorkbenchPart part = fEditor.getWorkbenchPart();
			final IWorkbenchSiteProgressService progress = (IWorkbenchSiteProgressService) part.getSite().getAdapter(IWorkbenchSiteProgressService.class);;
			if (progress != null) {
				progress.incrementBusy();
			}
			
			try {
				final IRCoreAccess rCore = (IRCoreAccess) fEditor.getAdapter(IRCoreAccess.class);
				final RIndentUtil util = new RIndentUtil(fDocument, rCore.getRCodeStyle());
				
				final int indent = util.getMultilineIndentColumn(fDocument.getLineOfOffset(fPosition.getOffset()),
						fDocument.getLineOfOffset(fPosition.getOffset() + fPosition.getLength()));
				final String delimiter = TextUtilities.getDefaultLineDelimiter(fDocument);
				final String prefix = delimiter + util.createIndentString(indent) + "# "; //$NON-NLS-1$
				
				final String[] lines = RUtil.LINE_SEPARATOR_PATTERN.split(fOutput);
				final int size = fOutput.length() + lines.length*(prefix.length()-1) + 2;
				fOutput.setLength(0);
				fOutput.ensureCapacity(size);
				for (int i = 0; i < lines.length; i++) {
					fOutput.append(prefix);
					fOutput.append(lines[i]);
				}
				fOutput.append(delimiter);
				final int pos = fPosition.getOffset()+fPosition.getLength();
				fDocument.replace(pos, 0, fOutput.toString());
				viewer.revealRange(pos, fOutput.length());
				if (progress != null) {
					progress.warnOfContentChange();
				}
			} catch (final BadLocationException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
						RLaunchingMessages.RunAndPasteLaunch_error_WhenPasting_message, e),
						StatusManager.LOG | StatusManager.SHOW);
			}
			finally {
				dispose();
				if (progress != null) {
					progress.decrementBusy();
				}
			}
			
		}
	}
	
	
	public RSelectionDirectAndPasteOutputLaunchShortcut() {
	}
	
	
	public void launch(final ISelection selection, final String mode) {
	}
	
	public void launch(final IEditorPart editor, final String mode) {
		final IEditorAdapter adapter = (IEditorAdapter) editor.getAdapter(IEditorAdapter.class);
		if (adapter != null) {
			runAndPasteSelection(adapter);
		}
	}
	
	private void runAndPasteSelection(final IEditorAdapter editor) {
		if (!editor.isEditable(true)) {
			cancel(editor, null, RLaunchingMessages.RunAndPasteLaunch_info_WriteProtected_status);
			return;
		}
		final SourceViewer viewer = editor.getSourceViewer();
		final ITextSelection selection = (ITextSelection) viewer.getSelection();
		final R r = new R(editor);
		if (!r.setupSource(selection)) {
			cancel(editor, r, RLaunchingMessages.RunAndPasteLaunch_error_Unspecific_status);
			return;
		}
		try {
			final ToolProcess process = NicoUI.getToolRegistry().getActiveToolSession(UIAccess.getActiveWorkbenchPage(true)).getProcess();
			final ToolController controller = NicoUITools.accessTool("R", process); //$NON-NLS-1$
			controller.submit(r);
		}
		catch (final CoreException e) {
			cancel(editor, r, e.getStatus().getMessage());
			return;
		}
	}
	
	private void cancel(final IEditorAdapter editor, final R r, final String message) {
		if (r != null) {
			r.dispose();
		}
		Display.getCurrent().beep();
		if (message != null) {
			editor.setStatusLineErrorMessage(message);
		}
	}
	
}
