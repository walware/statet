/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;


/**
 * Source unit implementation for R script files in workspace ("default R file").
 */
public final class RSourceUnit extends RResourceUnit implements IRSourceUnit {
	
	
	public RSourceUnit(final IFile file) {
		super(file);
	}
	
	
	@Override
	protected void init() {
		register();
	}
	
	@Override
	protected void dispose() {
		unregister();
	}
	
	@Override
	public String getModelTypeId() {
		return RModel.TYPE_ID;
	}
	
	@Override
	public int getElementType() {
		return IRSourceUnit.R_WORKSPACE_SU;
	}
	
	@Override
	public IRSourceUnit getUnderlyingUnit() {
		return null;
	}
	
	
	public synchronized void reconcileRModel(final int reconcileLevel, final IProgressMonitor monitor) {
	}
	
}
