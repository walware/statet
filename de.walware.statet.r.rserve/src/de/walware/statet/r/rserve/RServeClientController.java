/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License v2.0
 * or newer, which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.rserve;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.rosuda.JRclient.REXP;
import org.rosuda.JRclient.RSrvException;
import org.rosuda.JRclient.Rconnection;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.nico.runtime.IToolRunnable;
import de.walware.statet.nico.runtime.SubmitType;
import de.walware.statet.nico.runtime.ToolProcess;
import de.walware.statet.r.nico.AbstractRController;
import de.walware.statet.r.rserve.internal.launchconfigs.ConnectionConfig;


public class RServeClientController extends AbstractRController {

	
	private class CommandRunnable extends SimpleRunnable {

		CommandRunnable(String text, SubmitType type) {

			super(text, type);
		}
		
		@Override
		public void run() {
			
			doOnCommandRun(fText, fType);
			try {
				REXP rx = fRconnection.eval(fText);
				if (rx != null) {
					fDefaultOutputStream.append(rx.toString(), fType);
				}
				else {
					fErrorOutputStream.append("[RServe] Warning: Server returned null.", fType);
				}
			}
			catch (RSrvException e) {
				fErrorOutputStream.append("[RServe] Error: " + e.getLocalizedMessage() + ".", fType);
				if (!fRconnection.isConnected() || e.getRequestReturnCode() == -1) {
					try {
						fProcess.terminate();
					} catch (DebugException de) {
						StatetPlugin.log(de.getStatus());
					}
				}
			}
		}
	}
	
	
	private ConnectionConfig fConfig;
	private Rconnection fRconnection;
	
	
	public RServeClientController(ToolProcess process, ConnectionConfig config) {
		
		super(process);
		fConfig = config;
	}
	
	@Override
	protected void startTool() throws CoreException {
		
		try {
			fRconnection = new Rconnection(
					fConfig.getServerAddress(), 
					fConfig.getServerPort());
			
			if (!fRconnection.isConnected()) {
				throw new CoreException(new Status(
						IStatus.ERROR,
						RServePlugin.ID,
						0,
						"Cannot connect to RServe server.",
						null));
			}
			
			fDefaultOutputStream.append(
					"[RServe] Server version: " + fRconnection.getServerVersion() + ".",
					SubmitType.OTHER);
			
			if (fRconnection.needLogin()) {
				fRconnection.login("guest", "guest");
			}

			fRconnection.setSoTimeout(fConfig.getSocketTimeout());
		} 
		catch (RSrvException e) {
			throw new CoreException(new Status(
					IStatus.ERROR,
					RServePlugin.ID,
					0,
					"Error when connecting to RServe server.",
					e));
		}
	}
	
	@Override
	protected boolean terminateTool() {
		
		fRconnection.close();
		fRconnection = null;
		
		return true;
	}

	@Override
	protected IToolRunnable createCommandRunnable(String command, SubmitType type) {

		return new CommandRunnable(command, type);
	}
}
