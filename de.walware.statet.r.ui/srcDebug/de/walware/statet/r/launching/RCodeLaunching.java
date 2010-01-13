/*******************************************************************************
 * Copyright (c) 2008-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.launching;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.ui.IDebugUIConstants;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolWorkspace;

import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.internal.debug.ui.launcher.RCodeLaunchRegistry;
import de.walware.statet.r.internal.debug.ui.launcher.RCodeLaunchRegistry.ContentHandler.FileCommand;
import de.walware.statet.r.internal.nico.ui.RControllerCodeLaunchConnector;
import de.walware.statet.r.nico.AbstractRController;


/**
 * Provides methods to submit code to R
 * 
 * The methods use the code launch connector selected in the preferences
 * and therefore it supports external consoles too
 * (in contrast to direct usage of new {@link AbstractRController}).
 */
public final class RCodeLaunching {
	
	
	public static final String RUN_SELECTION_COMMAND_ID = "de.walware.statet.r.commands.RunSelectionInR"; //$NON-NLS-1$
	
	public static final String RUN_SELECTION_GOTOCONSOLE_COMMAND_ID = "de.walware.statet.r.commands.RunSelectionInR_GotoConsole"; //$NON-NLS-1$
	
	public static final String RUN_SELECTION_PASTEOUTPUT_COMMAND_ID = "de.walware.statet.r.commands.RunSelectionInR_PasteOutput"; //$NON-NLS-1$
	
	public static final String RUN_FILEVIACOMMAND_COMMAND_ID = "de.walware.statet.r.commands.RunFileViaCommand"; //$NON-NLS-1$
	
	public static final String RUN_FILEVIACOMMAND_GOTOCONSOLE_COMMAND_ID = "de.walware.statet.r.commands.RunFileViaCommand_GotoConsole"; //$NON-NLS-1$
	
	
	public static final String FILE_COMMAND_ID_PARAMTER_ID = "fileCommandId"; //$NON-NLS-1$
	
	
	private static final Pattern FILENAME_PATTERN = Pattern.compile("\\Q${resource_loc}\\E"); //$NON-NLS-1$
	
	private static final IStatus STATUS_PROMPTER = new Status(IStatus.INFO, IDebugUIConstants.PLUGIN_ID, 200, "", null); //$NON-NLS-1$
	private static final IStatus STATUS_SAVE = new Status(IStatus.INFO, DebugPlugin.getUniqueIdentifier(), 222, "", null); //$NON-NLS-1$
	
	
	public static void gotoRConsole() throws CoreException {
		final IRCodeLaunchConnector connector = RCodeLaunchRegistry.getDefault().getConnector();
		
		connector.gotoConsole();
	}
	
	public static String getFileCommand(final String id) {
		final FileCommand fileCommand = RCodeLaunchRegistry.getDefault().getFileCommand(id);
		if (fileCommand != null) {
			return fileCommand.getCurrentCommand();
		}
		return null;
	}
	
	public static String getPreferredFileCommand(final String contentType) {
		final FileCommand fileCommand = RCodeLaunchRegistry.getDefault().getContentFileCommand(contentType);
		return fileCommand.getCurrentCommand();
	}
	
	public static ICodeLaunchContentHandler getCodeLaunchContentHandler(final String contentType) {
		return RCodeLaunchRegistry.getDefault().getContentHandler(contentType);
	}
	
	
	/**
	 * Runs a file related command in R.
	 * <p>
	 * The pattern ${file} in command string is replaced by the path of
	 * the specified file.</p>
	 * 
	 * @param command the command, (at moment) should be single line.
	 * @param file the file.
	 * @throws CoreException if running failed.
	 */
	public static void runFileUsingCommand(final String command, final IFile file, final boolean gotoConsole) throws CoreException {
		// save before launch
		final IProject project = file.getProject();
		if (project != null) {
			final IProject[] referencedProjects = project.getReferencedProjects();
			final IProject[] allProjects = new IProject[referencedProjects.length+1];
			allProjects[0] = project;
			System.arraycopy(referencedProjects, 0, allProjects, 1, referencedProjects.length);
			if (!saveBeforeLaunch(allProjects)) {
				return;
			}
		}
		
		runFileUsingCommand(command, file.getLocationURI(), gotoConsole);
	}
	
//	/**
//	 * Runs a file related command in R.
//	 * Use this method only, if you don't have an IFile object for your file
//	 * (e.g. external file).
//	 * <p>
//	 * The pattern ${file} in command string is replaced by the path of
//	 * the specified file.</p>
//	 * 
//	 * @param command the command, (at moment) should be single line.
//	 * @param file the file.
//	 * @throws CoreException if running failed.
//	 */
//	public static void runFileUsingCommand(final String command, final IPath filePath, final boolean gotoConsole) throws CoreException {
//		final IRCodeLaunchConnector connector = RCodeLaunchRegistry.getDefault().getConnector();
//		
//		final String fileString = RUtil.escapeCompletly(filePath.makeAbsolute().toOSString());
//		final String cmd = FILENAME_PATTERN.matcher(command).replaceAll(Matcher.quoteReplacement(fileString));
//		connector.submit(new String[] { cmd }, gotoConsole);
//	}
	
	/**
	 * Runs a file related command in R.
	 * Use this method only, if you don't have an IFile or IPath object for your file
	 * (e.g. file on webserver).
	 * <p>
	 * The pattern ${file} in command string is replaced by the path of
	 * the specified file.</p>
	 * 
	 * @param command the command, (at moment) should be single line.
	 * @param file the file.
	 * @throws CoreException if running failed.
	 */
	public static void runFileUsingCommand(final String command, final URI fileURI, final boolean gotoConsole) throws CoreException {
		final IRCodeLaunchConnector connector = RCodeLaunchRegistry.getDefault().getConnector();
		IFileStore fileStore = null;
		try {
			fileStore = EFS.getStore(fileURI);
		}
		catch (final CoreException e) {
			fileStore = null;
		}
		
		if (fileStore != null && connector instanceof RControllerCodeLaunchConnector) {
			final IFileStore store = fileStore;
			((RControllerCodeLaunchConnector) connector).submit(new RControllerCodeLaunchConnector.CommandsCreator() {
				public IStatus submitTo(final ToolController controller) {
					final ToolWorkspace workspaceData = controller.getWorkspaceData();
					try {
						final String path = workspaceData.toToolPath(store);
						final String fileString = RUtil.escapeCompletly(path);
						final String cmd = FILENAME_PATTERN.matcher(command).replaceAll(Matcher.quoteReplacement(fileString));
						return controller.submit(cmd, SubmitType.EDITOR);
					}
					catch (final CoreException e) {
						return e.getStatus();
					}
				}
			}, gotoConsole);
		}
		else {
			String fileString = null;
			try {
				if (EFS.getLocalFileSystem().equals(EFS.getFileSystem(fileURI.getScheme()))) {
					fileString = EFS.getLocalFileSystem().getStore(fileURI).toString();
				}
			} catch (final CoreException e) {
			}
			if (fileString == null) {
				fileString = fileURI.toString();
			}
			
			fileString = RUtil.escapeCompletly(fileString);
			final String cmd = FILENAME_PATTERN.matcher(command).replaceAll(Matcher.quoteReplacement(fileString));
			connector.submit(new String[] { cmd }, gotoConsole);
		}
	}
	
	private static boolean saveBeforeLaunch(final IProject[] projects) throws CoreException {
		IStatusHandler prompter = null;
		prompter = DebugPlugin.getDefault().getStatusHandler(STATUS_PROMPTER);
		if (prompter != null) {
			return ((Boolean) prompter.handleStatus(STATUS_SAVE,
					new Object[] { null, projects } )).booleanValue();
		}
		return true;
	}
	
	public static boolean runRCodeDirect(final String[] lines, final boolean gotoConsole) throws CoreException {
		final IRCodeLaunchConnector connector = RCodeLaunchRegistry.getDefault().getConnector();
		
		return connector.submit(lines, gotoConsole);
	}
	
	public static boolean runRCodeDirect(final String code, final boolean gotoConsole) throws CoreException {
		final IRCodeLaunchConnector connector = RCodeLaunchRegistry.getDefault().getConnector();
		
		return connector.submit(listLines(code), gotoConsole);
	}
	
	private static String[] listLines(final String text) {
		final int n = text.length();
		if (n == 0) {
			return new String[0];
		}
		final List<String> lines = new ArrayList<String>(n/30);
		int i = 0;
		int lineStart = 0;
		while (i < n) {
			switch (text.charAt(i)) {
			case '\r':
				lines.add(text.substring(lineStart, i));
				i++;
				if (i < n && text.charAt(i) == '\n') {
					i++;
				}
				lineStart = i;
				continue;
			case '\n':
				lines.add(text.substring(lineStart, i));
				i++;
				if (i < n && text.charAt(i) == '\r') {
					i++;
				}
				lineStart = i;
				continue;
			default:
				i++;
				continue;
			}
		}
		if (lineStart < n) {
			lines.add(text.substring(lineStart, n));
		}
		
		return lines.toArray(new String[lines.size()]);
	}
	
}
