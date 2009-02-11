/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * Optional interface indicating, that the controller requires explicite sync
 * if necessary.
 */
public interface IRequireSynch {
	
	
	/**
	 * @param monitor
	 * @return pattern (optional), matching possible output of synch command
	 * @throws CoreException
	 */
	public Pattern synch(IProgressMonitor monitor) throws CoreException;
	
}
