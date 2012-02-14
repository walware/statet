/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui.page;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ISynchronizable;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.SourceContent;
import de.walware.ecommons.ltk.SourceDocumentRunnable;

import de.walware.statet.nico.ui.console.GenericConsoleSourceUnit;
import de.walware.statet.nico.ui.console.InputDocument;

import de.walware.statet.r.console.ui.RConsole;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IManagableRUnit;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.model.SpecialParseContent;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.rsource.ast.RAstInfo;


public class RConsoleSourceUnit extends GenericConsoleSourceUnit 
		implements IRSourceUnit, IManagableRUnit {
	
	
	private final RConsole fRConsole;
	
	private final Object fModelLock = new Object();
	private RAstInfo fAst;
	private IRModelInfo fModelInfo;
	
	
	public RConsoleSourceUnit(final RConsolePage page, final InputDocument document) {
		super(page.toString(), document);
		fRConsole = page.getConsole();
	}
	
	
	@Override
	public String getModelTypeId() {
		return RModel.TYPE_ID;
	}
	
	
	@Override
	public void reconcileRModel(final int reconcileLevel, final IProgressMonitor monitor) {
		RCore.getRModelManager().reconcile(this, reconcileLevel, true, monitor);
	}
	
	@Override
	public AstInfo getAstInfo(final String type, final boolean ensureSync, final IProgressMonitor monitor) {
		if (type == null || type.equals(RModel.TYPE_ID)) {
			if (ensureSync) {
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
			return fModelInfo;
		}
		return null;
	}
	
	@Override
	public void syncExec(final SourceDocumentRunnable runnable) throws InvocationTargetException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IProblemRequestor getProblemRequestor() {
		return null;
	}
	
	@Override
	public IRCoreAccess getRCoreAccess() {
		return fRConsole;
	}
	
	@Override
	public IREnv getREnv() {
		final IREnv rEnv = (IREnv) fRConsole.getProcess().getAdapter(IREnv.class);
		return (rEnv != null) ? rEnv : RCore.getREnvManager().getDefault();
	}
	
	@Override
	public boolean hasModelChildren(final Filter filter) {
		return false;
	}
	
	@Override
	public List<? extends IModelElement> getModelChildren(final Filter filter) {
		return null;
	}
	
	
	@Override
	public Object getModelLockObject() {
		return fModelLock;
	}
	
	@Override
	public SourceContent getParseContent(final IProgressMonitor monitor) {
		Object lock = null;
		if (fDocument instanceof ISynchronizable) {
			lock = ((ISynchronizable) fDocument).getLockObject();
		}
		if (lock == null) {
			lock = new Object();
		}
		synchronized (lock) {
			return new SpecialParseContent(
					fDocument.getModificationStamp(),
					fDocument.getMasterDocument().get(),
					-fDocument.getOffsetInMasterDocument() );
		}
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
	
}
