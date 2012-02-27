/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.core.impl.GenericResourceSourceUnit;

import de.walware.docmlet.tex.core.TexCore;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.sweave.ILtxRweaveSourceUnit;
import de.walware.statet.r.sweave.ITexRweaveCoreAccess;
import de.walware.statet.r.sweave.Sweave;
import de.walware.statet.r.sweave.TexRweaveCoreAccess;


public class LtxRweaveDocUnit extends GenericResourceSourceUnit 
		implements ILtxRweaveSourceUnit, IRWorkspaceSourceUnit {
	
	
	private ITexRweaveCoreAccess fCoreAccess;
	
	
	public LtxRweaveDocUnit(final String id, final IFile file) {
		super(id, file);
	}
	
	
	@Override
	public String getModelTypeId() {
		return Sweave.LTX_R_MODEL_TYPE_ID;
	}
	
	
	@Override
	public ITexRweaveCoreAccess getRCoreAccess() {
		ITexRweaveCoreAccess coreAccess = fCoreAccess;
		if (coreAccess == null) {
			final RProject rProject = RProject.getRProject(getResource().getProject());
			coreAccess = new TexRweaveCoreAccess(TexCore.getWorkbenchAccess(),
					(rProject != null) ? rProject : RCore.getWorkbenchAccess() );
			synchronized (this) {
				if (isConnected()) {
					fCoreAccess = coreAccess;
				}
			}
		}
		return coreAccess;
	}
	
	@Override
	public IREnv getREnv() {
		return RCore.getREnvManager().getDefault();
	}
	
	@Override
	public ITexRweaveCoreAccess getTexCoreAccess() {
		return getRCoreAccess();
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
		fCoreAccess = null;
		final IModelManager rManager = RCore.getRModelManager();
		if (rManager != null) {
			rManager.deregisterDependentUnit(this);
		}
		super.unregister();
	}
	
	@Override
	public void reconcileRModel(final int reconcileLevel, final IProgressMonitor monitor) {
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (required.equals(IRCoreAccess.class)) {
			return getRCoreAccess();
		}
		return super.getAdapter(required);
	}
	
}
