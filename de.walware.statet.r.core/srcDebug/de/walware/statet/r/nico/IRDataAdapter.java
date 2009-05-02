/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;


/**
 * 
 */
public interface IRDataAdapter extends IRBasicAdapter {
	
	
	public void evalVoid(String command, IProgressMonitor monitor) throws CoreException;
	
	public RObject evalData(String command, IProgressMonitor monitor) throws CoreException;
	public RObject evalData(String command, String factoryId, int options, int depth, IProgressMonitor monitor) throws CoreException;
	
	public RObject evalData(RReference reference, IProgressMonitor monitor) throws CoreException;
	public RObject evalData(RReference reference, String factoryId, int options, int depth, IProgressMonitor monitor) throws CoreException;
	
}
