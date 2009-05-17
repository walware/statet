/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico.impl;

import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_ADDRESS_DATA_KEY;
import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_CALLBACKS_DATA_KEY;
import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_MESSAGE_DATA_KEY;
import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_USERNAME_DATA_KEY;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.net.RMIAddress;

import de.walware.statet.nico.core.runtime.HistoryOperationsHandler;
import de.walware.statet.nico.core.runtime.IRemoteEngineController;
import de.walware.statet.nico.core.runtime.IToolEventHandler;
import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolProcess;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RReference;
import de.walware.rj.server.ConsoleCmdItem;
import de.walware.rj.server.DataCmdItem;
import de.walware.rj.server.ExtUICmdItem;
import de.walware.rj.server.FxCallback;
import de.walware.rj.server.MainCmdC2SList;
import de.walware.rj.server.MainCmdItem;
import de.walware.rj.server.MainCmdS2CList;
import de.walware.rj.server.RjException;
import de.walware.rj.server.RjsComObject;
import de.walware.rj.server.RjsPing;
import de.walware.rj.server.RjsStatus;
import de.walware.rj.server.RjsStatusImpl1;
import de.walware.rj.server.Server;
import de.walware.rj.server.ServerLogin;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.rdata.CombinedElement;
import de.walware.statet.r.internal.rdata.CombinedFactory;
import de.walware.statet.r.nico.AbstractRController;
import de.walware.statet.r.nico.IRCombinedDataAdapter;
import de.walware.statet.r.nico.IRDataAdapter;
import de.walware.statet.r.nico.RTool;
import de.walware.statet.r.nico.RWorkspace;


/**
 * Controller for RJ-Server
 */
public class RjsController extends AbstractRController implements IRemoteEngineController, IRDataAdapter, IRCombinedDataAdapter {
	
	
	static {
		DataCmdItem.registerRObjectFactory(CombinedFactory.FACTORY_ID, CombinedFactory.INSTANCE);
	}
	
	
	private RMIAddress fAddress;
	private String[] fRArgs;
	
	private Server fRjServer;
	private int fTicket;
	private final MainCmdC2SList fC2SList = new MainCmdC2SList();
	private boolean fConsoleReadCallbackRequest;
	private MainCmdItem fConsoleReadCallback;
	private int fDataLevelRequest = 0;
	private int fDataLevelAnswer = 0;
	private final DataCmdItem[] fDataAnswer = new DataCmdItem[16];
	private boolean fIsBusy = true;
	
	
	private boolean fEmbedded;
	private boolean fStartup;
	private String fStartupSnippet;
	private boolean fIsDisconnected = false;
	
	
	/**
	 * 
	 * @param process the process the controller belongs to
	 * @param address the RMI address
	 * @param username optional username, must not be correct
	 * @param sshPort optional sshPort
	 * @param embedded flag if running in embedded mode
	 * @param startup flag to start R (otherwise connect only)
	 * @param rArgs R arguments (required only if startup is <code>true</code>)
	 * @param initialWD
	 */
	public RjsController(final ToolProcess<RWorkspace> process, 
			final RMIAddress address, final Map<String, Object> initData,
			final boolean embedded, final boolean startup, final String[] rArgs,
			final IFileStore initialWD) {
		super(process, initData);
		if (address == null) {
			throw new IllegalArgumentException();
		}
		process.registerFeatureSet(RTool.R_DATA_FEATURESET_ID);
		if (!embedded) {
			process.registerFeatureSet(IRemoteEngineController.FEATURE_SET_ID);
		}
		fAddress = address;
		fEmbedded = embedded;
		fStartup = startup;
		fRArgs = rArgs;
		
		fWorkspaceData = new RWorkspace(this, (embedded || address.isLocalHost()) ? null :
				address.getHostAddress().getHostAddress());
		setWorkspaceDir(initialWD);
		initRunnableAdapter();
	}
	
	public void setStartupSnippet(final String code) {
		fStartupSnippet = code;
	}
	
	
	@Override
	public boolean supportsBusy() {
		return true;
	}
	
	@Override
	public boolean isBusy() {
		return fIsBusy;
	}
	
	public boolean isDisconnected() {
		return fIsDisconnected;
	}
	
	/**
	 * This is an async operation
	 * cancel is not supported by this implementation
	 * 
	 * @param monitor a progress monitor
	 */
	public void disconnect(final IProgressMonitor monitor) throws CoreException {
		switch (getStatus()) {
		case STARTED_IDLING:
		case STARTED_PROCESSING:
		case STARTED_PAUSED:
			monitor.beginTask("Disconnecting from R remote engine...", 1);
			synchronized (fQueue) {
				beginInternalTask();
			}
			try {
				final Server server = fRjServer;
				if (server != null) {
					server.disconnect(fTicket);
				}
				fIsDisconnected = true;
			}
			catch (final RemoteException e) {
				throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
						ICommonStatusConstants.LAUNCHING,
						"Disconnecting from R remote engine failed.", e));
			}
			finally {
				synchronized (fQueue) {
					scheduleControllerRunnable(new IToolRunnable() {
						public SubmitType getSubmitType() {
							return SubmitType.OTHER;
						}
						public String getTypeId() {
							return "common/disconnect/finish"; //$NON-NLS-1$
						}
						public String getLabel() {
							return "Disconnect";
						}
						public void changed(final int event, final ToolProcess process) {
						}
						public void run(final IToolRunnableControllerAdapter adapter, final IProgressMonitor monitor) throws InterruptedException, CoreException {
							if (!isTerminated()) {
								try {
									rjsSendPing(true, monitor);
								}
								catch (final RemoteException e) {
									rjsHandleStatus(new RjsStatusImpl1(RjsStatus.INFO, Server.S_LOST), monitor);
								}
							}
						}
					});
					endInternalTask();
				}
				monitor.done();
			}
		}
	}
	
	
	@Override
	protected IToolRunnable createStartRunnable() {
		return new StartRunnable() {
			@Override
			public String getLabel() {
				return "Connect to and load remote R engine.";
			}
		};
	}
	
	@Override
	protected void startTool(final IProgressMonitor monitor) throws CoreException {
		final Server server;
		int[] version;
		try {
			server = (Server) Naming.lookup(fAddress.getAddress());
			version = server.getVersion();
		}
		catch (final MalformedURLException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING,
					NLS.bind("The specified address '{0}' is invalid.", fAddress), e));
		}
		catch (final NotBoundException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING,
					"The specified R engine is not in the service registry (RMI).", e));
		}
		catch (final RemoteException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING,
					"The host/service registry (RMI) cannot be accessed.", e));
		}
		catch (final ClassCastException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING,
					NLS.bind("The specified R engine ({0}) is incompatibel to this client (0.2.x).", RjsUtil.getVersionString(null)), e));
		}
		if (version.length != 3 || version[0] != 0 || version[1] != 2) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING,
					NLS.bind("The specified R engine ({0}) is incompatibel to this client (0.2.x).", RjsUtil.getVersionString(version)),
					null));
		}
		
		try {
			final Map<String, Object> data = new HashMap<String, Object>();
			final IToolEventHandler loginHandler = getEventHandler(IToolEventHandler.LOGIN_REQUEST_EVENT_ID);
			String msg = null;
			TRY_LOGIN: while (fRjServer == null) {
				final Map<String, Object> initData = getInitData();
				final ServerLogin login = server.createLogin();
				try {
					final Callback[] callbacks = login.getCallbacks();
					if (callbacks != null) {
						final List<Callback> checked = new ArrayList<Callback>();
						FxCallback fx = null;
						for (final Callback callback : callbacks) {
							if (callback instanceof FxCallback) {
								fx = (FxCallback) callback;
							}
							else {
								checked.add(callback);
							}
						}
						
						if (initData != null) {
							data.putAll(initData);
						}
						data.put(LOGIN_ADDRESS_DATA_KEY, (fx != null) ? fAddress.getHost() : fAddress.getAddress());
						data.put(LOGIN_MESSAGE_DATA_KEY, msg);
						data.put(LOGIN_CALLBACKS_DATA_KEY, checked.toArray(new Callback[checked.size()]));
						
						if (loginHandler == null) {
							throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
									ICommonStatusConstants.LAUNCHING,
									"Login requested but not supported by this configuration.", null));
						}
						if (loginHandler.handle(IToolEventHandler.LOGIN_REQUEST_EVENT_ID, this, data, monitor) != IToolEventHandler.OK) {
							throw new CoreException(Status.CANCEL_STATUS);
						}
						
						if (fx != null) {
							RjsUtil.handleFxCallback(RjsUtil.getSession(data, new SubProgressMonitor(monitor, 1)), fx, new SubProgressMonitor(monitor, 1));
						}
					}
					
					msg = null;
					if (monitor.isCanceled()) {
						throw new CoreException(Status.CANCEL_STATUS);
					}
					
					if (fStartup) {
						fTicket = server.start(login.createAnswer(), fRArgs);
					}
					else {
						fTicket = server.connect(login.createAnswer());
					}
					
					fRjServer = server;
					
					if (callbacks != null) {
						loginHandler.handle(IToolEventHandler.LOGIN_OK_EVENT_ID, this, data, monitor);
						if (initData != null) {
							initData.put(LOGIN_USERNAME_DATA_KEY, data.get(LOGIN_USERNAME_DATA_KEY));
						}
					}
				}
				catch (final LoginException e) {
					msg = e.getLocalizedMessage();
				}
				finally {
					if (login != null) {
						login.clearData();
					}
				}
			}
			
			if (fStartup && fStartupSnippet != null && fStartupSnippet.length() > 0) {
				submit(RUtil.LINE_SEPARATOR_PATTERN.split(fStartupSnippet), SubmitType.OTHER);
			}
			fStartupSnippet = null;
			
			rjsRunMainLoop(null, null, monitor);
			fConsoleReadCallbackRequest = true;
			
			scheduleControllerRunnable(new IToolRunnable() {
				public SubmitType getSubmitType() {
					return SubmitType.OTHER;
				}
				public String getTypeId() {
					return "r/rj/start2";
				}
				public String getLabel() {
					return "Finish Initialization / Read Output";
				}
				public void changed(final int event, final ToolProcess process) {
				}
				public void run(final IToolRunnableControllerAdapter adapter, final IProgressMonitor monitor) throws InterruptedException, CoreException {
					if (fConsoleReadCallback == null) {
						rjsRunMainLoop(null, null, monitor);
					}
				}
			});
		}
		catch (final RemoteException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING,
					"The R engine could not be started.", e));
		}
		catch (final RjException e) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING,
					"An error occured when creating login data.", e));
		}
	}
	
	protected final Server ensureServer() {
		final Server server = fRjServer;
		if (server == null) {
			throw new IllegalStateException();
		}
		return server;
	}
	
//	public void controlNotification(final RjsComObject com) throws RemoteException {
//		if (com instanceof RjsStatus) {
//			final RjsStatusImpl2 serverStatus = (RjsStatusImpl2) com;
//			if (serverStatus.getCode() == Server.S_DISCONNECTED || serverStatus.getCode() == Server.S_STOPPED) {
//				scheduleControllerRunnable(new IToolRunnable() {
//					public String getTypeId() {
//						return null;
//					}
//					public String getLabel() {
//						return "Update State";
//					}
//					public SubmitType getSubmitType() {
//						return SubmitType.OTHER;
//					}
//					public void changed(final int event, final ToolProcess process) {
//					}
//					public void run(final IToolRunnableControllerAdapter tools, final IProgressMonitor monitor)
//							throws InterruptedException, CoreException {
//						if (!isTerminated()) {
//							rjsHandleStatus(serverStatus, monitor);
//						}
//					}
//					
//				});
//			}
//		}
//	}
	
	protected void rjsRunMainLoop(RjsComObject sendCom, MainCmdItem sendItem, final IProgressMonitor monitor) throws CoreException {
		int ok = 0;
		while (true) {
			try {
				RjsComObject receivedCom = null;
				if (sendItem != null) {
					if (sendItem.getCmdType() == MainCmdItem.T_CONSOLE_READ_ITEM) {
						fConsoleReadCallback = null;
					}
					fC2SList.setObjects(sendItem);
					sendCom = fC2SList;
					sendItem = null;
				}
//				System.out.println("client *-> server: " + sendCom);
				receivedCom = ensureServer().runMainLoop(fTicket, sendCom);
				sendCom = null;
//				System.out.println("client *<- server: " + receivedCom);
				switch (receivedCom.getComType()) {
				case RjsComObject.T_PING:
					sendCom = RjsStatus.OK_STATUS;
					ok = 0;
					continue;
				case RjsComObject.T_MAIN_LIST:
					sendItem = rjsHandleMainList((MainCmdS2CList) receivedCom, monitor);
					ok = 0;
					if (sendItem == null
							&& (!fConsoleReadCallbackRequest || fConsoleReadCallback != null)
							&& (fDataLevelRequest == fDataLevelAnswer) ) {
						return;
					}
					continue;
				case RjsComObject.T_STATUS:
					rjsHandleStatus((RjsStatus) receivedCom, monitor);
					ok = 0;
					return;
				}
			}
			catch (final RemoteException e) {
				RCorePlugin.logError(-1, "Communication error detail\nSEND="+sendItem, e);
//				e.printStackTrace(System.out);
				if (ping()) {
					if (fConsoleReadCallback == null && ok == 0) {
						ok++;
						handleStatus(new Status(IStatus.ERROR, RCore.PLUGIN_ID, "Communication error, see Eclipse log for detail."), monitor);
						continue;
					}
					throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, "Communication error.", e));
				}
				else {
					rjsHandleStatus(new RjsStatusImpl1(RjsComObject.V_INFO, Server.S_LOST), monitor);
					// throws CoreException
				}
			}
		}
	}
	
	private void rjsHandleStatus(final RjsStatus serverStatus, final IProgressMonitor monitor)
			throws CoreException {
		IStatus status;
		switch(serverStatus.getCode()) {
		case 0:
			return;
		case Server.S_DISCONNECTED:
			fIsDisconnected = true;
		case Server.S_LOST:
			if (fIsDisconnected) {
				status = new Status(IStatus.INFO, RCore.PLUGIN_ID, "R disconnected.");
				markAsTerminated();
				break;
			}
			else if (!fEmbedded) {
				status = new Status(IStatus.INFO, RCore.PLUGIN_ID, "R connection lost.");
				fIsDisconnected = true;
				markAsTerminated();
				break;
			}
			// continue stopped
		case Server.S_STOPPED:
			status = new Status(IStatus.INFO, RCore.PLUGIN_ID, "R stopped.");
			markAsTerminated();
			break;
		default:
			throw new IllegalStateException();
		}
		
		handleStatus(status, monitor);
		throw new CoreException(status);
	}
	
	private MainCmdItem rjsHandleMainList(final MainCmdS2CList list, final IProgressMonitor monitor) throws RemoteException, CoreException {
		try {
			final boolean isBusy = list.isBusy();
			if (fIsBusy != isBusy) {
				fIsBusy = isBusy;
//				loopBusyChanged(isBusy);
			}
			ITER_ITEMS : for (MainCmdItem item = list.getItems(); (item != null); item = item.next) {
				switch (item.getCmdType()) {
				case MainCmdItem.T_CONSOLE_WRITE_ITEM:
					((item.getCmdOption() == RjsComObject.V_OK) ?
							fDefaultOutputStream : fErrorOutputStream)
							.append(item.getDataText(), fCurrentRunnable.getSubmitType(), 0);
					continue ITER_ITEMS;
				case MainCmdItem.T_CONSOLE_READ_ITEM: {
					setCurrentPrompt(item.getDataText(), (item.getCmdOption() & 0xf) == RjsComObject.V_TRUE);
					if (item != list.getItems()) {
						rjsSendPing(false, monitor);
					}
					fConsoleReadCallback = item;
//					if (fDataLevelRequest > 0) {
//						throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, "Illegal nested console read"));
//					}
					return null;
				}
				case MainCmdItem.T_MESSAGE_ITEM:
					fInfoStream.append(item.getDataText(), fCurrentRunnable.getSubmitType(), 0);
					continue ITER_ITEMS;
				case MainCmdItem.T_EXTENDEDUI_ITEM:
					if (item.waitForClient()) {
						return rjsHandleUICallback((ExtUICmdItem) item, monitor);
					}
					else {
						rjsHandleUICallback((ExtUICmdItem) item, monitor);
						continue ITER_ITEMS;
					}
				case MainCmdItem.T_DATA_ITEM:
					if (fDataLevelRequest > 0) {
						if (addDataAnswer((DataCmdItem) item)) {
							rjsSendPing(false, monitor);
						}
					}
					continue ITER_ITEMS;
				default:
					throw new RemoteException("Illegal command from server: " + item.toString());
				}
			}
			return null;
		}
		catch (final RemoteException e) {
			throw e;
		}
		catch (final CoreException e) {
			throw e;
		}
		catch (final Exception e) {
			rjsComErrorRestore(e, monitor);
			// try to recover
			rjsSendPing(true, monitor);
			ITER_ITEMS : for (MainCmdItem item = list.getItems(); (item != null); item = item.next) {
				if (item.waitForClient()) {
					if (item.getCmdType() == MainCmdItem.T_CONSOLE_READ_ITEM && item instanceof ConsoleCmdItem) {
						fConsoleReadCallback = item;
						return null;
					}
					else {
						item.setAnswer(RjsComObject.V_ERROR);
						return item;
					}
				}
			}
			return null;
		}
	}
	
	private void rjsSendPing(final boolean checkAnswer, final IProgressMonitor monitor) throws RemoteException, CoreException {
//		System.out.println("client *-> server: " + RjsPing.INSTANCE);
		final RjsComObject receivedCom = ensureServer().runMainLoop(fTicket, RjsPing.INSTANCE);
//		System.out.println("client *<- server: " + receivedCom);
		if (checkAnswer) {
			if (receivedCom.getComType() != RjsComObject.T_STATUS) {
				throw new IllegalStateException();
			}
			rjsHandleStatus((RjsStatus) receivedCom, monitor);
		}
	}
	
	private ExtUICmdItem rjsHandleUICallback(final ExtUICmdItem cmd, final IProgressMonitor monitor) throws Exception {
		final String command = cmd.getCommand();
		// if we have more commands, we should create a hashmap
		try {
			if (command.equals(ExtUICmdItem.C_CHOOSE_FILE)) {
				final IToolEventHandler handler = getEventHandler(IToolEventHandler.SELECTFILE_EVENT_ID);
				if (handler != null) {
					final Map<String, Object> data = new HashMap<String, Object>();
					data.put("newResource", ((cmd.getCmdOption() & ExtUICmdItem.O_NEW) == ExtUICmdItem.O_NEW)); 
					if (handler.handle(IToolEventHandler.SELECTFILE_EVENT_ID, this, data, monitor) == IToolEventHandler.OK) {
						cmd.setAnswer((String) data.get("filename")); 
						return cmd;
					}
				}
				cmd.setAnswer(RjsComObject.V_CANCEL);
				return cmd;
			}
			if (command.equals(ExtUICmdItem.C_LOAD_HISTORY)) {
				handleUICmdByDataTextHandler(cmd, HistoryOperationsHandler.LOAD_HISTORY_ID, "filename", monitor); 
				return cmd;
			}
			if (command.equals(ExtUICmdItem.C_SAVE_HISTORY)) {
				handleUICmdByDataTextHandler(cmd, HistoryOperationsHandler.SAVE_HISTORY_ID, "filename", monitor); 
				return cmd;
			}
			if (command.equals(ExtUICmdItem.C_ADDTO_HISTORY)) {
				handleUICmdByDataTextHandler(cmd, HistoryOperationsHandler.ADDTO_HISTORY_ID, "text", monitor); 
				return cmd;
			}
			if (command.equals(ExtUICmdItem.C_SHOW_HISTORY)) {
				handleUICmdByDataTextHandler(cmd, IToolEventHandler.SHOW_HISTORY_ID, "pattern", monitor); 
				return cmd;
			}
			if (command.equals(ExtUICmdItem.C_OPENIN_EDITOR)) {
				handleUICmdByDataTextHandler(cmd, IToolEventHandler.SHOW_FILE_ID, "filename", monitor); 
				return cmd;
			}
			throw new Exception("Unknown command.");
		}
		catch (final Exception e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
					NLS.bind("An error occurred when exec RJ UI command ''{0}''.", command), e)); 
			if (cmd.waitForClient()) {
				cmd.setAnswer(RjsStatus.ERROR);
				return cmd;
			}
			else {
				return null;
			}
		}
	}
	
	private void handleUICmdByDataTextHandler(final ExtUICmdItem cmd, final String handlerId, final String textDataKey, final IProgressMonitor monitor) {
		final IToolEventHandler handler = getEventHandler(handlerId);
		if (handler != null) {
			final Map<String, Object> data = new HashMap<String, Object>();
			data.put(textDataKey, cmd.getDataText());
			cmd.setAnswer(handler.handle(handlerId, this, data, monitor));
			return;
		}
		RCorePlugin.log(new Status(IStatus.WARNING, RCore.PLUGIN_ID, -1,
				NLS.bind("Unhandled RJ UI command ''{0}'': no event handler for ''{1}''.", cmd.getCommand(), handlerId), null)); 
		cmd.setAnswer(RjsComObject.V_CANCEL);
	}
	
	private void rjsComErrorRestore(final Throwable e, final IProgressMonitor monitor) {
		handleStatus(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
				"An error occurred when running tasks for R. StatET will try to restore the communication, otherwise quit R.", e), monitor);
	}
	
	@Override
	protected void interruptTool(final int hardness) throws UnsupportedOperationException {
		final Server server = fRjServer;
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
		fCurrentInput = ""; 
		doSubmit(monitor);
	}
	
	@Override
	protected boolean isToolAlive() {
		if (!ping()) {
			return false;
		}
		if (Thread.currentThread() == getControllerThread() && fConsoleReadCallback == null) {
			return false;
		}
		return true;
	}
	
	private boolean ping() {
		final Server server = fRjServer;
		if (server != null) {
			try {
				return (RjsStatus.OK_STATUS.equals(server.runAsync(fTicket, RjsPing.INSTANCE)));
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
		
		if (fEmbedded && !fIsDisconnected) {
			try {
				Naming.unbind(fAddress.getAddress());
			}
			catch (final Throwable e) {
			}
		}
		
		fRjServer = null;
	}
	
	@Override
	protected int finishTool() {
		int exitCode = 0;
		if (fIsDisconnected) {
			exitCode = ToolProcess.EXITCODE_DISCONNECTED;
		}
		return exitCode;
	}
	
	
	@Override
	protected void doSubmit(final IProgressMonitor monitor) throws CoreException {
		fConsoleReadCallback.setAnswer(fCurrentInput + fLineSeparator);
		rjsRunMainLoop(null, fConsoleReadCallback, monitor);
	}
	
	
	private int newDataLevel() {
		final int level = ++fDataLevelRequest;
		if (level >= fDataAnswer.length) {
			fDataLevelRequest--;
			throw new UnsupportedOperationException("too much nested operations");
		}
		fDataLevelAnswer = 0;
		return level;
	}
	
	private void finalizeDataLevel() {
		final int level = fDataLevelRequest--;
		fDataAnswer[level] = null;
		fDataLevelAnswer = 0;
	}
	
	private boolean addDataAnswer(final DataCmdItem item) {
		fDataAnswer[fDataLevelRequest] = item;
		if (item.waitForClient()) {
			fDataLevelAnswer = fDataLevelRequest;
			return true;
		}
		return false;
	}
	
	public void evalVoid(final String command, final IProgressMonitor monitor) throws CoreException {
		newDataLevel();
		try {
			rjsRunMainLoop(null, new DataCmdItem(DataCmdItem.EVAL_VOID, 0, 0, command, null), monitor);
			return;
		}
		finally {
			finalizeDataLevel();
		}
	}
	
	public RObject evalData(final String command, final IProgressMonitor monitor) throws CoreException {
		return evalData(command, null, 0, -1, monitor);
	}
	
	public RObject evalData(final String command, final String factoryId,
			final int options, final int depth, final IProgressMonitor monitor) throws CoreException {
		final int level = newDataLevel();
		try {
			rjsRunMainLoop(null, new DataCmdItem(((options & RObjectFactory.F_ONLY_STRUCT) == RObjectFactory.F_ONLY_STRUCT) ?
					DataCmdItem.EVAL_STRUCT : DataCmdItem.EVAL_DATA, 0, depth, command, factoryId), monitor);
			final RObject data = (fDataAnswer[level] != null) ? (RObject) fDataAnswer[level].getData() : null;
			return data;
		}
		finally {
			finalizeDataLevel();
		}
	}
	
	public RObject evalData(final RReference reference, final IProgressMonitor monitor) throws CoreException {
		return evalData(reference, null, 0, -1, monitor);
	}
	
	public RObject evalData(final RReference reference, final String factoryId,
			final int options, final int depth, final IProgressMonitor monitor) throws CoreException {
		final int level = newDataLevel();
		try {
			final long handle = reference.getHandle();
			rjsRunMainLoop(null, new DataCmdItem(((options & RObjectFactory.F_ONLY_STRUCT) == RObjectFactory.F_ONLY_STRUCT) ?
					DataCmdItem.RESOLVE_STRUCT : DataCmdItem.RESOLVE_DATA, 0, depth, Long.toString(handle), factoryId), monitor);
			final RObject data = (fDataAnswer[level] != null) ? (RObject) fDataAnswer[level].getData() : null;
			return data;
		}
		finally {
			finalizeDataLevel();
		}
	}
	
	public ICombinedRElement evalCombinedStruct(final String command,
			final int options, final int depth, final IElementName name, final IProgressMonitor monitor) throws CoreException {
		final RObject data = evalData(command, CombinedFactory.FACTORY_ID, (options | RObjectFactory.F_ONLY_STRUCT), depth, monitor);
		if (data instanceof CombinedElement) {
			final CombinedElement e = (CombinedElement) data;
			CombinedFactory.INSTANCE.setElementName(e, name);
			return e;
		}
		return null;
	}
	
	public ICombinedRElement evalCombinedStruct(final RReference reference,
			final int options, final int depth, final IElementName name, final IProgressMonitor monitor) throws CoreException {
		final RObject data = evalData(reference, CombinedFactory.FACTORY_ID, (options | RObjectFactory.F_ONLY_STRUCT), depth, monitor);
		if (data instanceof CombinedElement) {
			final CombinedElement e = (CombinedElement) data;
			CombinedFactory.INSTANCE.setElementName(e, name);
			return e;
		}
		return null;
	}
	
}
