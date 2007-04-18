/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License 
 * v2.1 or newer, which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.rserve.launchconfigs;

import de.walware.statet.r.rserve.RServePlugin;


public interface IRServeConstants {

	String ID_RSERVE_LAUNCHCONFIG = "de.walware.statet.r.rserve.launching.RServeClientLaunchConfigurationType";
	
	String CONFIG_CONNECTION_SERVERADDRESS = 	RServePlugin.PLUGIN_ID + "/connection/server_address";
	String CONFIG_CONNECTION_SERVERPORT = 	    RServePlugin.PLUGIN_ID + "/connection/server_port";
	String CONFIG_CONNECTION_SOCKETTIMEOUT =    RServePlugin.PLUGIN_ID + "/connection/socket_timeout";
}
