/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.rtm.base.internal.ui.actions;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String NewRTaskWizard_title;
	public static String NewRTaskWizard_NewFile_title;
	public static String NewRTaskWizard_NewFile_description;
	public static String NewRTaskWizard_error_CreateFile_message;
	public static String NewRTaskWizard_error_OpenEditor_message;
	
	public static String NewTask_PerspSwitch_title;
	public static String NewTask_PerspSwitch_message;
	public static String NewTask_PerspSwitch_WithDesc_message;
	
	public static String RunTask_RequirePkgs_message;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
