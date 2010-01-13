/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.net;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.registry.Registry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.ecommons.ECommons;


/**
 * Address for RMI naming
 */
public class RMIAddress {
	
	
	public static final InetAddress LOOPBACK;
	
	public static void validate(final String address) throws MalformedURLException {
		try {
			new RMIAddress(address, false);
		}
		catch (final UnknownHostException e) {
		}
	}
	
	
	private static String checkChars(final String s) throws MalformedURLException {
		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			if (c == '?' || c == '#' || c == '[' || c == ']' || c == '@'
					|| c == '!' || c == '$' || c == '&' || c == '\'' || c == '(' || c == ')'
					|| c == '*' || c == '+' || c == ',' || c == ';' || c == '='
					|| c == '"' || c == '\\') {
				throw new MalformedURLException("Character '"+c+"' is not allowed.");
			}
		}
		return s;
	}
	
	private static int checkPort(final String port) throws MalformedURLException {
		final int portNum;
		try {
			portNum = (port != null) ? Integer.parseInt(port) : Registry.REGISTRY_PORT;
		}
		catch (final NumberFormatException e) {
			throw new MalformedURLException("Invalid port, " + e.getMessage());
		}
		return checkPort(portNum);
	}
	
	private static int checkPort(final int portNum) throws MalformedURLException {
		if (portNum < 0 || portNum > 65535) {
			throw new MalformedURLException("Invalid port, " + "Value must be in range 0-65535");
		}
		return portNum;
	}
	
	private static String build(final String host, final int portNum, final String name) {
		final StringBuilder sb = new StringBuilder("//");
		if (host != null) {
			sb.append(host);
		}
		sb.append(":");
		if (portNum >= 0) {
			sb.append(Integer.toString(portNum));
		}
		sb.append("/");
		if (name != null) {
			sb.append(name);
		}
		return sb.toString();
	}
	
	
	static {
		InetAddress loopbackAddress;
		try {
			loopbackAddress = InetAddress.getByAddress("localhost", new byte[] { 127, 0, 0, 1 }); //$NON-NLS-1$
		}
		catch (final UnknownHostException e) {
			loopbackAddress = null;
			ECommons.getEnv().log(new Status(IStatus.ERROR, ECommons.PLUGIN_ID, "Failed to create loopback address", e)); //$NON-NLS-1$
		}
		LOOPBACK = loopbackAddress;
	}
	
	
	private String host;
	private InetAddress hostAddress;
	private String port;
	private int portNum;
	private String path;
	
	private String address;
	
	
	public RMIAddress(final String address) throws UnknownHostException, MalformedURLException {
		this(address, true);
	}
	
	public RMIAddress(final String host, final int portNum, final String name) throws UnknownHostException, MalformedURLException {
		this(build(host, portNum, name), true);
	}
	
	public RMIAddress(final InetAddress address, final int port, final String name) throws MalformedURLException {
		this(address.getHostAddress(), address, Integer.toString(port), checkPort(port), (name != null) ? checkChars(name) : "");
	}
	
	public RMIAddress(final RMIAddress registry, final String name) throws MalformedURLException {
		this(registry.host, registry.hostAddress, registry.port, registry.portNum,
				(name != null) ? checkChars(name) : "");
	}
	
	private RMIAddress(String address, final boolean resolve) throws UnknownHostException, MalformedURLException {
		address = checkChars(address);
		
		if (address.startsWith("rmi:")) { //$NON-NLS-1$
			address = address.substring(4);
		}
		if (!address.startsWith("//")) { //$NON-NLS-1$
			address = "//"+address; //$NON-NLS-1$
		}
		
		final int idxPort = address.indexOf(':', 2);
		final int idxPath = address.indexOf('/', 2);
		if (idxPort > 0) {
			if (idxPath <= idxPort) {
				throw new IllegalArgumentException();
			}
			this.host = address.substring(2, idxPort);
			this.port = address.substring(idxPort+1, idxPath);
			this.path = address.substring(idxPath+1);
		}
		else if (idxPath > 0){
			this.host = address.substring(2, idxPath);
			this.port = null;
			this.path = address.substring(idxPath+1);
		}
		else {
			this.host = null;
			this.port = null;
			this.path = address.substring(2);
		}
		if (this.host != null && this.host.length() == 0) {
			this.host = null;
		}
		if (this.port != null && this.port.length() == 0) {
			this.port = null;
		}
		try {
			this.portNum = checkPort(this.port);
		}
		catch (final NumberFormatException e) {
			throw new MalformedURLException("Invalid port, " + e.getLocalizedMessage());
		}
		if (resolve) {
			this.hostAddress = (this.host != null) ? InetAddress.getByName(this.host) : LOOPBACK;
		}
	}
	
	private RMIAddress(final String host, final InetAddress hostAddress, final String port, final int portNum, final String path) {
		this.host = host;
		this.hostAddress = hostAddress;
		this.port = port;
		this.portNum = portNum;
		this.path = path;
	}
	
	
	/**
	 * @return the host as specified when creating the address
	 */
	public String getHost() {
		return (this.host != null) ? this.host : this.hostAddress.getHostAddress();
	}
	
	public InetAddress getHostAddress() {
		return this.hostAddress;
	}
	
	public boolean isLocalHost() {
		InetAddress localhost;
		try {
			localhost = InetAddress.getLocalHost();
		}
		catch (final UnknownHostException e) {
			localhost = null;
		}
		return (this.hostAddress.isLoopbackAddress() || this.hostAddress.equals(localhost));
	}
	
	/**
	 * @return the port
	 */
	public String getPort() {
		return this.port;
	}
	
	public int getPortNum() {
		return this.portNum;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return this.path;
	}
	
	/**
	 * Standard string presentation to use for rmi
	 * @return
	 */
	public String getAddress() {
		if (this.address == null) {
			final StringBuilder sb = new StringBuilder(32);
			sb.append("rmi://"); //$NON-NLS-1$
			if (this.host != null) {
				sb.append(this.host);
			}
			if (this.portNum != Registry.REGISTRY_PORT) {
				sb.append(':');
				sb.append(this.port);
			}
			sb.append('/');
			sb.append(this.path);
			this.address = sb.toString();
		}
		return this.address;
	}
	
	public RMIAddress getRegistryAddress() {
		return new RMIAddress(this.host, this.hostAddress, this.port, this.portNum, "");
	}
	
	
	@Override
	public String toString() {
		return getAddress();
	}
	
	@Override
	public int hashCode() {
		return getAddress().hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof RMIAddress)) {
			return false;
		}
		final RMIAddress other = (RMIAddress) obj;
		return (this.hostAddress.equals(other.hostAddress)
				&& this.portNum == other.portNum
				&& this.path.equals(other.path));
	}
	
}
