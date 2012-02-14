/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.rj.data.RReference;

import de.walware.statet.r.console.core.IRDataAdapter;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;


public interface ICombinedRDataAdapter extends IRDataAdapter {
	
	public ICombinedRElement evalCombinedStruct(final String command,
			final int options, final int depth, final RElementName name,
			final IProgressMonitor monitor) throws CoreException;
	
	public ICombinedRElement evalCombinedStruct(final RElementName name,
			final int options, final int depth,
			final IProgressMonitor monitor) throws CoreException;
	
	public ICombinedRElement evalCombinedStruct(final RReference reference,
			final int options, final int depth, final RElementName name,
			final IProgressMonitor monitor) throws CoreException;
	
}
