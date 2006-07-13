/*******************************************************************************
 * Copyright (c) 2005-2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License 
 * v2.1 or newer, which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
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
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.r.nico.AbstractRController;
import de.walware.statet.r.nico.IBasicRAdapter;
import de.walware.statet.r.nico.ISetupRAdapter;
import de.walware.statet.r.nico.IncompleteInputPrompt;
import de.walware.statet.r.nico.RWorkspace;
import de.walware.statet.r.rserve.internal.launchconfigs.ConnectionConfig;


public class RServeClientController 
		extends AbstractRController<IBasicRAdapter, RWorkspace> {

	
	private class RServeAdapter extends AbstractRAdapter implements IBasicRAdapter, ISetupRAdapter {
		
		
		@Override
		protected Prompt doSubmit(String input) {
			
			String completeInput = input;
			if ((fPrompt.meta & IBasicRAdapter.META_PROMPT_INCOMPLETE_INPUT) != 0) {
				completeInput = ((IncompleteInputPrompt) fPrompt).previousInput + input;
			}
			try {
				REXP rx = fRconnection.eval(completeInput);
				if (rx != null) {
					fDefaultOutputStream.append(rx.toString()+fLineSeparator, fCurrentRunnable.getType(), 0);
				}
				else {
					fErrorOutputStream.append("[RServe] Warning: Server returned null."+fLineSeparator, fCurrentRunnable.getType(), 0);
				}
				return fDefaultPrompt;
			}
			catch (RSrvException e) {
				if (e.getRequestReturnCode() == 2) {
					return createIncompleteInputPrompt(fPrompt, input);
				}
				fErrorOutputStream.append("[RServe] Error: "+e.getLocalizedMessage()+"."+fLineSeparator, fCurrentRunnable.getType(), 0);
				if (!fRconnection.isConnected() || e.getRequestReturnCode() == -1) {
					try {
						fProcess.terminate();
					} catch (DebugException de) {
						StatetPlugin.log(de.getStatus());
					}
					return Prompt.NONE;
				}
				else {
					return fDefaultPrompt;
				}
			}
		}
	}
	
	
	private ConnectionConfig fConfig;
	private Rconnection fRconnection;
	
	
	public RServeClientController(ToolProcess process, ConnectionConfig config) {
		
		super(process);
		fConfig = config;
		
		fWorkspaceData = new RWorkspace();
		addToolStatusListener(fWorkspaceData);
		fRunnableAdapter = new RServeAdapter();
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
						RServePlugin.PLUGIN_ID,
						0,
						"Cannot connect to RServe server.",
						null));
			}
			
			fRconnection.setSoTimeout(fConfig.getSocketTimeout());
			fDefaultOutputStream.append("[RServe] Server version: "+fRconnection.getServerVersion()+"."+fWorkspaceData.getLineSeparator(), SubmitType.OTHER, 0);
			
			if (fRconnection.needLogin()) {
				fRconnection.login("guest", "guest");
			}

			ISetupRAdapter system = (RServeAdapter) fRunnableAdapter;
			Prompt prompt = new Prompt("> ", IToolRunnableControllerAdapter.META_PROMPT_DEFAULT);
			system.setDefaultPrompt(prompt);
			system.setIncompletePromptText("$ ");
			system.setPrompt(prompt);
			system.setLineSeparator("\n");
		} 
		catch (RSrvException e) {
			throw new CoreException(new Status(
					IStatus.ERROR,
					RServePlugin.PLUGIN_ID,
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

}
