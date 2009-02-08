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

package de.walware.statet.r.debug.ui.launchconfigs;

import java.util.regex.Pattern;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.walware.ecommons.FileValidator;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolWorkspace;

import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RUI;


/**
 * 
 */
public class RErrorLineTracker implements IPatternMatchListener {
	
	
	private static class SourceLink implements IHyperlink {
		
		private IFileStore fBase;
		private String fFileName;
		private int fLine;
		
		public SourceLink(final IFileStore base, final String path, final int line) {
			fBase = base;
			fFileName = path;
			fLine = line;
		}
		
		public void linkEntered() {
		}
		
		public void linkExited() {
		}
		
		public void linkActivated() {
			final FileValidator fileValidator = new FileValidator(true);
			fileValidator.setOnDirectory(IStatus.ERROR);
			fileValidator.setResourceLabel(RLaunchingMessages.RErrorLineTracker_File_name);
			final IPath filePath = new Path(fFileName);
			if (filePath.isAbsolute()) {
				fileValidator.setExplicit(filePath);
			}
			else {
				fileValidator.setExplicit(URIUtil.toPath(fBase.toURI()).append(filePath).makeAbsolute());
			}
			final IStatus status = fileValidator.validate(null);
			if (status.getSeverity() == IStatus.ERROR) {
				StatusManager.getManager().handle(new Status(Status.ERROR, RUI.PLUGIN_ID,
						-1, NLS.bind(RLaunchingMessages.RErrorLineTracker_error_GetFile_message, fFileName),
						new CoreException(status)),
						StatusManager.LOG | StatusManager.SHOW);
				return;
			}
			final IFile wsFile = (IFile) fileValidator.getWorkspaceResource();
			Exception error = null;
			try {
				IEditorPart editor;
				if (wsFile != null) {
					editor = IDE.openEditor(UIAccess.getActiveWorkbenchPage(true), wsFile, RUI.R_EDITOR_ID, true);
				}
				else {
					editor = IDE.openEditor(UIAccess.getActiveWorkbenchPage(true), fileValidator.getFileStore().toURI(), RUI.R_EDITOR_ID, true);
				}
				final AbstractTextEditor textEditor = (AbstractTextEditor) editor;
				final IDocumentProvider documentProvider = textEditor.getDocumentProvider();
				if (documentProvider != null) {
					final IDocument doc = documentProvider.getDocument(textEditor.getEditorInput());
					final IRegion lineInfo = doc.getLineInformation(fLine);
					textEditor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
				}
			}
			catch (final PartInitException e) {
				error = e;
			}
			catch (final BadLocationException e) {
				error = e;
			}
			if (error != null) {
				StatusManager.getManager().handle(new Status(Status.ERROR, RUI.PLUGIN_ID,
						-1, NLS.bind(RLaunchingMessages.RErrorLineTracker_error_OpeningFile_message, fFileName), error),
						StatusManager.LOG | StatusManager.SHOW);
			}
		}
		
	}
	
	private static final String NUM_LINE_REGEX = "^\\d+\\Q: \\E.*"; //$NON-NLS-1$
	private static final Pattern NUM_LINE_PATTERN = Pattern.compile(NUM_LINE_REGEX);
	
	
	private TextConsole fConsole;
	private IFileStore fWorkingDirectory;
	private ToolProcess<ToolWorkspace> fTool;
	
	
	/**
	 * @param working directory
	 */
	public RErrorLineTracker(final IFileStore workingDirectory) {
		fWorkingDirectory = workingDirectory;
	}
	
	public RErrorLineTracker(final ToolProcess<ToolWorkspace> tool) {
		fTool = tool;
	}
	
	
	public int getCompilerFlags() {
		return Pattern.MULTILINE;
	}
	
	public String getLineQualifier() {
		return null;
	}
	
	public String getPattern() {
		return NUM_LINE_REGEX;
	}
	
	public void connect(final TextConsole console) {
		fConsole = console;
	}
	
	public void disconnect() {
		fConsole = null;
	}
	
	public void matchFound(final PatternMatchEvent event) {
		try {
			final IDocument document = fConsole.getDocument();
			int line = document.getLineOfOffset(event.getOffset());
			IRegion lineInfo;
			int result;
			LINE_BACK: while (true) {
				if (line <= 0) {
					return;
				}
				lineInfo = document.getLineInformation(--line);
				result = checkLine(document, lineInfo);
				switch (result) {
				case -2:
					return;
				case -1:
					continue LINE_BACK;
				default:
					break LINE_BACK;
				}
			}
			final String path = document.get(lineInfo.getOffset(), result-lineInfo.getOffset()).trim();
			final String number = document.get(event.getOffset(), Math.min(event.getLength(), 10));
			result = number.indexOf(':');
			if (result < 0) {
				return;
			}
			final int sourceline = Integer.parseInt(number.substring(0, result));
			fConsole.addHyperlink(new SourceLink(getWorkingDirectory(), path, sourceline-1), event.getOffset(), event.getLength());
		}
		catch (final BadLocationException e) {
			RUIPlugin.logError(-1, "Error while searching error line informations.", e); //$NON-NLS-1$
		}
	}
	
	protected IFileStore getWorkingDirectory() {
		if (fTool != null) {
			return fTool.getWorkspaceData().getWorkspaceDir();
		}
		else {
			return fWorkingDirectory;
		}
	}
	
	/**
	 * @return
	 * 		-2 wrong
	 * 		-1 number line
	 * 		>=0 index of ": "
	 */
	private int checkLine(final IDocument doc, final IRegion lineInfo) throws BadLocationException {
		final int offset = lineInfo.getOffset();
		final int end = offset + Math.min(lineInfo.getLength(), 500);
		if (end-offset <= 2) {
			return -2;
		}
		final char char0 = doc.getChar(offset);
		if (char0 >= 48 && char0 <= 57) {
			final String s = doc.get(offset, Math.min(end-offset, 10));
			if (NUM_LINE_PATTERN.matcher(s).matches()) {
				return -1;
			}
			return -2;
		}
		if (offset >= 5) {
			final IRegion prevLineInfo = doc.getLineInformationOfOffset(offset-1);
			final int prevLineEnd = prevLineInfo.getOffset()+prevLineInfo.getLength();
			// Line starts with Error, but can be translated, so test only the end
			if (!doc.get(prevLineEnd-3, 3).equals(" : ")) { //$NON-NLS-1$
				return -2;
			}
		}
		if (char0 == ' ') {
			final String s = doc.get(offset, end-offset);
			if (s.charAt(1) != ' ') {
				return -2;
			}
			final int found = s.indexOf(": ", 4); //$NON-NLS-1$
			if (found >= 0) {
				return offset+found;
			}
			return -2;
		}
		if (char0 == '\t') {
			final String s = doc.get(offset, end-offset);
			final int found = s.indexOf(": ", 3); //$NON-NLS-1$
			if (found >= 0) {
				return offset+found;
			}
			return -2;
		}
		return -2;
	}
	
}
