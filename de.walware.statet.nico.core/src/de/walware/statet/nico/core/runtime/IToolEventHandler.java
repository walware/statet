/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;


/**
 * Can react on tool events.
 * 
 * Tool events allows automation and the separation of UI.
 */
public interface IToolEventHandler {
	
	
	public static final int OK = 0;
	public static final int CANCEL = -1;
	public static final int YES = 0;
	public static final int NO = 1;
	
	
	public int handle(IToolRunnableControllerAdapter tools, Object contextData);
	
}
