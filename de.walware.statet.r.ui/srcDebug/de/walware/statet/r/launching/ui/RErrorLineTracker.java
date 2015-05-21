/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.launching.ui;

import java.util.regex.Matcher;
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

import de.walware.ecommons.io.FileValidator;
import de.walware.ecommons.runtime.core.utils.PathUtils;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolProcess;

import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RUI;


public class RErrorLineTracker implements IPatternMatchListener {
	
	
	private static class SourceLink implements IHyperlink {
		
		
		private final IFileStore pathBaseFolder;
		private final IPath path;
		
		private final int line;
		
		
		public SourceLink(final IFileStore pathBaseFolder, final IPath path, final int line) {
			this.pathBaseFolder= pathBaseFolder;
			this.path= path;
			this.line= line;
		}
		
		
		@Override
		public void linkEntered() {
		}
		
		@Override
		public void linkExited() {
		}
		
		@Override
		public void linkActivated() {
			final FileValidator fileValidator= new FileValidator(true);
			fileValidator.setOnDirectory(IStatus.ERROR);
			fileValidator.setResourceLabel(RLaunchingMessages.RErrorLineTracker_File_name);
			if (this.path.isAbsolute()) {
				fileValidator.setExplicit(this.path);
			}
			else {
				fileValidator.setExplicit(URIUtil.toPath(this.pathBaseFolder.toURI()).append(this.path).makeAbsolute());
			}
			final IStatus status= fileValidator.validate(null);
			if (status.getSeverity() == IStatus.ERROR) {
				StatusManager.getManager().handle(new Status(Status.ERROR, RUI.PLUGIN_ID, -1,
								NLS.bind(RLaunchingMessages.RErrorLineTracker_error_GetFile_message, this.path),
								new CoreException(status) ),
						StatusManager.LOG | StatusManager.SHOW );
				return;
			}
			final IFile wsFile= (IFile) fileValidator.getWorkspaceResource();
			try {
				IEditorPart editor;
				if (wsFile != null) {
					editor= IDE.openEditor(UIAccess.getActiveWorkbenchPage(true), wsFile, RUI.R_EDITOR_ID, true);
				}
				else {
					editor= IDE.openEditor(UIAccess.getActiveWorkbenchPage(true), fileValidator.getFileStore().toURI(), RUI.R_EDITOR_ID, true);
				}
				final AbstractTextEditor textEditor= (AbstractTextEditor) editor;
				final IDocumentProvider documentProvider= textEditor.getDocumentProvider();
				if (documentProvider != null) {
					final IDocument doc= documentProvider.getDocument(textEditor.getEditorInput());
					final IRegion lineInfo= doc.getLineInformation(this.line);
					textEditor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
				}
			}
			catch (final PartInitException | BadLocationException e) {
				StatusManager.getManager().handle(new Status(Status.ERROR, RUI.PLUGIN_ID, -1,
						NLS.bind(RLaunchingMessages.RErrorLineTracker_error_OpeningFile_message,
								this.path),
						e ), StatusManager.LOG | StatusManager.SHOW );
			}
		}
		
	}
	
	
	private static final String NUM_LINE_REGEX= "^\\d++\\:\\ .*"; //$NON-NLS-1$
	private static final String ROUND_LINE_REGEX= "^[ \\t]*\\(.+\\:\\d+(?:\\-\\d+)?\\).*"; //$NON-NLS-1$
	private static final String COMBINED_REGEX= "(?:" + NUM_LINE_REGEX + ")|(?:" + ROUND_LINE_REGEX + ")"; //$NON-NLS-1$
	private static final Pattern NUM_LINE_PATTERN= Pattern.compile(NUM_LINE_REGEX, Pattern.DOTALL);
	private static final Pattern NUM_LINE_GROUP_PATTERN= Pattern.compile("((\\d++)\\:)\\ .*", Pattern.DOTALL); //$NON-NLS-1$
	private static final Pattern ROUND_LINE_GROUP_PATTERN= Pattern.compile("[ \\t]*\\(((.+)\\:(\\d+)(?:\\-\\d+)?)\\).*", Pattern.DOTALL); //$NON-NLS-1$
	
	
	private TextConsole console;
	
	private IFileStore workingDirectory;
	private ToolProcess tool;
	
	private final Matcher numLineMatcher= NUM_LINE_PATTERN.matcher(""); //$NON-NLS-1$
	private final Matcher numLineGroupMatcher= NUM_LINE_GROUP_PATTERN.matcher(""); //$NON-NLS-1$
	private final Matcher checkLineGroupMatcher= ROUND_LINE_GROUP_PATTERN.matcher(""); //$NON-NLS-1$
	
	
	/**
	 * @param working directory
	 */
	public RErrorLineTracker(final IFileStore workingDirectory) {
		this.workingDirectory= workingDirectory;
	}
	
	public RErrorLineTracker(final ToolProcess tool) {
		this.tool= tool;
	}
	
	
	@Override
	public int getCompilerFlags() {
		return Pattern.MULTILINE;
	}
	
	@Override
	public String getLineQualifier() {
		return null;
	}
	
	@Override
	public String getPattern() {
		return COMBINED_REGEX;
	}
	
	@Override
	public void connect(final TextConsole console) {
		this.console= console;
	}
	
	@Override
	public void disconnect() {
		this.console= null;
	}
	
	@Override
	public void matchFound(final PatternMatchEvent event) {
		try {
			final IDocument document= this.console.getDocument();
			
			final String eventLine= document.get(event.getOffset(), event.getLength());
			if (this.numLineGroupMatcher.reset(eventLine).matches()) {
				final int srcLineNum= Integer.parseInt(this.numLineGroupMatcher.group(2));
				final IPath srcPath= createPath(searchPath(document, event));
				if (srcPath != null) {
					final int begin= this.numLineGroupMatcher.start(1);
					final int length= this.numLineGroupMatcher.end(1) - begin;
					this.console.addHyperlink(
							new SourceLink(getWorkingDirectory(), srcPath, srcLineNum - 1),
							event.getOffset() + begin, length );
				}
			}
			else if (this.checkLineGroupMatcher.reset(eventLine).matches()) {
				final IPath srcPath= createPath(this.checkLineGroupMatcher.group(2));
				final int srcLineNum= Integer.parseInt(this.checkLineGroupMatcher.group(3));
				if (srcPath != null) {
					final int begin= this.checkLineGroupMatcher.start(1);
					final int length= this.checkLineGroupMatcher.end(1) - begin;
					this.console.addHyperlink(
							new SourceLink(getWorkingDirectory(), srcPath, srcLineNum - 1),
							event.getOffset() + begin, length );
				}
			}
			else {
				throw new IllegalStateException("match= " + eventLine);
			}
		}
		catch (final Exception e) {
			RUIPlugin.logError(-1, "Error while searching error line informations.", e); //$NON-NLS-1$
		}
	}
	
	private IPath createPath(String path) {
		if (path == null || (path= path.trim()).isEmpty()) {
			return null;
		}
		if (this.tool != null) {
			return this.tool.getWorkspaceData().createToolPath(path);
		}
		return PathUtils.check(new Path(path));
	}
	
	protected IFileStore getWorkingDirectory() {
		if (this.tool != null) {
			return this.tool.getWorkspaceData().getWorkspaceDir();
		}
		else {
			return this.workingDirectory;
		}
	}
	
	private String searchPath(final IDocument document, final PatternMatchEvent event)
			throws BadLocationException {
		int line= document.getLineOfOffset(event.getOffset());
		LINE_BACK: while (--line >= 0) {
			final IRegion lineInfo= document.getLineInformation(line);
			final int result= checkLine(document, lineInfo);
			switch (result) {
			case -2:
				break LINE_BACK;
			case -1:
				continue LINE_BACK;
			default:
				return document.get(lineInfo.getOffset(), result - lineInfo.getOffset());
			}
		}
		return null;
	}
	
	/**
	 * @return
	 *    = -2 wrong
	 *    = -1 number line
	 *     >= 0 index of ": "
	 */
	private int checkLine(final IDocument doc, final IRegion lineInfo) throws BadLocationException {
		final int offset= lineInfo.getOffset();
		final int end= offset + Math.min(lineInfo.getLength(), 500);
		if (end - offset <= 2) {
			return -2;
		}
		final char char0= doc.getChar(offset);
		if (char0 >= 48 && char0 <= 57) {
			final String s= doc.get(offset, Math.min(end - offset, 10));
			if (this.numLineMatcher.reset(s).matches()) {
				return -1;
			}
			return -2;
		}
		if (offset >= 5) {
			final IRegion prevLineInfo= doc.getLineInformationOfOffset(offset - 1);
			final int prevLineEnd= prevLineInfo.getOffset() + prevLineInfo.getLength();
			// Line starts with Error, but can be translated, so test only the end
			if (!doc.get(prevLineEnd-3, 3).equals(" : ")) { //$NON-NLS-1$
				return -2;
			}
		}
		if (char0 == ' ') {
			final String s= doc.get(offset, end - offset);
			if (s.charAt(1) != ' ') {
				return -2;
			}
			final int found= s.indexOf(":", 4); //$NON-NLS-1$
			if (found >= 0) {
				return offset + found;
			}
			return -2;
		}
		if (char0 == '\t') {
			final String s= doc.get(offset, end - offset);
			final int found= s.indexOf(":", 3); //$NON-NLS-1$
			if (found >= 0) {
				return offset + found;
			}
			return -2;
		}
		return -2;
	}
	
}
