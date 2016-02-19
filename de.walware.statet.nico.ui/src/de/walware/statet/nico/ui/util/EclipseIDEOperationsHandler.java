/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.ui.util;

import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import de.walware.ecommons.ts.util.ToolCommandHandlerUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.IConsoleService;
import de.walware.statet.nico.core.runtime.IToolEventHandler;
import de.walware.statet.nico.core.runtime.ToolWorkspace;
import de.walware.statet.nico.internal.ui.AbstractConsoleCommandHandler;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.views.HistoryView;


/**
 * Handles in Eclipse IDE Platform:
 *  {@link IToolEventHandler#SHOW_HISTORY_ID} - shows the history view
 * 
 */
public class EclipseIDEOperationsHandler extends AbstractConsoleCommandHandler {
	
	
	public static final String SHOW_FILE_ID = "common/showFile"; //$NON-NLS-1$
	
	public static final String SHOW_HISTORY_ID = "common/showHistory"; //$NON-NLS-1$
	
	
	@Override
	public IStatus execute(final String id, final IConsoleService service, final Map<String, Object> data, final IProgressMonitor monitor) {
		if (id.equals(SHOW_FILE_ID)) {
			final IFileStore fileStore;
			String fileName = ToolCommandHandlerUtil.getCheckedData(data, "filename", String.class, false); //$NON-NLS-1$
			if (fileName == null) {
				fileName = ToolCommandHandlerUtil.getCheckedData(data, "fileName", String.class, true); //$NON-NLS-1$
			}
			final ToolWorkspace workspaceData = service.getWorkspaceData();
			try {
				fileStore = workspaceData.toFileStore(fileName);
			}
			catch (final CoreException e) {
				final Status status = new Status(IStatus.ERROR, NicoUI.PLUGIN_ID, -1, "Failed to resolve filename.", e);
				service.handleStatus(status, monitor);
				return status;
			}
			final Display display = UIAccess.getDisplay();
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					final IWorkbenchPage page = NicoUI.getToolRegistry().findWorkbenchPage(service.getTool());
					try {
						IDE.openEditorOnFileStore(page, fileStore);
					}
					catch (final PartInitException e) {
						service.handleStatus(new Status(IStatus.ERROR, NicoUI.PLUGIN_ID, -1,
								"An error occurred when trying open/activate the Editor.", e), monitor);
					}
				}
			});
			return Status.OK_STATUS;
		}
		if (id.equals(SHOW_HISTORY_ID)) {
			final String pattern = ToolCommandHandlerUtil.getCheckedData(data, "pattern", String.class, false); //$NON-NLS-1$
			final Display display = UIAccess.getDisplay();
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						final IWorkbenchPage page = NicoUI.getToolRegistry().findWorkbenchPage(service.getTool());
						final HistoryView view = (HistoryView) page.showView(NicoUI.HISTORY_VIEW_ID);
						if (pattern != null) {
							view.search(pattern, false);
						}
					}
					catch (final PartInitException e) {
						service.handleStatus(new Status(IStatus.ERROR, NicoUI.PLUGIN_ID, -1,
								"An error occurred when trying open/activate the History view.", e), monitor);
					}
				}
			});
			return Status.OK_STATUS;
		}
		throw new UnsupportedOperationException();
	}
	
}
