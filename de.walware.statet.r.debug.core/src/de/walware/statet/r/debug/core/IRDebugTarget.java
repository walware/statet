/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.debug.core;

import org.eclipse.debug.core.model.IDebugTarget;

import de.walware.statet.r.console.core.RProcess;


/**
 * Represents the R engine in the Eclipse debug model for R.
 */
public interface IRDebugTarget extends IDebugTarget {
	
	
	@Override
	RProcess getProcess();
	
	@Override
	IRDebugTarget getDebugTarget();
	
}
