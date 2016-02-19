/*=============================================================================#
 # Copyright (c) 2008-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

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

import de.walware.ecommons.ts.IToolCommandHandler;
import de.walware.ecommons.ts.IToolService;
import de.walware.ecommons.ts.util.ToolCommandHandlerUtil;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.internal.core.Messages;


/**
 * {@link #LOAD_HISTORY_ID}
 * {@link #SAVE_HISTORY_ID}
 * {@link #ADDTO_HISTORY_ID}
 */
public class HistoryOperationsHandler implements IToolCommandHandler {
	
	
	public static final String LOAD_HISTORY_ID = "common/loadHistory"; //$NON-NLS-1$
	
	public static final String SAVE_HISTORY_ID = "common/saveHistory"; //$NON-NLS-1$
	
	public static final String ADDTO_HISTORY_ID = "common/addtoHistory"; //$NON-NLS-1$
	
	
	@Override
	public IStatus execute(final String id, final IToolService service, final Map<String, Object> data, final IProgressMonitor monitor) {
		if (service instanceof IConsoleService) {
			final IConsoleService console = (IConsoleService) service;
			if (id.equals(LOAD_HISTORY_ID)) {
				return loadHistory(console, data, monitor);
			}
			if (id.equals(SAVE_HISTORY_ID)) {
				return saveHistory(console, data, monitor);
			}
			if (id.equals(ADDTO_HISTORY_ID)) {
				final String item = ToolCommandHandlerUtil.getCheckedData(data, "text", String.class, true); //$NON-NLS-1$
				console.getTool().getHistory().addCommand(item, console.getController().getCurrentSubmitType());
				return Status.OK_STATUS;
			}
		}
		throw new UnsupportedOperationException();
	}
	
	
	protected IStatus loadHistory(final IConsoleService tools, final Map<String, Object> data, final IProgressMonitor monitor) {
		try {
			CoreException fileException = null;
			IFileStore fileStore = null;
			final String filename = ToolCommandHandlerUtil.getCheckedData(data, "filename", String.class, true); //$NON-NLS-1$
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
				status = tools.getTool().getHistory().load(fileStore, workspaceData.getEncoding(), false, monitor);
			}
			tools.handleStatus(status, monitor);
			return status;
		}
		catch (final OperationCanceledException e) {
			return Status.CANCEL_STATUS;
		}
	}
	
	protected IStatus saveHistory(final IConsoleService tools, final Map<String, Object> data, final IProgressMonitor monitor) {
		try {
			CoreException fileException = null;
			IFileStore fileStore = null;
			final IStatus status;
			
			final String filename = ToolCommandHandlerUtil.getCheckedData(data, "filename", String.class, true); //$NON-NLS-1$
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
				status = tools.getTool().getHistory().save(fileStore, EFS.NONE, workspaceData.getEncoding(), false, monitor);
			}
			tools.handleStatus(status, monitor);
			return status;
		}
		catch (final OperationCanceledException e) {
			return Status.CANCEL_STATUS;
		}
	}
	
}
