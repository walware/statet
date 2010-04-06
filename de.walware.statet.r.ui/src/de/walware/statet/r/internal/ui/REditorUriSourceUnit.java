/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.GenericUriSourceUnit;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.ISourceUnitStateListener;
import de.walware.ecommons.ltk.IWorkingBuffer;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.SourceContent;
import de.walware.ecommons.ltk.SourceDocumentRunnable;
import de.walware.ecommons.ltk.WorkingContext;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ui.FileBufferWorkingBuffer;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.core.model.IManagableRUnit;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.ast.RAstInfo;


public class REditorUriSourceUnit extends GenericUriSourceUnit implements IRSourceUnit, IManagableRUnit {
	
	
	private RAstInfo fAst;
	private IRModelInfo fModelInfo;
	private final Object fModelLock = new Object();
	
	
	public REditorUriSourceUnit(final String id, final IFileStore store, final ISourceUnitStateListener listener) {
		super(id, store, listener);
	}
	
	
	public WorkingContext getWorkingContext() {
		return LTK.EDITOR_CONTEXT;
	}
	
	public String getModelTypeId() {
		return RModel.TYPE_ID;
	}
	
	@Override
	public int getElementType() {
		return IRSourceUnit.R_OTHER_SU;
	}
	
	
	@Override
	protected IWorkingBuffer createWorkingBuffer(final SubMonitor progress) {
		return new FileBufferWorkingBuffer(this);
	}
	
	@Override
	protected void register() {
		super.register();
		RCore.getRModelManager().registerDependentUnit(this);
	}
	
	@Override
	protected void unregister() {
		super.unregister();
		RCore.getRModelManager().deregisterDependentUnit(this);
	}
	
	public void reconcileRModel(final int reconcileLevel, final IProgressMonitor monitor) {
		RCore.getRModelManager().reconcile(this, reconcileLevel, true, monitor);
	}
	
	public AstInfo<? extends IAstNode> getAstInfo(final String type, final boolean ensureSync, final IProgressMonitor monitor) {
		if (type == null || type.equals(RModel.TYPE_ID)) {
			if (ensureSync) {
				RCore.getRModelManager().reconcile(this, IModelManager.AST, false, monitor);
			}
			return fAst;
		}
		return null;
	}
	
	public ISourceUnitModelInfo getModelInfo(final String type, final int syncLevel, final IProgressMonitor monitor) {
		if (type == null || type.equals(RModel.TYPE_ID)) {
			if (syncLevel > IModelManager.NONE) {
				RCore.getRModelManager().reconcile(this, syncLevel, false, monitor);
			}
			return fModelInfo;
		}
		return null;
	}
	
	public void syncExec(final SourceDocumentRunnable runnable) throws InvocationTargetException {
		FileBufferWorkingBuffer.syncExec(runnable);
	}
	
	public IProblemRequestor getProblemRequestor() {
		return null;
	}
	
	public IRCoreAccess getRCoreAccess() {
		return RCore.getWorkbenchAccess();
	}
	
	public RProject getRProject() {
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
