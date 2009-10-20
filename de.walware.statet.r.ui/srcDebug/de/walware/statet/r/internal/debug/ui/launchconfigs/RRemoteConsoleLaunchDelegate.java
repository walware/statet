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

package de.walware.statet.r.internal.debug.ui.launchconfigs;

import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_SSH_HOST_DATA_KEY;
import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_SSH_PORT_DATA_KEY;
import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_USERNAME_DATA_KEY;
import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_USERNAME_FORCE_DATA_KEY;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;

import de.walware.ecommons.FileValidator;
import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.debug.core.OverlayLaunchConfiguration;
import de.walware.ecommons.debug.ui.LaunchConfigUtil;
import de.walware.ecommons.debug.ui.UnterminatedLaunchAlerter;
import de.walware.ecommons.net.RMIAddress;
import de.walware.ecommons.preferences.Preference;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.preferences.Preference.StringPref;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.variables.core.StringVariable;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.runtime.IRemoteEngineController;
import de.walware.statet.nico.core.runtime.IToolEventHandler;
import de.walware.statet.nico.core.runtime.Queue;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolRunner;
import de.walware.statet.nico.core.runtime.ToolStatus;
import de.walware.statet.nico.core.runtime.ToolController.IToolStatusListener;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.console.NIConsole;
import de.walware.statet.nico.ui.console.NIConsoleColorAdapter;
import de.walware.statet.nico.ui.util.LoginHandler;
import de.walware.statet.nico.ui.util.WorkbenchStatusHandler;

import de.walware.rj.server.Server;

import de.walware.statet.r.core.RUtil;
import de.walware.statet.r.debug.ui.launchconfigs.REnvTab;
import de.walware.statet.r.debug.ui.launchconfigs.RLaunchConfigurations;
import de.walware.statet.r.internal.debug.ui.RDebugPreferenceConstants;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.launching.RConsoleLaunching;
import de.walware.statet.r.nico.RWorkspace;
import de.walware.statet.r.nico.impl.RjsController;
import de.walware.statet.r.nico.impl.RjsUtil;
import de.walware.statet.r.nico.ui.RConsole;
import de.walware.statet.r.ui.RUI;


/**
 * 
 * 
 * TODO: externalize error message strings
 */
public class RRemoteConsoleLaunchDelegate extends LaunchConfigurationDelegate {
	
	
	public static final int DEFAULT_SSH_PORT = 22;
	
	public static final String WD_VARIABLE_NAME = "r_wd"; //$NON-NLS-1$
	public static final IStringVariable WD_VARIABLE = new StringVariable(WD_VARIABLE_NAME, "The configured R working directory (converted to remote path)");
	private static final Pattern WD_PATTERN = Pattern.compile("\\Q${"+WD_VARIABLE_NAME+"}\\E"); //$NON-NLS-1$ //$NON-NLS-2$
	
	public static final String ADDRESS_VARIABLE_NAME = "address"; //$NON-NLS-1$;
	public static final IStringVariable ADDRESS_VARIABLE = new StringVariable(ADDRESS_VARIABLE_NAME, "The address of the remote R engine");
	private static final Pattern ADDRESS_PATTERN = Pattern.compile("\\Q${"+ADDRESS_VARIABLE_NAME+"}\\E"); //$NON-NLS-1$
	
	public static final String NAME_VARIABLE_NAME = "name"; //$NON-NLS-1$;
	public static final IStringVariable NAME_VARIABLE = new StringVariable(NAME_VARIABLE_NAME, "The name of the remote R engine (last segment of the address)");
	private static final Pattern NAME_PATTERN = Pattern.compile("\\Q${"+NAME_VARIABLE_NAME+"}\\E"); //$NON-NLS-1$
	
	public static final String DEFAULT_COMMAND;
	private static final Preference<String> DEFAULT_COMMAND_PATH = new StringPref(RDebugPreferenceConstants.CAT_RREMOTE_LAUNCHING_QUALIFIER,
			"rj.startupscript.path"); //$NON-NLS-1$
	
	static {
		String path = PreferencesUtil.getInstancePrefs().getPreferenceValue(DEFAULT_COMMAND_PATH);
		if (path == null || path.length() == 0) {
			path = "~/.RJServer/startup.sh"; //$NON-NLS-1$
		}
		DEFAULT_COMMAND = path
				+ " \"${"+ADDRESS_VARIABLE_NAME+"}\"" //$NON-NLS-1$ //$NON-NLS-2$
				+ " -wd=\"${"+WD_VARIABLE_NAME+"}\""; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	
	private static final int TODO_START_SERVER = 1;
	private static final int TODO_START_R = 2;
	private static final int TODO_CONNECT = 3;
	
	
	public RRemoteConsoleLaunchDelegate() {
	}
	
	
	@Override
	protected boolean saveBeforeLaunch(final ILaunchConfiguration configuration, final String mode, final IProgressMonitor monitor) throws CoreException {
		return true; // continue launch
	}
	
	@Override
	public boolean buildForLaunch(final ILaunchConfiguration configuration, final String mode, final IProgressMonitor monitor) throws CoreException {
		return false; // no incremental build
	}
	
	public void launch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
			final IProgressMonitor monitor) throws CoreException {
		try {
			if (monitor.isCanceled()) {
				return;
			}
			
			final String type = configuration.getAttribute(RConsoleLaunching.ATTR_TYPE, "").trim(); //$NON-NLS-1$
			if (type.equals(RConsoleLaunching.REMOTE_RJS) || type.equals(RConsoleLaunching.REMOTE_RJS_SSH)) { 
				launchRjsJriRemote(configuration, mode, launch, monitor);
				return;
			}
			if (type.equals(RConsoleLaunching.REMOTE_RJS_RECONNECT)) {
				if (configuration.hasAttribute(IRemoteEngineController.LAUNCH_RECONNECT_ATTRIBUTE)) {
					launchRjsJriRemote(configuration, mode, launch, monitor);
					return;
				}
				
				final AtomicReference<String> address = new AtomicReference<String>();
				final String username = configuration.getAttribute(RConsoleLaunching.ATTR_LOGIN_NAME, (String) null);
				UIAccess.getDisplay().syncExec(new Runnable() {
					public void run() {
						final RRemoteConsoleSelectionDialog dialog = new RRemoteConsoleSelectionDialog(null, true);
						dialog.setUser(username);
						if (dialog.open() == Dialog.OK) {
							address.set((String) dialog.getFirstResult());
						}
					}
				});
				if (address.get() != null) {
					final Map<String, Object> map = new HashMap<String, Object>();
					map.put(IRemoteEngineController.LAUNCH_RECONNECT_ATTRIBUTE, Collections.EMPTY_MAP);
					map.put(RConsoleLaunching.ATTR_ADDRESS, address.get());
					launchRjsJriRemote(new OverlayLaunchConfiguration(configuration, map), mode, launch, monitor);
					return;
				}
				throw new CoreException(new Status(IStatus.CANCEL, RUI.PLUGIN_ID, ""));
			}
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					NLS.bind("R Remote Console launch type ''{0}'' is not available.", type), null));
		}
		finally {
			monitor.done();
		}
	}
	
	private void launchRjsJriRemote(final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
			final IProgressMonitor monitor) throws CoreException {
		final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
		final SubMonitor progress = LaunchConfigUtil.initProgressMonitor(configuration, monitor, 25);
		
		final String type = configuration.getAttribute(RConsoleLaunching.ATTR_TYPE, (String) null).trim();
		final String username = configuration.getAttribute(RConsoleLaunching.ATTR_LOGIN_NAME, (String) null);
		
		progress.worked(1);
		if (progress.isCanceled()) {
			return;
		}
		
		final Map<String, Object> reconnect = configuration.getAttribute(IRemoteEngineController.LAUNCH_RECONNECT_ATTRIBUTE, (Map<String, Object>) null);
		final ToolProcess prevProcess;
		boolean prevProcessDisposeFinally = true;
		if (reconnect != null) {
			prevProcess = (ToolProcess) reconnect.get("process"); //$NON-NLS-1$
		}
		else {
			prevProcess = null;
		}
		
		progress.worked(1);
		if (progress.isCanceled()) {
			return;
		}
		
		try {
			// r env
//			REnvConfiguration renv = REnvTab.getREnv(configuration);
//			renv.validate();
//			
//			progress.worked(1);
//			if (monitor.isCanceled()) {
//				return;
//			}
			
			// arguments
			String address;
			if (reconnect != null && reconnect.containsKey("address")) {
				address = (String) reconnect.get("address"); //$NON-NLS-1$
			}
			else {
				address = configuration.getAttribute(RConsoleLaunching.ATTR_ADDRESS, (String) null);
			}
			if (address == null || address.length() == 0) {
				throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
						RLaunchingMessages.RJLaunchDelegate_error_MissingAddress_message, null));
			}
			if (!(address.startsWith("//") || address.startsWith("rmi:"))) { //$NON-NLS-1$
				address = "//" + address; //$NON-NLS-1$
			}
			// Working directory
			final FileValidator validator = REnvTab.getWorkingDirectoryValidator(configuration, false);
			final IFileStore workingDirectory = (validator.validate(null).getSeverity() != IStatus.ERROR) ?
					validator.getFileStore() : null;
			{	// Replace variable in address
				final Matcher matcher = WD_PATTERN.matcher(address);
				if (matcher.find()) {
					if (workingDirectory == null) {
						throw new CoreException(validator.getStatus());
					}
					address = matcher.replaceAll(workingDirectory.getName());
				}
			}
			
			RMIAddress rmiAddress = null;
			int todo = TODO_START_SERVER;
			Exception todoException = null;
			try {
				rmiAddress = new RMIAddress(address);
				final Remote remote = Naming.lookup(address);
				if (remote instanceof Server) {
					final Server server = (Server) remote;
					switch (server.getState()) {
					case Server.S_NOT_STARTED:
						todo = TODO_START_R;
						if (reconnect != null) {
							throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, 0,
									NLS.bind("Cannot reconnect, the R engine at ''{0}'' is not yet started.", address), null));
						}
						break;
					case Server.S_CONNECTED:
						todo = TODO_CONNECT;
						if (reconnect != null) {
						}
						else {
							final Shell shell = page.getWorkbenchWindow().getShell();
							final Display display = UIAccess.getDisplay(shell);
							final String msg = NLS.bind("It seems, a client is already connected to the remote R engine (''{0}'').\n Do you want to disconnect this client and connect to the engine?", address);
							final AtomicBoolean force = new AtomicBoolean(false);
							display.syncExec(new Runnable() {
								public void run() {
									force.set(MessageDialog.openQuestion(shell, "Connect", msg));
								}
							});
							if (!force.get()) {
								return;
							}
						}
						break;
					case Server.S_DISCONNECTED:
					case Server.S_LOST:
						todo = TODO_CONNECT;
						break;
					case Server.S_STOPPED:
						if (reconnect != null) {
							throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, 0,
									NLS.bind("Cannot reconnect, the R engine at ''{0}'' is terminated.", address), null));
						}
						todo = TODO_START_SERVER;
					default:
						throw new IllegalStateException();
					}
				}
			}
			catch (final UnknownHostException e) {
				throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
						RLaunchingMessages.RJLaunchDelegate_error_InvalidAddress_message, e));
			}
			catch (final MalformedURLException e) {
				throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
						RLaunchingMessages.RJLaunchDelegate_error_InvalidAddress_message, e));
			}
			catch (final RemoteException e) {
				todoException = e;
				todo = TODO_START_SERVER;
			}
			catch (final NotBoundException e) {
				todoException = e;
				todo = TODO_START_SERVER;
			}
			
			progress.worked(5);
			if (progress.isCanceled()) {
				return;
			}
			
			final String[] args = LaunchConfigUtil.getProcessArguments(configuration, RConsoleLaunching.ATTR_OPTIONS);
			final Map<String, Object> loginData = new HashMap<String, Object>();
			loginData.put(LOGIN_USERNAME_DATA_KEY, username);
			if (type.equals(RConsoleLaunching.REMOTE_RJS_SSH)) {
				loginData.put(LOGIN_USERNAME_FORCE_DATA_KEY, true);
			}
			
			final int sshPort = configuration.getAttribute(RConsoleLaunching.ATTR_SSH_PORT, DEFAULT_SSH_PORT);
			loginData.put(LOGIN_SSH_HOST_DATA_KEY, rmiAddress.getHostAddress().getHostAddress());
			loginData.put(LOGIN_SSH_PORT_DATA_KEY, Integer.valueOf(sshPort));
			
			if (reconnect != null) {
				final Map<String, String> reconnectData = (Map<String, String>) reconnect.get("initData"); //$NON-NLS-1$
				if (reconnectData != null) {
					loginData.putAll(reconnectData);
				}
			}
			
			String command = null;
			if (todo == TODO_START_SERVER) {
				progress.subTask(RLaunchingMessages.RJLaunchDelegate_StartR_subtask);
				progress.setWorkRemaining(21);
				if (type.equals(RConsoleLaunching.REMOTE_RJS_SSH)) {
					command = configuration.getAttribute(RConsoleLaunching.ATTR_COMMAND, "");
					if (command.length() == 0) {
						throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
								"Command to startup R over SSH is missing.", null)); //$NON-NLS-1$
					}
					
					final Matcher addressMatcher = ADDRESS_PATTERN.matcher(command);
					if (addressMatcher.find()) {
						command = addressMatcher.replaceAll(rmiAddress.getAddress());
					}
					
					final Matcher nameMatcher = NAME_PATTERN.matcher(command);
					if (nameMatcher.find()) {
						command = nameMatcher.replaceAll(rmiAddress.getName());
					}
					
					final Matcher wdMatcher = WD_PATTERN.matcher(command);
					if (wdMatcher.find()) {
						if (workingDirectory == null) {
							throw new CoreException(validator.getStatus());
						}
						final IPath path = NicoCore.mapFileStoreToRemoteResource(rmiAddress.getHostAddress().getHostAddress(), workingDirectory);
						if (path == null) {
							throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID,
									NLS.bind("Cannot resolve working directory ''{0}'' to remote path.", workingDirectory.toString())));
						}
						command = wdMatcher.replaceAll(path.toString());
					}
					
					final Hashtable<String, String> envp = new Hashtable<String, String>();
					envp.put("LC_ALL", "C"); //$NON-NLS-1$ //$NON-NLS-2$
					envp.put("LANG", "C"); //$NON-NLS-1$ //$NON-NLS-2$
					RjsUtil.startRemoteServerOverSsh(RjsUtil.getSession(loginData, progress.newChild(5)), command, envp, progress.newChild(5));
					
					progress.subTask(RLaunchingMessages.RJLaunchDelegate_WaitForR_subtask);
					WAIT: for (int i = 0; i < 50; i++) {
						if (progress.isCanceled()) {
							throw new CoreException(Status.CANCEL_STATUS);
						}
						try {
							final String[] list = Naming.list(rmiAddress.getRegistryAddress().getAddress());
							for (final String entry : list) {
								try {
									if (new RMIAddress(entry).equals(rmiAddress)) {
										break WAIT;
									}
								}
								catch (final UnknownHostException e) {}
							}
						}
						catch (final RemoteException e) {
							if (i > 25) {
								break WAIT;
							}
						}
						catch (final MalformedURLException e) {
						}
						try {
							Thread.sleep(500);
						}
						catch (final InterruptedException e) {
							Thread.interrupted();
						}
					}
					progress.worked(5);
					
					todo = TODO_START_R;
				}
				else {
					if (reconnect != null) {
						throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, 0,
								NLS.bind("Cannot reconnect to server, no R engine is available at ''{0}''.", address), todoException));
					}
					else {
						throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, 0,
								NLS.bind("Cannot start or reconnect to server, to R engine at ''{0}''. You have to restart the server (manually or using SSH automation).", address), null));
					}
				}
			}
			
			// create process
			UnterminatedLaunchAlerter.registerLaunchType(RLaunchConfigurations.ID_R_REMOTE_CONSOLE_CONFIGURATION_TYPE);
			final boolean startup = (todo == TODO_START_R);
			
			String name = rmiAddress.toString();
			name += ' ' + LaunchConfigUtil.createProcessTimestamp();
			final ToolProcess<RWorkspace> process = new ToolProcess<RWorkspace>(launch, "R", //$NON-NLS-1$
					LaunchConfigUtil.createLaunchPrefix(configuration),
					" (Remote) : R Console/RJ ~ " + name, //$NON-NLS-1$
					rmiAddress.toString()); 
			if (command == null) {
				command = "rjs-connect" + name; //$NON-NLS-1$
			}
			process.setAttribute(IProcess.ATTR_CMDLINE, command + " \n" + Arrays.toString(args)); //$NON-NLS-1$
			
			final HashMap<String, Object> rjsProperties = new HashMap<String, Object>();
			rjsProperties.put("rj.data.lists.structs.max_length",
					configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_LISTS_MAX_LENGTH, 10000));
			rjsProperties.put("rj.data.envs.structs.max_length",
					configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_ENVS_MAX_LENGTH, 10000));
			final RjsController controller = new RjsController(process, rmiAddress, loginData,
					false, startup, args, rjsProperties, null);
			
			// move all tasks, if started
			if (reconnect != null && prevProcess != null) {
				controller.addToolStatusListener(new IToolStatusListener() {
					public void controllerStatusRequested(final ToolStatus currentStatus, final ToolStatus requestedStatus, final List<DebugEvent> eventCollection) {
					}
					public void controllerStatusRequestCanceled(final ToolStatus currentStatus, final ToolStatus requestedStatus, final List<DebugEvent> eventCollection) {
					}
					public void controllerStatusChanged(final ToolStatus oldStatus, final ToolStatus newStatus, final List<DebugEvent> eventCollection) {
						if (newStatus != ToolStatus.TERMINATED) {
							final Queue prevQueue = prevProcess.getQueue();
							prevQueue.moveAllElements(process.getQueue());
						}
						prevProcess.restartCompleted(reconnect);
						controller.removeToolStatusListener(this);
					}
				});
			}
			process.init(controller);
			
			RConsoleLaunching.registerDefaultHandlerTo(controller);
			controller.addEventHandler(IToolEventHandler.LOGIN_REQUEST_EVENT_ID, new LoginHandler());
			
			progress.worked(5);
			
			if (startup) {
				final String startupSnippet = configuration.getAttribute(RConsoleLaunching.ATTR_INIT_SCRIPT_SNIPPET, (String) null);
				if (startupSnippet != null && startupSnippet.length() > 0) {
					controller.submit(RUtil.LINE_SEPARATOR_PATTERN.split(startupSnippet), SubmitType.OTHER);
				}
			}
			controller.setRObjectDB(configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_ENABLED, true));
			controller.getWorkspaceData().setAutoRefresh(configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_AUTOREFRESH_ENABLED, true));
			
			final NIConsole console = new RConsole(process, new NIConsoleColorAdapter());
			NicoUITools.startConsoleLazy(console, page,
					configuration.getAttribute(RConsoleLaunching.ATTR_PIN_CONSOLE, false));
			// start
			new ToolRunner().runInBackgroundThread(process, new WorkbenchStatusHandler());
			prevProcessDisposeFinally = false;
		}
		
		finally {
			if (prevProcessDisposeFinally && reconnect != null && prevProcess != null) {
				prevProcess.restartCompleted(reconnect);
			}
		}
	}
	
}
