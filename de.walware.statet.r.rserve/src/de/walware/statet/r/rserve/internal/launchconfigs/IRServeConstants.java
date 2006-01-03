/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License v2.0
 * or newer, which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.rserve.internal.launchconfigs;

import de.walware.statet.r.rserve.RServePlugin;


public interface IRServeConstants {

	String CONFIG_CONNECTION_SERVERADDRESS = 	RServePlugin.ID + "/connection/server_address";
	String CONFIG_CONNECTION_SERVERPORT = 	    RServePlugin.ID + "/connection/server_port";
	String CONFIG_CONNECTION_SOCKETTIMEOUT =    RServePlugin.ID + "/connection/socket_timeout";
}
