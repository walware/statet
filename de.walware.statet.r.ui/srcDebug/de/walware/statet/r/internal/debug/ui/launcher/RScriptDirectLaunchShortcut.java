/*******************************************************************************
 * Copyright (c) 2005-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import de.walware.ecommons.ltk.ui.util.LTKSelectionUtil;

import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.ICodeLaunchContentHandler;
import de.walware.statet.r.launching.RCodeLaunching;


/**
 * Launch shortcut, which submits the whole script directly to R
 * and does not change the focus.
 */
public class RScriptDirectLaunchShortcut implements ILaunchShortcut {
	
	
	private final boolean fGotoConsole;
	
	
	public RScriptDirectLaunchShortcut() {
		this(false);
	}
	
	protected RScriptDirectLaunchShortcut(final boolean gotoConsole) {
		fGotoConsole = gotoConsole;
	}
	
	
	@Override
	public void launch(final ISelection selection, final String mode) {
		assert mode.equals("run"); //$NON-NLS-1$
		
		try {
			final IFile[] files = LTKSelectionUtil.getSelectedFiles(selection);
			if (files != null) {
				final int last = files.length-1;
				for (int i = 0; i <= last; i++) {
					final String[] lines = LaunchShortcutUtil.getCodeLines(files[i]);
					RCodeLaunching.runRCodeDirect(lines, (i == last) && fGotoConsole, null);
				}
				return;
			}
			
			LaunchShortcutUtil.handleUnsupportedExecution(null);
		}
		catch (final Exception e) {
			LaunchShortcutUtil.handleRLaunchException(e,
					RLaunchingMessages.RScriptLaunch_error_message, null);
		}
	}
	
	@Override
	public void launch(final IEditorPart editor, final String mode) {
		assert mode.equals("run"); //$NON-NLS-1$
		
		try {
			if (editor instanceof AbstractTextEditor) {
				final AbstractTextEditor redt = (AbstractTextEditor) editor;
				final ICodeLaunchContentHandler handler = RCodeLaunching.getCodeLaunchContentHandler(
						LaunchShortcutUtil.getContentTypeId(redt.getEditorInput()));
				final IDocument document = redt.getDocumentProvider().getDocument(editor.getEditorInput() );
				final String[] lines = handler.getCodeLines(document);
				RCodeLaunching.runRCodeDirect(lines, fGotoConsole, null);
				return;
			}
			
			LaunchShortcutUtil.handleUnsupportedExecution(null);
		}
		catch (final Exception e) {
			LaunchShortcutUtil.handleRLaunchException(e,
					RLaunchingMessages.RScriptLaunch_error_message, null);
		}
	}
	
}
