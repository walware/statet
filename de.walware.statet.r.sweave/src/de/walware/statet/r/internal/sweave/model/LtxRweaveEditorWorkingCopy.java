/*******************************************************************************
 * Copyright (c) 2007-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.model;

import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ui.GenericEditorWorkspaceSourceUnitWorkingCopy;

import de.walware.docmlet.tex.core.model.LtxSuModelContainer;
import de.walware.docmlet.tex.core.model.TexModel;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.sweave.ILtxRweaveSourceUnit;
import de.walware.statet.r.sweave.ITexRweaveCoreAccess;
import de.walware.statet.r.sweave.Sweave;


public class LtxRweaveEditorWorkingCopy extends GenericEditorWorkspaceSourceUnitWorkingCopy
		implements ILtxRweaveSourceUnit, IRWorkspaceSourceUnit {
	
	
	private final LtxRweaveSuModelContainer fModel = new LtxRweaveSuModelContainer(this);
	
	
	public LtxRweaveEditorWorkingCopy(final IRWorkspaceSourceUnit from) {
		super(from);
	}
	
	
	@Override
	public ITexRweaveCoreAccess getRCoreAccess() {
		return ((ILtxRweaveSourceUnit) fFrom).getRCoreAccess();
	}
	
	@Override
	public IREnv getREnv() {
		return RCore.getREnvManager().getDefault();
	}
	
	@Override
	public ITexRweaveCoreAccess getTexCoreAccess() {
		return ((ILtxRweaveSourceUnit) fFrom).getTexCoreAccess();
	}
	
	
	@Override
	protected void register() {
		super.register();
		final IModelManager rManager = RCore.getRModelManager();
		if (rManager != null) {
			rManager.deregisterDependentUnit(this);
		}
	}
	
	@Override
	protected void unregister() {
		final IModelManager rManager = RCore.getRModelManager();
		if (rManager != null) {
			rManager.deregisterDependentUnit(this);
		}
		super.unregister();
	}
	
	
	@Override
	public AstInfo getAstInfo(final String type, final boolean ensureSync, final IProgressMonitor monitor) {
		if (type == null || type == TexModel.LTX_TYPE_ID || type == Sweave.LTX_R_MODEL_TYPE_ID) {
			return fModel.getAstInfo(ensureSync, monitor);
		}
		if (type == RModel.TYPE_ID) {
			return null;
		}
		return null;
	}
	
	@Override
	public ISourceUnitModelInfo getModelInfo(final String type, final int syncLevel, final IProgressMonitor monitor) {
		if (type == null || type == TexModel.LTX_TYPE_ID || type == Sweave.LTX_R_MODEL_TYPE_ID) {
			return fModel.getModelInfo(syncLevel, monitor);
		}
		if (type == RModel.TYPE_ID) {
			return fModel.getRModelInfo(syncLevel, monitor);
		}
		return null;
	}
	
	@Override
	public Object getAdapter(final Class required) {
		if (LtxSuModelContainer.class.equals(required)) {
			return fModel;
		}
		return super.getAdapter(required);
	}
	
	@Override
	public void reconcileRModel(final int reconcileLevel, final IProgressMonitor monitor) {
	}
	
}
