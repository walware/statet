/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.console.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.rj.data.RObject;
import de.walware.rj.services.RService;

import de.walware.statet.r.core.tool.IRConsoleService;


/**
 * Adapter interface for {@link RConsoleTool#R_DATA_FEATURESET_ID}.
 * 
 * Makes {@link RService} available.
 */
public interface IRDataAdapter extends IRBasicAdapter, IRConsoleService {
	
	
	RObject[] findData(final String symbol, final RObject envir, final boolean inherits,
			final String factoryId, final int options, final int depth,
			final IProgressMonitor monitor) throws CoreException;
	
}
