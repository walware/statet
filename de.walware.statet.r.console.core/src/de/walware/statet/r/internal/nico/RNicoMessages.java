/*=============================================================================#
 # Copyright (c) 2005-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.nico;

import org.eclipse.osgi.util.NLS;


public class RNicoMessages extends NLS {
	
	
	public static String Rterm_StartTask_label;
	public static String RTerm_CancelTask_label;
	public static String RTerm_CancelTask_SendSignal_label;
	public static String RTerm_error_Starting_message;
	
	public static String R_Info_Started_message;
	public static String R_Info_Reconnected_message;
	public static String R_Info_Disconnected_message;
	public static String R_Info_ConnectionLost_message;
	public static String R_Info_Stopped_message;
	
	
	static {
		NLS.initializeMessages(RNicoMessages.class.getName(), RNicoMessages.class);
	}
	private RNicoMessages() {}
	
}
