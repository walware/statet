/*******************************************************************************
 * Copyright (c) 2006-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import de.walware.statet.nico.core.NicoCoreMessages;


public enum ToolStatus {
	
	
	STARTING (NicoCoreMessages.Status_Starting_label, NicoCoreMessages.Status_Starting_info,
			true, false),
	STARTED_IDLING (NicoCoreMessages.Status_StartedIdle_label, NicoCoreMessages.Status_StartedIdle_info,
			false, true),
	STARTED_PROCESSING (NicoCoreMessages.Status_StartedProcessing_label, NicoCoreMessages.Status_StartedProcessing_info,
			true, false),
	STARTED_PAUSED (NicoCoreMessages.Status_StartedPaused_label, NicoCoreMessages.Status_StartedPaused_info,
			false, false),
	STARTED_SUSPENDED (NicoCoreMessages.Status_StartedSuspended_label, NicoCoreMessages.Status_StartedSuspended_info,
			false, true),
	TERMINATED (NicoCoreMessages.Status_Terminated_label,  NicoCoreMessages.Status_Terminated_info,
			false, false);
	
	
	private final String fLabel;
	private final String fMarkedLabel;
	
	private final boolean fIsRunning;
	private final boolean fIsWaiting;
	
	
	ToolStatus(final String label, final String info,
			final boolean isRunning, final boolean isWaiting) {
		fLabel = label;
		fMarkedLabel = info;
		
		fIsRunning = isRunning;
		fIsWaiting = isWaiting;
	}
	
	
	/**
	 * Returns the label of the status.
	 * 
	 * @return the status label
	 */
	public String getLabel() {
		return fLabel;
	}
	
	public String getMarkedLabel() {
		return fMarkedLabel;
	}
	
	/**
	 * Returns if the status indicating that the tool is evaluating.
	 * This is true for STARTING and STARTED_PROCESSING
	 * 
	 * @return if tool is running
	 */
	public boolean isRunning() {
		return fIsRunning;
	}
	
	/**
	 * Returns if the status indicating that the tool is waiting for user input.
	 * This is true for STARTED_IDLING and STARTED_SUSPENDED
	 * 
	 * @return if tool is waiting
	 */
	public boolean isWaiting() {
		return fIsWaiting;
	}
	
}
