/*******************************************************************************
 * Copyright (c) 2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.rsource.ast.RAstNode;


public class AnonymousAccess extends RElementAccess {
	
	
	RAstNode fNode;
	IRFrame fFrame;
	
	
	public AnonymousAccess(RAstNode node, IRFrame frame) {
		fNode = node;
		fFrame = frame;
	}
	
	
	@Override
	public int getType() {
		return RElementName.ANONYMOUS;
	}
	
	
	@Override
	public IRFrame getFrame() {
		return fFrame;
	}
	
	@Override
	public RElementAccess[] getAllInUnit() {
		return new RElementAccess[] { this };
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
		return fNode;
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
