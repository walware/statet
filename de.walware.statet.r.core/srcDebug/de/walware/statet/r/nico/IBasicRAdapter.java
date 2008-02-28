/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import org.eclipse.core.runtime.IAdaptable;

import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;


/**
 * Interface to access R by a ToolRunnable.
 */
public interface IBasicRAdapter extends
		IToolRunnableControllerAdapter, IAdaptable {
	
	
//	public void voidEval(String );
	
//	public REXP eval(String );
	
}
