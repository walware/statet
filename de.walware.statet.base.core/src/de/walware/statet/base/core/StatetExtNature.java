/*******************************************************************************
 * Copyright (c) 2005-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import de.walware.statet.base.internal.core.BaseCorePlugin;


/**
 * Project to extend for a special StatET project nature. 
 */
public abstract class StatetExtNature implements IProjectNature {
	
	
	protected IProject fProject;
	
	protected StatetProject fStatetNature;
	
	
	public StatetExtNature() {
		super();
	}
	
	
/*-- IProjectNature ----------------------------------------------------------*/
	
	@Override
	public void setProject(final IProject project) {
		fProject = project;
	}
	
	@Override
	public IProject getProject() {
		return fProject;
	}
	
	
	public StatetProject getStatetProject() {
		if (fStatetNature == null) {
			try {
				fStatetNature = (StatetProject) fProject.getNature(StatetProject.NATURE_ID);
			}
			catch (final CoreException e) {
				BaseCorePlugin.logError(-1, "An error occurred when accessing StatET project nature this nature depends on.", e); //$NON-NLS-1$
			}
		}
		return fStatetNature;
	}
	
}
