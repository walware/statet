/*=============================================================================#
 # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.debug.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.jcraft.jsch.Session;


/**
 * Manager for SSH sessions
 */
public interface ISshSessionService {
	
	
	public static final int SSH_DEFAULT_PORT = 22;
	
	
	public Session getSshSession(final String username, final String host, final int port,
			final IProgressMonitor monitor) throws CoreException;
	
}
