/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.rtools;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	public static String HelpCommand_name;
	public static String PrintCommand_name;
	
	public static String Variable_error_EmptySelection_message;
	public static String Variable_error_InvalidProject_NotExists_message;
	public static String Variable_error_InvalidProject_NoRProject_message;
	public static String Variable_error_NoSingleRElement_message;
	public static String Variable_error_InvalidObject_QualifiedName_message;
	public static String Variable_error_InvalidProject_NoPkgName_message;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
