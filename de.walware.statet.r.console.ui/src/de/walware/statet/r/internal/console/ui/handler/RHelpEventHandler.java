/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui.handler;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.statet.nico.core.runtime.IConsoleService;
import de.walware.statet.nico.core.runtime.IToolEventHandler;
import de.walware.statet.nico.core.util.ToolEventHandlerUtil;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.internal.ui.rhelp.OpenRHelpHandler;
import de.walware.statet.r.nico.AbstractRController;
import de.walware.statet.r.ui.RUI;


public class RHelpEventHandler implements IToolEventHandler {
	
	
	public RHelpEventHandler() {
	}
	
	
	@Override
	public IStatus handle(final String id, final IConsoleService tools, final Map<String, Object> data, final IProgressMonitor monitor) {
		if (id.equals(AbstractRController.SHOW_RHELP_HANDLER_ID)) {
			String url = ToolEventHandlerUtil.getCheckedData(data, "url", String.class, true);
			if (url.startsWith("html:///")) { //$NON-NLS-1$
				int idx = url.indexOf("<head"); //$NON-NLS-1$
				if (idx >= 0) {
					idx = url.indexOf('>', idx+5);
					if (idx >= 0) {
						idx++;
						url = url.substring(0, idx) + "<base href=\"about:\"/>" + url.substring(idx); //$NON-NLS-1$
					}
				}
			}
			
			final RProcess process = (RProcess) tools.getTool();
			final IREnv rEnv = (IREnv) process.getAdapter(IREnv.class);
			if (rEnv == null) {
				return new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1, "Not supported.", null);
			}
			OpenRHelpHandler handler = new OpenRHelpHandler(rEnv, process, true);
			return handler.execute(url);
		}
		
		throw new UnsupportedOperationException();
	}
	
}
