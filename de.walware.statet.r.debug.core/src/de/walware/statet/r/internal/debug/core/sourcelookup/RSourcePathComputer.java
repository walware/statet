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

package de.walware.statet.r.internal.debug.core.sourcelookup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.debug.core.sourcelookup.REnvLibraryPathSourceContainer;
import de.walware.statet.r.launching.core.RLaunching;


public class RSourcePathComputer implements ISourcePathComputerDelegate {
	
	
	/** Created via extension point */
	public RSourcePathComputer() {
	}
	
	
	@Override
	public ISourceContainer[] computeSourceContainers(final ILaunchConfiguration configuration,
			final IProgressMonitor monitor) throws CoreException {
		final IREnv rEnv = RLaunching.readREnv(configuration);
		if (rEnv == null) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, 0,
					Messages.RSourcePathComputer_error_REnvNotFound_message, null)); 
		}
		return new ISourceContainer[] {
				new REnvLibraryPathSourceContainer(rEnv),
		};
	}
	
}
