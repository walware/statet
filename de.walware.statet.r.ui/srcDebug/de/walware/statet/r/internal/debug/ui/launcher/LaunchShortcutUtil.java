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

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.eclipsecommons.ICommonStatusConstants;

import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RUI;


public class LaunchShortcutUtil {
	
	
	public static String[] listLines(final IDocument doc, final ITextSelection selection) {
		int line = selection.getStartLine();
		final int endLine = selection.getEndLine();
		if (line < 0 || endLine < 0) {
			return null;
		}
		
		try {
			final String[] lines = new String[endLine - line +1];
			
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
		catch (final BadLocationException e) {
			RUIPlugin.logError(RUIPlugin.INTERNAL_ERROR, "Error collecting selected text to submit.", e); //$NON-NLS-1$
			return null;
		}
	}
	
	private static int getLineLength(final IDocument doc, final int line) throws BadLocationException {
		int lineLength = doc.getLineLength(line);
		final String lineDelimiter = doc.getLineDelimiter(line);
		if (lineDelimiter != null)
			lineLength -= lineDelimiter.length();
		
		return lineLength;
	}
	
	private static int getEndLineLength(final IDocument doc, final ITextSelection selection) throws BadLocationException {
		final int endLine = selection.getEndLine();
		return Math.min(
				getLineLength(doc, endLine),
				selection.getOffset()+selection.getLength() - doc.getLineOffset(endLine) );
	}
	
	public static String getContentTypeId(final IFile file) {
		try {
			final IContentDescription contentTypeDescription = file.getContentDescription();
			if (contentTypeDescription != null) {
				final IContentType contentType = contentTypeDescription.getContentType();
				return (contentType != null) ? contentType.getId() : null;
			}
		} catch (final CoreException e) {
		}
		return null;
	}
	
	public static String getContentTypeId(final IEditorInput input) {
		final IFile file = ResourceUtil.getFile(input);
		if (file != null) {
			return getContentTypeId(file);
		}
		if (input instanceof IPathEditorInput) {
			final IPath path = ((IPathEditorInput) input).getPath();
			return getContentTypeId(path);
		}
		else if (input instanceof IURIEditorInput) {
			final URI uri = ((IURIEditorInput) input).getURI();
			return getContentTypeId(uri);
		}
		return null;
	}
	
	public static String getContentTypeId(final IPath path) {
		final IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(path.lastSegment());
		return (contentType != null) ? contentType.getId() : null;
	}
	
	public static String getContentTypeId(final URI uri) {
		String path = uri.getPath();
		final int separator = path.lastIndexOf('/');
		if (separator >= 0) {
			path = path.substring(separator+1);
		}
		final IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(path);
		return (contentType != null) ? contentType.getId() : null;
	}
	
	public static void handleRLaunchException(final Throwable e, final String defaultMessage) {
//		CoreException core;
//		if (e instanceof CoreException)
//			core = (CoreException) e;
//		else
//			core = new CoreException(new Status(
//					IStatus.ERROR,
//					RUI.PLUGIN_ID,
//					ICommonStatusConstants.LAUNCHING,
//					defaultMessage,
//					e));
		StatusManager.getManager().handle(new Status(Status.ERROR, RUI.PLUGIN_ID,
				ICommonStatusConstants.LAUNCHING, defaultMessage, e));
	}
	
}
