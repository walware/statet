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

package de.walware.statet.nico.ui;

import org.eclipse.osgi.util.NLS;


public class NicoUIMessages extends NLS {
	
	
	public static String SubmitAction_name;
	public static String PasteSubmitAction_name;
	public static String SubmitTask_name;
	public static String Submit_error_message;
	
	public static String LoadHistory_title;
	public static String LoadHistoryAction_name;
	public static String LoadHistoryAction_tooltip;
	public static String SaveHistory_title;
	public static String SaveHistoryAction_name;
	public static String SaveHistoryAction_tooltip;
	
	
	static {
		NLS.initializeMessages(NicoUIMessages.class.getName(), NicoUIMessages.class);
	}
	private NicoUIMessages() {}
	
}
