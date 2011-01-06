/*******************************************************************************
 * Copyright (c) 2006-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;


/**
 * Interface to access R by a ToolRunnable.
 */
public interface IRBasicAdapter extends
		IToolRunnableControllerAdapter, IAdaptable {
	
	
	/**
	 * This flag indicates that the current input is incomplete.
	 * 
	 * The prompt have to be a instance of {@link IncompleteInputPrompt<RunnableAdapterType, WorkspaceType>}.
	 */
	int META_PROMPT_INCOMPLETE_INPUT = 1 << 8;
	
	
	ToolProcess<? extends RWorkspace> getProcess();
	
	ToolController<? extends RWorkspace> getController();
	
	/**
	 * Quits R.
	 * <code>q()</code>
	 */
	void quit(final IProgressMonitor monitor) throws CoreException;
	
	void briefAboutChange(int o);
	void briefAboutChange(Object changed, int o);
	int getBriefedChanges();
	
}
