/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
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

import de.walware.eclipsecommons.FileValidator;
import de.walware.eclipsecommons.ui.util.UIAccess;

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
		
		public SourceLink(IFileStore base, String path, int line) {
			fBase = base;
			fFileName = path;
			fLine = line;
		}

		public void linkEntered() {
		}

		public void linkExited() {
		}

		public void linkActivated() {
			FileValidator fileValidator = new FileValidator(true);
			fileValidator.setOnDirectory(IStatus.ERROR);
			fileValidator.setResourceLabel(RLaunchingMessages.RErrorLineTracker_File_name);
			IPath filePath = new Path(fFileName);
			if (filePath.isAbsolute()) {
				fileValidator.setExplicit(filePath);
			}
			else {
				fileValidator.setExplicit(URIUtil.toPath(fBase.toURI()).append(filePath).makeAbsolute());
			}
			IStatus status = fileValidator.validate(null);
			if (status.getSeverity() == IStatus.ERROR) {
				StatusManager.getManager().handle(new Status(Status.ERROR, RUI.PLUGIN_ID,
						-1, NLS.bind(RLaunchingMessages.RErrorLineTracker_error_GetFile_message, fFileName),
						new CoreException(status)),
						StatusManager.LOG | StatusManager.SHOW);
				return;
			}
			IFile wsFile = (IFile) fileValidator.getWorkspaceResource();
			Exception error = null;
			try {
				IEditorPart editor;
				if (wsFile != null) {
					editor = IDE.openEditor(UIAccess.getActiveWorkbenchPage(true), wsFile, RUI.R_EDITOR_ID, true);
				}
				else {
					editor = IDE.openEditor(UIAccess.getActiveWorkbenchPage(true), fileValidator.getFileStore().toURI(), RUI.R_EDITOR_ID, true);
				}
				AbstractTextEditor textEditor = (AbstractTextEditor) editor;
				IDocument doc = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
				IRegion lineInfo = doc.getLineInformation(fLine);
				textEditor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
			}
			catch (PartInitException e) {
				error = e;
			}
			catch (BadLocationException e) {
				error = e;
			}
			if (error != null) {
				StatusManager.getManager().handle(new Status(Status.ERROR, RUI.PLUGIN_ID,
						-1, NLS.bind(RLaunchingMessages.RErrorLineTracker_error_OpeningFile_message, fFileName), error),
						StatusManager.LOG | StatusManager.SHOW);
			}
		}

	}
	
	private TextConsole fConsole;
	private IFileStore fWorkingDirectory;
	private ToolProcess<ToolWorkspace> fTool;
	
	
	/**
	 * @param working directory
	 */
	public RErrorLineTracker(IFileStore workingDirectory) {
		fWorkingDirectory = workingDirectory;
	}

	public RErrorLineTracker(ToolProcess<ToolWorkspace> tool) {
		fTool = tool;
	}

	
	public int getCompilerFlags() {
		return Pattern.MULTILINE;
	}
	
	public String getLineQualifier() {
		return null;
	}
	
	public String getPattern() {
		return "^\\d+\\Q: \\E.*"; //$NON-NLS-1$
	}
	
	public void connect(TextConsole console) {
		fConsole = console;
	}
	
	public void disconnect() {
		fConsole = null;
	}
	
	public void matchFound(PatternMatchEvent event) {
		try {
			IDocument document = fConsole.getDocument();
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
			String path = document.get(lineInfo.getOffset(), result-lineInfo.getOffset()).trim();
			String number = document.get(event.getOffset(), Math.min(event.getLength(), 10));
			result = number.indexOf(':');
			if (result < 0) {
				return;
			}
			int sourceline = Integer.parseInt(number.substring(0, result));
	//		System.out.println("offset="+event.getOffset()+",length="+event.getLength());
	//		System.out.println("source="+event.getSource().toString());
			fConsole.addHyperlink(new SourceLink(getWorkingDirectory(), path, sourceline-1), event.getOffset(), event.getLength());
		}
		catch (BadLocationException e) {
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
	 * 		>=0 index of ": syntax error"
	 */
	private int checkLine(IDocument doc, IRegion lineInfo) throws BadLocationException {
		int offset = lineInfo.getOffset();
		int end = offset + Math.min(lineInfo.getLength(), 500);
		int state = -1;
		ITER_CHARS: while (offset < end) {
			char c = doc.getChar(offset++);
			switch (state) {
			case -1:
				if (c >= 48 && c <= 57) {
					state = 10;
					continue ITER_CHARS;
				}
				state = 20;
				continue ITER_CHARS;
			case 10:
				if (c >= 48 && c <= 57) {
					continue ITER_CHARS;
				}
				if (c == ':') {
					state = 11;
					continue ITER_CHARS;
				}
				return -2;
			case 11:
				if (c == ' ') {
					return -1;
				}
				return -2;
			case 20:
				if (c == ':') {
					state = 21;
				}
				continue ITER_CHARS;
			case 21:
				if (c == ' ') {
					state = 22;
					continue ITER_CHARS;
				}
				state = 20;
				continue ITER_CHARS;
			case 22:
				if (c == 's') {
					"yntax error".equals(doc.get(offset, 11)); //$NON-NLS-1$
					return offset-3;
				}
				state = 20;
				continue ITER_CHARS;
			}
		}
		return -2;
	}
	
}
