/*******************************************************************************
 * Copyright (c) 2005-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.util.WorkbenchUIUtil;

import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.ICodeLaunchContentHandler;
import de.walware.statet.r.launching.RCodeLaunching;
import de.walware.statet.r.ui.RUI;


public class LaunchShortcutUtil {
	
	
	public static String getContentTypeId(final IFile file) {
		try {
			final IContentDescription contentTypeDescription = file.getContentDescription();
			if (contentTypeDescription != null) {
				final IContentType contentType = contentTypeDescription.getContentType();
				return (contentType != null) ? contentType.getId() : null;
			}
		}
		catch (final CoreException e) {
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
	
	public static String[] getCodeLines(final IFile file) throws CoreException {
		InputStream input = null;
		try {
			final String charset;
			try {
				input = file.getContents();
				charset = file.getCharset();
			}
			catch (final CoreException e) {
				throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.IO_ERROR,
						RLaunchingMessages.RunCode_error_WhenAnalyzingAndCollecting_message, e));
			}
			try {
				final BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset));
				final StringBuilder buffer = new StringBuilder();
				final char[] readBuffer = new char[2048];
				int n;
				while ((n = reader.read(readBuffer)) > 0) {
					buffer.append(readBuffer, 0, n);
				}
				
				final ICodeLaunchContentHandler handler = RCodeLaunching.getCodeLaunchContentHandler(
						LaunchShortcutUtil.getContentTypeId(file));
				final String[] lines = handler.getCodeLines(new Document(buffer.toString()));
				return lines;
			}
			catch (final CoreException e) {
				throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.INTERNAL_PLUGGED_IN,
						RLaunchingMessages.RunCode_error_WhenAnalyzingAndCollecting_message, e));
			}
			catch (final IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.IO_ERROR,
						RLaunchingMessages.RunCode_error_WhenAnalyzingAndCollecting_message, e));
			}
			catch (final BadLocationException e) {
				throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
						RLaunchingMessages.RunCode_error_WhenAnalyzingAndCollecting_message, e));
			}
		}
		finally {
			if (input != null) {
				try {
					input.close();
				} catch (final IOException ignore) {}
			}
		}
	}
	
	public static String getSelectedCode(final ExecutionEvent event) throws CoreException {
		try {
			final ISelection selection = WorkbenchUIUtil.getCurrentSelection(event.getApplicationContext());
			final IWorkbenchPart workbenchPart = HandlerUtil.getActivePart(event);
			if (selection instanceof ITextSelection) {
				final ITextSelection textSelection = (ITextSelection) selection;
				if (textSelection.getLength() > 0) {
					final String code = textSelection.getText();
					if (code != null) {
						return code;
					}
				}
				IDocument document = null;
				if (workbenchPart instanceof ITextEditor) {
					final ITextEditor editor = (ITextEditor) workbenchPart;
					final IDocumentProvider documentProvider = editor.getDocumentProvider();
					if (documentProvider != null) {
						document = documentProvider.getDocument(editor.getEditorInput());
					}
				}
				if (document == null) {
					final ISourceEditor editor = (ISourceEditor) workbenchPart.getAdapter(ISourceEditor.class);
					if (editor != null) {
						document = editor.getViewer().getDocument();
					}
				}
				if (document != null) {
					if (textSelection.getLength() > 0) {
						return document.get(textSelection.getOffset(), textSelection.getLength());
					}
					else {
						final int line = document.getLineOfOffset(textSelection.getOffset());
						final IRegion lineInformation = document.getLineInformation(line);
						return document.get(lineInformation.getOffset(), lineInformation.getLength());
					}
				}
			}
			return null;
		}
		catch (final BadLocationException e) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					RLaunchingMessages.RunCode_error_WhenAnalyzingAndCollecting_message, e));
		}
	}
	
	public static void handleUnsupportedExecution(final ExecutionEvent executionEvent) {
		WorkbenchUIUtil.indicateStatus(new Status(IStatus.INFO, RUI.PLUGIN_ID, 
				RLaunchingMessages.RunCode_info_NotSupported_message), executionEvent);
	}
	
	public static void handleRLaunchException(final Throwable e, final String defaultMessage, final ExecutionEvent executionEvent) {
		final Status status = new Status(Status.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHING, defaultMessage, e);
		StatusManager.getManager().handle(status);
		if (e instanceof CoreException) {
			WorkbenchUIUtil.indicateStatus(((CoreException) e).getStatus(), executionEvent);
		}
		else {
			WorkbenchUIUtil.indicateStatus(status, executionEvent);
		}
	}
	
	
	private LaunchShortcutUtil() {}
	
}
