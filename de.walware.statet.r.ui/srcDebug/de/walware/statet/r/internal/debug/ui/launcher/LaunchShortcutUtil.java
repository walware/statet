/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import de.walware.eclipsecommons.ICommonStatusConstants;

import de.walware.statet.base.ui.util.ExceptionHandler;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RUI;


public class LaunchShortcutUtil {
	
	
	public static String[] listLines(IDocument doc, ITextSelection selection) {
		int line = selection.getStartLine();
		int endLine = selection.getEndLine();
		if (line < 0 || endLine < 0) {
			return null;
		}
		
		try {
			String[] lines = new String[endLine - line +1];

			if (line == endLine) {
				lines[0] = (selection.getLength() > 0) ?
						doc.get(selection.getOffset(), getEndLineLength(doc, selection)-(selection.getOffset()-doc.getLineOffset(line))) :
						doc.get(doc.getLineOffset(line), getLineLength(doc, endLine));
			}
			else {
				int i = 0;
				lines[i++] = doc.get(selection.getOffset(),
						getLineLength(doc, line) - (selection.getOffset()-doc.getLineOffset(line)) );
				line++;

				while (line < endLine) {
					lines[i++] = doc.get(doc.getLineOffset(line), getLineLength(doc, line));
					line++;
				}
				
				lines[i] = doc.get(doc.getLineOffset(line), getEndLineLength(doc, selection) );
			}
			
			return lines;
		}
		catch (BadLocationException e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "Error collecting selected text to submit.", e); //$NON-NLS-1$
			return null;
		}
	}

	private static int getLineLength(IDocument doc, int line) throws BadLocationException {
		int lineLength = doc.getLineLength(line);
		String lineDelimiter = doc.getLineDelimiter(line);
		if (lineDelimiter != null)
			lineLength -= lineDelimiter.length();
		
		return lineLength;
	}

	private static int getEndLineLength(IDocument doc, ITextSelection selection) throws BadLocationException {
		int endLine = selection.getEndLine();
		return Math.min(
				getLineLength(doc, endLine),
				selection.getOffset()+selection.getLength() - doc.getLineOffset(endLine) );
	}


	public static void handleRLaunchException(Throwable e, String defaultMessage) {
		CoreException core;
		if (e instanceof CoreException)
			core = (CoreException) e;
		else
			core = new CoreException(new Status(
					IStatus.ERROR,
					RUI.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING_ERROR,
					defaultMessage,
					e));
		ExceptionHandler.handle(core, RLaunchingMessages.RLaunch_error_description);
	}
	
}
