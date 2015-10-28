/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.sourcemodel;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.rsource.ast.RAstNode;


public final class AnonymousAccess extends RElementAccess {
	
	
	private final RAstNode node;
	private IRFrame frame;
	
	
	public AnonymousAccess(final RAstNode node, final IRFrame frame) {
		this.node= node;
		this.frame= frame;
	}
	
	
	@Override
	public int getType() {
		return RElementName.ANONYMOUS;
	}
	
	
	@Override
	public IRFrame getFrame() {
		return this.frame;
	}
	
	@Override
	public ImList<? extends RElementAccess> getAllInUnit(final boolean includeSlaves) {
		return ImCollections.newList(this);
	}
	
	@Override
	public boolean isWriteAccess() {
		return true;
	}
	
	@Override
	public boolean isFunctionAccess() {
		return true;
	}
	
	@Override
	public boolean isCallAccess() {
		return false;
	}
	
	@Override
	public RAstNode getNode() {
		return this.node;
	}
	
	@Override
	public RAstNode getNameNode() {
		return null;
	}
	
	
	@Override
	public RElementName getNamespace() {
		return null;
	}
	
	@Override
	public String getSegmentName() {
		return null;
	}
	
	@Override
	public RElementAccess getNextSegment() {
		return null;
	}
	
}
