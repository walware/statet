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

package de.walware.statet.r.nico.ui.tools;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.internal.nico.ui.RNicoMessages;
import de.walware.statet.r.nico.IBasicRAdapter;
import de.walware.statet.r.nico.ISetupRAdapter;


/**
 *
 */
public class ChangeWDRunnable implements IToolRunnable<IBasicRAdapter> {

	public static final String TYPE_ID = "r/tools/changeWorkingDir"; //$NON-NLS-1$
	
	
	private IFileStore fWorkingDir;
	
	
	public ChangeWDRunnable(IFileStore workingdir) {
		fWorkingDir = workingdir;
	}
	
	
	public String getTypeId() {
		return TYPE_ID;
	}

	public String getLabel() {
		return RNicoMessages.ChangeWorkingDir_Task_label;
	}

	public SubmitType getSubmitType() {
		return SubmitType.TOOLS;
	}

	public void run(IBasicRAdapter tools, IProgressMonitor monitor)
			throws InterruptedException, CoreException {
		
		String path = URIUtil.toPath(fWorkingDir.toURI()).toOSString();
		String command = "setwd(\"" + RUtil.escapeCompletly(path) + "\")"; //$NON-NLS-1$ //$NON-NLS-2$
		tools.submitToConsole(command, monitor);
		if (tools instanceof ISetupRAdapter) {
			((ISetupRAdapter) tools).setWorkspaceDir(fWorkingDir);
		}
	}
	
}
