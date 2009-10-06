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

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class RMIRegistry {
	
	
	private final RMIAddress address;
	
	private final Registry registry;
	
	
	public RMIRegistry(final RMIAddress address, final Registry registry, final boolean validate) throws RemoteException {
		if (address == null) {
			throw new NullPointerException();
		}
		if (address.getName().length() > 0) {
			throw new IllegalArgumentException();
		}
		this.address = address;
		this.registry = (registry != null) ? registry : LocateRegistry.getRegistry(address.getHost(), address.getPortNum());
		if (validate) {
			this.registry.list();
		}
	}
	
	
	public RMIAddress getAddress() {
		return this.address;
	}
	
	public Registry getRegistry() {
		return this.registry;
	}
	
}
