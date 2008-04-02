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

package de.walware.statet.r.internal.core.rmodel;

import de.walware.statet.r.core.rmodel.IElementAccess;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.SubIndexed;


final class SubIndexedElementAccess extends SubAbstractElementAccess {
	
	
	final SubIndexed fNode;
	
	
	SubIndexedElementAccess(final ElementAccess root, final SubIndexed node) {
		fRoot = root;
		fNode = node;
	}
	
	
	public final int getType() {
		return SUB_INDEXED;
	}
	
	public final String getName() {
		return null;
	}
	
	public final RAstNode getNode() {
		return fNode;
	}
	
	public final RAstNode getNameNode() {
		return null;
	}
	
	
	@Override
	public final IElementAccess[] getAllInUnit() {
		return null;
	}
	
}
