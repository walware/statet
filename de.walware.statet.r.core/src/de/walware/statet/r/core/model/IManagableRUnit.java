/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.model;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rsource.ast.RAstInfo;


/**
 * Extends source unit, so that they can be updated by the R model manager
 * {@link RCore#getRModelManager()}
 */
public interface IManagableRUnit extends IRSourceUnit {
	
	
	public Object getModelLockObject();
	
	public void setRAst(RAstInfo ast);
	public RAstInfo getCurrentRAst();
	
	public void setRModel(IRModelInfo model);
	public IRModelInfo getCurrentRModel();
	
}
