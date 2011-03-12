/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.SourceContent;

import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.core.model.IManagableRUnit;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.RAstInfo;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 * Source unit implementation for R script files in workspace ("default R file").
 */
public final class RSourceUnit extends RResourceUnit
		implements IRWorkspaceSourceUnit, IManagableRUnit {
	
	
	private final Object fModelLock = new Object();
	private IRModelInfo fModelInfo;
	
	
	public RSourceUnit(final String id, final IFile file) {
		super(id, file);
	}
	
	
	@Override
	protected void unregister() {
		super.unregister();
		synchronized (fModelLock) {
			fModelInfo = null;
		}
	}
	
	public String getModelTypeId() {
		return RModel.TYPE_ID;
	}
	
	@Override
	public int getElementType() {
		return IRSourceUnit.R_WORKSPACE_SU;
	}
	
	
	@Override
	public RAstInfo getAstInfo(final String type, final boolean ensureSync, final IProgressMonitor monitor) {
		final RAstInfo ast = getCurrentRAst();
		final long stamp = getResource().getModificationStamp();
		if (ast != null && ast.stamp == stamp) {
			return ast;
		}
		// TODO ask saved
		return null;
	}
	
	@Override
	public ISourceUnitModelInfo getModelInfo(final String type, final int syncLevel, final IProgressMonitor monitor) {
		if (type == null || type.equals(RModel.TYPE_ID)) {
			final IRModelInfo model = getCurrentRModel();
			final long stamp = getResource().getModificationStamp();
			if (model != null && model.getStamp() == stamp) {
				return model;
			}
			final RCorePlugin plugin = RCorePlugin.getDefault();
			if (plugin != null && syncLevel > IModelManager.NONE) {
				return plugin.getRModelManager().reconcile2(this, syncLevel, false, monitor);
			}
		}
		return null;
	}
	
	public synchronized void reconcileRModel(final int reconcileLevel, final IProgressMonitor monitor) {
	}
	
	
	public Object getModelLockObject() {
		return fModelLock;
	}
	
	public SourceContent getParseContent(final IProgressMonitor monitor) {
		return getContent(monitor);
	}
	
	public void setRAst(final RAstInfo ast) {
	}
	
	public RAstInfo getCurrentRAst() {
		final IRModelInfo model = getCurrentRModel();
		if (model != null) {
			return model.getAst();
		}
		return null;
	}
	
	public void setRModel(final IRModelInfo model) {
//		fModelInfo = model;
	}
	
	public IRModelInfo getCurrentRModel() {
		return fModelInfo;
	}
	
}
