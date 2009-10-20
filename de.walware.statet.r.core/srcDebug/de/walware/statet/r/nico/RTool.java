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
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolWorkspace;


public final class RTool {
	
	/**
	 * Tool type of R instances.
	 * 
	 * @see ToolProcess#getMainType()
	 */
	public static final String TYPE = "R"; //$NON-NLS-1$
	
	
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
	 * Feature set for R, providing methods to work directly with R data
	 * 
	 * Data are exchanged as RObject by the RJ data library. Operations usually 
	 * doesn't appears in the console output.
	 * <p>
	 * Feature Set:
	 * <ul>
	 * <li>{@link IToolRunnableControllerAdapter} implements {@link IRDataAdapter}</li>
	 * </ul>
	 */
	public static final String R_DATA_FEATURESET_ID = "de.walware.statet.r.data";
	
}
