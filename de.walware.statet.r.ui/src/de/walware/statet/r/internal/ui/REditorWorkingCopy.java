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
import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.SourceContent;
import de.walware.ecommons.ltk.ui.GenericEditorWorkspaceSourceUnitWorkingCopy;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IManagableRUnit;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.rsource.ast.RAstInfo;


/**
 * R source unit working copy which can be processed by the model manager.
 */
public class REditorWorkingCopy extends GenericEditorWorkspaceSourceUnitWorkingCopy
		implements IRWorkspaceSourceUnit, IManagableRUnit {
	
	
	private final Object fModelLock = new Object();
	private RAstInfo fAst;
	private IRModelInfo fModelInfo;
	
	
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
		RCore.getRModelManager().reconcile(this, reconcileLevel, true, monitor);
	}
	
	@Override
	public AstInfo getAstInfo(final String type, final boolean ensureSync, final IProgressMonitor monitor) {
		if (type == null || type.equals(RModel.TYPE_ID)) {
			if (ensureSync || fAst == null) {
				RCore.getRModelManager().reconcile(this, IModelManager.AST, false, monitor);
			}
			return fAst;
		}
		return null;
	}
	
	@Override
	public ISourceUnitModelInfo getModelInfo(final String type, final int syncLevel, final IProgressMonitor monitor) {
		if (type == null || type.equals(RModel.TYPE_ID)) {
			if (syncLevel > IModelManager.NONE) {
				RCore.getRModelManager().reconcile(this, syncLevel, false, monitor);
			}
			return getCurrentRModel();
		}
		return null;
	}
	
	
	@Override
	public Object getModelLockObject() {
		return fModelLock;
	}
	
	@Override
	public SourceContent getParseContent(final IProgressMonitor monitor) {
		return getContent(monitor);
	}
	
	@Override
	public void setRAst(final RAstInfo ast) {
		fAst = ast;
	}
	
	@Override
	public RAstInfo getCurrentRAst() {
		return fAst;
	}
	
	@Override
	public void setRModel(final IRModelInfo model) {
		fModelInfo = model;
	}
	
	@Override
	public IRModelInfo getCurrentRModel() {
		return fModelInfo;
	}
	
	@Override
	public IProblemRequestor getProblemRequestor() {
		return (IProblemRequestor) RUIPlugin.getDefault().getRDocumentProvider().getAnnotationModel(this);
	}
	
}
