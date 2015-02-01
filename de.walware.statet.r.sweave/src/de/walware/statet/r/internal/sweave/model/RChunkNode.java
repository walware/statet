/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.model;

import java.lang.reflect.InvocationTargetException;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rsource.ast.FCall.Args;
import de.walware.statet.r.core.rsource.ast.SourceComponent;


/**
 * R Chunk
 */
public class RChunkNode implements IAstNode {
	
	
	private final IAstNode parent;
	// start/stop control chunk
	Args weaveArgs;
	SourceComponent[] rSources;
	
	int startOffset;
	int stopOffset;
	
	
	RChunkNode(final IAstNode parent) {
		this.parent= parent;
	}
	
	
	@Override
	public int getStatusCode() {
		return 0;
	}
	
	
	@Override
	public IAstNode getParent() {
		return this.parent;
	}
	
	@Override
	public IAstNode getRoot() {
		return this.parent.getRoot();
	}
	
	
	@Override
	public boolean hasChildren() {
		return true;
	}
	
	@Override
	public int getChildCount() {
		return this.rSources.length+1;
	}
	
	@Override
	public IAstNode getChild(final int index) {
		if (index == 0) {
			return this.weaveArgs;
		}
		return this.rSources[index-1];
	}
	
	@Override
	public int getChildIndex(final IAstNode element) {
		if (this.weaveArgs == element) {
			return 0;
		}
		for (int i= 0; i < this.rSources.length; i++) {
			if (this.rSources[i] == element) {
				return i+1;
			}
		}
		return -1;
	}
	
	
	@Override
	public void accept(final ICommonAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	@Override
	public void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		if (this.weaveArgs != null) {
			visitor.visit(this.weaveArgs);
		}
		for (final SourceComponent node : this.rSources) {
			visitor.visit(node);
		}
	}
	
	
	@Override
	public int getOffset() {
		return this.startOffset;
	}
	
	@Override
	public int getStopOffset() {
		return this.stopOffset;
	}
	
	@Override
	public int getLength() {
		return this.stopOffset-this.startOffset;
	}
	
}
