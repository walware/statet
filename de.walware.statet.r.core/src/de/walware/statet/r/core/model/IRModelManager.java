/*******************************************************************************
 * Copyright (c) 2009-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.model;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;

import de.walware.statet.r.core.RProject;


public interface IRModelManager extends IModelManager {
	
	
	IRFrame getProjectFrame(RProject project) throws CoreException;
	
	IRModelInfo reconcile(IRSourceUnit su, ISourceUnitModelInfo modelInfo,
			List<? extends RChunkElement> chunks,
			int level, IProgressMonitor monitor);
	
	List<String> findReferencingSourceUnits(IProject project, RElementName name) throws CoreException;
	
}
