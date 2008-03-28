/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.rosuda.JRclient.REXP;
import org.rosuda.JRclient.RSrvException;
import org.rosuda.JRclient.Rconnection;

import de.walware.eclipsecommons.preferences.PreferencesUtil;

import de.walware.statet.nico.core.NicoPreferenceNodes;
import de.walware.statet.nico.core.runtime.IToolEventHandler;
import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.IToolEventHandler.LoginEventData;
import de.walware.statet.r.internal.rserve.launchconfigs.ConnectionConfig;
import de.walware.statet.r.nico.AbstractRController;
import de.walware.statet.r.nico.BasicR;
import de.walware.statet.r.nico.IBasicRAdapter;
import de.walware.statet.r.nico.IncompleteInputPrompt;
import de.walware.statet.r.nico.RWorkspace;
import de.walware.statet.r.nico.impl.RQuitRunnable;


/**
 * Controller for RServe.
 */
public class RServeClientController extends AbstractRController {


	private final ConnectionConfig fConfig;
	private Rconnection fRconnection;
	
	
	public RServeClientController(final ToolProcess process, final ConnectionConfig config) {
		super(process);
		fConfig = config;
		
		fWorkspaceData = new RWorkspace(this);
		initRunnableAdapter();
	}
	
	@Override
	protected void startTool(final IProgressMonitor monitor) throws CoreException {
		try {
    		final int timeout = PreferencesUtil.getInstancePrefs().getPreferenceValue(NicoPreferenceNodes.KEY_DEFAULT_TIMEOUT);
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
				final LoginEventData login = new LoginEventData();
				login.name = "guest";
				login.password = "guest";
				int result = IToolEventHandler.OK;
				final IToolEventHandler handler = getEventHandler(IToolEventHandler.LOGIN_EVENT_ID);
				if (handler != null) {
					result = handler.handle(this, login);
				}
				if (result == IToolEventHandler.OK) {
					fRconnection.login(login.name, login.password);
				}
				else {
					killTool(new NullProgressMonitor());
				}
			}

//			ISetupRAdapter system = (RServeAdapter) fRunnableAdapter;
//			system.setDefaultPromptText("> ");
//			system.setIncompletePromptText("+ ");
//			system.setLineSeparator("\n");
		}
		catch (final RSrvException e) {
			throw new CoreException(new Status(
					IStatus.ERROR,
					RServePlugin.PLUGIN_ID,
					0,
					"Error when connecting to RServe server.",
					e));
		}
	}
	
	@Override
	protected void interruptTool(final int hardness) {
		if (hardness == 0) {
			return;
		}
		getControllerThread().interrupt();
	}
	
	@Override
	protected void postCancelTask(final int options, final IProgressMonitor monitor) throws CoreException {
		super.postCancelTask(options, monitor);
		fDefaultOutputStream.append(fLineSeparator, fCurrentRunnable.getSubmitType(), 0);
		setCurrentPrompt(fDefaultPrompt);
	}
	
	@Override
	protected boolean isToolAlive() {
		final Rconnection con = fRconnection;
		if (con != null && con.isConnected()) {
			return true;
		}
		return false;
	}
	
	@Override
	protected IToolRunnable createStartRunnable() {
		return new StartRunnable();
	}
	
	@Override
	protected IToolRunnable createQuitRunnable() {
		return new RQuitRunnable() {
			@Override
			public void run(final IBasicRAdapter tools, final IProgressMonitor monitor)
					throws InterruptedException, CoreException {
				fRconnection.close();
				markAsTerminated();
			}
		};
	}
	
	@Override
	protected void killTool(final IProgressMonitor monitor) {
		final Rconnection con = fRconnection;
		if (con != null) {
			con.close();
			fRconnection = null;
		}
		markAsTerminated();
	}

	
//-- RunnableAdapter
	
	@Override
	protected void doSubmit(final IProgressMonitor monitor) {
		final String completeInput = ((fCurrentPrompt.meta & BasicR.META_PROMPT_INCOMPLETE_INPUT) == 0) ?
				fCurrentInput : ((IncompleteInputPrompt) fCurrentPrompt).previousInput + fCurrentInput;
		monitor.subTask(fDefaultPrompt.text + " " + completeInput);  //$NON-NLS-1$
		try {
			final REXP rx = fRconnection.eval(completeInput);
			if (rx != null) {
				fDefaultOutputStream.append(rx.toString()+fLineSeparator, fCurrentRunnable.getSubmitType(), 0);
			}
			else {
				fErrorOutputStream.append("[RServe] Warning: Server returned null."+fLineSeparator, fCurrentRunnable.getSubmitType(), 0);
			}
			setCurrentPrompt(fDefaultPrompt);
			return;
		}
		catch (final RSrvException e) {
			if (e.getRequestReturnCode() == 2) {
				setCurrentPrompt(fIncompletePromptText, true);
				return;
			}
			fErrorOutputStream.append("[RServe] Error: "+e.getLocalizedMessage()+"."+fLineSeparator, fCurrentRunnable.getSubmitType(), 0);
			if (!fRconnection.isConnected() || e.getRequestReturnCode() == -1) {
				killTool(new NullProgressMonitor());
				setCurrentPrompt(Prompt.NONE);
				return;
			}
			setCurrentPrompt(fDefaultPrompt);
			return;
		}
	}
	
}
