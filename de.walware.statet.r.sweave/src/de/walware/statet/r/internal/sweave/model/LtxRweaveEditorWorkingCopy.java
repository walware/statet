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

package de.walware.statet.r.internal.sweave.model;

import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ui.GenericEditorWorkspaceSourceUnitWorkingCopy2;

import de.walware.docmlet.tex.core.model.ILtxWorkspaceSourceUnit;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.sweave.ILtxRweaveSourceUnit;
import de.walware.statet.r.sweave.ITexRweaveCoreAccess;


public class LtxRweaveEditorWorkingCopy
		extends GenericEditorWorkspaceSourceUnitWorkingCopy2<LtxRweaveSuModelContainer>
		implements ILtxRweaveSourceUnit, ILtxWorkspaceSourceUnit, IRWorkspaceSourceUnit {
	
	
	public LtxRweaveEditorWorkingCopy(final IRWorkspaceSourceUnit from) {
		super(from);
	}
	
	@Override
	protected LtxRweaveSuModelContainer createModelContainer() {
		return new LtxRweaveSuModelContainer(this);
	}
	
	
	@Override
	public ITexRweaveCoreAccess getRCoreAccess() {
		return ((ILtxRweaveSourceUnit) getUnderlyingUnit()).getRCoreAccess();
	}
	
	@Override
	public IREnv getREnv() {
		return RCore.getREnvManager().getDefault();
	}
	
	@Override
	public ITexRweaveCoreAccess getTexCoreAccess() {
		return ((ILtxRweaveSourceUnit) getUnderlyingUnit()).getTexCoreAccess();
	}
	
	
	@Override
	protected void register() {
		super.register();
		final IModelManager rManager= RCore.getRModelManager();
		if (rManager != null) {
			rManager.deregisterDependentUnit(this);
		}
	}
	
	@Override
	protected void unregister() {
		final IModelManager rManager= RCore.getRModelManager();
		if (rManager != null) {
			rManager.deregisterDependentUnit(this);
		}
		super.unregister();
	}
	
	
	@Override
	public ISourceUnitModelInfo getModelInfo(final String type, final int syncLevel, final IProgressMonitor monitor) {
		if (type == RModel.TYPE_ID) {
			return getModelContainer().getRModelInfo(syncLevel, monitor);
		}
		return super.getModelInfo(type, syncLevel, monitor);
	}
	
	@Override
	public void reconcileRModel(final int reconcileLevel, final IProgressMonitor monitor) {
	}
	
}
