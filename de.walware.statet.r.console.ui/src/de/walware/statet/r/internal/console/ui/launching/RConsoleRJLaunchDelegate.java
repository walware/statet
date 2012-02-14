/*******************************************************************************
 * Copyright (c) 2008-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui.launching;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.Version;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.debug.ui.LaunchConfigUtil;
import de.walware.ecommons.debug.ui.UnterminatedLaunchAlerter;
import de.walware.ecommons.net.RMIAddress;
import de.walware.ecommons.net.RMIRegistry;
import de.walware.ecommons.net.RMIUtil;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolService;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ILogOutput;
import de.walware.statet.nico.core.runtime.ToolRunner;
import de.walware.statet.nico.core.util.HistoryTrackingConfiguration;
import de.walware.statet.nico.core.util.TrackingConfiguration;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.console.NIConsoleColorAdapter;
import de.walware.statet.nico.ui.util.WorkbenchStatusHandler;

import de.walware.rj.server.RjsComConfig;
import de.walware.rj.server.srvext.ServerUtil;

import de.walware.statet.r.console.core.IRDataAdapter;
import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.console.ui.RConsole;
import de.walware.statet.r.console.ui.launching.RConsoleLaunching;
import de.walware.statet.r.console.ui.tools.REnvIndexAutoUpdater;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.internal.console.ui.RConsoleMessages;
import de.walware.statet.r.internal.console.ui.RConsoleUIPlugin;
import de.walware.statet.r.launching.core.ILaunchDelegateAddon;
import de.walware.statet.r.launching.core.RLaunching;
import de.walware.statet.r.nico.RWorkspaceConfig;
import de.walware.statet.r.nico.impl.RjsController;
import de.walware.statet.r.nico.impl.RjsController.RjsConnection;


/**
 * Launch delegate for RJ based R console using embedded RJ server
 */
public class RConsoleRJLaunchDelegate extends LaunchConfigurationDelegate {
	
	
	static final long TIMEOUT = 60L * 1000000000L;
	
	static final Version VERSION_2_12_0 = new Version(2, 12, 0);
	
	static class ConfigRunnable implements ISystemRunnable {
		
		
		private final ITool fTool;
		private final boolean fEnableRHelp;
		private final boolean fEnableRGraphics;
		private final boolean fEnableRDbgExt;
		
		public ConfigRunnable(final ITool tool, final boolean enableRHelp,
				final boolean enableRGraphics, final boolean enableRDbgExt) {
			fTool = tool;
			fEnableRHelp = enableRHelp;
			fEnableRGraphics = enableRGraphics;
			fEnableRDbgExt = enableRDbgExt;
		}
		
		
		@Override
		public String getTypeId() {
			return "r/integration"; //$NON-NLS-1$
		}
		
		@Override
		public String getLabel() {
			return "Initialize R-StatET Tools";
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return (tool == fTool);
		}
		
		@Override
		public boolean changed(final int event, final ITool process) {
			if ((event & MASK_EVENT_GROUP) == REMOVING_EVENT_GROUP) {
				return false;
			}
			return true;
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			final IRDataAdapter r = (IRDataAdapter) service;
			final Version rVersion = r.getPlatform().getRVersion();
			if (rVersion.compareTo(VERSION_2_12_0) < 0) {
				r.evalVoid("library('rj')", monitor); //$NON-NLS-1$
			}
			else {
				r.evalVoid("library('rj', quietly= TRUE)", monitor); //$NON-NLS-1$
			}
			if (fEnableRHelp) {
				r.evalVoid(".statet.reassignHelp()", monitor); //$NON-NLS-1$
			}
			if (fEnableRGraphics) {
				try {
					r.evalVoid("rj.gd:::.rj.getGDVersion()", monitor); //$NON-NLS-1$
					r.evalVoid("options(device=rj.gd::rj.GD)", monitor); //$NON-NLS-1$
				}
				catch (final CoreException e) {
					r.handleStatus(new Status(IStatus.INFO, RConsoleUIPlugin.PLUGIN_ID,
							"The graphic device for the R Graphic view cannot be initialized. " +
							"Is the R package 'rj.gd' installed?", e),
							monitor );
				}
			}
			if (fEnableRDbgExt) {
				r.evalVoid(".statet.initDebug()", monitor); //$NON-NLS-1$
			}
		}
		
	}
	
	static RWorkspaceConfig createWorkspaceConfig(final ILaunchConfiguration configuration) throws CoreException {
		final RWorkspaceConfig config = new RWorkspaceConfig();
		config.setEnableObjectDB(configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_ENABLED, true));
		config.setEnableAutoRefresh(configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_AUTOREFRESH_ENABLED, true));
		return config;
	}
	
	static void initConsoleOptions(final RjsController controller, final ILaunchConfiguration configuration,
			final boolean isStartup) throws CoreException {
		new REnvIndexAutoUpdater(controller.getTool());
		
		controller.addStartupRunnable(new ConfigRunnable(
				controller.getTool(),
				configuration.getAttribute(RConsoleOptionsTab.ATTR_INTEGRATION_RHELP_ENABLED, true),
				configuration.getAttribute(RConsoleOptionsTab.ATTR_INTEGRATION_RGRAPHICS_ASDEFAULT, true),
				configuration.getAttribute(RConsoleOptionsTab.ATTR_INTEGRATION_RDBGEXT_ENABLED, true) ));
		if (isStartup) {
			RConsoleLaunching.scheduleStartupSnippet(controller, configuration);
		}
	}
	
	
	private ILaunchDelegateAddon fAddon;
	
	
	public RConsoleRJLaunchDelegate() {
	}
	
	public RConsoleRJLaunchDelegate(final ILaunchDelegateAddon addon) {
		fAddon = addon;
	}
	
	
	@Override
	public void launch(final ILaunchConfiguration configuration, final String mode, 
			final ILaunch launch, final IProgressMonitor monitor) throws CoreException {
		final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
		final SubMonitor progress = LaunchConfigUtil.initProgressMonitor(configuration, monitor, 25);
		
		final long timestamp = System.currentTimeMillis();
		
		progress.worked(1);
		if (progress.isCanceled()) {
			return;
		}
		
		// load tracking configurations
		final List<TrackingConfiguration> trackingConfigs;
		{	final List<String> trackingIds = configuration.getAttribute(RConsoleOptionsTab.TRACKING_ENABLED_IDS, Collections.EMPTY_LIST);
			trackingConfigs = new ArrayList<TrackingConfiguration>(trackingIds.size());
			for (final String id : trackingIds) {
				final TrackingConfiguration trackingConfig;
				if (id.equals(HistoryTrackingConfiguration.HISTORY_TRACKING_ID)) {
					trackingConfig = new HistoryTrackingConfiguration(id);
				}
				else {
					trackingConfig = new TrackingConfiguration(id);
				}
				RConsoleOptionsTab.TRACKING_UTIL.load(trackingConfig, configuration);
				trackingConfigs.add(trackingConfig);
			}
		}
		
		progress.worked(1);
		if (progress.isCanceled()) {
			return;
		}
		
		// r env
		final IREnvConfiguration rEnv = RLaunching.getREnvConfig(configuration, true);
		
		final RMIRegistry registry;
		boolean requireCodebase;
		{	final String s = System.getProperty("de.walware.statet.r.console.rmiRegistryPort");
			int port = -1;
			if (s != null && s.length() > 0) {
				try {
					port = Integer.parseInt(s);
					final RMIAddress registryAddress = new RMIAddress(RMIAddress.LOOPBACK, port, null);
					registry = new RMIRegistry(registryAddress, null, true);
					requireCodebase = true;
				}
				catch (final NumberFormatException e) {
					throw new CoreException(new Status(IStatus.ERROR, RConsoleUIPlugin.PLUGIN_ID,
							ICommonStatusConstants.LAUNCHCONFIG_ERROR,
							"The registry port specified by 'de.walware.statet.r.console.rmiRegistryPort' is invalid.", e ));
				}
				catch (final MalformedURLException e) {
					throw new CoreException(new Status(IStatus.ERROR, RConsoleUIPlugin.PLUGIN_ID,
							ICommonStatusConstants.LAUNCHCONFIG_ERROR,
							"The registry port specified by 'de.walware.statet.r.console.rmiRegistryPort' is invalid.", e ));
				}
				catch (final RemoteException e) {
					throw new CoreException(new Status(IStatus.ERROR, RConsoleUIPlugin.PLUGIN_ID,
							ICommonStatusConstants.LAUNCHCONFIG_ERROR,
							"Connection setup to the registry specified by 'de.walware.statet.r.console.rmiRegistryPort' failed.", e ));
				}
			}
			else {
				registry = RMIUtil.INSTANCE.getEmbeddedPrivateRegistry(progress.newChild(1));
				requireCodebase = false;
			}
		}
		final RMIAddress rmiAddress;
		try {
			rmiAddress = new RMIAddress(RMIAddress.LOOPBACK, registry.getAddress().getPortNum(),
					"rjs-local-"+System.currentTimeMillis()); //$NON-NLS-1$
		}
		catch (final MalformedURLException e) {
			throw new CoreException(new Status(IStatus.ERROR, RConsoleUIPlugin.PLUGIN_ID,
					ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					RConsoleMessages.LaunchDelegate_error_InvalidAddress_message, e));
		}
		final RJEngineLaunchDelegate engineLaunchDelegate = new RJEngineLaunchDelegate(
				rmiAddress.getAddress(), requireCodebase, rEnv);
		
		progress.worked(1);
		if (progress.isCanceled()) {
			return;
		}
		
		// start server
		progress.subTask(RConsoleMessages.LaunchDelegate_StartREngine_subtask);
		try {
			RjsComConfig.setRMIClientSocketFactory(null);
			
			engineLaunchDelegate.launch(configuration, mode, launch, progress.newChild(10));
			final IProcess[] processes = launch.getProcesses();
			if (processes.length == 0) {
				return;
			}
			
			progress.worked(1);
			if (progress.isCanceled()) {
				return;
			}
			
			// arguments
			final String[] rArgs = LaunchConfigUtil.getProcessArguments(configuration, RConsoleLaunching.ATTR_OPTIONS);
			
			progress.worked(1);
			if (progress.isCanceled()) {
				return;
			}
			
			// create process
			UnterminatedLaunchAlerter.registerLaunchType(RConsoleLaunching.R_CONSOLE_CONFIGURATION_TYPE_ID);
			
			final RProcess process = new RProcess(launch, rEnv,
					LaunchConfigUtil.createLaunchPrefix(configuration),
					rEnv.getName() + " / RJ " + LaunchConfigUtil.createProcessTimestamp(timestamp), //$NON-NLS-1$
					rmiAddress.toString(),
					null, // wd is set at rjs startup
					timestamp );
			process.setAttribute(IProcess.ATTR_CMDLINE, rmiAddress.toString() + '\n' + Arrays.toString(rArgs));
			
			// Wait until the engine is started or died
			progress.subTask(RConsoleMessages.LaunchDelegate_WaitForR_subtask);
			final long t = System.nanoTime();
			WAIT: for (int i = 0; true; i++) {
				if (processes[0].isTerminated()) {
					final boolean silent = configuration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true);
					final IStatus logStatus = ToolRunner.createOutputLogStatus(
							(ILogOutput) processes[0].getAdapter(ILogOutput.class) );
					// move to R server?
					final StringBuilder sb = new StringBuilder();
					sb.append("Launching the R Console was cancelled, because it seems starting the R engine failed. \n");
					sb.append("Please make sure that R package ");
					if (ServerUtil.RJ_VERSION[0] > 0 || ServerUtil.RJ_VERSION[1] > 5 || ServerUtil.RJ_VERSION[2] >= 5) {
						sb.append("'rj' ("); //$NON-NLS-1$;
						ServerUtil.prettyPrintVersion(ServerUtil.RJ_VERSION, sb);
						sb.append(" or compatible)"); //$NON-NLS-1$
					}
					else {
						sb.append("'rJava' (with JRI)"); //$NON-NLS-1$
					}
					sb.append(" is installed and that the R library paths are set correctly for the R environment configuration '");
					sb.append(rEnv.getName());
					sb.append("'.");
					
					StatusManager.getManager().handle(new Status(silent ? IStatus.INFO : IStatus.ERROR,
							RConsoleUIPlugin.PLUGIN_ID, sb.toString(),
							(logStatus != null) ? new CoreException(logStatus) : null ),
							silent ? (StatusManager.LOG) : (StatusManager.LOG | StatusManager.SHOW) );
					return;
				}
				if (progress.isCanceled()) {
					processes[0].terminate();
					throw new CoreException(Status.CANCEL_STATUS);
				}
				try {
					final String[] list = registry.getRegistry().list();
					for (final String entry : list) {
						if (entry.equals(rmiAddress.getName())) {
							break WAIT;
						}
					}
					if (i > 1 && System.nanoTime() - t > TIMEOUT) {
						break WAIT;
					}
				}
				catch (final RemoteException e) {
					if (i > 0 && System.nanoTime() - t > TIMEOUT / 3) {
						break WAIT;
					}
				}
				try {
					Thread.sleep(333);
				}
				catch (final InterruptedException e) {
					// continue, monitor and process is checked
				}
			}
			progress.worked(5);
			
			final RjsConnection connection = RjsController.lookup(registry.getRegistry(), null, rmiAddress);
			
			final HashMap<String, Object> rjsProperties = new HashMap<String, Object>();
			rjsProperties.put(RjsComConfig.RJ_DATA_STRUCTS_LISTS_MAX_LENGTH_PROPERTY_ID,
					configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_LISTS_MAX_LENGTH, 10000));
			rjsProperties.put(RjsComConfig.RJ_DATA_STRUCTS_ENVS_MAX_LENGTH_PROPERTY_ID,
					configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_ENVS_MAX_LENGTH, 10000));
			rjsProperties.put("rj.session.startup.time", timestamp); //$NON-NLS-1$
			final RjsController controller = new RjsController(process, rmiAddress, connection, null,
					true, true, rArgs, rjsProperties, engineLaunchDelegate.getWorkingDirectory(),
					createWorkspaceConfig(configuration), trackingConfigs);
			process.init(controller);
			RConsoleLaunching.registerDefaultHandlerTo(controller);
			
			progress.worked(5);
			
			initConsoleOptions(controller, configuration, true);
			
			if (fAddon != null) {
				fAddon.init(configuration, mode, controller, monitor);
			}
			
			final RConsole console = new RConsole(process, new NIConsoleColorAdapter());
			NicoUITools.startConsoleLazy(console, page, 
					configuration.getAttribute(RConsoleLaunching.ATTR_PIN_CONSOLE, false));
			
			new ToolRunner().runInBackgroundThread(process, new WorkbenchStatusHandler());
		}
		finally {
			RjsComConfig.clearRMIClientSocketFactory();
		}
		
		if (monitor != null) {
			monitor.done();
		}
	}
	
}
