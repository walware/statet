/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rmodel;

import de.walware.eclipsecommons.ltk.AstInfo;

import de.walware.statet.r.core.rsource.ast.RAstNode;


/**
 * Extends source unit, so that they can be managed by the R model manager
 * {@link RCore#getRModelManger()}
 */
public interface IManagableRUnit extends IRSourceUnit {
	
	
	public Object getModelLockObject();
	
	public void setRAst(AstInfo ast);
	public AstInfo<RAstNode> getCurrentRAst();
	
}
