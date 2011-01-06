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

package de.walware.statet.r.core.model;

import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.GenericSourceUnitWorkingCopy;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ISourceUnitStateListener;
import de.walware.ecommons.ltk.SourceContent;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.rsource.ast.RAstInfo;
import de.walware.statet.r.core.rsource.ast.SourceComponent;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 * R source unit working copy which can be processed by the model manager.
 */
public abstract class RManagedWorkingCopy extends GenericSourceUnitWorkingCopy implements IRSourceUnit, IManagableRUnit {
	
	
	private final Object fModelLock = new Object();
	private RAstInfo fAst;
	private IRModelInfo fModelInfo;
	
	
	public RManagedWorkingCopy(final IRSourceUnit from, final ISourceUnitStateListener listener) {
		super(from, listener);
	}
	
	
	@Override
	protected final void register() {
		super.register();
		if (!getModelTypeId().equals(RModel.TYPE_ID)) {
			RCorePlugin.getDefault().getRModelManager().registerDependentUnit(this);
		}
	}
	
	@Override
	protected final void unregister() {
		super.unregister();
		if (!getModelTypeId().equals(RModel.TYPE_ID)) {
			RCorePlugin.getDefault().getRModelManager().deregisterDependentUnit(this);
		}
	}
	
	public IRCoreAccess getRCoreAccess() {
		return ((IRSourceUnit) fFrom).getRCoreAccess();
	}
	
	public IREnv getREnv() {
		return ((IRSourceUnit) fFrom).getREnv();
	}
	
	@Override
	public IRSourceUnit getUnderlyingUnit() {
		return (IRSourceUnit) super.getUnderlyingUnit();
	}
	
	public void reconcileRModel(final int reconcileLevel, final IProgressMonitor monitor) {
		RCorePlugin.getDefault().getRModelManager().reconcile(this, reconcileLevel, true, monitor);
	}
	
	@Override
	public AstInfo<SourceComponent> getAstInfo(final String type, final boolean ensureSync, final IProgressMonitor monitor) {
		if (type == null || type.equals(RModel.TYPE_ID)) {
			if (ensureSync || fAst == null) {
				RCorePlugin.getDefault().getRModelManager().reconcile(this, IModelManager.AST, false, monitor);
			}
			return fAst;
		}
		return null;
	}
	
	@Override
	public ISourceUnitModelInfo getModelInfo(final String type, final int syncLevel, final IProgressMonitor monitor) {
		if (type == null || type.equals(RModel.TYPE_ID)) {
			final RCorePlugin plugin = RCorePlugin.getDefault();
			if (plugin != null && syncLevel > IModelManager.NONE) {
				plugin.getRModelManager().reconcile(this, syncLevel, false, monitor);
			}
			return getCurrentRModel();
		}
		return null;
	}
	
	
	public Object getModelLockObject() {
		return fModelLock;
	}
	
	public SourceContent getParseContent(final IProgressMonitor monitor) {
		return getContent(monitor);
	}
	
	public void setRAst(final RAstInfo ast) {
		fAst = ast;
	}
	
	public RAstInfo getCurrentRAst() {
		return fAst;
	}
	
	public void setRModel(final IRModelInfo model) {
		fModelInfo = model;
	}
	
	public IRModelInfo getCurrentRModel() {
		return fModelInfo;
	}
	
}
