/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.console.ui.tools;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;

import de.walware.statet.r.console.core.IRBasicAdapter;
import de.walware.statet.r.console.core.RTool;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.internal.console.ui.RConsoleMessages;
import de.walware.statet.r.internal.console.ui.RConsoleUIPlugin;


/**
 * ToolRunnable to change the working directory of R.
 * 
 * Supports path mapping (e.g. for remote console).
 */
public class ChangeWDRunnable implements IToolRunnable {
	
	public static final String TYPE_ID = "r/tools/changeWorkingDir"; //$NON-NLS-1$
	
	
	private final IFileStore fWorkingDir;
	
	
	public ChangeWDRunnable(final IFileStore workingdir) {
		fWorkingDir = workingdir;
	}
	
	
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean isRunnableIn(final ITool tool) {
		return (tool.isProvidingFeatureSet(RTool.R_BASIC_FEATURESET_ID));
	}
	
	@Override
	public String getLabel() {
		return RConsoleMessages.ChangeWorkingDir_Task_label;
	}
	
	@Override
	public boolean changed(final int event, final ITool process) {
		return true;
	}
	
	@Override
	public void run(final IToolService service,
			final IProgressMonitor monitor) throws CoreException {
		final IRBasicAdapter r = (IRBasicAdapter) service;
		try {
			final String toolPath = r.getWorkspaceData().toToolPath(fWorkingDir);
			final String command = "setwd(\"" + RUtil.escapeCompletely(toolPath) + "\")"; //$NON-NLS-1$ //$NON-NLS-2$
			r.submitToConsole(command, monitor);
		}
		catch (final CoreException e) {
			r.handleStatus(new Status(IStatus.ERROR, RConsoleUIPlugin.PLUGIN_ID,
					RConsoleMessages.ChangeWorkingDir_error_ResolvingFailed_message, e ), monitor);
			return;
		}
		r.refreshWorkspaceData(0, monitor);
	}
	
}
