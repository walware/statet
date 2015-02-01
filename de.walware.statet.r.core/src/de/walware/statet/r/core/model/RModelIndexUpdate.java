/*=============================================================================#
 # Copyright (c) 2014-2015 Stephan Wahlbrink (WalWare.de) and others.
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.core.impl.GenericResourceSourceUnit;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.sourcemodel.RModelIndexOrder;
import de.walware.statet.r.internal.core.sourcemodel.RModelManager;


public class RModelIndexUpdate extends RModelIndexOrder {
	
	
	public RModelIndexUpdate(final IRProject rProject,
			final List<String> modelTypeIds, final boolean isFullBuild) {
		super(rProject, modelTypeIds, isFullBuild);
	}
	
	
	public void update(final IRSourceUnit sourceUnit, final IRModelInfo model) {
		final Result result= createResult(sourceUnit, model);
		if (result != null) {
			this.updated.add(result);
		}
	}
	
	public void remove(final IRSourceUnit sourceUnit) {
		if (!this.isFullBuild) {
			this.removed.add(sourceUnit.getId());
		}
	}
	
	public void remove(final IFile file) {
		if (!this.isFullBuild) {
			this.removed.add(GenericResourceSourceUnit.createResourceId(file));
		}
	}
	
	public void submit(final IProgressMonitor monitor) throws CoreException {
		final RModelManager rManager= RCorePlugin.getDefault().getRModelManager();
		rManager.getIndex().update(this, monitor);
	}
	
}
