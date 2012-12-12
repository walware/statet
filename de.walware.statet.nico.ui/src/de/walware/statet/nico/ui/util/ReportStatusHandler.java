/*******************************************************************************
 * Copyright (c) 2008-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.util;

import static de.walware.statet.nico.core.runtime.IToolEventHandler.REPORT_STATUS_DATA_KEY;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ts.util.ToolCommandHandlerUtil;

import de.walware.statet.nico.core.runtime.IConsoleService;
import de.walware.statet.nico.core.runtime.IToolEventHandler;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolStreamMonitor;
import de.walware.statet.nico.internal.ui.AbstractConsoleCommandHandler;


/**
 * @see {@link IToolEventHandler#REPORT_STATUS_EVENT_ID}
 */
public class ReportStatusHandler extends AbstractConsoleCommandHandler {
	
	
	@Override
	public IStatus execute(final String id, final IConsoleService service, final Map<String, Object> data, final IProgressMonitor monitor) {
		final IStatus status = ToolCommandHandlerUtil.getCheckedData(data, REPORT_STATUS_DATA_KEY, IStatus.class, false); 
		if (status != null) {
			final String br = service.getWorkspaceData().getLineSeparator();
			final StringBuilder msg = new StringBuilder(br);
			switch (status.getSeverity()) {
			case IStatus.ERROR:
				msg.append("[ERROR] ");
				break;
			case IStatus.WARNING:
				msg.append("[WARNING] ");
				break;
			case IStatus.INFO:
				msg.append("[INFO] ");
				break;
			}
			msg.append(status.getMessage());
			msg.append(br);
			boolean details = (status.getException() != null);
			final IStatus[] children = status.getChildren();
			for (final IStatus subStatus : children) {
				msg.append("    "); //$NON-NLS-1$
				msg.append(subStatus.getMessage());
				msg.append(br);
				details |= (subStatus.getChildren().length > 0 || subStatus.getException() != null);
			}
			if (details) {
				msg.append("    (details available in Eclipse error log)");
				msg.append(br);
			}
			
			try {
				final ToolStreamMonitor infoStream = service.getController().getStreams().getInfoStreamMonitor();
				infoStream.append(msg.toString(), SubmitType.OTHER, 0); // Error stream when error?
			}
			catch (final Exception e) {
			}
			
			switch (status.getSeverity()) {
			case IStatus.ERROR:
				StatusManager.getManager().handle(status, StatusManager.LOG | StatusManager.SHOW);
				break;
			case IStatus.WARNING:
				StatusManager.getManager().handle(status, StatusManager.LOG);
				break;
			default:
				break;
			}
		}
		
		return Status.OK_STATUS;
	}
	
}
