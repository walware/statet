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

package de.walware.statet.nico.ui;

import org.eclipse.osgi.util.NLS;


public class NicoMessages extends NLS {
	

	public static String Status_Starting_description;
	public static String Status_StartedIdle_description;
	public static String Status_StartedCalculating_description;
	public static String Status_StartedPaused_description;
	public static String Status_Terminated_description;

	public static String SubmitAction_name;
	
	public static String PasteSubmitAction_name;
	
	public static String PauseAction_name;
	public static String PasteSubmitAction_tooltip;

	public static String SubmitTask_name;
	public static String Submit_error_message;

	public static String Runtime_error_message;
	
	
	private static final String BUNDLE_NAME = NicoMessages.class.getName();
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, NicoMessages.class);
	}
	
}
