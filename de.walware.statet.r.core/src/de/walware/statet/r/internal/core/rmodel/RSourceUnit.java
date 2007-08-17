/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.rmodel;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.IModelElement;
import de.walware.eclipsecommons.ltk.WorkingContext;

import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 *
 */
public class RSourceUnit extends RResourceUnit implements IRSourceUnit {
	
	
	public RSourceUnit(IFile file) {
		super(file);
	}
	
	@Override
	protected void init() {
		RCorePlugin.getDefault().getRModelManager().registerWorkingCopy(this);
	}

	@Override
	protected void dispose() {
		RCorePlugin.getDefault().getRModelManager().removeWorkingCopy(this);
	}
	
	@Override
	public IRSourceUnit getUnderlyingUnit() {
		return null;
	}
	
	@Override
	public IRSourceUnit getWorkingCopy(WorkingContext context, boolean create) {
		synchronized (context) {
			String id = getId();
			IRSourceUnit u = RCorePlugin.getDefault().getRModelManager().getWorkingCopy(id, context);
			if (u == null && create) {
				u = new RSourceUnitWorkingCopy(this, context);
			}
			u.connect();
			disconnect();
			return u;
		}
	}

	
	@Override
	public boolean hasChildren(Object filter) {
		return false;
	}
	
	@Override
	public IModelElement[] getChildren(Object filter) {
		return new IModelElement[] { };
	}
	

	public synchronized void reconcile(int level, IProgressMonitor monitor) {
	}
	
	public AstInfo<RAstNode> getAstInfo(boolean ensureSync, IProgressMonitor monitor) {
		return null;
	}

}
