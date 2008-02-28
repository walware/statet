/*******************************************************************************
 * Copyright (c) 2006-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.util;

import de.walware.statet.nico.core.runtime.ToolProcess;


/**
 * Allows access to tool instance. Counterpart of {@link IToolRetargetable}.
 */
public interface IToolProvider {
	
	
	public ToolProcess getTool();
	
	public void addToolRetargetable(IToolRetargetable action);
	
}
