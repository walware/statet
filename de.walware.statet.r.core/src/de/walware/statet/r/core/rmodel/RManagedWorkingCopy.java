/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rmodel;

import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.eclipsecommons.ltk.AstInfo;

import de.walware.statet.r.core.RProject;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 * 
 */
public abstract class RManagedWorkingCopy extends RWorkingCopy implements IRSourceUnit, IManagableRUnit {
	
	
	private AstInfo<RAstNode> fAst;
	private final Object fAstLock = new Object();
	
	
	public RManagedWorkingCopy(final IRSourceUnit from) {
		super(from);
	}
	
	
	public RProject getRProject() {
		return ((IRSourceUnit) fFrom).getRProject();
	}
	
	@Override
	public IRSourceUnit getUnderlyingUnit() {
		return (IRSourceUnit) super.getUnderlyingUnit();
	}
	
	public void reconcile(final int reconcileLevel, final IProgressMonitor monitor) {
		RCorePlugin.getDefault().getRModelManager().reconcile(this, reconcileLevel, reconcileLevel, monitor);
	}
	
	@Override
	public AstInfo<RAstNode> getAstInfo(final String type, final boolean ensureSync, final IProgressMonitor monitor) {
		if (type == null || type.equals("r")) { //$NON-NLS-1$
			if (ensureSync) {
				return RCorePlugin.getDefault().getRModelManager().reconcile(this, RAst.LEVEL_MODEL_DEFAULT, 0, monitor);
			}
			return fAst;
		}
		return null;
	}
	
	public Object getModelLockObject() {
		return fAstLock;
	}
	
	
	public void setRAst(final AstInfo ast) {
		fAst = ast;
	}
	
	public AstInfo<RAstNode> getCurrentRAst() {
		return fAst;
	}
	
}
