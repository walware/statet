/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.base.internal.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jsch.core.IJSchLocation;
import org.eclipse.jsch.core.IJSchService;

import de.walware.ecommons.ECommons;
import de.walware.ecommons.IDisposable;
import de.walware.ecommons.net.ssh.ISshSessionService;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;



/**
 * SSH session service implementation.
 * 
 * Don't forget to dispose to close the open connections.
 */
public class SshSessionManager implements ISshSessionService, IDisposable {
	
	
	private static final int SSH_TIMEOUT = 60000;
	
	
	private Map<String, Session> fPool;
	
	
	public SshSessionManager() {
		fPool = new HashMap<String, Session>();
	}
	
	
	@Override
	public Session getSshSession(final String username, final String host, final int port,
			final IProgressMonitor monitor) throws CoreException {
		final String key = username + '@' + host + ':' + Integer.toString((port > 0) ? port : 22);
		Session session = fPool.get(key);
		try {
			final IJSchService jschService = BaseCorePlugin.getDefault().getJSchService();
			if (session == null) {
				final IJSchLocation location = jschService.getLocation(username, host, port);
				session = jschService.createSession(location, null);
			}
			if (!session.isConnected()) {
				jschService.connect(session, SSH_TIMEOUT, new SubProgressMonitor(monitor, 1));
			}
			fPool.put(key, session);
			return session;
		}
		catch (final JSchException e) {
			// create new session, if existing session is broken
			if ("Packet corrupt".equals(e.getMessage()) && fPool.values().remove(session)) { //$NON-NLS-1$
				return getSshSession(username, host, port, monitor);
			}
			
			throw new CoreException(new Status(IStatus.ERROR, ECommons.PLUGIN_ID, "Failed to create SSH connection", e));
		}
	}
	
	@Override
	public void dispose() {
		final Collection<Session> sessions = fPool.values();
		for (final Session session : sessions) {
			session.disconnect();
		}
		fPool = null;
	}
	
}
