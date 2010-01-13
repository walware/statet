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

package de.walware.statet.nico.core.runtime;

import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.util.ToolEventHandlerUtil;
import de.walware.statet.nico.internal.core.Messages;


/**
 * {@link #LOAD_HISTORY_ID}
 * {@link #SAVE_HISTORY_ID}
 * {@link #ADDTO_HISTORY_ID}
 */
public class HistoryOperationsHandler implements IToolEventHandler {
	
	
	public static final String LOAD_HISTORY_ID = "common/loadHistory"; //$NON-NLS-1$
	
	public static final String SAVE_HISTORY_ID = "common/saveHistory"; //$NON-NLS-1$
	
	public static final String ADDTO_HISTORY_ID = "common/addtoHistory"; //$NON-NLS-1$
	
	
	public IStatus handle(final String id, final IToolRunnableControllerAdapter tools, final Map<String, Object> data, final IProgressMonitor monitor) {
		if (id.equals(LOAD_HISTORY_ID)) {
			return loadHistory(tools, data, monitor);
		}
		if (id.equals(SAVE_HISTORY_ID)) {
			return saveHistory(tools, data, monitor);
		}
		if (id.equals(ADDTO_HISTORY_ID)) {
			final String item = ToolEventHandlerUtil.getCheckedData(data, "text", String.class, true); //$NON-NLS-1$
			tools.getProcess().getHistory().addCommand(item, tools.getCurrentRunnable().getSubmitType());
			return Status.OK_STATUS;
		}
		throw new UnsupportedOperationException();
	}
	
	
	protected IStatus loadHistory(final IToolRunnableControllerAdapter tools, final Map<String, Object> data, final IProgressMonitor monitor) {
		try {
			CoreException fileException = null;
			IFileStore fileStore = null;
			final String filename = ToolEventHandlerUtil.getCheckedData(data, "filename", String.class, true); //$NON-NLS-1$
			final ToolWorkspace workspaceData = tools.getWorkspaceData();
			try {
				fileStore = workspaceData.toFileStore(filename);
			}
			catch (final CoreException e) {
				fileException = e; 
			}
			final IStatus status;
			if (fileStore == null) {
				status = new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, -1, NLS.bind(
						Messages.ToolController_FileOperation_error_CannotResolve_message, filename), 
						fileException);
			}
			else {
				status = tools.getProcess().getHistory().load(fileStore, workspaceData.getEncoding(), false, monitor);
			}
			tools.handleStatus(status, monitor);
			return status;
		}
		catch (final OperationCanceledException e) {
			return Status.CANCEL_STATUS;
		}
	}
	
	protected IStatus saveHistory(final IToolRunnableControllerAdapter tools, final Map<String, Object> data, final IProgressMonitor monitor) {
		try {
			CoreException fileException = null;
			IFileStore fileStore = null;
			final IStatus status;
			
			final String filename = ToolEventHandlerUtil.getCheckedData(data, "filename", String.class, true); //$NON-NLS-1$
			final ToolWorkspace workspaceData = tools.getWorkspaceData();
			try {
				fileStore = workspaceData.toFileStore(filename);
			}
			catch (final CoreException e) {
				fileException = e; 
			}
			if (fileStore == null) {
				status = new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, -1, NLS.bind(
						Messages.ToolController_FileOperation_error_CannotResolve_message, filename), 
						fileException);
			}
			else {
				status = tools.getProcess().getHistory().save(fileStore, EFS.NONE, workspaceData.getEncoding(), false, monitor);
			}
			tools.handleStatus(status, monitor);
			return status;
		}
		catch (final OperationCanceledException e) {
			return Status.CANCEL_STATUS;
		}
	}
	
}
