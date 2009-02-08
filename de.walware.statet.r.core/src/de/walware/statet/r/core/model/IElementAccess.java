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

package de.walware.statet.r.core.model;

import java.util.Comparator;

import de.walware.ecommons.ltk.IElementName;

import de.walware.statet.r.core.rsource.ast.RAstNode;


/**
 * Access of a variable of class in source code
 * 
 * Is created by code analysis, not indent to implement by clients.
 */
public interface IElementAccess extends IElementName {
	
	
	public static final Comparator<IElementAccess> NAME_POSITION_COMPARATOR = 
		new Comparator<IElementAccess>() {
			public int compare(final IElementAccess o1, final IElementAccess o2) {
				return (o1.getNameNode().getOffset() - o2.getNameNode().getOffset()); 
			}
	};
	
	
	public IEnvirInSource getFrame();
	
	public boolean isWriteAccess();
	
	public RAstNode getNode();
	
	public RAstNode getNameNode();
	
	public IElementAccess getNextSegment();
	
	public IElementAccess[] getAllInUnit();
	
}
