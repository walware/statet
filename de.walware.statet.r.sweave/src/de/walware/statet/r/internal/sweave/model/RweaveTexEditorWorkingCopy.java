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

package de.walware.statet.r.internal.sweave.model;

import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ui.GenericEditorWorkspaceSourceUnitWorkingCopy;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.renv.IREnv;


public class RweaveTexEditorWorkingCopy extends GenericEditorWorkspaceSourceUnitWorkingCopy
		implements IRWorkspaceSourceUnit {
	
	
	private final RweaveTexSuModelContainer fModel = new RweaveTexSuModelContainer(this);
	
	
	public RweaveTexEditorWorkingCopy(final IRWorkspaceSourceUnit from) {
		super(from);
	}
	
	
	@Override
	public IRCoreAccess getRCoreAccess() {
		return ((IRSourceUnit) fFrom).getRCoreAccess();
	}
	
	@Override
	public IREnv getREnv() {
		return RCore.getREnvManager().getDefault();
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
		return fModel.getAstInfo(ensureSync, monitor);
	}
	
	@Override
	public ISourceUnitModelInfo getModelInfo(final String type, final int syncLevel, final IProgressMonitor monitor) {
		return fModel.getModelInfo(syncLevel, monitor);
	}
	
	@Override
	public Object getAdapter(final Class required) {
		return super.getAdapter(required);
	}
	
	@Override
	public void reconcileRModel(final int reconcileLevel, final IProgressMonitor monitor) {
	}
	
	public void reconcileR(final IProgressMonitor monitor) {
		fModel.reconcileAst(0, monitor);
	}
	
}
