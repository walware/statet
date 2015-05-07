/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.rhelp;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.ts.util.ToolCommandHandlerUtil;

import de.walware.rj.eclient.AbstractRToolCommandHandler;
import de.walware.rj.eclient.IRToolService;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.ui.RUI;


public class RHelpUICommandHandler extends AbstractRToolCommandHandler {
	
	
	public static final String SHOW_HELP_COMMAND_ID = "showHelp"; //$NON-NLS-1$
	
	
	public RHelpUICommandHandler() {
	}
	
	
	@Override
	public IStatus execute(final String id, final IRToolService r, final Map<String, Object> data, final IProgressMonitor monitor) {
		if (id.equals(SHOW_HELP_COMMAND_ID)) {
			String url = ToolCommandHandlerUtil.getCheckedData(data, "url", String.class, true);
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
			
			final RProcess process = (RProcess) r.getTool();
			final IREnv rEnv = (IREnv) process.getAdapter(IREnv.class);
			if (rEnv == null) {
				return new Status(IStatus.ERROR, RUI.PLUGIN_ID, -1, "Not supported.", null);
			}
			final OpenRHelpHandler handler = new OpenRHelpHandler(rEnv, process, true);
			return handler.execute(url);
		}
		
		throw new UnsupportedOperationException();
	}
	
}
