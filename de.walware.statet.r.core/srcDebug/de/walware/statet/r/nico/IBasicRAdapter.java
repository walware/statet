/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License v2.0
 * or newer, which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;


/**
 * Interface to access R by a ToolRunnable.
 */
public interface IBasicRAdapter extends
		IToolRunnableControllerAdapter {

	
	/**
	 * This flag indicates that the current input is incomplete.
	 * 
	 * The prompt have to be a instance of {@link IncompleteInputPrompt<RunnableAdapterType, WorkspaceType>}.
	 */
	public static final int META_PROMPT_INCOMPLETE_INPUT = 1 << 8;

}
