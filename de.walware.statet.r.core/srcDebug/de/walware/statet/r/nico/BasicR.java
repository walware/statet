/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.ToolWorkspace;


/**
 * Feature set for R, providing basic methods independent 
 * of the implementation of integration in the controller.
 * <p>
 * Feature Set:
 * <ul>
 * <li>{@link IToolRunnableControllerAdapter} implements {@link IBasicRAdapter}</li>
 * <li>{@link ToolWorkspace} instance of {@link RWorkspace}</li>
 * </ul>
 */
public final class BasicR {

	public static final String FEATURESET_ID = "de.walware.statet.r.basic";
	
	
	/**
	 * This flag indicates that the current input is incomplete.
	 * 
	 * The prompt have to be a instance of {@link IncompleteInputPrompt<RunnableAdapterType, WorkspaceType>}.
	 */
	public static final int META_PROMPT_INCOMPLETE_INPUT = 1 << 8;
	
}
