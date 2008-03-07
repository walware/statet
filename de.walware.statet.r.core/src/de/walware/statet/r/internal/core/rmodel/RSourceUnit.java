/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.rmodel;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.eclipsecommons.ltk.IModelElement;

import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.core.rmodel.IRSourceUnit;


/**
 * 
 */
public class RSourceUnit extends RResourceUnit implements IRSourceUnit {
	
	
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
	public String getTypeId() {
		return "r"; //$NON-NLS-1$
	}
	
	@Override
	public IRSourceUnit getUnderlyingUnit() {
		return null;
	}
	
	@Override
	public boolean hasChildren(final Object filter) {
		return false;
	}
	
	@Override
	public IModelElement[] getChildren(final Object filter) {
		return new IModelElement[] { };
	}
	
	
	public synchronized void reconcile(final int level, final IProgressMonitor monitor) {
	}
	
}
