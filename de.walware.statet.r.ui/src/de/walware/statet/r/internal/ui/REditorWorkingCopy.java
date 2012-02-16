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

package de.walware.statet.r.internal.ui;

import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ui.GenericEditorWorkspaceSourceUnitWorkingCopy;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.model.RSuModelContainer;
import de.walware.statet.r.core.renv.IREnv;


/**
 * R source unit working copy which can be processed by the model manager.
 */
public class REditorWorkingCopy extends GenericEditorWorkspaceSourceUnitWorkingCopy
		implements IRWorkspaceSourceUnit {
	
	
	private final RSuModelContainer fModel = new RUISuModelContainer(this);
	
	
	public REditorWorkingCopy(final IRWorkspaceSourceUnit from) {
		super(from);
	}
	
	
	@Override
	protected final void register() {
		super.register();
		if (!getModelTypeId().equals(RModel.TYPE_ID)) {
			RCore.getRModelManager().registerDependentUnit(this);
		}
	}
	
	@Override
	protected final void unregister() {
		super.unregister();
		if (!getModelTypeId().equals(RModel.TYPE_ID)) {
			RCore.getRModelManager().deregisterDependentUnit(this);
		}
	}
	
	@Override
	public IRCoreAccess getRCoreAccess() {
		return ((IRSourceUnit) fFrom).getRCoreAccess();
	}
	
	@Override
	public IREnv getREnv() {
		return ((IRSourceUnit) fFrom).getREnv();
	}
	
	@Override
	public void reconcileRModel(final int reconcileLevel, final IProgressMonitor monitor) {
		RCore.getRModelManager().reconcile(fModel, (reconcileLevel | IModelManager.RECONCILER),
				monitor );
	}
	
	@Override
	public AstInfo getAstInfo(final String type, final boolean ensureSync, final IProgressMonitor monitor) {
		if (type == null || type.equals(RModel.TYPE_ID)) {
			return fModel.getAstInfo(ensureSync, monitor);
		}
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
	public Object getAdapter(final Class required) {
		if (RSuModelContainer.class.equals(required)) {
			return fModel;
		}
		return super.getAdapter(required);
	}
	
}
