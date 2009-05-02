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

package de.walware.statet.r.internal.core.sourcemodel;

import de.walware.statet.r.core.model.IElementAccess;
import de.walware.statet.r.core.model.IFrame;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.rsource.ast.RAstNode;


public abstract class ElementAccess extends RElementName implements IElementAccess {
	
	
	public static final int A_READ =    0x0;
	public static final int A_WRITE =   0x000002;
	public static final int A_DELETE =  0x000003;
	
	public static final int A_SUB =     0x000100;
	public static final int A_S4 =      0x000200;
	
	public static final int A_FUNC =    0x000010;
	public static final int A_ARG =     0x000020;
	
	
	public final static class Default extends ElementAccess {
		
		public Default(final RAstNode node) {
			super(node, null);
		}
		
		public int getType() {
			return RElementName.MAIN_DEFAULT;
		}
		
	}
	
	public final static class Slot extends ElementAccess {
		
		public Slot(final RAstNode node) {
			super(node, null);
		}
		
		public int getType() {
			return RElementName.MAIN_SLOT;
		}
		
	}
	
	public final static class Class extends ElementAccess {
		
		public Class(final RAstNode node) {
			super(node, null);
		}
		
		public int getType() {
			return RElementName.MAIN_CLASS;
		}
		
	}
	
	public final static class Package extends ElementAccess {
		
		public Package(final RAstNode node, final RAstNode nameNode) {
			super(node, nameNode);
		}
		
		public int getType() {
			return RElementName.MAIN_PACKAGE;
		}
		
	}
	
	
	int fFlags;
	RAstNode fFullNode;
	RAstNode fNameNode;
	Envir.ElementAccessList fShared;
	SubAbstractElementAccess fNextSegment;
	
	
	private ElementAccess(final RAstNode node, final RAstNode nameNode) {
		fFullNode = node;
		fNameNode = nameNode;
	}
	
	
	public final String getSegmentName() {
		return fShared.name;
	}
	
	@Override
	public String getDisplayName() {
		return RElementName.createDisplayName(this);
	}
	
	public final IFrame getFrame() {
		return fShared.frame;
	}
	
	public final IElementAccess[] getAllInUnit() {
		return fShared.entries.toArray(new IElementAccess[fShared.entries.size()]);
	}
	
	public final boolean isWriteAccess() {
		return ((fFlags & A_WRITE) != 0);
	}
	
	public final boolean isDeletion() {
		return ((fFlags & A_DELETE) == A_DELETE);
	}
	
	public final boolean isFunction() {
		return ((fFlags & A_FUNC) == A_FUNC);
	}
	
	public final RAstNode getNode() {
		return fFullNode;
	}
	
	public final RAstNode getNameNode() {
		return fNameNode;
	}
	
	public final IElementAccess getNextSegment() {
		return fNextSegment;
	}
	
	final void appendSubElement(final SubAbstractElementAccess newSub) {
		if (fNextSegment == null) {
			fNextSegment = newSub;
			return;
		}
		SubAbstractElementAccess parent = fNextSegment;
		while (parent.fNextSub != null) {
			parent = parent.fNextSub;
		}
		parent.fNextSub = newSub;
	}
	
}
