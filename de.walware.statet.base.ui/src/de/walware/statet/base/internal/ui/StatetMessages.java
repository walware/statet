/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.internal.ui;

import org.eclipse.osgi.util.NLS;


public class StatetMessages extends NLS {
	
	
	public static String InternalError_UnexpectedException;
	
	public static String TaskPriority_High;
	public static String TaskPriority_Normal;
	public static String TaskPriority_Low;
	
	public static String CodeFolding_label;
	public static String CodeFolding_Enable_label;
	public static String CodeFolding_Enable_mnemonic;
	public static String CodeFolding_ExpandAll_label;
	public static String CodeFolding_ExpandAll_mnemonic;
	public static String CodeFolding_CollapseAll_label;
	public static String CodeFolding_CollapseAll_mnemonic;
	
	public static String SyncWithEditor_label;
	public static String SelectSourceCode_label;
	
	
	static {
		NLS.initializeMessages(StatetMessages.class.getName(), StatetMessages.class);
	}
	private StatetMessages() {}
	
}
