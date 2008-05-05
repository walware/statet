/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launcher;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.ICodeLaunchContentHandler;
import de.walware.statet.r.launching.RCodeLaunchRegistry;


/**
 * Launch shortcut, which submits the whole script directly to R
 * and does not change the focus.
 */
public class RScriptDirectLaunchShortcut implements ILaunchShortcut {
	
	
	protected boolean fGotoConsole;
	
	
	public RScriptDirectLaunchShortcut() {
		this(false);
	}
	
	protected RScriptDirectLaunchShortcut(final boolean gotoConsole) {
		fGotoConsole = gotoConsole;
	}
	
	
	public void launch(final ISelection selection, final String mode) {
		assert mode.equals("run"); //$NON-NLS-1$
		
		try {
			final IStructuredSelection sel = (IStructuredSelection) selection;
			final Object firstElement = sel.getFirstElement();
			if (firstElement instanceof IFile) {
				doRun((IFile) firstElement);
			}
		}
		catch (final Exception e) {
			LaunchShortcutUtil.handleRLaunchException(e,
					RLaunchingMessages.RScriptLaunch_error_message);
		}
	}
	
	public void launch(final IEditorPart editor, final String mode) {
		assert mode.equals("run"); //$NON-NLS-1$
		
		try {
			if (editor instanceof AbstractTextEditor) {
				final AbstractTextEditor redt = (AbstractTextEditor) editor;
				final ICodeLaunchContentHandler handler = RCodeLaunchRegistry.getCodeLaunchContentHandler(
						LaunchShortcutUtil.getContentTypeId(redt.getEditorInput()));
				final IDocument document = redt.getDocumentProvider().getDocument(editor.getEditorInput() );
				final String[] lines = handler.getCodeLines(document);
				RCodeLaunchRegistry.runRCodeDirect(lines, fGotoConsole);
			}
		}
		catch (final Exception e) {
			LaunchShortcutUtil.handleRLaunchException(e,
					RLaunchingMessages.RScriptLaunch_error_message);
		}
	}
	
	
	private void doRun(final IFile file) throws Exception {
		final InputStream input = file.getContents();
		final String charset = file.getCharset();
		final BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset));
		final StringBuilder buffer = new StringBuilder();
		final char[] readBuffer = new char[2048];
		int n;
		while ((n = reader.read(readBuffer)) > 0) {
			buffer.append(readBuffer, 0, n);
		}
		
		final ICodeLaunchContentHandler handler = RCodeLaunchRegistry.getCodeLaunchContentHandler(
				LaunchShortcutUtil.getContentTypeId(file));
		final String[] lines = handler.getCodeLines(new Document(buffer.toString()));
		RCodeLaunchRegistry.runRCodeDirect(lines, fGotoConsole);
	}
	
}
