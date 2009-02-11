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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.Bundle;

import de.walware.ecommons.debug.ui.LaunchConfigUtil;

import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.debug.ui.launchconfigs.REnvTab;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RUI;


/**
 * Launches RJ Server using JDT java launch mechanism
 */
public class RJEngineLaunchDelegate extends JavaLaunchDelegate {
	
	
	public static void addPluginClasspath(final Set<String> classpath, final boolean desktop, final boolean is64) {
		final List<Bundle> bundles = new ArrayList<Bundle>();
		final Bundle rjBundle = Platform.getBundle("de.walware.rj"); //$NON-NLS-1$
		bundles.add(rjBundle);
		Bundle[] fragments = Platform.getFragments(rjBundle);
		if (fragments != null) {
			bundles.addAll(Arrays.asList(fragments));
		}
		final Bundle swtBundle = Platform.getBundle("org.eclipse.swt"); //$NON-NLS-1$
		bundles.add(swtBundle);
		fragments = Platform.getFragments(swtBundle);
		if (fragments != null && fragments.length > 0) {
			bundles.addAll(Arrays.asList(fragments));
		}
		
		final URL platform = Platform.getInstallLocation().getURL();
		for (final Bundle bundle : bundles) {
			String location = bundle.getLocation();
			if (location.startsWith("reference:file:")) { //$NON-NLS-1$
				location = location.substring(15);
				IPath path = new Path(location);
				if (!path.isAbsolute()) {
					path = new Path(platform.getFile()).append(path);
				}
				String checked = path.lastSegment();
				if (checked.contains("motif")) { //$NON-NLS-1$
					checked = checked.replaceAll("motif", "gtk"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				if (checked.contains("gtk")) { //$NON-NLS-1$
					if (is64 && !checked.contains("64")) { //$NON-NLS-1$
						checked = checked.replaceAll("x86", "x86_64"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					if (!is64 && checked.contains("64")) { //$NON-NLS-1$
						checked = checked.replaceAll("x86_64", "x86"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				final String s = path.removeLastSegments(1).append(checked).makeAbsolute().toOSString();
				if (location.endsWith("/")) { // //$NON-NLS-1$
					if (Platform.inDevelopmentMode()) {
						classpath.add(s+File.separatorChar+"bin"+File.separatorChar); //$NON-NLS-1$
					}
					classpath.add(s+File.separatorChar);
				}
				else {
					classpath.add(s);
				}
			}
			else {
				StatusManager.getManager().handle(new Status(IStatus.WARNING, RUI.PLUGIN_ID, 
						"Unknown type for plug-in location: '"+location+"'. May cause fail to startup RJ (RMI/JRI)")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	
	
	private String fAddress;
	private REnvConfiguration fRenv;
	
	
	public RJEngineLaunchDelegate(final String address, final REnvConfiguration renv) throws CoreException {
		fAddress = address;
		fRenv = renv;
	}
	
	
	@Override
	public IPath getWorkingDirectoryPath(final ILaunchConfiguration configuration) throws CoreException {
		final IFileStore workingDirectory = REnvTab.getWorkingDirectory(configuration);
		return URIUtil.toPath(workingDirectory.toURI());
	}
	
	@Override
	public String[] getEnvironment(final ILaunchConfiguration configuration) throws CoreException {
		final Map<String, String> envp = LaunchConfigUtil.createEnvironment(configuration, 
				new Map[] { fRenv.getEnvironmentsVariables() });
		return LaunchConfigUtil.toKeyValueStrings(envp);
	}
	
	@Override
	public String[] getClasspath(final ILaunchConfiguration configuration) throws CoreException {
		final LinkedHashSet<String> classpath = new LinkedHashSet<String>();
		addPluginClasspath(classpath, true, (fRenv.getRBits() == 64)); 
		classpath.addAll(Arrays.asList(super.getClasspath(configuration)));
		
		return classpath.toArray(new String[classpath.size()]);
	}
	
	@Override
	public String getVMArguments(final ILaunchConfiguration configuration) throws CoreException {
		final String args = super.getVMArguments(configuration);
		final StringBuilder s = new StringBuilder(" "); //$NON-NLS-1$
		if (args != null) {
			s.append(args);
		}
		if (s.indexOf(" -Djava.security.policy=") < 0) { //$NON-NLS-1$
			try {
				final URL intern = Platform.getBundle("de.walware.rj").getEntry("/localhost.policy"); //$NON-NLS-1$ //$NON-NLS-2$
				final URL java = FileLocator.resolve(intern);
				s.append(" -Djava.security.policy=\""); //$NON-NLS-1$
				s.append(java.toString());
				s.append("\""); //$NON-NLS-1$
			}
			catch (final IOException e) {
				RUIPlugin.logError(-1, "Error trying to resolve path to security policy", e); //$NON-NLS-1$
			}
		}
		if (s.indexOf(" -Djava.rmi.server.hostname=") < 0) { //$NON-NLS-1$
			s.append(" -Djava.rmi.server.hostname=localhost"); //$NON-NLS-1$
		}
		if (s.indexOf(" -Xss") < 0) { //$NON-NLS-1$
			s.append(" -Xss").append(fRenv.getRBits()*256).append("k"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return s.substring(1);
	}
	
	@Override
	public String getMainTypeName(final ILaunchConfiguration configuration) throws CoreException {
		return "de.walware.rj.server.RMIServerControl"; //$NON-NLS-1$
	}
	
	@Override
	public String getProgramArguments(final ILaunchConfiguration configuration) throws CoreException {
		final StringBuilder args = new StringBuilder("start"); //$NON-NLS-1$
		args.append(' ');
		args.append(fAddress);
		
		args.append(" -plugins:"); //$NON-NLS-1$
		args.append("awt,"); //$NON-NLS-1$
		if (Platform.getOS().equals(Platform.OS_WIN32)) { 
			args.append("swt,"); //$NON-NLS-1$
		}
		
		return args.toString();
	}
	
	@Override
	protected void prepareStopInMain(final ILaunchConfiguration configuration) throws CoreException {
	}
	
}
