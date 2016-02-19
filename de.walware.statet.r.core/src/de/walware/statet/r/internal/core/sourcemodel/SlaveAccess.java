/*=============================================================================#
 # Copyright (c) 2015-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.sourcemodel;

import de.walware.jcommons.collections.ImList;

import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.rsource.ast.RAstNode;


public final class SlaveAccess extends RElementAccess {
	
	
	private final RElementAccess master;
	
	private RElementAccess nextSegment;
	
	
	public SlaveAccess(final RElementAccess master) {
		this.master= master;
	}
	
	
	@Override
	public int getType() {
		return this.master.getType();
	}
	
	@Override
	public String getSegmentName() {
		return this.master.getSegmentName();
	}
	
	@Override
	public IRFrame getFrame() {
		return this.master.getFrame();
	}
	
	@Override
	public boolean isWriteAccess() {
		return this.master.isWriteAccess();
	}
	
	@Override
	public boolean isFunctionAccess() {
		return this.master.isFunctionAccess();
	}
	
	@Override
	public boolean isCallAccess() {
		return this.master.isCallAccess();
	}
	
	@Override
	public boolean isMaster() {
		return false;
	}
	
	@Override
	public boolean isSlave() {
		return true;
	}
	
	@Override
	public RElementAccess getMaster() {
		return this.master;
	}
	
	@Override
	public RAstNode getNode() {
		return this.master.getNameNode();
	}
	
	@Override
	public RAstNode getNameNode() {
		return this.master.getNameNode();
	}
	
	@Override
	public RElementAccess getNextSegment() {
		return this.nextSegment;
	}
	
	@Override
	public ImList<? extends RElementAccess> getAllInUnit(final boolean includeSlaves) {
		return this.master.getAllInUnit(includeSlaves);
	}
	
	@Override
	public RElementName getScope() {
		return this.master.getScope();
	}
	
}
