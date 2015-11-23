/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.model;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.core.model.ISourceUnitModelInfo;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.rsource.ast.SourceComponent;


public interface IRModelManager extends IModelManager {
	
	
	/**
	 * Returns the frame for the project scope containing all main element definition in the
	 * project.
	 * 
	 * @param rProject the R project
	 * @return the R frame
	 * @throws CoreException
	 */
	IRFrame getProjectFrame(IRProject rProject) throws CoreException;
	
	/**
	 * Returns the package names of R package projects in the workspace.
	 * 
	 * @return set with all package names
	 */
	Set<String> getPkgNames();
	
	IRFrame getPkgProjectFrame(String pkgName) throws CoreException;
	
	
	IRModelInfo reconcile(IRSourceUnit su, ISourceUnitModelInfo modelInfo,
			List<? extends RChunkElement> chunks, List<? extends SourceComponent> inlineNodes,
			int level, IProgressMonitor monitor);
	
	/**
	 * Find source units with references to the specified name in the specified project.
	 * 
	 * Note: The returned source units are already {@link ISourceUnit#connect(IProgressMonitor) connected}.
	 * The caller is responsible to disconnect, if they are no longer required.
	 * 
	 * @param rProject the R project
	 * @param name the name of the R element
	 * @param monitor
	 * @return list of referencing source units
	 * @throws CoreException
	 */
	List<ISourceUnit> findReferencingSourceUnits(IRProject rProject, RElementName name,
			IProgressMonitor monitor) throws CoreException;
	
	
}
