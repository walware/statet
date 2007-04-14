/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import de.walware.statet.nico.core.NicoCoreMessages;


public enum ToolStatus {

	STARTING (NicoCoreMessages.Status_Starting_label),
	STARTED_IDLING (NicoCoreMessages.Status_StartedIdle_label),
	STARTED_PROCESSING (NicoCoreMessages.Status_StartedProcessing_label),
	STARTED_PAUSED (NicoCoreMessages.Status_StartedPaused_label),
//	STARTED_SUSPENDED (NicoCoreMessages.Status_StartedSuspended_label),
//	STARTED_CUSTOM,
	TERMINATED (NicoCoreMessages.Status_Terminated_label);
		
	private String fLabel;
	private String fMarkedLabel;
		
	ToolStatus(String label) {
			
		fLabel = label;
		fMarkedLabel = "<"+label+">"; //$NON-NLS-1$ //$NON-NLS-2$
	}
		
	public String getLabel() {
		
		return fLabel;
	}
		
	public String getMarkedLabel() {
			
		return fMarkedLabel;
	}
}
