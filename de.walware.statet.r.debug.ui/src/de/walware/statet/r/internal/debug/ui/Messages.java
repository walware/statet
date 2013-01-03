/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String Breakpoint_Line_label;
	public static String Breakpoint_SubLabel_copula;
	public static String Breakpoint_Function_prefix;
	public static String Breakpoint_S4Method_prefix;
	public static String Breakpoint_ScriptLine_prefix;
	
	public static String Breakpoint_DefaultDetailPane_name;
	public static String Breakpoint_DefaultDetailPane_description;
	
	public static String MethodBreakpoint_Entry_label;
	public static String MethodBreakpoint_Exit_label;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
