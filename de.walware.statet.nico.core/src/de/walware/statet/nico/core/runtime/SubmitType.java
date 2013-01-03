/*******************************************************************************
 * Copyright (c) 2005-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import java.util.EnumSet;

import de.walware.statet.nico.internal.core.Messages;


public enum SubmitType {
	
	/** Console interaction by user */
	CONSOLE (Messages.SubmitType_Console_label),
	/** Submit from editor and other code based views */
	EDITOR (Messages.SubmitType_Editor_label),
	/** Submit from tools (like the object browser) */
	TOOLS (Messages.SubmitType_Tools_label),
	/** Others, e.g. from controller */
	OTHER (Messages.SubmitType_Other_label),
	;
	
	
	public static EnumSet<SubmitType> getDefaultSet() {
		return EnumSet.range(CONSOLE, OTHER);
	}
	
	
	private String fLabel;
	
	
	SubmitType(final String label) {
		fLabel = label;
	}
	
	
	public String getLabel() {
		return fLabel;
	}
	
}
