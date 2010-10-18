/*******************************************************************************
 * Copyright (c) 2008-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launchconfigs;

import static de.walware.rj.server.srvext.ServerUtil.RJ_DATA_ID;
import static de.walware.rj.server.srvext.ServerUtil.RJ_SERVER_ID;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import de.walware.ecommons.debug.ui.LaunchConfigUtil;
import de.walware.ecommons.net.RMIAddress;

import de.walware.rj.server.srvext.EServerUtil;
import de.walware.rj.server.srvext.ServerUtil;

import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.debug.ui.launchconfigs.REnvTab;
import de.walware.statet.r.internal.ui.RUIPlugin;


/**
 * Launches RJ Server using JDT java launch mechanism
 */
public class RJEngineLaunchDelegate extends JavaLaunchDelegate {
	
	
	private static final String[] CLASSPATH_LIBS = new String[] {
			RJ_SERVER_ID, RJ_DATA_ID, "org.eclipse.swt", //$NON-NLS-1$
	};
	private static final String[] CODEBASE_LIBS = new String[] {
			RJ_SERVER_ID,
	};
	
	
	private final String fAddress;
	private final IREnvConfiguration fRenv;
	
	private File fWorkingDirectory;
	
	
	public RJEngineLaunchDelegate(final String address, final IREnvConfiguration renv) throws CoreException {
		fAddress = address;
		fRenv = renv;
	}
	
	
	@Override
	public IPath getWorkingDirectoryPath(final ILaunchConfiguration configuration) throws CoreException {
		final IFileStore workingDirectory = REnvTab.getWorkingDirectory(configuration);
		return URIUtil.toPath(workingDirectory.toURI());
	}
	
	@Override
	public File verifyWorkingDirectory(final ILaunchConfiguration configuration) throws CoreException {
		return fWorkingDirectory = super.verifyWorkingDirectory(configuration);
	}
	
	public IFileStore getWorkingDirectory() {
		if (fWorkingDirectory != null) {
			return EFS.getLocalFileSystem().fromLocalFile(fWorkingDirectory);
		}
		return null;
	}
	
	@Override
	public String[] getEnvironment(final ILaunchConfiguration configuration) throws CoreException {
		final Map<String, String> envp = LaunchConfigUtil.createEnvironment(configuration, 
				new Map[] { fRenv.getEnvironmentsVariables() });
		return LaunchConfigUtil.toKeyValueStrings(envp);
	}
	
	@Override
	public String[] getClasspath(final ILaunchConfiguration configuration) throws CoreException {
		final String[] rjLibs = EServerUtil.searchRJLibsInPlatform(CLASSPATH_LIBS, (fRenv.getRBits() == 64));
		
		final LinkedHashSet<String> classpath = new LinkedHashSet<String>();
		classpath.addAll(Arrays.asList(super.getClasspath(configuration)));
		classpath.addAll(Arrays.asList(rjLibs));
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
				final URL intern = Platform.getBundle(RJ_SERVER_ID).getEntry("/localhost.policy"); //$NON-NLS-1$ 
				final URL java = FileLocator.resolve(intern);
				s.append(" -Djava.security.policy="); //$NON-NLS-1$
				s.append('"');
				s.append(java.toString());
				s.append('"');
			}
			catch (final IOException e) {
				RUIPlugin.logError(-1, "Error trying to resolve path to security policy", e); //$NON-NLS-1$
			}
		}
		if (s.indexOf(" -Djava.rmi.server.hostname=") < 0) { //$NON-NLS-1$
			s.append(" -Djava.rmi.server.hostname="); //$NON-NLS-1$
			s.append(RMIAddress.LOOPBACK.getHostAddress());
		}
		if (s.indexOf(" -Djava.rmi.server.codebase=") < 0) { //$NON-NLS-1$
			s.append(" -Djava.rmi.server.codebase=\""); //$NON-NLS-1$
			final String[] rjLibs = EServerUtil.searchRJLibsInPlatform(CODEBASE_LIBS, (fRenv.getRBits() == 64));
			s.append(ServerUtil.concatCodebase(rjLibs));
			s.append("\""); //$NON-NLS-1$
		}
		if (s.indexOf(" -Xss") < 0) { //$NON-NLS-1$
			s.append(" -Xss").append(fRenv.getRBits()*256).append("k"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		if (configuration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, false)
				&& s.indexOf(" -Dde.walware.rj.verbose=") < 0) { //$NON-NLS-1$
			s.append(" -Dde.walware.rj.verbose=true"); //$NON-NLS-1$
		}
		if (Platform.getOS().equals(Platform.OS_MACOSX)
				&& s.indexOf(" -d32") < 0 && s.indexOf(" -d64") < 0) { //$NON-NLS-1$ //$NON-NLS-2$
			final String rArch = fRenv.getSubArch();
			if (rArch != null && rArch.length() > 0) {
				if (rArch.equals("i386") || rArch.equals("i586") || rArch.equals("i686")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					s.append("-d32"); //$NON-NLS-1$
				}
				else if (rArch.equals("x86_64")) { //$NON-NLS-1$
					s.append("-d64"); //$NON-NLS-1$
				}
			}
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
		
		args.append(" -auth=none"); //$NON-NLS-1$
		
		args.append(" -plugins="); //$NON-NLS-1$
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
