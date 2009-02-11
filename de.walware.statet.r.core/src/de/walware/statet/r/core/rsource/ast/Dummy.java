/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource.ast;

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AFTER_OP_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_BEFORE_OP_MISSING;

import java.lang.reflect.InvocationTargetException;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * <code></code>
 */
public abstract class Dummy extends RAstNode {
	
	
	static class Terminal extends Dummy {
		
		
		String fText;
		
		
		Terminal(final int status) {
			super(status);
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.ERROR_TERM;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return null;
		}
		
		@Override
		public final String getText() {
			return fText;
		}
		
		@Override
		public final boolean hasChildren() {
			return false;
		}
		
		@Override
		public final int getChildCount() {
			return 0;
		}
		
		@Override
		public final RAstNode getChild(final int index) {
			throw new IndexOutOfBoundsException();
		}
		
		@Override
		public final RAstNode[] getChildren() {
			return NO_CHILDREN;
		}
		
		@Override
		public final int getChildIndex(final IAstNode child) {
			return -1;
		}
		
		@Override
		public final void acceptInRChildren(final RAstVisitor visitor) {
		}
		
		public final void acceptInChildren(final ICommonAstVisitor visitor) {
		}
		
		
		@Override
		final Expression getExpr(final RAstNode child) {
			return null;
		}
		
		@Override
		final Expression getLeftExpr() {
			return null;
		}
		
		@Override
		final Expression getRightExpr() {
			return null;
		}
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.ERROR_TERM);
		}
		
		
		@Override
		final int getMissingExprStatus(final Expression expr) {
			throw new IllegalArgumentException();
		}
		
	}
	
	
	static class Operator extends Dummy {
		
		
		final Expression fLeftExpr = new Expression();
		final Expression fRightExpr = new Expression();
		
		
		Operator(final int status) {
			super(status);
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.ERROR;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return null;
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
				return fLeftExpr.node;
			case 1:
				return fRightExpr.node;
			default:
				throw new IndexOutOfBoundsException();
			}
		}
		
		@Override
		public final RAstNode[] getChildren() {
			return new RAstNode[] { fLeftExpr.node, fRightExpr.node };
		}
		
		@Override
		public final int getChildIndex(final IAstNode child) {
			if (fLeftExpr.node == child) {
				return 0;
			}
			if (fRightExpr.node == child) {
				return 1;
			}
			return -1;
		}
		
		@Override
		public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
			if (fLeftExpr.node != null) {
				fLeftExpr.node.acceptInR(visitor);
			}
			fRightExpr.node.acceptInR(visitor);
		}
		
		public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
			if (fLeftExpr.node != null) {
				fLeftExpr.node.accept(visitor);
			}
			fRightExpr.node.accept(visitor);
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.ERROR);
		}
		
		
		@Override
		final Expression getExpr(final RAstNode child) {
			if (fRightExpr.node == child) {
				return fRightExpr;
			}
			if (fLeftExpr.node == child) {
				return fLeftExpr;
			}
			return null;
		}
		
		@Override
		final Expression getLeftExpr() {
			return fLeftExpr;
		}
		
		@Override
		final Expression getRightExpr() {
			return fRightExpr;
		}
		
		@Override
		final int getMissingExprStatus(final Expression expr) {
			if (expr == fLeftExpr) {
				return STATUS2_SYNTAX_EXPR_BEFORE_OP_MISSING;
			}
			if (expr == fRightExpr) {
				return STATUS2_SYNTAX_EXPR_AFTER_OP_MISSING;
			}
			throw new IllegalArgumentException();
		}
		
	}
	
	
	Dummy(final int status) {
		super(status);
	}
	
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	
	final void updateStartOffset() {
		final Expression left = getLeftExpr();
		if (left != null && left.node != null) {
			fStartOffset = left.node.fStartOffset;
		}
	}
	@Override
	final void updateStopOffset() {
		final Expression right = getRightExpr();
		if (right != null && right.node != null) {
			fStopOffset = right.node.fStopOffset;
		}
	}
	
}
