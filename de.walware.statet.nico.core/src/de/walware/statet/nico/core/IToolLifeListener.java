/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core;

import de.walware.statet.nico.core.runtime.ToolProcess;


/**
 * Is notified about any started and terminated NICO tool
 * 
 * @see NicoCore#addToolLifeListener(IToolLifeListener)
 * @see NicoCore#removeToolLifeListener(IToolLifeListener)
 */
public interface IToolLifeListener {
	
	
	public void toolStarted(ToolProcess process);
	
	public void toolTerminated(ToolProcess process);
	
}
