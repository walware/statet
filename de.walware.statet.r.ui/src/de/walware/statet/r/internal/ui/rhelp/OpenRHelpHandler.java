/*******************************************************************************
 * Copyright (c) 2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.rhelp;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.ui.util.UIAccess.CheckedRunnable;

import de.walware.statet.nico.ui.NicoUITools;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.rhelp.IRHelpManager;
import de.walware.statet.r.ui.RUI;


public class OpenRHelpHandler extends AbstractHandler {
	
	
	private final IREnv fREnv;
	private final RProcess fTool;
	
	private final boolean fReuse;
	
	
	public OpenRHelpHandler(final IREnv rEnv, final RProcess tool, final boolean reuse) {
		if (rEnv == null) {
			throw new NullPointerException("rEnv"); //$NON-NLS-1$
		}
		fREnv = rEnv;
		fTool = tool;
		fReuse = reuse;
	}
	
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		return execute(event.getParameter("url")); //$NON-NLS-1$
	}
	
	public IStatus execute(String url) {
		final RProcess tool = fTool;
		final IRHelpManager rHelpManager = RCore.getRHelpManager();
		if (url == null || url.isEmpty() || url.equals("about:blank")) { //$NON-NLS-1$
			url = rHelpManager.getREnvHttpUrl(fREnv, RHelpUIServlet.BROWSE_TARGET);
		}
		else {
			url = rHelpManager.toHttpUrl(url, fREnv, RHelpUIServlet.BROWSE_TARGET);
		}
		if (url == null) {
			return null;
		}
		try {
			final String urlToOpen = url;
			UIAccess.checkedSyncExec(new CheckedRunnable() {
				@Override
				public void run() throws CoreException {
					final RHelpView view = (RHelpView) NicoUITools.getView(RUI.R_HELP_VIEW_ID, tool, true);
					view.openUrl(urlToOpen, (fReuse) ? view.findBrowserSession(urlToOpen) : null);
				}
			});
			return Status.OK_STATUS;
		}
		catch (final CoreException e) {
			final IStatus status = new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
					"An error occured when opening R help page in R help view.", e );
			StatusManager.getManager().handle(status);
			return status;
		}
	}
	
}
