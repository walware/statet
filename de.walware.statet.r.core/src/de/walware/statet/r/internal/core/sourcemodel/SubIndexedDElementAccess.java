/*******************************************************************************
 * Copyright (c) 2008-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.SubIndexed;


final class SubIndexedDElementAccess extends SubAbstractElementAccess {
	
	
	final SubIndexed fNode;
	
	
	SubIndexedDElementAccess(final ElementAccess root, final SubIndexed node) {
		fRoot = root;
		fNode = node;
	}
	
	
	@Override
	public final int getType() {
		return RElementName.SUB_INDEXED_D;
	}
	
	@Override
	public final String getSegmentName() {
		return null;
	}
	
	@Override
	public final RAstNode getNode() {
		return fNode;
	}
	
	@Override
	public final RAstNode getNameNode() {
		return null;
	}
	
	
	@Override
	public final RElementAccess[] getAllInUnit() {
		return null;
	}
	
}
