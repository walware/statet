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
import de.walware.statet.r.core.rmodel.IScope;
import de.walware.statet.r.core.rsource.ast.RAstNode;


public abstract class ElementAccess implements IElementAccess {
	
	
	public static final int A_READ =    0x0;
	public static final int A_WRITE =   0x000002;
	
	public static final int A_SUB =     0x000100;
	public static final int A_S4 =      0x000200;
	
	public static final int A_FUNC =    0x000010;
	public static final int A_ARG =     0x000020;
	public static final int A_CLASS =   0x000040;
	
	
	public final static class Default extends ElementAccess {
		
		public Default(final RAstNode node) {
			super(node);
		}
		
		public int getType() {
			return MAIN_DEFAULT;
		}
		
	}
	
	public final static class Class extends ElementAccess {
		
		public Class(final RAstNode node) {
			super(node);
		}
		
		public int getType() {
			return MAIN_CLASS;
		}
		
	}
	
	
	int fFlags;
	RAstNode fFullNode;
	RAstNode fNameNode;
	Scope.ElementAccessList fShared;
	SubAbstractElementAccess fSubElement;
	
	
	private ElementAccess(final RAstNode node) {
		fFullNode = node;
	}
	
	
	public final String getName() {
		return fShared.name;
	}
	
	public final IScope getScope() {
		return fShared.scope;
	}
	
	public final IElementAccess[] getAllInUnit() {
		return fShared.entries.toArray(new IElementAccess[fShared.entries.size()]);
	}
	
	public final boolean isReadAccess() {
		return ((fFlags & A_WRITE) == 0);
	}
	
	public final boolean isWriteAccess() {
		return ((fFlags & A_WRITE) != 0);
	}
	
	public final boolean isObject() {
		return ((fFlags & A_CLASS) == 0);
	}
	
	public final RAstNode getNode() {
		return fFullNode;
	}
	
	public final RAstNode getNameNode() {
		return fNameNode;
	}
	
	public final IElementAccess getSubElementAccess() {
		return fSubElement;
	}
	
	
	final void appendSubElement(final SubAbstractElementAccess newSub) {
		if (fSubElement == null) {
			fSubElement = newSub;
			return;
		}
		SubAbstractElementAccess parent = fSubElement;
		while (parent.fNextSub != null) {
			parent = parent.fNextSub;
		}
		parent.fNextSub = newSub;
	}
	
}
