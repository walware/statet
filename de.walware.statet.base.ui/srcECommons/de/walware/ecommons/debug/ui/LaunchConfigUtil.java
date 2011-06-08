/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.debug.ui;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.DateFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
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
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.debug.internal.ui.Messages;
import de.walware.ecommons.debug.internal.ui.WinEnvpMap;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * Methods for common task when working with launch configurations and processes
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
		final Map<String, String> resolved = new HashMap<String, String>();
		final Set<Entry<String, String>> entries = add.entrySet();
		for (final Entry<String, String> entry : entries) {
			String value = entry.getValue();
			if (value != null && value.length() > 0) {
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
			}
			resolved.put(entry.getKey(), value);
		}
		return resolved;
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
		
		
		private final ILaunchConfiguration fConfiguration;
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
				final Job job = new Job(Messages.BackgroundResourceRefresher_Job_name) {
					@Override
					public IStatus run(final IProgressMonitor monitor) {
						try {
							RefreshTab.refreshResources(fConfiguration, monitor);
						}
						catch (final CoreException e) {
							StatusManager.getManager().handle(new Status(
									ICommonStatusConstants.LAUNCHING, StatetUIPlugin.PLUGIN_ID,
									NLS.bind("An error occurred when refreshing resources for launch configuration ''{0}''.", fConfiguration.getName()), e));
							return e.getStatus();
						}
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}
		}
	}
	
	
	public static SubMonitor initProgressMonitor(final ILaunchConfiguration configuration,
			IProgressMonitor monitor, final int taskTotalWork) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		final SubMonitor progress = SubMonitor.convert(monitor, Messages.LaunchDelegate_LaunchingTask_label, taskTotalWork);
		progress.subTask(Messages.LaunchDelegate_Init_subtask);
		return progress;
	}
	
	public static String createProcessTimestamp(final long time) {
		return "("+DateFormat.getDateTimeInstance().format(new Date(time))+")"; //$NON-NLS-1$ //$NON-NLS-2$
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
				}
				catch (final InterruptedException e) {
					// continue loop, monitor and process is checked
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
	
	public static String[] toKeyValueStrings(final Map<String, String> map) {
		final String[] array = new String[map.size()];
		final Iterator<Map.Entry<String, String>> iter = map.entrySet().iterator();
		for (int i = 0; i < array.length; i++) {
			final Entry<String, String> entry = iter.next();
			array[i] = entry.getKey()+'='+entry.getValue();
		}
		return array;
	}
	
	
	public static class LaunchConfigurationComparator implements Comparator<ILaunchConfiguration> {
		
		private final Collator fCollator = Collator.getInstance();
		
		public int compare(final ILaunchConfiguration c1, final ILaunchConfiguration c2) {
			return fCollator.compare(c1.getName(), c2.getName());
		}
		
	};
	
	
	private LaunchConfigUtil() {}
	
}
