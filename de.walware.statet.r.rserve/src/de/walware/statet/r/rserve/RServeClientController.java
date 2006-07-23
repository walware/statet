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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.rosuda.JRclient.REXP;
import org.rosuda.JRclient.RSrvException;
import org.rosuda.JRclient.Rconnection;

import de.walware.eclipsecommons.preferences.PreferencesUtil;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.nico.core.NicoPreferenceNodes;
import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.r.nico.AbstractRController;
import de.walware.statet.r.nico.BasicR;
import de.walware.statet.r.nico.IBasicRAdapter;
import de.walware.statet.r.nico.ISetupRAdapter;
import de.walware.statet.r.nico.IncompleteInputPrompt;
import de.walware.statet.r.nico.RWorkspace;
import de.walware.statet.r.rserve.internal.launchconfigs.ConnectionConfig;


public class RServeClientController 
		extends AbstractRController<IBasicRAdapter, RWorkspace> {

	
	private class RServeAdapter extends AbstractRAdapter implements IBasicRAdapter, ISetupRAdapter, IAdaptable {
		
		
		@Override
		protected Prompt doSubmit(String input, IProgressMonitor monitor) {
			
			String completeInput = input;
			if ((fPrompt.meta & BasicR.META_PROMPT_INCOMPLETE_INPUT) != 0) {
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
		
		fWorkspaceData = new RWorkspace(this);
		fRunnableAdapter = new RServeAdapter();
	}
	
	@Override
	protected void startTool() throws CoreException {
		
		try {
    		int timeout = PreferencesUtil.getInstancePrefs().getPreferenceValue(NicoPreferenceNodes.KEY_DEFAULT_TIMEOUT);
			fRconnection = new Rconnection(
					fConfig.getServerAddress(), fConfig.getServerPort(),
					timeout);
			
			if (!fRconnection.isConnected()) {
				throw new CoreException(new Status(
						IStatus.ERROR,
						RServePlugin.PLUGIN_ID,
						0,
						"Cannot connect to RServe server.",
						null));
			}
			
			fInfoStream.append("[RServe] Server version: "+fRconnection.getServerVersion()+"."+fWorkspaceData.getLineSeparator(), SubmitType.OTHER, 0);
			
			if (fRconnection.needLogin()) {
				fRconnection.login("guest", "guest");
			}

//			ISetupRAdapter system = (RServeAdapter) fRunnableAdapter;
//			system.setDefaultPromptText("> ");
//			system.setIncompletePromptText("+ ");
//			system.setLineSeparator("\n");
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
	protected boolean terminateTool(boolean forced) {
		
		Rconnection con = fRconnection;
		if (con != null) {
			con.close();
			fRconnection = null;
		}
		return true;
	}
	
	@Override
	protected void interruptTool(int hardness) {
		
		if (hardness == 0) {
			return;
		}
		getControllerThread().interrupt();
	}
	
	@Override
	protected boolean isToolAlive() {
		
		return fRconnection.isConnected();
	}
	
}
