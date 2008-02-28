/*******************************************************************************
 * Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
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


public enum SubmitType {
	
	/** Console interaction by user */
	CONSOLE,
	/** Submit from editor and other code based views */
	EDITOR,
	/** Submit from tools (GUI etc.) */
	TOOLS,
	/** Others, e.g. by controller */
	OTHER,
//	/** Use only, if you know, what you do */
//	INTERNAL,
	;
	
	
	public static EnumSet<SubmitType> getDefaultSet() {
		
		return EnumSet.range(CONSOLE, OTHER);
	}
	
}