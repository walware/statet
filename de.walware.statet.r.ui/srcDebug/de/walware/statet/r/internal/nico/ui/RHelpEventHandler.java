/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.nico.ui;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.ui.util.UIAccess.CheckedRunnable;

import de.walware.statet.nico.core.runtime.IToolEventHandler;
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.util.ToolEventHandlerUtil;
import de.walware.statet.nico.ui.NicoUITools;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.internal.ui.rhelp.RHelpUIServlet;
import de.walware.statet.r.internal.ui.rhelp.RHelpView;
import de.walware.statet.r.nico.AbstractRController;
import de.walware.statet.r.nico.RProcess;
import de.walware.statet.r.ui.RUI;


public class RHelpEventHandler implements IToolEventHandler {
	
	
	public RHelpEventHandler() {
	}
	
	
	public IStatus handle(final String id, final IToolRunnableControllerAdapter tools, final Map<String, Object> data, final IProgressMonitor monitor) {
		if (id.equals(AbstractRController.SHOW_RHELP_HANDLER_ID)) {
			String url = ToolEventHandlerUtil.getCheckedData(data, "url", String.class, true);
			if (url.startsWith("html:///")) {
				int idx = url.indexOf("<head");
				if (idx >= 0) {
					idx = url.indexOf('>', idx+5);
					if (idx >= 0) {
						idx++;
						url = url.substring(0, idx) + "<base href=\"about:\"/>" + url.substring(idx);
					}
				}
			}
			
			final RProcess process = (RProcess) tools.getProcess();
			final IREnvConfiguration rEnvConfig = (IREnvConfiguration) process.getAdapter(IREnvConfiguration.class);
			final IREnv rEnv = (rEnvConfig != null) ? rEnvConfig.getReference() : RCore.getREnvManager().getDefault();
			url = RCore.getRHelpManager().toHttpUrl(url, rEnv, RHelpUIServlet.BROWSE_TARGET);
			
			final String urlToOpen = url;
			try {
				UIAccess.checkedSyncExec(new CheckedRunnable() {
					public void run() throws CoreException {
						final RHelpView view = (RHelpView) NicoUITools.getView(RUI.R_HELP_VIEW_ID, tools.getProcess(), true);
						view.openUrl(urlToOpen, null);
					}
				});
				return Status.OK_STATUS;
			}
			catch (final Exception e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1,
						"An error occured when opening R help page in R help view.", e));
				return new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1, "", null);
			}
		}
		
		throw new UnsupportedOperationException();
	}
	
}
