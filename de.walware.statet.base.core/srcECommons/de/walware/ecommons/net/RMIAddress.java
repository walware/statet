/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.statet.base.internal.core.BaseCorePlugin;


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
	
	static {
		InetAddress loopbackAddress; 
		try {
			loopbackAddress = InetAddress.getByAddress("localhost", new byte[] { 127, 0, 0, 1 });
		}
		catch (final UnknownHostException e) {
			loopbackAddress = null;
			BaseCorePlugin.logError(-1, "Failed to create loopback address", e); //$NON-NLS-1$
		}
		LOOPBACK = loopbackAddress;
	}
	
	
	private String fHost;
	private InetAddress fHostAddress;
	private String fPort;
	private int fPortNum;
	private String fPath;
	
	private String fAddress;
	
	
	public RMIAddress(final String address) throws UnknownHostException, MalformedURLException {
		this(address, true);
	}
	
	private RMIAddress(String address, final boolean resolve) throws UnknownHostException, MalformedURLException {
		for (int i = 0; i < address.length(); i++) {
			final char c = address.charAt(i);
			if (c == '?' || c == '#' || c == '[' || c == ']' || c == '@'
					|| c == '!' || c == '$' || c == '&' || c == '\'' || c == '(' || c == ')'
					|| c == '*' || c == '+' || c == ',' || c == ';' || c == '='
					|| c == '"' || c == '\\') {
				throw new MalformedURLException("Character '"+c+"' is not allowed.");
			}
		}
		
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
			fHost = address.substring(2, idxPort);
			fPort = address.substring(idxPort+1, idxPath);
			fPath = address.substring(idxPath+1);
		}
		else if (idxPath > 0){
			fHost = address.substring(2, idxPath);
			fPort = null;
			fPath = address.substring(idxPath+1);
		}
		else {
			fHost = null;
			fPort = null;
			fPath = address.substring(2);
		}
		if (fHost != null && fHost.length() == 0) {
			fHost = null;
		}
		if (fPort != null && fPort.length() == 0) {
			fPort = null;
		}
		if (resolve) {
			fHostAddress = (fHost != null) ? InetAddress.getByName(fHost) : LOOPBACK;
		}
		try {
			fPortNum = (fPort != null) ? Integer.parseInt(fPort) : Registry.REGISTRY_PORT;
			if (fPortNum < 0 || fPortNum > 65535) {
				throw new NumberFormatException("Value must be in range 0-65535");
			}
		}
		catch (final NumberFormatException e) {
			throw new MalformedURLException("Invalid port, " + e.getLocalizedMessage());
		}
	}
	
	
	/**
	 * @return the host as specified when creating the address
	 */
	public String getHost() {
		return fHost;
	}
	
	public InetAddress getHostAddress() {
		return fHostAddress;
	}
	
	public boolean isLocalHost() {
		InetAddress localhost;
		try {
			localhost = InetAddress.getLocalHost();
		}
		catch (final UnknownHostException e) {
			localhost = null;
		}
		return (fHostAddress.isLoopbackAddress() || fHostAddress.equals(localhost));
	}
	
	/**
	 * @return the port
	 */
	public String getPort() {
		return fPort;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return fPath;
	}
	
	/**
	 * Standard string presentation to use for rmi
	 * @return
	 */
	public String getAddress() {
		if (fAddress == null) {
			final StringBuilder sb = new StringBuilder(32);
			sb.append("//"); //$NON-NLS-1$
			if (fHost != null) {
				sb.append(fHost);
			}
			if (fPortNum != Registry.REGISTRY_PORT) {
				sb.append(':');
				sb.append(fPort);
			}
			sb.append('/');
			sb.append(fPath);
			fAddress = sb.toString();
		}
		return fAddress;
	}
	
	public String getRegistryAddress() {
		final StringBuilder sb = new StringBuilder(32);
		sb.append("//"); //$NON-NLS-1$
		if (fHost != null) {
			sb.append(fHost);
		}
		if (fPortNum != Registry.REGISTRY_PORT) {
			sb.append(':');
			sb.append(Integer.toString(fPortNum));
		}
		sb.append('/');
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return "rmi:"+getAddress(); //$NON-NLS-1$
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
		return (fHostAddress.equals(other.fHostAddress)
				&& fPortNum == other.fPortNum
				&& fPath.equals(other.fPath));
	}
	
}
