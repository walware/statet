/*=============================================================================#
 # Copyright (c) 2008-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.model;

import java.util.Comparator;
import java.util.List;

import de.walware.ecommons.collections.ImList;

import de.walware.statet.r.core.rsource.ast.RAstNode;


/**
 * Access of a variable of class in source code
 * 
 * Is created by code analysis, not indent to implement by clients.
 */
public abstract class RElementAccess extends RElementName {
	
	
	public static final Comparator<RElementAccess> NAME_POSITION_COMPARATOR= 
		new Comparator<RElementAccess>() {
			private int comparePosition(final RAstNode n1, final RAstNode n2) {
				if (n1 != null) {
					if (n2 != null) {
						return n1.getOffset() - n2.getOffset();
					}
					else {
						return -1;
					}
				}
				else {
					if (n2 != null) {
						return 1;
					}
					else {
						return 0;
					}
				}
			}
			@Override
			public int compare(RElementAccess o1, RElementAccess o2) {
				int offset= comparePosition(o1.getNameNode(), o2.getNameNode());
				while (offset == 0) {
					o1= o1.getNextSegment();
					o2= o2.getNextSegment();
					if (o1 != null) {
						if (o2 != null) {
							offset= comparePosition(o1.getNameNode(), o2.getNameNode());
						}
						else {
							return 1;
						}
					}
					else {
						if (o2 != null) {
							return -1;
						}
						else {
							return 0;
						}
					}
				}
				return offset;
			}
	};
	
	public static RElementAccess getMainElementAccessOfNameNode(final RAstNode nameNode) {
		RAstNode node= nameNode;
		while (node != null) {
			final List<Object> attachments= node.getAttachments();
			for (final Object attachment : attachments) {
				if (attachment instanceof RElementAccess) {
					RElementAccess access= (RElementAccess) attachment;
					do {
						if (access.isMaster() && access.getNameNode() == nameNode) {
							return (RElementAccess) attachment;
						}
						access= access.getNextSegment();
					} while (access != null);
				}
			}
			node= node.getRParent();
		}
		return null;
	}
	
	public static RElementAccess getElementAccessOfNameNode(final RAstNode nameNode) {
		RAstNode node= nameNode;
		while (node != null) {
			final List<Object> attachments= node.getAttachments();
			for (final Object attachment : attachments) {
				if (attachment instanceof RElementAccess) {
					RElementAccess access= (RElementAccess) attachment;
					do {
						if (access.isMaster() && access.getNameNode() == nameNode) {
							return access;
						}
						access= access.getNextSegment();
					} while (access != null);
				}
			}
			node= node.getRParent();
		}
		return null;
	}
	
	
	public abstract IRFrame getFrame();
	
	public abstract boolean isWriteAccess();
	public abstract boolean isFunctionAccess();
	public abstract boolean isCallAccess();
	
	
	public boolean isMaster() {
		return true;
	}
	
	public boolean isSlave() {
		return false;
	}
	
	public RElementAccess getMaster() {
		return this;
	}
	
	
	public abstract RAstNode getNode();
	
	public abstract RAstNode getNameNode();
	
	@Override
	public abstract RElementAccess getNextSegment();
	
	public abstract ImList<? extends RElementAccess> getAllInUnit(boolean includeSlaves);
	
}
