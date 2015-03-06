/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.util.LTKWorkbenchUIUtil;
import de.walware.ecommons.text.TextUtil;
import de.walware.ecommons.workbench.ui.WorkbenchUIUtil;

import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.ICodeSubmitContentHandler;
import de.walware.statet.r.launching.RCodeLaunching;
import de.walware.statet.r.ui.RUI;


public class LaunchShortcutUtil {
	
	
	public static final String TOGGLE_ECHO_COMMAND_ID = "de.walware.statet.r.commands.ToggleRunEcho"; //$NON-NLS-1$
	
	public static String getContentTypeId(final IFile file) {
		final IContentType contentType = IDE.guessContentType(file);
		return (contentType != null) ? contentType.getId() : null;
	}
	
	public static String getContentTypeId(final URI uri) {
		final String fileName = URIUtil.lastSegment(uri);
		if (fileName != null) {
			final IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(fileName);
			return (contentType != null) ? contentType.getId() : null;
		}
		return null;
	}
	
	public static List<String> getCodeLines(final IFile file) throws CoreException {
		InputStream input = null;
		try {
			final String charset;
			try {
				input = file.getContents();
				charset = file.getCharset();
			}
			catch (final CoreException e) {
				throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.IO_ERROR,
						RLaunchingMessages.SubmitCode_error_WhenAnalyzingAndCollecting_message, e));
			}
			try {
				final BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset));
				final StringBuilder buffer = new StringBuilder();
				final char[] readBuffer = new char[2048];
				int n;
				while ((n = reader.read(readBuffer)) > 0) {
					buffer.append(readBuffer, 0, n);
				}
				
				final ICodeSubmitContentHandler handler = RCodeLaunching.getCodeSubmitContentHandler(
						LaunchShortcutUtil.getContentTypeId(file));
				
				final Document document= new Document(buffer.toString());
				handler.setup(document);
				return handler.getCodeLines(document);
			}
			catch (final CoreException e) {
				throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.INTERNAL_PLUGGED_IN,
						RLaunchingMessages.SubmitCode_error_WhenAnalyzingAndCollecting_message, e));
			}
			catch (final IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.IO_ERROR,
						RLaunchingMessages.SubmitCode_error_WhenAnalyzingAndCollecting_message, e));
			}
			catch (final BadLocationException e) {
				throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
						RLaunchingMessages.SubmitCode_error_WhenAnalyzingAndCollecting_message, e));
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
	
	public static List<String> getSelectedCodeLines(final ExecutionEvent event) throws CoreException {
		try {
			final ISelection selection = WorkbenchUIUtil.getCurrentSelection(event.getApplicationContext());
			final IWorkbenchPart workbenchPart = HandlerUtil.getActivePart(event);
			if (selection instanceof ITextSelection) {
				final ITextSelection textSelection = (ITextSelection) selection;
				if (textSelection.getLength() > 0) {
					final String code = textSelection.getText();
					if (code != null) {
						final ArrayList<String> lines = new ArrayList<String>(2 + code.length()/30);
						TextUtil.addLines(code, lines);
						return lines;
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
					final ArrayList<String> lines = new ArrayList<String>(
							document.getNumberOfLines(textSelection.getOffset(), textSelection.getLength()) );
					if (textSelection.getLength() > 0) {
						TextUtil.addLines(document, textSelection.getOffset(), textSelection.getLength(), lines);
					}
					else {
						final int line = document.getLineOfOffset(textSelection.getOffset());
						final IRegion lineInformation = document.getLineInformation(line);
						lines.add(document.get(lineInformation.getOffset(), lineInformation.getLength()));
					}
					return lines;
				}
			}
			return null;
		}
		catch (final BadLocationException e) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					RLaunchingMessages.SubmitCode_error_WhenAnalyzingAndCollecting_message, e));
		}
	}
	
	public static Object getFile(final IEditorInput editorInput) {
		if (editorInput instanceof IFileEditorInput) {
			return ((IFileEditorInput) editorInput).getFile();
		}
		if (editorInput instanceof IURIEditorInput) {
			return ((IURIEditorInput) editorInput).getURI();
		}
		return null;
	}
	
	public static IStatus createUnsupported() {
		return new Status(IStatus.ERROR, RUI.PLUGIN_ID, 
				RLaunchingMessages.SubmitCode_info_NotSupported_message );
	}
	
	public static void handleUnsupportedExecution(final ExecutionEvent executionEvent) {
		LTKWorkbenchUIUtil.indicateStatus(createUnsupported(), executionEvent);
	}
	
	public static void handleRLaunchException(final Throwable e, final String defaultMessage, final ExecutionEvent executionEvent) {
		final Status status = new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHING, defaultMessage, e);
		StatusManager.getManager().handle(status);
		if (e instanceof CoreException) {
			LTKWorkbenchUIUtil.indicateStatus(((CoreException) e).getStatus(), executionEvent);
		}
		else {
			LTKWorkbenchUIUtil.indicateStatus(status, executionEvent);
		}
	}
	
	
	private LaunchShortcutUtil() {}
	
}
