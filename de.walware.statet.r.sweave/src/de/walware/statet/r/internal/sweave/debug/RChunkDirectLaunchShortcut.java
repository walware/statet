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

package de.walware.statet.r.internal.sweave.debug;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.texteditor.ITextEditor;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.text.TextUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.internal.sweave.Messages;
import de.walware.statet.r.internal.sweave.Rweave;
import de.walware.statet.r.internal.sweave.SweavePlugin;
import de.walware.statet.r.internal.sweave.editors.RweaveTexDocumentProvider;
import de.walware.statet.r.launching.RCodeLaunching;
import de.walware.statet.r.ui.RUI;


/**
 * Launch shortcut, which submits the chunks (touched by selection).
 * 
 * Supports only text editors.
 */
public class RChunkDirectLaunchShortcut implements ILaunchShortcut {
	
	
	protected boolean fGotoConsole = false;
	
	
	public RChunkDirectLaunchShortcut() {
	}
	
	
	public void launch(final ISelection selection, final String mode) {
		// not supported
	}
	
	public void launch(final IEditorPart editor, final String mode) {
		assert mode.equals("run"); //$NON-NLS-1$
		
		try {
			final IEditorInput editorInput = editor.getEditorInput();
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
						doLaunch(editorInput, textSelection.get(), monitor);
					}
					catch (final CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		}
		catch (final InvocationTargetException e) {
			StatusManager.getManager().handle(new Status(Status.ERROR, RUI.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING, Messages.RChunkLaunch_error_message, e.getTargetException()));
		} catch (final InterruptedException e) {
			Thread.interrupted();
		}
	}
	
	private void doLaunch(final IEditorInput editorInput, final ITextSelection selection, final IProgressMonitor monitor)
			throws CoreException {
		final RweaveTexDocumentProvider documentProvider = SweavePlugin.getDefault().getRTexDocumentProvider();
		documentProvider.connect(editorInput);
		try {
			final IDocument doc = documentProvider.getDocument(editorInput);
			if (!(doc instanceof IDocumentExtension3)) {
				// message
				return;
			}
			
			final ITypedRegion[] cats = Rweave.R_TEX_CAT_UTIL.getCats(doc, selection.getOffset(), selection.getLength());
			final ArrayList<String> lines = new ArrayList<String>();
			for (int i = 0; i < cats.length; i++) {
				final ITypedRegion cat = cats[i];
				if (cat.getType() == Rweave.R_CAT) {
					TextUtil.addLines(doc, cat.getOffset(), cat.getLength(), lines);
				}
			}
			
			if (monitor.isCanceled()) {
				return;
			}
			
			if (lines == null || lines.size() == 0 || monitor.isCanceled()) {
				return;
			}
			
			monitor.subTask(RLaunchingMessages.RCodeLaunch_SubmitCode_task);
			RCodeLaunching.runRCodeDirect(lines.toArray(new String[lines.size()]), fGotoConsole);
		}
		catch (final BadLocationException e) {
			throw new CoreException(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID,
					-1, "An error occurred when pick code lines.", e));
		}
		finally {
			documentProvider.disconnect(editorInput);
		}
	}
	
}
