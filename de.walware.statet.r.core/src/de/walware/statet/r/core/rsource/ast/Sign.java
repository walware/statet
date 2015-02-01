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

package de.walware.statet.r.core.rsource.ast;

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AFTER_OP_MISSING;

import java.lang.reflect.InvocationTargetException;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * <code>+ §right§</code>
 * <code>- §right§</code>
 */
public abstract class Sign extends RAstNode {
	
	
	static class PlusSign extends Sign {
		
		
		PlusSign() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.SIGN;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.PLUS;
		}
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.SIGN);
		}
		
	}
	
	static class MinusSign extends Sign {
		
		
		MinusSign() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.SIGN;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.MINUS;
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.SIGN);
		}
		
	}
	
	static class Not extends Sign {
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.NOT;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.NOT;
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.NOT);
		}
		
	}
	
	
	final Expression fRightExpr = new Expression();
	
	
	protected Sign() {
	}
	
	
	@Override
	public final boolean hasChildren() {
		return true;
	}
	
	@Override
	public final int getChildCount() {
		return 1;
	}
	
	@Override
	public final RAstNode getChild(final int index) {
		if (index == 0) {
			return fRightExpr.node;
		}
		throw new IndexOutOfBoundsException();
	}
	
	@Override
	public final RAstNode[] getChildren() {
		return new RAstNode[] { fRightExpr.node };
	}
	
	@Override
	public final int getChildIndex(final IAstNode child) {
		if (fRightExpr.node == child) {
			return 0;
		}
		return -1;
	}
	
	public final RAstNode getRightChild() {
		return fRightExpr.node;
	}
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
		fRightExpr.node.acceptInR(visitor);
	}
	
	@Override
	public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(fRightExpr.node);
	}
	
	
	@Override
	final Expression getExpr(final RAstNode child) {
		if (fRightExpr.node == child) {
			return fRightExpr;
		}
		return null;
	}
	
	@Override
	final Expression getLeftExpr() {
		return null;
	}
	
	@Override
	final Expression getRightExpr() {
		return fRightExpr;
	}
	
	
	@Override
	final int getMissingExprStatus(final Expression expr) {
		if (fRightExpr == expr) {
			return STATUS2_SYNTAX_EXPR_AFTER_OP_MISSING;
		}
		throw new IllegalArgumentException();
	}
	
	@Override
	final void updateStopOffset() {
		fStopOffset = fRightExpr.node.fStopOffset;
	}
	
	
	@Override
	public boolean equalsValue(final RAstNode element) {
		return (element.getNodeType() == getNodeType()
				&& element.getOperator(0) == getOperator(0)
				&& element.getChild(0).equals(getChild(0)));
	}
	
}
