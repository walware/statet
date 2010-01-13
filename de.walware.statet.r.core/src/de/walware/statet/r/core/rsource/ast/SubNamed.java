/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource.ast;

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AS_REF_MISSING;

import java.lang.reflect.InvocationTargetException;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * <code>§ref§ $ §subname§</code>
 */
public abstract class SubNamed extends RAstNode {
	
	
	static class Named extends SubNamed {
		
		
		Named() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.SUB_NAMED_PART;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.SUB_NAMED_PART;
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.SUB_NAMED_PART && super.equalsSingle(element));
		}
		
	}
	
	static class Slot extends SubNamed {
		
		
		Slot() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.SUB_NAMED_SLOT;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.SUB_NAMED_SLOT;
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.SUB_NAMED_SLOT && super.equalsSingle(element));
		}
		
	}
	
	
	final Expression fExpr = new Expression();
	SingleValue fSubname;
	int fOperatorOffset = Integer.MIN_VALUE;
	
	
	protected SubNamed() {
	}
	
	
	@Override
	public final boolean hasChildren() {
		return true;
	}
	
	@Override
	public final int getChildCount() {
		return 2;
	}
	
	@Override
	public final RAstNode getChild(final int index) {
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
	public final int getChildIndex(final IAstNode child) {
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
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
		fExpr.node.acceptInR(visitor);
		fSubname.acceptInR(visitor);
	}
	
	public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(fExpr.node);
		visitor.visit(fSubname);
	}
	
	
	@Override
	final Expression getExpr(final RAstNode child) {
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
	public boolean equalsSingle(final RAstNode element) {
		final SubNamed other = (SubNamed) element;
		return (	(this.fExpr.node == other.fExpr.node
						|| (this.fExpr.node != null && other.fExpr.node != null && this.fExpr.node.equalsSingle(other.fExpr.node)) )
				&& 	(this.fSubname == other.fSubname
						|| (this.fSubname != null && other.fSubname != null && this.fSubname.equalsSingle(other.fSubname)) )
				);
	}
	
	
	@Override
	final int getMissingExprStatus(final Expression expr) {
		if (fExpr == expr) {
			return STATUS2_SYNTAX_EXPR_AS_REF_MISSING;
		}
		throw new IllegalArgumentException();
	}
	
	final void updateStartOffset() {
		fStartOffset = fExpr.node.fStartOffset;
	}
	
	@Override
	final void updateStopOffset() {
		fStopOffset = fSubname.fStopOffset;
	}
	
}
