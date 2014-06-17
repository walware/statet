/*=============================================================================#
 # Copyright (c) 2008-2014 Stephan Wahlbrink (WalWare.de) and others.
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

import de.walware.statet.r.core.rsource.ast.RAstNode;


/**
 * Access of a variable of class in source code
 * 
 * Is created by code analysis, not indent to implement by clients.
 */
public abstract class RElementAccess extends RElementName {
	
	
	public static final Comparator<RElementAccess> NAME_POSITION_COMPARATOR= 
		new Comparator<RElementAccess>() {
			@Override
			public int compare(final RElementAccess o1, final RElementAccess o2) {
				return (o1.getNameNode().getOffset() - o2.getNameNode().getOffset()); 
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
						if (access.getNameNode() == nameNode) {
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
						if (access.getNameNode() == nameNode) {
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
	
	public abstract RAstNode getNode();
	
	public abstract RAstNode getNameNode();
	
	@Override
	public abstract RElementAccess getNextSegment();
	
	public abstract RElementAccess[] getAllInUnit();
	
}
