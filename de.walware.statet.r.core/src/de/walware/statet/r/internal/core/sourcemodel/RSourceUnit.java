/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.sourcemodel;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;

import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.model.RSuModelContainer;


/**
 * Source unit implementation for R script files in workspace ("default R file").
 */
public final class RSourceUnit extends RResourceUnit implements IRWorkspaceSourceUnit {
	
	
	private final RSuModelContainer fModel = new RSuModelContainer(this);
	
	
	public RSourceUnit(final String id, final IFile file) {
		super(id, file);
	}
	
	
	@Override
	protected void unregister() {
		super.unregister();
		synchronized (fModel) {
			fModel.clear();
		}
	}
	
	@Override
	public String getModelTypeId() {
		return RModel.TYPE_ID;
	}
	
	@Override
	public String getContentTypeId() {
		return IRSourceUnit.R_CONTENT;
	}
	
	@Override
	public int getElementType() {
		return IRSourceUnit.R_WORKSPACE_SU;
	}
	
	
	@Override
	public AstInfo getAstInfo(final String type, final boolean ensureSync, final IProgressMonitor monitor) {
		final AstInfo ast = fModel.getCurrentAst();
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
			return fModel.getModelInfo(syncLevel, monitor);
		}
		return null;
	}
	
	@Override
	public synchronized void reconcileRModel(final int reconcileLevel, final IProgressMonitor monitor) {
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (RSuModelContainer.class.equals(required)) {
			return fModel;
		}
		return super.getAdapter(required);
	}
	
}
