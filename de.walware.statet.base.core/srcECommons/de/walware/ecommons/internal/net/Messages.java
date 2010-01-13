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

package de.walware.ecommons.internal.net;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String RMI_status_RegistryAlreadyStarted_message;
	public static String RMI_status_RegistryStartFailed_message;
	public static String RMI_status_RegistryStartFailedWithExitValue_message;
	public static String RMI_status_RegistryStopFailedNotFound_message;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
