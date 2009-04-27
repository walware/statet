/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
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
 * The tool registry tracks activation of tools. One Eclipse workbench page has
 * one active tool session.
 */
public interface IToolRegistry {
	
	/**
	 * 
	 * @param listener the listener to register
	 * @param page the workbench page or <code>null</code> if register to all pages
	 */
	public void addListener(IToolRegistryListener listener, IWorkbenchPage page);
	
	/**
	 * 
	 * @param listener the listener to remove
	 */
	public void removeListener(IToolRegistryListener listener);
	
	
	/**
	 * 
	 * @return never <code>null</code>, but the fields can be <code>null</code>.
	 */
	public ToolSessionUIData getActiveToolSession(IWorkbenchPage page);
	
	/**
	 * 
	 * @return best workbench page, never <code>null</code>
	 */
	public IWorkbenchPage findWorkbenchPage(ToolProcess process);
	
}
