/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource.ast;

import de.walware.eclipsecommons.ltk.ast.CommonAstVisitor;
import de.walware.eclipsecommons.ltk.ast.IAstNode;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 *
 */
public abstract class SubNamed extends RAstNode {
	
	
	static class Named extends SubNamed {
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.SUB_NAMED;
		}
		
		public final RTerminal getOperator() {
			return RTerminal.SUB_NAMED;
		}
		
		@Override
		public final boolean equalsSingle(RAstNode element) {
			return (element.getNodeType() == NodeType.SUB_NAMED && super.equalsSingle(element));
		}
		
	}

	static class Slot extends SubNamed {
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.SUB_SLOT;
		}
		
		public final RTerminal getOperator() {
			return RTerminal.SUB_AT;
		}
		
		@Override
		public final boolean equalsSingle(RAstNode element) {
			return (element.getNodeType() == NodeType.SUB_SLOT && super.equalsSingle(element));
		}
		
	}

	
	final Expression fExpr = new Expression();
	SingleValue fSubname;
	
	
	@Override
	public final boolean hasChildren() {
		return true;
	}
	
	@Override
	public final int getChildCount() {
		return 2;
	}
	
	@Override
	public final RAstNode getChild(int index) {
		switch (index) {
		case 0:
			return fExpr.node;
		case 1:
			return fSubname;
		default:
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public final RAstNode[] getChildren() {
		return new RAstNode[] { fExpr.node, fSubname };
	}
	
	@Override
	public final int getChildIndex(IAstNode child) {
		if (fExpr.node == child) {
			return 0;
		}
		if (fSubname == child) {
			return 1;
		}
		return -1;
	}
	
	public final RAstNode getRefChild() {
		return fExpr.node;
	}
	
	public final RAstNode getSubnameChild() {
		return fSubname;
	}
	
	@Override
	public final void accept(RAstVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInChildren(RAstVisitor visitor) {
		fExpr.node.accept(visitor);
		fSubname.accept(visitor);
	}
	
	public final void acceptInChildren(CommonAstVisitor visitor) {
		fExpr.node.accept(visitor);
		fSubname.accept(visitor);
	}
	

	@Override
	final Expression getExpr(RAstNode child) {
		if (fExpr.node == child) {
			return fExpr;
		}
		return null;
	}
	
	@Override
	final Expression getLeftExpr() {
		return fExpr;
	}
	
	@Override
	final Expression getRightExpr() {
		return null;
	}
	
	@Override
	public boolean equalsSingle(RAstNode element) {
		SubNamed other = (SubNamed) element;
		return (	(this.fExpr.node == other.fExpr.node
						|| (this.fExpr.node != null && other.fExpr.node != null && this.fExpr.node.equalsSingle(other.fExpr.node)) )
				&& 	(this.fSubname == other.fSubname
						|| (this.fSubname != null && other.fSubname != null && this.fSubname.equalsSingle(other.fSubname)) )
				);
	}
	
	final void updateStartOffset() {
		fStartOffset = fExpr.node.fStartOffset;
	}
	
	@Override
	final void updateStopOffset() {
		fStopOffset = fSubname.fStopOffset;
	}
	
}
