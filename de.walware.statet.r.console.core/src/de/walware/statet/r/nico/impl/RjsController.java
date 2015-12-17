/*=============================================================================#
 # Copyright (c) 2008-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 *
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.nico.impl;

import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_ADDRESS_DATA_KEY;
import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_CALLBACKS_DATA_KEY;
import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_MESSAGE_DATA_KEY;
import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_OK_EVENT_ID;
import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_REQUEST_EVENT_ID;
import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_USERNAME_DATA_KEY;

import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;

import com.ibm.icu.text.DateFormat;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.osgi.util.NLS;

import de.walware.jcommons.lang.SystemUtils;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.core.model.IModelElement;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.net.RMIAddress;
import de.walware.ecommons.ts.IToolCommandHandler;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;

import de.walware.statet.nico.core.runtime.IRemoteEngineController;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolStreamProxy;
import de.walware.statet.nico.core.util.TrackingConfiguration;

import de.walware.rj.RjException;
import de.walware.rj.data.RDataJConverter;
import de.walware.rj.data.REnvironment;
import de.walware.rj.data.RList;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RObjectFactory;
import de.walware.rj.data.RReference;
import de.walware.rj.data.defaultImpl.RObjectFactoryImpl;
import de.walware.rj.eclient.graphics.comclient.ERClientGraphicActions;
import de.walware.rj.server.ConsoleEngine;
import de.walware.rj.server.ConsoleWriteCmdItem;
import de.walware.rj.server.DbgCmdItem;
import de.walware.rj.server.FxCallback;
import de.walware.rj.server.RjsComConfig;
import de.walware.rj.server.RjsStatus;
import de.walware.rj.server.Server;
import de.walware.rj.server.ServerInfo;
import de.walware.rj.server.ServerLogin;
import de.walware.rj.server.client.AbstractRJComClient;
import de.walware.rj.server.client.FunctionCallImpl;
import de.walware.rj.server.client.RClientGraphicFactory;
import de.walware.rj.server.client.RGraphicCreatorImpl;
import de.walware.rj.server.dbg.CallStack;
import de.walware.rj.server.dbg.CtrlReport;
import de.walware.rj.server.dbg.DbgEnablement;
import de.walware.rj.server.dbg.DbgFilterState;
import de.walware.rj.server.dbg.DbgRequest;
import de.walware.rj.server.dbg.ElementTracepointInstallationReport;
import de.walware.rj.server.dbg.ElementTracepointInstallationRequest;
import de.walware.rj.server.dbg.FrameContext;
import de.walware.rj.server.dbg.FrameContextDetailRequest;
import de.walware.rj.server.dbg.SetDebugReport;
import de.walware.rj.server.dbg.SetDebugRequest;
import de.walware.rj.server.dbg.SrcfileData;
import de.walware.rj.server.dbg.TracepointEvent;
import de.walware.rj.server.dbg.TracepointStatesUpdate;
import de.walware.rj.services.FunctionCall;
import de.walware.rj.services.RGraphicCreator;
import de.walware.rj.services.RPlatform;
import de.walware.rj.services.RServiceControlExtension;

import de.walware.statet.r.console.core.IRBasicAdapter;
import de.walware.statet.r.console.core.IRDataAdapter;
import de.walware.statet.r.console.core.RConsoleTool;
import de.walware.statet.r.console.core.RDbg;
import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRModelManager;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.console.core.RConsoleCorePlugin;
import de.walware.statet.r.internal.nico.RNicoMessages;
import de.walware.statet.r.internal.rdata.CombinedElement;
import de.walware.statet.r.internal.rdata.CombinedFactory;
import de.walware.statet.r.nico.AbstractRDbgController;
import de.walware.statet.r.nico.ICombinedRDataAdapter;
import de.walware.statet.r.nico.IRModelSrcref;
import de.walware.statet.r.nico.IRSrcref;
import de.walware.statet.r.nico.RWorkspaceConfig;


/**
 * Controller for RJ-Server
 */
public class RjsController extends AbstractRDbgController
		implements IRemoteEngineController, IRDataAdapter, ICombinedRDataAdapter, RServiceControlExtension {
	
	
	static {
		RjsComConfig.registerRObjectFactory(CombinedFactory.FACTORY_ID, CombinedFactory.INSTANCE);
	}
	
	public static class RjsConnection {
		
		private final RMIAddress fRMIAddress;
		private final Server fServer;
		
		
		private RjsConnection(final RMIAddress rmiAddress, final Server server) {
			fRMIAddress = rmiAddress;
			fServer = server;
		}
		
		
		public RMIAddress getRMIAddress() {
			return fRMIAddress;
		}
		
		public Server getServer() {
			return fServer;
		}
		
	}
	
	
	public static RjsConnection lookup(final Registry registry, final RemoteException registryException,
			final RMIAddress address) throws CoreException {
		if (address == null) {
			throw new NullPointerException();
		}
		
		final int[] clientVersion = AbstractRJComClient.version();
		clientVersion[2] = -1;
		final Server server;
		int[] version;
		try {
			if (registryException != null) {
				throw registryException;
			}
			server = (Server) registry.lookup(address.getName());
			version = server.getVersion();
		}
		catch (final NotBoundException e) {
			throw new CoreException(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING,
					"The specified R engine is not in the service registry (RMI).", e ));
		}
		catch (final RemoteException e) {
			throw new CoreException(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING,
					NLS.bind("Cannot access the host/service registry (RMI) at ''{0}''.", address.getRegistryAddress()),
					e ));
		}
		catch (final ClassCastException e) {
			throw new CoreException(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING,
					NLS.bind("The specified R engine ({0}) is incompatibel to this client ({1}).", RjsUtil.getVersionString(null), RjsUtil.getVersionString(clientVersion)),
					e ));
		}
		if (version.length != 3 || version[0] != clientVersion[0] || version[1] != clientVersion[1]) {
			throw new CoreException(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING,
					NLS.bind("The specified R engine ({0}) is incompatibel to this client ({1}).", RjsUtil.getVersionString(version), RjsUtil.getVersionString(clientVersion)),
					null ));
		}
		return new RjsConnection(address, server);
	}
	
	
	private static final IModelElement.Filter TAG_ELEMENT_FILTER = new IModelElement.Filter() {
		@Override
		public boolean include(final IModelElement element) {
			return ((element.getElementType() & IRElement.MASK_C1) == IRElement.C1_METHOD);
		}
	};
	
	
	private class NicoComClient extends AbstractRJComClient {
		
		
		public NicoComClient() {
		}
		
		
		@Override
		protected void initGraphicFactory() {
			final IToolCommandHandler handler = getCommandHandler(INIT_RGRAPHIC_FACTORY_HANDLER_ID);
			final Map<String, Object> data= new HashMap<>();
			final IStatus status = executeHandler(INIT_RGRAPHIC_FACTORY_HANDLER_ID, handler, data, null);
			final RClientGraphicFactory factory = (RClientGraphicFactory) data.get("factory"); //$NON-NLS-1$
			if (status != null && status.isOK() && factory != null) {
				setGraphicFactory(factory, new ERClientGraphicActions(this, getTool()));
			}
		}
		
		@Override
		protected void updateBusy(final boolean isBusy) {
//			try {
				fIsBusy = isBusy;
//			}
//			catch (Exception e) {
//			}
		}
		
		@Override
		protected void updatePrompt(final String text, final boolean addToHistory) {
			try {
				RjsController.this.setCurrentPromptL(text, addToHistory);
			}
			catch (final Exception e) {
			}
		}
		
		@Override
		protected void writeConsoleOutput(final byte streamId, final String text) {
			try {
				final ToolStreamProxy streams = getStreams();
				final SubmitType submitType = getCurrentSubmitType();
				
				switch (streamId) {
				case ConsoleWriteCmdItem.R_OUTPUT:
					streams.getOutputStreamMonitor().append(text, submitType, 0);
					return;
				case ConsoleWriteCmdItem.R_ERROR:
					streams.getErrorStreamMonitor().append(text, submitType, 0);
					return;
				default:
					streams.getSystemOutputMonitor().append(text, submitType, 0);
					return;
				}
			}
			catch (final Exception e) {
			}
		}
		
		@Override
		protected void showMessage(final String text) {
			try {
				final ToolStreamProxy streams = getStreams();
				final SubmitType submitType = getCurrentSubmitType();
				
				streams.getInfoStreamMonitor().append(text, submitType, 0);
			}
			catch (final Exception e) {
			}
		}
		
		
		@Override
		protected RList handleUICallback(String commandId, final RList args,
				final IProgressMonitor monitor) throws Exception {
			// TODO: allow handlers to use RJ data objects
			// TODO: allow handlers to return values
			// TODO: provide extension point for event handlers
			IToolCommandHandler handler = getCommandHandler(commandId);
			if (handler == null && commandId.startsWith("r/")) {
				final String s = commandId.substring(2);
				handler = getCommandHandler(s);
				if (handler != null) {
					commandId = s;
				}
			}
			if (handler != null) {
				final RDataJConverter converter = new RDataJConverter();
				converter.setKeepArray1(false);
				converter.setRObjectFactory(fRObjectFactory);
				
				final Map<String, Object> javaArgs= new HashMap<>();
				if (args != null) {
					for (int i = 0; i < args.getLength(); i++) {
						javaArgs.put(args.getName(i), converter.toJava(args.get(i)));
					}
				}
				
				final IStatus status = handler.execute(commandId, RjsController.this, javaArgs, monitor);
				switch (status.getSeverity()) {
				case IStatus.OK:
					break;
				default:
					throw new CoreException(status);
				}
				
				Map<String, Object> javaAnswer = null;
				if (commandId.equals("common/chooseFile")) { //$NON-NLS-1$
					javaAnswer = Collections.singletonMap(
							"filename", javaArgs.get("filename") ); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				if (javaAnswer != null) {
					final RList answer = (RList) converter.toRJ(javaAnswer);
					return answer;
				}
				else {
					return null;
				}
			}
			
			return super.handleUICallback(commandId, args, monitor);
		}
		
		@Override
		protected void handleDbgEvent(final byte dbgOp, final Object event) {
			if (dbgOp == DbgCmdItem.OP_NOTIFY_TP_EVENT) {
				handle((TracepointEvent) event);
			}
			super.handleDbgEvent(dbgOp, event);
		}
		
		@Override
		protected void log(final IStatus status) {
			RConsoleCorePlugin.log(status);
		}
		
		@Override
		protected void handleServerStatus(final RjsStatus serverStatus, final IProgressMonitor monitor) throws CoreException {
			String specialMessage = null;
			switch (serverStatus.getCode()) {
			case 0:
				return;
			case Server.S_DISCONNECTED:
				fConnectionState = Server.S_DISCONNECTED;
				//$FALL-THROUGH$
			case Server.S_LOST:
				if (fConnectionState == Server.S_DISCONNECTED) {
					specialMessage = RNicoMessages.R_Info_Disconnected_message;
					break;
				}
				else if (!fEmbedded) {
					fConnectionState = Server.S_LOST;
					specialMessage = RNicoMessages.R_Info_ConnectionLost_message;
					break;
				}
				//$FALL-THROUGH$
			case Server.S_STOPPED:
				fConnectionState = Server.S_STOPPED;
				specialMessage = RNicoMessages.R_Info_Stopped_message;
				break;
			default:
				throw new IllegalStateException();
			}
			
			if (!isClosed()) {
				markAsTerminated();
				setClosed(true);
				handleStatus(new Status(IStatus.INFO, RConsoleCorePlugin.PLUGIN_ID, addTimestampToMessage(specialMessage, System.currentTimeMillis())), monitor);
			}
			throw new CoreException(new Status(IStatus.CANCEL, RConsoleCorePlugin.PLUGIN_ID, specialMessage));
		}
		
		@Override
		protected void handleStatus(final Status status, final IProgressMonitor monitor) {
			RjsController.this.handleStatus(status, monitor);
		}
		
		@Override
		protected void processHotMode() {
			runHotModeLoop();
		}
		
		@Override
		protected void processExtraMode(final int position) {
			runSuspendedLoopL(SUSPENDED_DEEPLEVEL);
		}
		
		@Override
		protected void scheduleConnectionCheck() {
			synchronized (fQueue) {
				if (getStatusL().isWaiting()) {
					scheduleControllerRunnable(new ControllerSystemRunnable(
							"r/check", "Connection Check") { //$NON-NLS-1$
						
						@Override
						public void run(final IToolService s,
								final IProgressMonitor monitor) throws CoreException {
							fRjs.runMainLoopPing(monitor);
						}
						
					});
				}
			}
		}
		
	}
	
	
	private final RMIAddress fAddress;
	private final String[] fRArgs;
	
	private boolean fIsBusy = true;
	
	private final RjsConnection fRjsConnection;
	private final NicoComClient fRjs = new NicoComClient();
	private int fRjsId;
	
	private final boolean fEmbedded;
	private final boolean fStartup;
	private final Map<String, Object> fRjsProperties;
	
	private int fConnectionState;
	
	private final RObjectFactory fRObjectFactory = RObjectFactoryImpl.INSTANCE;
	
	
	/**
	 * 
	 * @param process the R process the controller belongs to
	 * @param address the RMI address
	 * @param initData the initialization data
	 * @param embedded flag if running in embedded mode
	 * @param startup flag to start R (otherwise connect only)
	 * @param rArgs R arguments (required only if startup is <code>true</code>)
	 * @param initialWD
	 */
	public RjsController(final RProcess process,
			final RMIAddress address, final RjsConnection connection, final Map<String, Object> initData,
			final boolean embedded, final boolean startup, final String[] rArgs,
			final Map<String, Object> rjsProperties, final IFileStore initialWD,
			final RWorkspaceConfig workspaceConfig,
			final List<TrackingConfiguration> trackingConfigurations) {
		super(process, initData);
		if (address == null || connection == null) {
			throw new IllegalArgumentException();
		}
		process.registerFeatureSet(RConsoleTool.R_DATA_FEATURESET_ID);
		process.registerFeatureSet("de.walware.rj.services.RService"); //$NON-NLS-1$
		if (!embedded) {
			process.registerFeatureSet(IRemoteEngineController.FEATURE_SET_ID);
		}
		fAddress = address;
		fRjsConnection = connection;
		fEmbedded = embedded;
		fStartup = startup;
		fRArgs = rArgs;
		fRjsProperties = (rjsProperties != null) ? rjsProperties : new HashMap<String, Object>();
		
		fTrackingConfigurations = trackingConfigurations;
		
		setWorksapceData(new RWorkspace(this,
				(embedded || address.isLocalHost()) ? null :
						address.getHostAddress().getHostAddress(), workspaceConfig ));
		setWorkspaceDirL(initialWD);
		initRunnableAdapterL();
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
	public boolean isDisconnected() {
		return (fConnectionState == Server.S_DISCONNECTED || fConnectionState == Server.S_LOST);
	}
	
	/**
	 * This is an async operation
	 * cancel is not supported by this implementation
	 * 
	 * @param monitor a progress monitor
	 */
	@Override
	public void disconnect(final IProgressMonitor monitor) throws CoreException {
		switch (getStatus()) {
		case STARTED_IDLING:
		case STARTED_SUSPENDED:
		case STARTED_PROCESSING:
		case STARTED_PAUSED:
			monitor.beginTask("Disconnecting from R remote engine...", 1);
			synchronized (fQueue) {
				beginInternalTask();
			}
			try {
				fRjs.getConsoleServer().disconnect();
				fConnectionState = Server.S_DISCONNECTED;
			}
			catch (final RemoteException e) {
				throw new CoreException(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID,
						ICommonStatusConstants.LAUNCHING,
						"Disconnecting from R remote engine failed.", e));
			}
			finally {
				synchronized (fQueue) {
					scheduleControllerRunnable(new ControllerSystemRunnable(
							"common/disconnect/finish", "Disconnect") { //$NON-NLS-1$
						
						@Override
						public void run(final IToolService s,
								final IProgressMonitor monitor) throws CoreException {
							if (!isTerminated()) {
								fRjs.runMainLoopPing(monitor);
								fRjs.handleServerStatus(new RjsStatus(RjsStatus.INFO, Server.S_DISCONNECTED), monitor);
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
	protected void startToolL(final IProgressMonitor monitor) throws CoreException {
		fRjsId = RjsComConfig.registerClientComHandler(fRjs);
		fRjs.initClient(getTool(), this, fRjsProperties, fRjsId);
		try {
			final Map<String, Object> data= new HashMap<>();
			final IToolCommandHandler loginHandler = getCommandHandler(LOGIN_REQUEST_EVENT_ID);
			String msg = null;
			boolean connected = false;
			while (!connected) {
				final Map<String, Object> initData = getInitData();
				final ServerLogin login = fRjsConnection.getServer().createLogin(Server.C_CONSOLE_CONNECT);
				try {
					final Callback[] callbacks = login.getCallbacks();
					if (callbacks != null) {
						final List<Callback> checked= new ArrayList<>();
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
							throw new CoreException(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID,
									ICommonStatusConstants.LAUNCHING,
									"Login requested but not supported by this configuration.", null ));
						}
						if (!loginHandler.execute(LOGIN_REQUEST_EVENT_ID, this, data, monitor).isOK()) {
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
					
					final Map<String, Object> args= new HashMap<>();
					args.putAll(fRjsProperties);
					ConsoleEngine rjServer;
					if (fStartup) {
						args.put("args", fRArgs); //$NON-NLS-1$
						rjServer = (ConsoleEngine) fRjsConnection.getServer().execute(Server.C_CONSOLE_START, args, login.createAnswer());
					}
					else {
						rjServer = (ConsoleEngine) fRjsConnection.getServer().execute(Server.C_CONSOLE_CONNECT, args, login.createAnswer());
					}
					fRjs.setServer(rjServer, 0);
					connected = true;
					
					if (callbacks != null) {
						loginHandler.execute(LOGIN_OK_EVENT_ID, this, data, monitor);
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
			
			final ServerInfo info = fRjsConnection.getServer().getInfo();
			if (getWorkspaceData().isRemote()) {
				try {
					final String wd = FileUtil.toString(getWorkspaceData().toFileStore(info.getDirectory()));
					if (wd != null) {
						setStartupWD(wd);
					}
				}
				catch (final CoreException e) {}
				
				try {
					String sep= fRjs.getProperty(SystemUtils.FILE_SEPARATOR_KEY);
					if (sep == null) {
						final String osName= fRjs.getProperty(SystemUtils.OS_NAME_KEY);
						if (osName != null && SystemUtils.isOSWindows(osName)) {
							sep= "\\"; //$NON-NLS-1$
						}
					}
					if (sep != null && !sep.isEmpty()) {
						setFileSeparatorL(sep.charAt(0));
					}
				}
				catch (final Exception e) {}
			}
			else {
				setStartupWD(info.getDirectory());
			}
			final long timestamp = info.getTimestamp();
			if (timestamp != 0) {
				setStartupTimestamp(timestamp);
			}
			
			final List<IStatus> warnings= new ArrayList<>();
			
			initTracks(info.getDirectory(), monitor, warnings);
			
			if (fStartup && !fStartupsRunnables.isEmpty()) {
				fQueue.add(fStartupsRunnables);
				fStartupsRunnables.clear();
			}
			
			if (!fStartup) {
				handleStatus(new Status(IStatus.INFO, RConsoleCorePlugin.PLUGIN_ID,
						addTimestampToMessage(RNicoMessages.R_Info_Reconnected_message, getTool().getConnectionTimestamp()) ),
						monitor);
			}
			// fRjs.runMainLoop(null, null, monitor); must not wait at server side
			fRjs.activateConsole();
			scheduleControllerRunnable(new ControllerSystemRunnable(
					"r/rj/start2", "Finish Initialization / Read Output") { //$NON-NLS-1$
				
				@Override
				public void run(final IToolService s,
						final IProgressMonitor monitor) throws CoreException {
					if (!fRjs.isConsoleReady()) { // R is still working
						fRjs.runMainLoop(null, null, monitor);
					}
					for (final IStatus status : warnings) {
						handleStatus(status, monitor);
					}
				}
				
			});
		}
		catch (final RemoteException e) {
			throw new CoreException(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING,
					"The R engine could not be started.", e ));
		}
		catch (final RjException e) {
			throw new CoreException(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHING,
					"An error occured when creating login data.", e ));
		}
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
	
	
	protected String addTimestampToMessage(final String message, final long timestamp) {
		final String datetime = DateFormat.getDateTimeInstance().format(System.currentTimeMillis());
		return datetime + " - " + message; //$NON-NLS-1$
	}
	
	@Override
	protected void requestHotMode(final boolean async) {
		fRjs.requestHotMode(async);
	}
	
	@Override
	protected boolean initilizeHotMode() {
		return fRjs.startHotMode();
	}
	
	
	@Override
	protected int setSuspended(final int level, final int enterDetail, final Object enterData) {
		final int diff = super.setSuspended(level, enterDetail, enterData);
		if (level > 0 && diff > 0) {
			fRjs.requestExtraMode(
					(AbstractRJComClient.EXTRA_BEFORE | AbstractRJComClient.EXTRA_NESTED) );
		}
		return diff;
	}
	
	@Override
	protected CallStack doEvalCallStack(final IProgressMonitor monitor) throws CoreException {
		return (CallStack) fRjs.execSyncDbgOp(DbgCmdItem.OP_LOAD_FRAME_LIST,
				null, monitor );
	}
	
	@Override
	protected FrameContext doEvalFrameContext(final int position,
			final IProgressMonitor monitor) throws Exception {
		return (FrameContext) fRjs.execSyncDbgOp(DbgCmdItem.OP_LOAD_FRAME_CONTEXT,
				new FrameContextDetailRequest(position), monitor );
	}
	
	
	@Override
	protected void interruptTool() throws UnsupportedOperationException {
		fRjs.runAsyncInterrupt();
	}
	
	@Override
	protected void postCancelTask(final int options, final IProgressMonitor monitor) throws CoreException {
		super.postCancelTask(options, monitor);
		fCurrentInput = ""; //$NON-NLS-1$
		doSubmitL(monitor);
		fCurrentInput = ""; //$NON-NLS-1$
		doSubmitL(monitor);
	}
	
	@Override
	protected boolean isToolAlive() {
		if (fConnectionState != 0 || !fRjs.runAsyncPing()) {
			return false;
		}
		if (Thread.currentThread() == getControllerThread() && !isInHotModeL()
				&& !fRjs.isConsoleReady()) {
			return false;
		}
		return true;
	}
	
	@Override
	protected void killTool(final IProgressMonitor monitor) {
		fRjs.setClosed(true);
		final ToolProcess consoleProcess = getTool();
		// TODO: kill remote command?
		final IProcess[] processes = consoleProcess.getLaunch().getProcesses();
		for (int i = 0; i < processes.length; i++) {
			if (processes[i] != consoleProcess && !processes[i].isTerminated()) {
				try {
					processes[i].terminate();
				}
				catch (final Exception e) {
				}
			}
		}
	}
	
	@Override
	protected void clear() {
		fRjs.setClosed(true);
		
		super.clear();
		
		if (fEmbedded && !isDisconnected()) {
			try {
				Naming.unbind(fAddress.getAddress());
			}
			catch (final Throwable e) {
			}
		}
		fRjs.disposeAllGraphics();
		if (fRjsId > 0) {
			RjsComConfig.unregisterClientComHandler(fRjsId);
			fRjsId = 0;
		}
	}
	
	@Override
	protected int finishToolL() {
		int exitCode = 0;
		if (isDisconnected()) {
			exitCode = ToolProcess.EXITCODE_DISCONNECTED;
		}
		return exitCode;
	}
	
	@Override
	protected boolean canSuspend(final IProgressMonitor monitor) {
		return (fRjs.getDataLevel() == 0);
	}
	
	@Override
	protected void doRequestSuspend(final IProgressMonitor monitor) throws CoreException {
		fRjs.execSyncDbgOp(DbgCmdItem.OP_REQUEST_SUSPEND,
				null, monitor );
	}
	
	@Override
	protected SetDebugReport doExec(final SetDebugRequest request,
			final IProgressMonitor monitor) throws CoreException {
		return (SetDebugReport) fRjs.execSyncDbgOp(DbgCmdItem.OP_SET_DEBUG, request, monitor);
	}
	
	@Override
	protected CtrlReport doExec(final DbgRequest request,
			final IProgressMonitor monitor) throws CoreException {
		return (CtrlReport) fRjs.execSyncDbgOp(request.getOp(), request, monitor);
	}
	
	@Override
	protected void doPrepareSrcfile(final String srcfile, final String statetPath,
			final IProgressMonitor monitor) throws CoreException {
		final FunctionCall prepare = createFunctionCall("rj:::.statet.prepareSrcfile");
		prepare.addChar("filename", srcfile);
		prepare.addChar("path", statetPath);
		prepare.evalVoid(monitor);
	}
	
	@Override
	public ElementTracepointInstallationReport exec(
			final ElementTracepointInstallationRequest request,
			final IProgressMonitor monitor) throws CoreException {
		return (ElementTracepointInstallationReport) fRjs.execSyncDbgOp(
				DbgCmdItem.OP_INSTALL_TP_POSITIONS, request, monitor );
	}
	
	@Override
	public void exec(final DbgEnablement request) throws CoreException {
		fRjs.execAsyncDbgOp(DbgCmdItem.OP_SET_ENABLEMENT, request);
	}
	
	@Override
	public void exec(final DbgFilterState request) throws CoreException {
		fRjs.execAsyncDbgOp(DbgCmdItem.OP_RESET_FILTER_STATE, request);
	}
	
	@Override
	public void exec(final TracepointStatesUpdate request) throws CoreException {
		fRjs.execAsyncDbgOp(DbgCmdItem.OP_UPDATE_TP_STATES, request);
	}
	
	@Override
	public void exec(final TracepointStatesUpdate request,
			final IProgressMonitor monitor) throws CoreException {
		fRjs.execSyncDbgOp(DbgCmdItem.OP_UPDATE_TP_STATES, request, monitor);
	}
	
	
	@Override
	protected void doSubmitCommandL(final String[] lines, final SrcfileData srcfile,
			final IRSrcref srcref,
			final IProgressMonitor monitor) throws CoreException {
		if ((fCurrentPrompt.meta & (IRBasicAdapter.META_PROMPT_DEFAULT | IRBasicAdapter.META_PROMPT_SUSPENDED)) == 0) {
			super.doSubmitCommandL(lines, srcfile, srcref, monitor);
			return;
		}
		
		final FunctionCall prepare = createFunctionCall("rj:::.statet.prepareCommand");
		prepare.add("lines", fRObjectFactory.createVector(fRObjectFactory.createCharData(lines)));
		
		if (srcfile != null && srcref != null) {
			final List<String> attributeNames= new ArrayList<>();
			final List<RObject> attributeValues= new ArrayList<>();
			
			if (srcfile.getName() != null) {
				prepare.addChar("filename", srcfile.getName());
			}
//			if (srcfile.workspacePath != null) {
//				attributeNames.add("statet.Path");
//				attributeValues.add(fRObjectFactory.createVector(fRObjectFactory.createCharData(
//						new String[] { srcfile.workspacePath } )));
//			}
			if (srcfile.getTimestamp() != 0) {
				attributeNames.add("timestamp");
				attributeValues.add(fRObjectFactory.createVector(fRObjectFactory.createNumData(
						new double[] { srcfile.getTimestamp() } )));
			}
			final int[] rjSrcref = RDbg.createRJSrcref(srcref);
			if (rjSrcref != null) {
				attributeNames.add("linesSrcref");
				attributeValues.add(fRObjectFactory.createVector(fRObjectFactory.createIntData(
						rjSrcref )));
			}
			
			if (attributeNames.size() > 0) {
				prepare.add("srcfileAttributes", fRObjectFactory.createList(
						attributeValues.toArray(new RObject[attributeValues.size()]),
						attributeNames.toArray(new String[attributeNames.size()]) ));
			}
			
			if (srcref instanceof IRModelSrcref) {
				// Move to abstract controller or breakpoint adapter?
				final IRModelSrcref modelSrcref = (IRModelSrcref) srcref;
				final List<IRLangSourceElement> elements = modelSrcref.getElements();
				if (elements.size() > 0) {
					final List<String> elementIds= new ArrayList<>(elements.size());
					final List<RObject> elementIndexes= new ArrayList<>(elements.size());
					for (final IRLangSourceElement element : elements) {
						if (TAG_ELEMENT_FILTER.include(element)) {
							final FDef fdef = (FDef) element.getAdapter(FDef.class);
							if (fdef != null) {
								final String elementId = RDbg.getElementId(element);
								final RAstNode cont = fdef.getContChild();
								final int[] path = RAst.computeRExpressionIndex(cont,
										RAst.getRRootNode(cont, modelSrcref) );
								if (elementId != null && path != null) {
									final int[] fullPath = new int[path.length+1];
									fullPath[0] = 1;
									System.arraycopy(path, 0, fullPath, 1, path.length);
									elementIds.add(elementId);
									elementIndexes.add(fRObjectFactory.createVector(
											fRObjectFactory.createIntData(fullPath)));
								}
							}
						}
					}
					if (elementIds.size() > 0) {
						prepare.add("elementIds", fRObjectFactory.createList(
								elementIndexes.toArray(new RObject[elementIndexes.size()]),
								elementIds.toArray(new String[elementIds.size()]) ));
					}
				}
			}
		}
		
		prepare.evalVoid(monitor);
		
		final boolean addToHistory = (fCurrentPrompt.meta & IRBasicAdapter.META_HISTORY_DONTADD) == 0;
		fCurrentInput = lines[0];
		doBeforeSubmitL();
		for (int i = 1; i < lines.length; i++) {
			setCurrentPromptL(fContinuePromptText, addToHistory);
			fCurrentInput = lines[i];
			doBeforeSubmitL();
		}
		fCurrentInput = "rj:::.statet.evalCommand()";
		doSubmitL(monitor);
	}
	
	@Override
	public void doSubmitFileCommandToConsole(final String[] lines,
			final SrcfileData srcfile, final ISourceUnit su,
			final IProgressMonitor monitor) throws CoreException {
		if (srcfile != null && su instanceof IRWorkspaceSourceUnit
				&& su.getModelTypeId() == RModel.TYPE_ID) {
			try {
				final IRModelInfo modelInfo = (IRModelInfo) su.getModelInfo(RModel.TYPE_ID,
						IRModelManager.MODEL_FILE, monitor );
				if (modelInfo != null) {
					final IRLangSourceElement fileElement = modelInfo.getSourceElement();
					final RAstNode rootNode = (RAstNode) fileElement.getAdapter(IAstNode.class);
					final List<? extends IRLangSourceElement> elements = modelInfo.getSourceElement()
							.getSourceChildren(TAG_ELEMENT_FILTER);
					
					final List<String> elementIds= new ArrayList<>(elements.size());
					final List<RObject> elementIndexes= new ArrayList<>(elements.size());
					
					for (final IRLangSourceElement element : elements) {
						final FDef fdef = (FDef) element.getAdapter(FDef.class);
						if (fdef != null) {
							final String elementId = RDbg.getElementId(element);
							final RAstNode cont = fdef.getContChild();
							final int[] path = RAst.computeRExpressionIndex(cont, rootNode);
							if (elementId != null && path != null) {
								elementIds.add(elementId);
								elementIndexes.add(fRObjectFactory.createVector(
										fRObjectFactory.createIntData(path)));
							}
						}
					}
					
					final FunctionCall prepare = createFunctionCall("rj:::.statet.prepareSource"); //$NON-NLS-1$
					prepare.add(fRObjectFactory.createList(new RObject[] {
							fRObjectFactory.createVector(fRObjectFactory.createCharData(
									new String[] { srcfile.getPath() })),
							fRObjectFactory.createVector(fRObjectFactory.createNumData(
									new double[] { srcfile.getTimestamp() })),
							fRObjectFactory.createVector(fRObjectFactory.createIntData(
									new int[] { rootNode.getChildCount() })),
							fRObjectFactory.createList(
									elementIndexes.toArray(new RObject[elementIndexes.size()]),
									elementIds.toArray(new String[elementIds.size()]) ),
					}, new String[] { "path", "timestamp", "exprsLength", "elementIds" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					prepare.evalVoid(monitor);
				}
			}
			catch (final CoreException e) {
				RConsoleCorePlugin.log(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID, -1,
						NLS.bind("An error occurred when preparing element tagging for file ''{0}''.",
								srcfile.getPath() ), e ));
			}
		}
		super.doSubmitFileCommandToConsole(lines, srcfile, su, monitor);
	}
	
	@Override
	protected void doSubmitL(final IProgressMonitor monitor) throws CoreException {
		fRjs.answerConsole(fCurrentInput + fLineSeparator, monitor);
	}
	
	
	@Override
	public String getProperty(final String key) {
		return fRjs.getProperty(key);
	}
	
	@Override
	public RPlatform getPlatform() {
		return fRjs.getRPlatform();
	}
	
	@Override
	public void evalVoid(final String command, final IProgressMonitor monitor) throws CoreException {
		fRjs.evalVoid(command, null, monitor);
	}
	
	@Override
	public RObject evalData(final String command, final IProgressMonitor monitor) throws CoreException {
		return fRjs.evalData(command, null, null, 0, -1, monitor);
	}
	
	@Override
	public RObject evalData(final String command, final String factoryId,
			final int options, final int depth, final IProgressMonitor monitor) throws CoreException {
		return fRjs.evalData(command, null, factoryId, options, depth, monitor);
	}
	
	public RObject evalData(final String command, final RObject envir,
			final String factoryId, final int options, final int depth,
			final IProgressMonitor monitor) throws CoreException {
		return fRjs.evalData(command, envir, factoryId, options, depth, monitor);
	}
	
	@Override
	public RObject evalData(final RReference reference, final IProgressMonitor monitor) throws CoreException {
		return fRjs.evalData(reference, null, 0, -1, monitor);
	}
	
	@Override
	public RObject evalData(final RReference reference, final String factoryId,
			final int options, final int depth, final IProgressMonitor monitor) throws CoreException {
		return fRjs.evalData(reference, factoryId, options, depth, monitor);
	}
	
	public RObject[] findData(final String symbol, final RObject envir, final boolean inherits,
			final String factoryId, final int options, final int depth,
			final IProgressMonitor monitor) throws CoreException {
		return fRjs.findData(symbol, envir, inherits, factoryId, options, depth, monitor);
	}
	
	@Override
	public ICombinedRElement evalCombinedStruct(final String command,
			final int options, final int depth, final RElementName name, final IProgressMonitor monitor) throws CoreException {
		final RObject data = this.fRjs.evalData(command, null,
				CombinedFactory.FACTORY_ID, (options | RObjectFactory.F_ONLY_STRUCT), depth,
				monitor );
		if (data instanceof CombinedElement) {
			final CombinedElement e = (CombinedElement) data;
			e.setElementName(name);
			return e;
		}
		return null;
	}
	
	public ICombinedRElement evalCombinedStruct(final String command, final RObject envir,
			final int options, final int depth, final RElementName name,
			final IProgressMonitor monitor) throws CoreException {
		final RObject data= this.fRjs.evalData(command, envir,
				CombinedFactory.FACTORY_ID, (options | RObjectFactory.F_ONLY_STRUCT), depth,
				monitor );
		if (data instanceof CombinedElement) {
			final CombinedElement e = (CombinedElement) data;
			e.setElementName(name);
			return e;
		}
		return null;
	}
	
	private ICombinedRElement evalCombinedStructSpecialEnv(final RElementName name,
			final int options, final int depth,
			final IProgressMonitor monitor) throws CoreException {
		final byte envType;
		switch (name.getType()) {
		case RElementName.SCOPE_NS:
			envType= REnvironment.ENVTYPE_NAMESPACE_EXPORTS;
			break;
		case RElementName.SCOPE_NS_INT:
			envType= REnvironment.ENVTYPE_NAMESPACE;
			break;
		default:
			throw new IllegalArgumentException();
		}
		final RObject data= this.fRjs.evalData(envType, name.getSegmentName(),
				CombinedFactory.FACTORY_ID, (options | RObjectFactory.F_ONLY_STRUCT), depth,
				monitor );
		if (data instanceof CombinedElement) {
			final CombinedElement e = (CombinedElement) data;
			e.setElementName(name);
			return e;
		}
		return null;
	}
	
	@Override
	public ICombinedRElement evalCombinedStruct(final RElementName name,
			final int options, final int depth,
			final IProgressMonitor monitor) throws CoreException {
		switch (name.getType()) {
		case RElementName.SCOPE_NS:
		case RElementName.SCOPE_NS_INT:
			if (name.getNextSegment() == null) {
				return evalCombinedStructSpecialEnv(name, options, depth, monitor);
			}
			break;
		default:
			break;
		}
		
		final String command = RElementName.createDisplayName(name, RElementName.DISPLAY_FQN | RElementName.DISPLAY_EXACT);
		if (command == null) {
			throw new CoreException(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID, 0, "Illegal R element name.", null));
		}
		return evalCombinedStruct(command, options, depth, name, monitor);
	}
	
	@Override
	public ICombinedRElement evalCombinedStruct(final RReference reference,
			final int options, final int depth, final RElementName name,
			final IProgressMonitor monitor) throws CoreException {
		final RObject data = evalData(reference, CombinedFactory.FACTORY_ID, (options | RObjectFactory.F_ONLY_STRUCT), depth, monitor);
		if (data instanceof CombinedElement) {
			final CombinedElement e = (CombinedElement) data;
			e.setElementName(name);
			return e;
		}
		return null;
	}
	
	@Override
	public void assignData(final String expression, final RObject data,
			final IProgressMonitor monitor) throws CoreException {
		fRjs.assignData(expression, data, null, monitor);
	}
	
	@Override
	public void downloadFile(final OutputStream out, final String fileName, final int options,
			final IProgressMonitor monitor) throws CoreException {
		fRjs.downloadFile(out, fileName, options, monitor);
	}
	
	@Override
	public byte[] downloadFile(final String fileName, final int options,
			final IProgressMonitor monitor) throws CoreException {
		return fRjs.downloadFile(fileName, options, monitor);
	}
	
	@Override
	public void uploadFile(final InputStream in, final long length, final String fileName, final int options,
			final IProgressMonitor monitor) throws CoreException {
		fRjs.uploadFile(in, length, fileName, options, monitor);
	}
	
	@Override
	public FunctionCall createFunctionCall(final String name) throws CoreException {
		return new FunctionCallImpl(fRjs, name, fRObjectFactory);
	}
	
	@Override
	public RGraphicCreator createRGraphicCreator(final int options) throws CoreException {
		return new RGraphicCreatorImpl(this, fRjs, options);
	}
	
	
	@Override
	public void addCancelHandler(final Callable<Boolean> handler) {
		fRjs.addCancelHandler(handler);
	}
	
	@Override
	public void removeCancelHandler(final Callable<Boolean> handler) {
		fRjs.removeCancelHandler(handler);
	}
	
	@Override
	public Lock getWaitLock() {
		return fRjs.getWaitLock();
	}
	
	@Override
	public void waitingForUser(final IProgressMonitor monitor) {
		fRjs.waitingForUser();
	}
	
	@Override
	public void resume() {
		fRjs.resume();
	}
	
}
