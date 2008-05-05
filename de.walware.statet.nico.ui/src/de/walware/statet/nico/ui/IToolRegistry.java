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

package de.walware.statet.nico.ui;

import org.eclipse.ui.IWorkbenchPage;

import de.walware.statet.nico.core.runtime.ToolProcess;


/**
 * 
 */
public interface IToolRegistry {
	
	
	public void addListener(IToolRegistryListener listener, IWorkbenchPage page);
	
	public void removeListener(IToolRegistryListener listener);
	
	
	/**
	 * 
	 * @return never <code>null</code>, but the fields can be <code>null</code>.
	 */
	public ToolSessionUIData getActiveToolSession(IWorkbenchPage page);
	
	public IWorkbenchPage findWorkbenchPage(ToolProcess process);
	
}
