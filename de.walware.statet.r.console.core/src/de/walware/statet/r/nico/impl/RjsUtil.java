/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketOptions;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.security.SecureRandom;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.debug.core.ISshSessionService;

import de.walware.statet.base.core.StatetCore;

import com.jcraft.jsch.ChannelDirectTCPIP;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import de.walware.rj.server.FxCallback;

import de.walware.statet.r.internal.console.core.RConsoleCorePlugin;


public class RjsUtil {
	
	
	public static Session getSession(final Map<String, Object> loginData, final IProgressMonitor monitor) throws CoreException {
		final String username = (String) loginData.get(LOGIN_USERNAME_DATA_KEY);
		final String sshHost = (String) loginData.get(LOGIN_SSH_HOST_DATA_KEY);
		final Integer sshPort = (Integer) loginData.get(LOGIN_SSH_PORT_DATA_KEY);
		
		return StatetCore.getSshSessionManager().getSshSession(
				username, sshHost, (sshPort != null) ? sshPort.intValue() : ISshSessionService.SSH_DEFAULT_PORT, monitor);
	}
	
	
	private static class RMIOverSshClientSocket extends Socket {
		
		public RMIOverSshClientSocket(final ChannelDirectTCPIP channel) throws SocketException, IOException {
			super(new RMIOverSshClientSocketImpl(channel));
		}
		
	}
	
	private static class RMIOverSshClientSocketImpl extends SocketImpl {
		
		private static class ChannelInputStream extends PipedInputStream {
			
			public ChannelInputStream() {
				super();
				this.buffer = new byte[32*1024];
			}
			
			public int getBufferSize() {
				return this.buffer.length;
			}
			
		}
		
		
		private final ChannelDirectTCPIP fChannel;
		
		private final ChannelInputStream fInputStream;
		private final OutputStream fOutputStream;
		
		
		public RMIOverSshClientSocketImpl(final ChannelDirectTCPIP channel) throws IOException {
			fChannel = channel;
			fInputStream = new ChannelInputStream();
			channel.setOutputStream(new PipedOutputStream(fInputStream));
			fOutputStream = channel.getOutputStream();
			
			localport = 0;
		}
		
		
		@Override
		protected void create(final boolean stream) throws IOException {
			if (!stream) {
				throw new IOException("Not supported");
			}
		}
		
		@Override
		protected void connect(final String host, final int port) throws IOException {
			connect(InetSocketAddress.createUnresolved(host, port), 0);
		}
		@Override
		protected void connect(final InetAddress address, final int port) throws IOException {
			connect(new InetSocketAddress(address, port), 0);
		}
		@Override
		protected void connect(final SocketAddress address, final int timeout) throws IOException {
			final InetSocketAddress inetAddress = ((InetSocketAddress) address);
			if (this.localport != 0) {
				throw new IOException("Not supported: reconnect to " + address.toString());
			}
			this.address = inetAddress.getAddress();
			this.port = inetAddress.getPort();
			this.localport = -1;
		}
		
		@Override
		protected void bind(final InetAddress host, final int port) throws IOException {
			throw new IOException("Not supported");
		}
		
		@Override
		protected void listen(final int backlog) throws IOException {
			throw new IOException("Not supported");
		}
		
		@Override
		protected void accept(final SocketImpl s) throws IOException {
			throw new IOException("Not supported");
		}
		
		@Override
		public InputStream getInputStream() throws IOException {
			return fInputStream;
		}
		
		@Override
		public OutputStream getOutputStream() throws IOException {
			return fOutputStream;
		}
		
		@Override
		protected int available() throws IOException {
			return fInputStream.available();
		}
		
		@Override
		protected void close() throws IOException {
			localport = -1;
			fChannel.disconnect();
		}
		
		@Override
		protected void shutdownInput() throws IOException {
			fInputStream.close();
		}
		
		@Override
		protected void shutdownOutput() throws IOException {
			fOutputStream.close();
		}
		
		@Override
		protected void sendUrgentData(final int data) throws IOException {
			throw new IOException("Not supported");
		}
		
		@Override
		public void setOption(final int optID, final Object value) throws SocketException {
			switch (optID) {
			case SocketOptions.TCP_NODELAY:
				if (((Boolean) value).booleanValue() == true) {
					return;
				}
				break;
			case SocketOptions.SO_KEEPALIVE:
				if (((Boolean) value).booleanValue() == false) {
					return;
				}
				break;
			case SocketOptions.SO_TIMEOUT:
				return;
			case SocketOptions.SO_SNDBUF:
			case SocketOptions.SO_RCVBUF:
				return;
			default:
//				System.out.println("SshSocket setOption " + optID + "= " + value);
				break;
			}
			throw new SocketException("Not supported: option= " + optID + " with value= " + value);
		}
		
		@Override
		public Object getOption(final int optID) throws SocketException {
			switch (optID) {
			case SocketOptions.TCP_NODELAY:
				return Boolean.TRUE;
			case SocketOptions.SO_KEEPALIVE:
				return Boolean.FALSE;
			case SocketOptions.SO_TIMEOUT:
				return Integer.valueOf(0);
			case SocketOptions.SO_SNDBUF:
				return 1024;
			case SocketOptions.SO_RCVBUF:
				return fInputStream.getBufferSize();
			default:
//				System.out.println("SshSocket getOption " + optID);
				break;
			}
			throw new SocketException("Not supported: option= " + optID);
		}
		
	}
	
	public static RMIClientSocketFactory createRMIOverSshClientSocketFactory(final Session session) {
		return new RMIClientSocketFactory() {
			@Override
			public Socket createSocket(final String host, final int port) throws IOException {
//				System.out.println("SshSocket new: to= " + host + ":" + port);
				try {
					final ChannelDirectTCPIP tcpipChannel = (ChannelDirectTCPIP) session.openChannel("direct-tcpip");
					tcpipChannel.setHost(host);
					tcpipChannel.setPort(port);
					final Socket socket = new RMIOverSshClientSocket(tcpipChannel);
					socket.connect(InetSocketAddress.createUnresolved(host, port));
					tcpipChannel.connect();
					return socket;
				}
				catch (final JSchException e) {
					final IOException ioException = new IOException();
					ioException.initCause(e);
					throw ioException;
				}
			}
		};
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
					// continue directly, monitor is checked
				}
			}
			status = execChannel.getExitStatus();
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
			throw new CoreException(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID,
					"Failed to start remote R server over SSH.", error ));
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
					// continue directly, monitor is checked
				}
			}
			status = execChannel.getExitStatus();
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
			throw new CoreException(new Status(IStatus.ERROR, RConsoleCorePlugin.PLUGIN_ID,
					"Failed to authenticate over SSH connection.", error ));
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
