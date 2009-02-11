/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core;

import org.eclipse.osgi.util.NLS;


/**
 * Public/shared strings of Nico Core.
 */
public class NicoCoreMessages {
	
	
	public static String Status_Starting_label;
	public static String Status_StartedIdle_label;
	public static String Status_StartedProcessing_label;
	public static String Status_StartedPaused_label;
	public static String Status_StartedSuspended_label;
	public static String Status_Terminated_label;
	
	public static String LoadHistoryJob_label;
	public static String SaveHistoryJob_label;
	public static String SubmitTask_label;
	
	
	static {
		NLS.initializeMessages(NicoCoreMessages.class.getName(), NicoCoreMessages.class);
	}
	private NicoCoreMessages() {}
	
}
