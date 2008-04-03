/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico.impl;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.osgi.util.NLS;

import de.walware.eclipsecommons.ICommonStatusConstants;

import de.walware.rj.server.ConsoleCmdItem;
import de.walware.rj.server.ExtUICmdItem;
import de.walware.rj.server.MainCmdItem;
import de.walware.rj.server.MainCmdList;
import de.walware.rj.server.RjsComObject;
import de.walware.rj.server.RjsPing;
import de.walware.rj.server.RjsStatus;
import de.walware.rj.server.Server;

import de.walware.statet.nico.core.runtime.IToolEventHandler;
import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.IToolEventHandler.SelectFileEventData;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.nico.AbstractRController;
import de.walware.statet.r.nico.RWorkspace;


/**
 * Controller for RJ-Server
 */
public class RjsController extends AbstractRController {
	
	
	private String fAddress;
	private String[] fRArgs;
	
	private Server fRJServer;
	private int fTicket;
	private ConsoleCmdItem fServerCallback;
	private boolean fIsBusy;
	private boolean fIsDisconnected = false;
	
	
	public RjsController(final ToolProcess<RWorkspace> process, final String address, final String[] rArgs,
			final IFileStore initialWD) {
		super(process);
		if (address == null || address.length() == 0) {
			throw new IllegalArgumentException();
		}
		fAddress = address;
		fRArgs = rArgs;
		
		fWorkspaceData = new RWorkspace(this);
		setWorkspaceDir(initialWD);
		initRunnableAdapter();
	}
	
	
	@Override
	public boolean supportsBusy() {
		return true;
	}
	
	@Override
	public boolean isBusy() {
		return fIsBusy;
	}
	
	
	@Override
	protected IToolRunnable createStartRunnable() {
		return new StartRunnable() {
			@Override
			public String getLabel() {
				return "Connect to and load remote R instance.";
			}
		};
	}
	
	@Override
	protected IToolRunnable createQuitRunnable() {
		return new RQuitRunnable();
	}
	
	@Override
	protected void startTool(final IProgressMonitor monitor) throws InterruptedException, CoreException {
		try {
			final Server server = (Server) Naming.lookup(fAddress);
			fTicket = server.start(0, fRArgs);
			fRJServer = server;
			
			rjsRunMainLoop(null, monitor);
		}
		catch (final MalformedURLException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING,
					NLS.bind("The specified server address '{0}' is invalid.", fAddress), e));
		}
		catch (final NotBoundException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING,
					"The specified server is not in the service registry (RMI).", e));
		}
		catch (final ClassCastException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING,
					"The specified server is incompatibel to this client.", e));
		}
		catch (final RemoteException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING,
					"The R remote service could not be started.", e));
		}
	}
	
	protected final Server ensureServer() {
		final Server server = fRJServer;
		if (server == null) {
			throw new IllegalStateException();
		}
		return server;
	}
	
	protected void rjsRunMainLoop(RjsComObject sendCom, final IProgressMonitor monitor) throws CoreException {
		try {
			while (true) {
				RjsComObject receivedCom = null;
				fServerCallback = null;
				receivedCom = ensureServer().runMainLoop(fTicket, sendCom);
				switch (receivedCom.getComType()) {
				case RjsComObject.T_PING:
					sendCom = RjsStatus.OK_STATUS;
					continue;
				case RjsComObject.T_MAIN_LIST:
					sendCom = rjsHandleMainList((MainCmdList) receivedCom, monitor);
					if (fServerCallback != null) {
						return;
					}
					continue;
				case RjsComObject.T_STATUS:
					rjsHandleStatus((RjsStatus) receivedCom);
					return;
				}
			}
		}
		catch (final RemoteException e) {
			if (isToolAlive()) {
				throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, "Communication error", e));
			}
			rjsHandleStatus(new RjsStatus(RjsComObject.V_INFO, Server.S_STOPPED));
		}
	}
	
	private void rjsHandleStatus(final RjsStatus status) {
		switch(status.getCode()) {
		case 0:
			return;
		case Server.S_DISCONNECTED:
			fIsDisconnected = true;
			markAsTerminated();
			handleStatus(new Status(IStatus.INFO, RCore.PLUGIN_ID, "R disconnected."));
			return;
		case Server.S_STOPPED: {
			markAsTerminated();
			final IToolRunnable runnable = fCurrentRunnable;
			if (runnable == null || !runnable.getTypeId().equals(QUIT_TYPE_ID)) {
				handleStatus(new Status(IStatus.INFO, RCore.PLUGIN_ID, "R stopped/connection lost."));
			}
			return;
			}
		}
	}
	
	private RjsComObject rjsHandleMainList(final MainCmdList list, final IProgressMonitor monitor) throws RemoteException {
		try {
			final boolean isBusy = list.isBusy();
			if (fIsBusy != isBusy) {
				fIsBusy = isBusy;
//				loopBusyChanged(isBusy);
			}
			final MainCmdItem[] items = list.getItems();
			ITER_ITEMS : for (int i = 0; i < items.length; i++) {
				switch (items[i].getComType()) {
				case MainCmdItem.T_CONSOLE_WRITE_ITEM:
					((items[i].getOption() == RjsComObject.V_OK) ?
							fDefaultOutputStream : fErrorOutputStream)
							.append(items[i].getDataText(), fCurrentRunnable.getSubmitType(), 0);
					continue ITER_ITEMS;
				case MainCmdItem.T_CONSOLE_READ_ITEM: {
					final ConsoleCmdItem item = (ConsoleCmdItem) items[i];
					setCurrentPrompt(item.getDataText(), item.getOption() == RjsComObject.V_TRUE);
					if (items.length > 1) {
						rjsSendPing(false);
					}
					fServerCallback = item;
					return null;
				}
				case MainCmdItem.T_MESSAGE_ITEM:
					fInfoStream.append(items[i].getDataText(), fCurrentRunnable.getSubmitType(), 0);
					continue ITER_ITEMS;
				case MainCmdItem.T_EXTENDEDUI_ITEM:
					if (items.length > 1) {
						rjsSendPing(false);
					}
					return rjsCheckCallback((ExtUICmdItem) items[i], monitor);
				default:
					throw new RemoteException("Illegal command from server: " + items[i].toString());
				}
			}
			fServerCallback = null;
			return null;
		}
		catch (final RemoteException e) {
			throw e;
		}
		catch (final Exception e) {
			rjsComErrorRestore(e);
			// try to recover
			rjsSendPing(true);
			final MainCmdItem[] items = list.getItems();
			for (int i = items.length-1; i >= 0; i++) {
				if (items[i] != null && items[i].waitForClient()) {
					if (items[i].getComType() == RjsComObject.T_CONSOLE_READ_ITEM && items[i] instanceof ConsoleCmdItem) {
						fServerCallback = (ConsoleCmdItem) items[i];
						return null;
					}
					else {
						items[i].setAnswer(RjsComObject.V_ERROR);
						return items[i];
					}
				}
			}
			return null;
		}
	}
	
	private void rjsSendPing(final boolean checkAnswer) throws RemoteException {
		final RjsComObject com = ensureServer().runMainLoop(fTicket, RjsPing.INSTANCE);
		if (checkAnswer) {
			if (com.getComType() != RjsComObject.T_STATUS) {
				throw new IllegalStateException();
			}
			rjsHandleStatus((RjsStatus) com);
		}
	}
	
	private RjsComObject rjsCheckCallback(final ExtUICmdItem extUICmdItem, final IProgressMonitor monitor) {
		final String command = extUICmdItem.getCommand();
		// if we have more commands, we should create a hashmap
		if (command.equals(ExtUICmdItem.C_CHOOSE_FILE)) {
			try {
				final IToolEventHandler handler = getEventHandler(IToolEventHandler.SELECTFILE_EVENT_ID);
				if (handler != null) {
					final SelectFileEventData data = new IToolEventHandler.SelectFileEventData();
					data.newFile = ((extUICmdItem.getOption() & ExtUICmdItem.O_NEW) == ExtUICmdItem.O_NEW);
					if (handler.handle(this, data) == IToolEventHandler.OK) {
						extUICmdItem.setAnswer(data.filename);
						return extUICmdItem;
					}
				}
			}
			catch (final Exception e) {
				rjsComErrorRestore(e);
			}
			extUICmdItem.setAnswer(RjsComObject.V_CANCEL);
			return extUICmdItem;
		}
		if (command.equals(ExtUICmdItem.C_HISTORY_LOAD)) {
			extUICmdItem.setAnswer(loadHistory(extUICmdItem.getDataText(), monitor));
			return extUICmdItem;
		}
		if (command.equals(ExtUICmdItem.C_HISTORY_SAVE)) {
			extUICmdItem.setAnswer(saveHistory(extUICmdItem.getDataText(), monitor));
			return extUICmdItem;
		}
		return null;
	}
	
	private void rjsComErrorRestore(final Throwable e) {
		handleStatus(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
				"An error occurred when running tasks for R. StatET will try to restore the communication, otherwise quit R.", e));
	}
	
	@Override
	protected void interruptTool(final int hardness) throws UnsupportedOperationException {
		final Server server = fRJServer;
		if (server != null) {
			try {
				server.interrupt(fTicket);
			} catch (final RemoteException e) {
				RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
						"An error occurred when trying to interrupt R.", e));
			}
		}
		if (hardness > 6) {
			super.interruptTool(hardness);
		}
	}
	
	@Override
	protected void postCancelTask(final int options, final IProgressMonitor monitor) throws CoreException {
		super.postCancelTask(options, monitor);
		fCurrentInput = ""; //$NON-NLS-1$
		doSubmit(monitor);
	}
	
	@Override
	protected boolean isToolAlive() {
		final Server server = fRJServer;
		if (server != null) {
			try {
				if (server != null) {
					return (RjsStatus.OK_STATUS.equals(server.runAsync(fTicket, RjsPing.INSTANCE)));
				}
				else {
					return false;
				}
			}
			catch (final RemoteException e) {
				// no need to log here
			}
		}
		return false;
	}
	
	@Override
	protected void killTool(final IProgressMonitor monitor) {
		if (getControllerThread() == null) {
			markAsTerminated();
			return;
		}
		interruptTool(9);
		final ToolProcess consoleProcess = getProcess();
		// TODO: kill remote command?
		final IProcess[] processes = consoleProcess.getLaunch().getProcesses();
		for (int i = 0; i < processes.length; i++) {
			if (processes[i] != consoleProcess && !processes[i].isTerminated()) {
				try {
					processes[i].terminate();
				}
				catch (final DebugException e) {
				}
			}
		}
		interruptTool(9);
		markAsTerminated();
	}
	
	@Override
	protected void clear() {
		super.clear();
		
		if (!fIsDisconnected) {
			try {
				Naming.unbind(fAddress);
			}
			catch (final Throwable e) {
			}
		}
		fRJServer = null;
	}
	
	
	@Override
	protected void doSubmit(final IProgressMonitor monitor) throws CoreException {
		fServerCallback.setAnswer(fCurrentInput + fLineSeparator);
		rjsRunMainLoop(fServerCallback, monitor);
	}
	
}
