/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;


/**
 * Nature of R package projects
 */
public class RPkgProject implements IProjectNature {
	
	
	private IProject project;
	
	
	public RPkgProject() {
	}
	
	
	@Override
	public void setProject(final IProject project) {
		this.project= project;
	}
	
	@Override
	public final IProject getProject() {
		return this.project;
	}
	
	@Override
	public void configure() throws CoreException {
	}
	
	@Override
	public void deconfigure() throws CoreException {
	}
	
}
