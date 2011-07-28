/*******************************************************************************
 * Copyright (c) 2008-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.model;

import java.util.Comparator;

import de.walware.statet.r.core.rsource.ast.RAstNode;


/**
 * Access of a variable of class in source code
 * 
 * Is created by code analysis, not indent to implement by clients.
 */
public abstract class RElementAccess extends RElementName {
	
	
	public static final Comparator<RElementAccess> NAME_POSITION_COMPARATOR = 
		new Comparator<RElementAccess>() {
			public int compare(final RElementAccess o1, final RElementAccess o2) {
				return (o1.getNameNode().getOffset() - o2.getNameNode().getOffset()); 
			}
	};
	
	
	public abstract IRFrame getFrame();
	
	public abstract boolean isWriteAccess();
	public abstract boolean isFunctionAccess();
	public abstract boolean isCallAccess();
	
	public abstract RAstNode getNode();
	
	public abstract RAstNode getNameNode();
	
	@Override
	public abstract RElementAccess getNextSegment();
	
	public abstract RElementAccess[] getAllInUnit();
	
}
