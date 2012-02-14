/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.lauching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.debug.ui.LaunchConfigUtil;
import de.walware.statet.r.console.ui.launching.AbstractRConsoleLaunchDelegate;
import de.walware.statet.r.console.ui.launching.RConsoleLaunching;
import de.walware.statet.r.internal.console.ui.launching.RConsoleRJLaunchDelegate;
import de.walware.statet.r.ui.RUI;


public class RConsoleDebugLaunchDelegate extends AbstractRConsoleLaunchDelegate {
	
	
	public RConsoleDebugLaunchDelegate() {
	}
	
	
	@Override
	public void launch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		try {
			monitor = LaunchConfigUtil.initProgressMonitor(configuration, monitor, 100);
			if (monitor.isCanceled()) {
				return;
			}
			
			final String type = configuration.getAttribute(RConsoleLaunching.ATTR_TYPE, ""); //$NON-NLS-1$
			LaunchConfigurationDelegate delegate = null;
			if (type.equals(RConsoleLaunching.LOCAL_RJS)) {
				delegate = new RConsoleRJLaunchDelegate(
						new DebugLaunchDelegateAddon() );
			}
			if (delegate != null) {
				delegate.launch(configuration, mode, launch, monitor);
				return;
			}
			throw new CoreException(new Status(IStatus.ERROR, RUI.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					NLS.bind("R Console launch type ''{0}'' is not available.", type), null));
		}
		finally {
			monitor.done();
		}
	}
	
}
