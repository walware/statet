/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui.debug;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.icu.text.DateFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.RefreshTab;

import de.walware.statet.base.internal.ui.StatetMessages;
import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * 
 */
public class LaunchConfigUtil {
	
	public static String[] getProcessArguments(final ILaunchConfiguration configuration,
			final String attr) throws CoreException {
		
		final String args = configuration.getAttribute(attr, ""); //$NON-NLS-1$
		final String expanded = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(args);
		return DebugPlugin.parseArguments(expanded);
	}
	
	public static void configureEnvironment(final Map<String, String> env, final ILaunchConfiguration configuration, final Map<String, String> add)
			throws CoreException {
		env.clear();
		env.putAll(createEnvironment(configuration, new Map[] { add }));
	}
	
	/**
	 * Adds environment variables specified in launch configuration to the map.
	 * Explicit specified variables replaces values already configured, but not
	 * appended variables from OS.
	 * 
	 * @param configuration
	 * @param environment
	 * @throws CoreException
	 */
	public static Map<String, String> createEnvironment(final ILaunchConfiguration configuration, final Map<String, String>[] add)
			throws CoreException {
		final Map<String, String> envp = (Platform.getOS().startsWith("win")) ? new WinEnvpMap() : new HashMap<String, String>(); //$NON-NLS-1$
		if (configuration == null || configuration.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true)) {
			envp.putAll(DebugPlugin.getDefault().getLaunchManager().getNativeEnvironmentCasePreserved());
		}
		
		Map<String, String> custom = (configuration != null) ? configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map) null) : null;
		if (add != null) {
			for (int i = 0; i < add.length; i++) {
				if (add[i] != null) {
					envp.putAll(check(envp, add[i]));
				}
				else if (custom != null) {
					envp.putAll(check(envp, custom));
					custom = null;
				}
			}
		}
		if (custom != null) {
			envp.putAll(check(envp, custom));
			custom = null;
		}
		
		return envp;
	}
	
	private static Pattern ENV_PATTERN = Pattern.compile("\\Q${env_var:\\E([^\\}]*)\\}"); //$NON-NLS-1$
	
	private static Map<String, String> check(final Map<String,String> current, final Map<String,String> add) throws CoreException {
		final String[] keys = add.keySet().toArray(new String[add.keySet().size()]);
		for (int i = 0; i < keys.length; i++) {
			final String orgValue = add.get(keys[i]);
			if (orgValue != null && orgValue.length() > 0) {
				String value = orgValue;
				if (value.contains("${env_var:")) { //$NON-NLS-1$
					final StringBuffer sb = new StringBuffer(value.length()+32);
					final Matcher matcher = ENV_PATTERN.matcher(value);
					while (matcher.find()) {
						final String var = matcher.group(1);
						final String varValue = current.get(var);
						matcher.appendReplacement(sb, (varValue != null) ? Matcher.quoteReplacement(varValue) : ""); //$NON-NLS-1$
					}
					matcher.appendTail(sb);
					value = sb.toString();
				}
				if (value.contains("${")) { //$NON-NLS-1$
					value = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(value, true);
				}
				if (value != orgValue) {
					add.put(keys[i], value);
				}
			}
		}
		return add;
	}
	
	
	private static final Pattern DOUBLE_QUOTE_PATTERN = Pattern.compile(Pattern.quote("\""));  //$NON-NLS-1$
	private static final String DOUBLE_QUOTE_REPLACEMENT = Matcher.quoteReplacement("\\\"");  //$NON-NLS-1$
	
	/**
	 * Creates UI presentation of command line (command string for shell).
	 */
	public static String generateCommandLine(final List<String> commandLine) {
		final StringBuilder builder = new StringBuilder();
		for (final String arg : commandLine) {
			DOUBLE_QUOTE_PATTERN.matcher(arg).replaceAll(DOUBLE_QUOTE_REPLACEMENT);
			if (arg.indexOf(' ') >= 0) {
				builder.append('\"');
				builder.append(arg);
				builder.append('\"');
			} else {
				builder.append(arg);
			}
			builder.append(' ');
		}
		if (builder.length() > 0) {
			return builder.substring(0, builder.length()-1);
		}
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * Refreshes resources as specified by a launch configuration, when
	 * an associated process terminates.
	 */
	private static class BackgroundResourceRefresher implements IDebugEventSetListener  {
		
		
		private ILaunchConfiguration fConfiguration;
		private IProcess fProcess;
		
		
		public BackgroundResourceRefresher(final ILaunchConfiguration configuration, final IProcess process) {
			fConfiguration = configuration;
			fProcess = process;
			
			initialize();
		}
		
		/**
		 * If the process has already terminated, resource refreshing is scheduled
		 * immediately. Otherwise, refreshing is done when the process terminates.
		 */
		private synchronized void initialize() {
			DebugPlugin.getDefault().addDebugEventListener(this);
			if (fProcess.isTerminated()) {
				sheduleRefresh();
			}
		}
		
		public void handleDebugEvents(final DebugEvent[] events) {
			for (int i = 0; i < events.length; i++) {
				final DebugEvent event = events[i];
				if (event.getSource() == fProcess && event.getKind() == DebugEvent.TERMINATE) {
					sheduleRefresh();
					return;
				}
			}
		}
		
		/**
		 * Submits a job to do the refresh
		 */
		protected synchronized void sheduleRefresh() {
			if (fProcess != null) {
				DebugPlugin.getDefault().removeDebugEventListener(this);
				fProcess = null;
				final Job job = new Job(StatetMessages.BackgroundResourceRefresher_Job_name) {
					@Override
					public IStatus run(final IProgressMonitor monitor) {
						try {
							RefreshTab.refreshResources(fConfiguration, monitor);
						} catch (final CoreException e) {
							StatetUIPlugin.logUnexpectedError(e);
							return e.getStatus();
						}
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}
		}
	}
	
	
	public static IProgressMonitor initProgressMonitor(final ILaunchConfiguration configuration,
			IProgressMonitor monitor, final int taskTotalWork) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (CommonTab.isLaunchInBackground(configuration)) {
			monitor.beginTask(StatetMessages.LaunchDelegate_LaunchingTask_label, taskTotalWork);
		}
		else {
			monitor.beginTask(StatetMessages.LaunchDelegate_RunningTask_label, taskTotalWork*2);
			monitor.subTask(StatetMessages.LaunchDelegate_LaunchingTask_label);
		}
		return monitor;
	}
	
	public static String createProcessTimestamp() {
		return "("+DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()))+")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static String createLaunchPrefix(final ILaunchConfiguration config) {
		final StringBuilder s = new StringBuilder();
		if (config != null) {
			String type = null;
			try {
				type = config.getType().getName();
			} catch (final CoreException e) {}
			s.append(config.getName());
			if (type != null) {
				s.append(" ["); //$NON-NLS-1$
				s.append(type);
				s.append("]"); //$NON-NLS-1$
			}
		}
		else {
			s.append("[-]"); //$NON-NLS-1$
		}
		return s.toString();
	}
	
	/**
	 * Manages resource refresh according to the settings in launch configuration.
	 */
	public static void launchResourceRefresh(final ILaunchConfiguration configuration,
			final IProcess process, final IProgressMonitor monitor) throws CoreException {
		if (CommonTab.isLaunchInBackground(configuration)) {
			// refresh resources after process finishes
			if (RefreshTab.getRefreshScope(configuration) != null) {
				new BackgroundResourceRefresher(configuration, process);
			}
		} else {
			// wait for process to exit
			while (!process.isTerminated()) {
				try {
					if (monitor.isCanceled()) {
						process.terminate();
						break;
					}
					Thread.sleep(50);
				} catch (final InterruptedException e) {
					Thread.interrupted();
				}
			}
			
			// refresh resources
			RefreshTab.refreshResources(configuration, monitor);
		}
	}
	
	public static IProject[] getProjectList(final IResource resource) {
		try {
			final IProject project = resource.getProject();
			if (project != null) {
				final IProject[] referencedProjects = project.getReferencedProjects();
				final IProject[] allProjects = new IProject[referencedProjects.length+1];
				allProjects[0] = project;
				System.arraycopy(referencedProjects, 0, allProjects, 1, referencedProjects.length);
				return allProjects;
			}
		}
		catch (final CoreException e) {
		}
		return new IProject[0];
	}
	
	
	private LaunchConfigUtil() {}
	
}
