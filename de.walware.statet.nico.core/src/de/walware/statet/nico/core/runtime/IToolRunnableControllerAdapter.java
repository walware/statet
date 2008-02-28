/*******************************************************************************
 * Copyright (c) 2006-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * This is the interface of the controller to the runnables.
 * The runnable gets the adapter with the call of its run method.
 * 
 * It is intended to extends this interface for more particular
 * needs. The concrete interface can be specified in implementations
 * of ToolController and IToolRunnable.
 * 
 * For implementations:
 * The lifecycle is the same as the ToolController (not ToolProcess!).
 * The methods should only be used in the tool lifecycle thread.
 */
public interface IToolRunnableControllerAdapter {
	
	
	public static final int META_NONE = 0;
	public static final int META_HISTORY_DONTADD = 1 << 0;
	public static final int META_PROMPT_DEFAULT = 1 << 1;
	
	
	public ToolController getController();
	
	public ToolWorkspace getWorkspaceData();
	
	public void submitToConsole(String input, IProgressMonitor monitor)
			throws CoreException, InterruptedException;
	
}
