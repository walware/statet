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

package de.walware.statet.r.internal.debug.ui.launchconfigs;

import static de.walware.statet.r.internal.debug.ui.RDebugPreferenceConstants.PREF_LOCAL_REGISTRY_PORT;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
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

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.debug.ui.LaunchConfigUtil;
import de.walware.ecommons.debug.ui.UnterminatedLaunchAlerter;
import de.walware.ecommons.net.RMIAddress;
import de.walware.ecommons.net.RMIRegistry;
import de.walware.ecommons.net.RMIUtil;
import de.walware.ecommons.net.RMIUtil.StopRule;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.IToolRunnable;
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolRunner;
import de.walware.statet.nico.core.util.HistoryTrackingConfiguration;
import de.walware.statet.nico.core.util.TrackingConfiguration;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.console.NIConsoleColorAdapter;
import de.walware.statet.nico.ui.util.WorkbenchStatusHandler;

import de.walware.rj.data.RDataUtil;
import de.walware.rj.data.RObject;
import de.walware.rj.data.UnexpectedRDataException;
import de.walware.rj.server.RjsComConfig;

import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.debug.ui.launchconfigs.REnvTab;
import de.walware.statet.r.debug.ui.launchconfigs.RLaunchConfigurations;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;
import de.walware.statet.r.internal.nico.ui.REnvIndexAutoUpdater;
import de.walware.statet.r.launching.RConsoleLaunching;
import de.walware.statet.r.nico.IRDataAdapter;
import de.walware.statet.r.nico.RProcess;
import de.walware.statet.r.nico.impl.RjsController;
import de.walware.statet.r.nico.ui.RConsole;
import de.walware.statet.r.ui.RUI;


/**
 * Launch delegate for RJ based R console using embedded RJ server
 */
public class RConsoleRJLaunchDelegate extends LaunchConfigurationDelegate {
	
	
	static class ConfigRunnable implements IToolRunnable {
		
		
		private final boolean fEnableHelp;
		
		
		public ConfigRunnable(final boolean enableHelp) {
			fEnableHelp = enableHelp;
		}
		
		
		public String getTypeId() {
			return "r/integration"; //$NON-NLS-1$
		}
		
		public SubmitType getSubmitType() {
			return SubmitType.OTHER;
		}
		
		public String getLabel() {
			return "Initialize R-StatET Tools";
		}
		
		public void changed(final int event, final ToolProcess process) {
		}
		
		public void run(final IToolRunnableControllerAdapter adapter, final IProgressMonitor monitor) throws InterruptedException, CoreException {
			final IRDataAdapter r = (IRDataAdapter) adapter;
			try {
				if (!RDataUtil.checkSingleLogiValue(r.evalData("\"rj\" %in% installed.packages()[,\"Package\"]", monitor))) { //$NON-NLS-1$
					r.handleStatus(new Status(IStatus.INFO, RUI.PLUGIN_ID, "The R package 'rj' is not available, R-StatET tools cannot be initialized."), monitor);
					return;
				}
				final RObject rjPackageLoaded = r.evalData("require(\"rj\", quietly = TRUE)", monitor); //$NON-NLS-1$
				if (RDataUtil.checkSingleLogiValue(rjPackageLoaded)) {
					if (fEnableHelp) {
						r.evalVoid(".statet.reassign_help()", monitor); //$NON-NLS-1$
					}
				}
				else {
					r.handleStatus(new Status(IStatus.INFO, RUI.PLUGIN_ID, "The R package 'rj' could not be loaded, R-StatET tools cannot be initialized."), monitor);
				}
			}
			catch (final UnexpectedRDataException e) {
			}
		}
		
	}
	
	static void initConsoleOptions(final RjsController controller, final ILaunchConfiguration configuration,
			final boolean isStartup) throws CoreException {
		new REnvIndexAutoUpdater(controller.getProcess());
		
		if (configuration.getAttribute(RConsoleOptionsTab.ATTR_INTEGRATION_RPACKAGES_LOAD_ENABLED, true)) {
			controller.addStartupRunnable(new ConfigRunnable(
					configuration.getAttribute(RConsoleOptionsTab.ATTR_INTEGRATION_RHELP_ENABLED, true) ));
		}
		if (isStartup) {
			RConsoleLaunching.scheduleStartupSnippet(controller, configuration);
		}
		controller.setRObjectDB(configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_ENABLED, true));
		controller.getWorkspaceData().setAutoRefresh(configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_AUTOREFRESH_ENABLED, true));
	}
	
	
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
		final IREnvConfiguration renv = REnvTab.getREnvConfig(configuration, true);
		
		final Integer port = PreferencesUtil.getInstancePrefs().getPreferenceValue(PREF_LOCAL_REGISTRY_PORT);
		final RMIAddress rmiAddress;
		try {
			rmiAddress = new RMIAddress(RMIAddress.LOOPBACK, port,
					"rjs-local-"+System.currentTimeMillis()); //$NON-NLS-1$
		}
		catch (final MalformedURLException e) {
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					RLaunchingMessages.RJLaunchDelegate_error_InvalidAddress_message, e));
		}
		final RJEngineLaunchDelegate engineLaunchDelegate = new RJEngineLaunchDelegate(rmiAddress.getAddress(), renv);
		
		progress.worked(1);
		if (progress.isCanceled()) {
			return;
		}
		
		// start server
		progress.subTask(RLaunchingMessages.RJLaunchDelegate_StartR_subtask);
		// RMI registry
		final IStatus registryStatus = RMIUtil.INSTANCE.startSeparateRegistry(port, StopRule.IF_EMPTY);
		if (registryStatus.getSeverity() >= IStatus.ERROR) {
			throw new CoreException(registryStatus);
		}
		final RMIRegistry registry = RMIUtil.INSTANCE.getRegistry(port);
		
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
		UnterminatedLaunchAlerter.registerLaunchType(RLaunchConfigurations.ID_R_CONSOLE_CONFIGURATION_TYPE);
		
		final RProcess process = new RProcess(launch, renv,
				LaunchConfigUtil.createLaunchPrefix(configuration),
				renv.getName() + " / RJ " + LaunchConfigUtil.createProcessTimestamp(timestamp), //$NON-NLS-1$
				rmiAddress.toString(), null, timestamp); // wd is set at rjs startup
		process.setAttribute(IProcess.ATTR_CMDLINE, rmiAddress.toString() + '\n' + Arrays.toString(rArgs));
		
		// Wait until the engine is started or died
		progress.subTask(RLaunchingMessages.RJLaunchDelegate_WaitForR_subtask);
		WAIT: for (int i = 0; i < 50; i++) {
			if (processes[0].isTerminated()) {
				final boolean silent = configuration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true);
				StatusManager.getManager().handle(new Status(silent ? IStatus.INFO : IStatus.ERROR, RUI.PLUGIN_ID,
						"Launching the R Console was cancelled, because it seems starting the Java " +
						"process/R engine failed. \n"+
						"Please make sure that R package 'rJava' with JRI is installed and that the " +
						"R library paths are set correctly in the R environment configuration."),
						silent ? (StatusManager.LOG) :(StatusManager.LOG | StatusManager.SHOW));
				return;
			}
			if (progress.isCanceled()) {
				processes[0].terminate();
				throw new CoreException(Status.CANCEL_STATUS);
			}
			try {
				final String[] list = registry.getRegistry().list();
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
		
		final HashMap<String, Object> rjsProperties = new HashMap<String, Object>();
		rjsProperties.put(RjsComConfig.RJ_DATA_STRUCTS_LISTS_MAX_LENGTH_PROPERTY_ID,
				configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_LISTS_MAX_LENGTH, 10000));
		rjsProperties.put(RjsComConfig.RJ_DATA_STRUCTS_ENVS_MAX_LENGTH_PROPERTY_ID,
				configuration.getAttribute(RConsoleLaunching.ATTR_OBJECTDB_ENVS_MAX_LENGTH, 10000));
		rjsProperties.put("rj.session.startup.time", timestamp); //$NON-NLS-1$
		final RjsController controller = new RjsController(process, rmiAddress, null,
				true, true, rArgs, rjsProperties,
				engineLaunchDelegate.getWorkingDirectory(), trackingConfigs);
		process.init(controller);
		RConsoleLaunching.registerDefaultHandlerTo(controller);
		
		progress.worked(5);
		
		initConsoleOptions(controller, configuration, true);
		
		final RConsole console = new RConsole(process, new NIConsoleColorAdapter());
		NicoUITools.startConsoleLazy(console, page, 
				configuration.getAttribute(RConsoleLaunching.ATTR_PIN_CONSOLE, false));
		
		new ToolRunner().runInBackgroundThread(process, new WorkbenchStatusHandler());
		
		if (monitor != null) {
			monitor.done();
		}
	}
	
}
