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

package de.walware.statet.rtm.base.internal.ui.editors;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String EFEditor_MultiObjectSelected_message;
	public static String EFEditor_error_ProblemsInFile_message;
	public static String EFEditor_FileConflict_title;
	public static String EFEditor_FileConflict_message;
	
	public static String RTaskEditor_FirstPage_label;
	public static String RTaskEditor_RCodePage_label;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
