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

package de.walware.statet.r.nico;

import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.ToolWorkspace;


public final class RTool {
	
	/**
	 * Feature set for R, providing basic methods independent 
	 * of the implementation of integration in the controller.
	 * <p>
	 * Feature Set:
	 * <ul>
	 * <li>{@link IToolRunnableControllerAdapter} implements {@link IRBasicAdapter}</li>
	 * <li>{@link ToolWorkspace} instance of {@link RWorkspace}</li>
	 * </ul>
	 */
	public static final String R_BASIC_FEATURESET_ID = "de.walware.statet.r.basic"; //$NON-NLS-1$
	
	
	/**
	 * This flag indicates that the current input is incomplete.
	 * 
	 * The prompt have to be a instance of {@link IncompleteInputPrompt<RunnableAdapterType, WorkspaceType>}.
	 */
	public static final int META_PROMPT_INCOMPLETE_INPUT = 1 << 8;
	
	
	public static final String R_DATA_FEATURESET_ID = "de.walware.statet.r.data";
	
}
