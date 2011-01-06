/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico.impl;

import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_SSH_HOST_DATA_KEY;
import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_SSH_PORT_DATA_KEY;
import static de.walware.statet.nico.core.runtime.IToolEventHandler.LOGIN_USERNAME_DATA_KEY;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.debug.core.ISshSessionService;

import de.walware.statet.base.core.StatetCore;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import de.walware.rj.server.FxCallback;

import de.walware.statet.r.core.RCore;


public class RjsUtil {
	
	
	public static Session getSession(final Map<String, Object> loginData, final IProgressMonitor monitor) throws CoreException {
		final String username = (String) loginData.get(LOGIN_USERNAME_DATA_KEY);
		final String sshHost = (String) loginData.get(LOGIN_SSH_HOST_DATA_KEY);
		final Integer sshPort = (Integer) loginData.get(LOGIN_SSH_PORT_DATA_KEY);
		
		return StatetCore.getSshSessionManager().getSshSession(
				username, sshHost, (sshPort != null) ? sshPort.intValue() : ISshSessionService.SSH_DEFAULT_PORT, monitor);
	}
	
	
	public static void startRemoteServerOverSsh(final Session session, final String command, final Hashtable<String, String> envp,
			final IProgressMonitor monitor) throws CoreException {
		
		ChannelExec execChannel = null;
		int status = -11111111;
		Exception error = null;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			execChannel = (ChannelExec) session.openChannel("exec"); //$NON-NLS-1$
			execChannel.setCommand(command);
			if (envp != null) {
				execChannel.setEnv(envp);
			}
			
			execChannel.setInputStream(null);
			execChannel.setOutputStream(output, false);
			execChannel.setErrStream(output, false);
			
			execChannel.connect();
			
			while (!execChannel.isClosed()) {
				if (monitor.isCanceled()) {
					execChannel.disconnect();
					throw new CoreException(Status.CANCEL_STATUS);
				}
				try {
					Thread.sleep(200);
				}
				catch (final InterruptedException e) {
					Thread.interrupted();
				}
			}
			status = execChannel.getExitStatus();
		}
		catch (final NullPointerException e) {
			// TODO: Should be fixed in newer version / https://bugs.eclipse.org/bugs/show_bug.cgi?id=232416
			error = new JSchException("Probably: The SSH connection was closed unexpected by timeout or a failure.", e);
			status = -11111113;
		}
		catch (final JSchException e) {
			error = e;
			status = -11111114;
		}
		finally {
			if (execChannel != null) {
				execChannel.disconnect();
			}
		}
		if (status != 0 && error == null) {
			try {
				error = new RemoteException("Exit status: " + status + //$NON-NLS-1$
						"\nMessage:\n" + output.toString("UTF-8"));
			} catch (final UnsupportedEncodingException e) {}
		}
		if (error != null) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					"Failed to start remote R server over SSH.", error));
		}
	}
	
	public static void handleFxCallback(final Session session, final FxCallback callback,
			final IProgressMonitor monitor) throws CoreException {
		final byte[] clientKey = new byte[1024];
		new SecureRandom().nextBytes(clientKey);
		final String filename = callback.getFilename();
		final byte[] content = callback.createContent(clientKey);
		
		ChannelExec execChannel = null;
		int status = -11111111;
		Exception error = null;
		try {
			execChannel = (ChannelExec) session.openChannel("exec"); //$NON-NLS-1$
			execChannel.setCommand("cat >> " + filename); //$NON-NLS-1$
			
			final ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
			execChannel.setInputStream(inputStream, false);
			execChannel.setOutputStream(null);
			execChannel.setErrStream(null);
			
			execChannel.connect();
			
			while (!execChannel.isClosed()) {
				if (monitor.isCanceled()) {
					execChannel.disconnect();
					throw new CoreException(Status.CANCEL_STATUS);
				}
				try {
					Thread.sleep(200);
				}
				catch (final InterruptedException e) {
					Thread.interrupted();
				}
			}
			status = execChannel.getExitStatus();
		}
		catch (final NullPointerException e) {
			// TODO: Should be fixed in newer version / https://bugs.eclipse.org/bugs/show_bug.cgi?id=232416
			error = new JSchException("Probably: The SSH connection was closed unexpected by timeout or a failure.", e);
			status = -11111113;
		}
		catch (final JSchException e) {
			error = e;
			status = -11111114;
		}
		finally {
			if (execChannel != null) {
				execChannel.disconnect();
			}
		}
		if (status != 0 && error == null) {
			error = new RemoteException("Exit code: " + status); //$NON-NLS-1$
		}
		if (error != null) {
			throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID,
					"Failed to authenticate over SSH connection.", error));
		}
	}
	
	public static String getVersionString(final int[] version) {
		if (version == null) {
			return "no version information";
		}
		if (version.length >= 3) {
			final StringBuilder sb = new StringBuilder();
			sb.append(version[0]);
			sb.append('.');
			sb.append((version[1] >= 0) ? Integer.toString(version[1]) : "x");
			sb.append('.');
			sb.append((version[2] >= 0) ? Integer.toString(version[2]) : "x");
			return sb.toString();
		}
		return "invalid version information";
	}
	
}
