/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico;

import java.util.EnumMap;

import org.eclipse.osgi.util.NLS;

import de.walware.statet.nico.runtime.ToolController.ToolStatus;


public class Messages extends NLS {
	

	public static String Status_Starting_description;
	public static String Status_StartedIdle_description;
	public static String Status_StartedCalculating_description;
	public static String Status_StartedPaused_description;
	public static String Status_Terminated_description;

	public static String SubmitAction_name;
	
	public static String PasteSubmitAction_name;

	public static String SubmitTask_name;
	public static String Submit_error_message;

	public static String LaunchDelegate_error_UnexpectedTermination_message;
	
	public static String Runtime_error_message;

	
	private static EnumMap<ToolStatus, String> fgStatusDescription;
	
	private static void initDefaultStatusDescriptions() {
		
		fgStatusDescription = new EnumMap<ToolStatus, String>(ToolStatus.class);
		fgStatusDescription.put(ToolStatus.STARTING, Messages.Status_Starting_description);
		fgStatusDescription.put(ToolStatus.STARTED_IDLE, Messages.Status_StartedIdle_description);
		fgStatusDescription.put(ToolStatus.STARTED_CALCULATING, Messages.Status_StartedCalculating_description);
		fgStatusDescription.put(ToolStatus.STARTED_PAUSED, Messages.Status_StartedPaused_description);
		fgStatusDescription.put(ToolStatus.TERMINATED, Messages.Status_Terminated_description);
	}
	
	public static String getDefaultStatusDescription(ToolStatus status) {
		
		return fgStatusDescription.get(status);
	}
	
	
	private static final String BUNDLE_NAME = Messages.class.getName();
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
		initDefaultStatusDescriptions();
	}
	
}
