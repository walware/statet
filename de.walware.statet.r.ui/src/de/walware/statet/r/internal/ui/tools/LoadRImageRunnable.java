/*******************************************************************************
 * Copyright (c) 2007-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.tools;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;

import de.walware.statet.r.console.core.IRBasicAdapter;
import de.walware.statet.r.console.core.RConsoleTool;
import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.core.RUtil;


public class LoadRImageRunnable implements IToolRunnable {
	
	public static final String TYPE_ID = "r/tools/loadData"; //$NON-NLS-1$
	
	static final String REQUIRED_FEATURESET_ID = RConsoleTool.R_BASIC_FEATURESET_ID;
	
	
	private final IFileStore fDataFile;
	
	
	public LoadRImageRunnable(final IFileStore workingdir) {
		fDataFile = workingdir;
	}
	
	
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean isRunnableIn(final ITool tool) {
		return (tool.isProvidingFeatureSet(REQUIRED_FEATURESET_ID));
	}
	
	@Override
	public String getLabel() {
		return NLS.bind(Messages.LoadData_Runnable_label, fDataFile.getName());
	}
	
	@Override
	public boolean changed(final int event, final ITool process) {
		return true;
	}
	
	@Override
	public void run(final IToolService service,
			final IProgressMonitor monitor) throws CoreException {
		final IRBasicAdapter r = (IRBasicAdapter) service;
		final String toolPath = r.getWorkspaceData().toToolPath(fDataFile);
		final String command = "load(\"" + RUtil.escapeCompletely(toolPath) + "\")"; //$NON-NLS-1$ //$NON-NLS-2$
		r.submitToConsole(command, monitor);
		r.refreshWorkspaceData(RWorkspace.REFRESH_AUTO, monitor);
	}
	
}
