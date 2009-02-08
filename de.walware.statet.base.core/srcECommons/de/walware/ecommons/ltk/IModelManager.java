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

package de.walware.ecommons.ltk;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;


/**
 * 
 */
public interface IModelManager {
	
	public static int NONE = 0;
	public static int AST = 1;
	public static int MODEL_FILE = 2;
	public static int MODEL_DEPENDENCIES = 3;
	
	
	/**
	 * Returns all loaded source units of the given context.
	 */
	public List<? extends ISourceUnit> getWorkingCopies(WorkingContext context);
	
	/**
	 * Refreshes the model info of all loaded source units in given context.
	 */
	public void refresh(WorkingContext context);
	
	public void addElementChangedListener(IElementChangedListener listener, WorkingContext context);
	public void removeElementChangedListener(IElementChangedListener listener, WorkingContext context);
	
	public void registerDependentUnit(ISourceUnit su);
	public void deregisterDependentUnit(ISourceUnit su);
	
	public void reconcile(final ISourceUnit u, final int level, final boolean reconciler, final IProgressMonitor monitor);
	
}
