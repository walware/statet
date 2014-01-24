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

package de.walware.statet.r.internal.ui.tools;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String LoadData_Wizard_title;
	public static String LoadData_Wizard_SelectPage_title;
	public static String LoadData_Wizard_SelectPage_description;
	public static String LoadData_Wizard_File_label;
	public static String LoadData_Wizard_File_RImages_name;
	public static String LoadData_Runnable_label;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
