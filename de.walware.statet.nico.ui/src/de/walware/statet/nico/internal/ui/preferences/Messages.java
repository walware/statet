/*=============================================================================#
 # Copyright (c) 2013-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.ui.preferences;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String TextStyle_link;
	public static String TextStyle_Input_label;
	public static String TextStyle_Input_description;
	public static String TextStyle_Info_label;
	public static String TextStyle_Info_description;
	public static String TextStyle_StandardOutput_label;
	public static String TextStyle_StandardOutput_description;
	public static String TextStyle_SystemOutput_label;
	public static String TextStyle_SystemOutput_description;
	public static String TextStyle_StandardError_label;
	public static String TextStyle_StandardError_description;
	public static String TextStyle_SpecialBackground_label;
	public static String TextStyle_SpecialBackground_Tasks_description;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
