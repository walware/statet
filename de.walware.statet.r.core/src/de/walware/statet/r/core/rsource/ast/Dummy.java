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

import org.eclipse.core.runtime.IStatus;

import de.walware.eclipsecommons.ltk.ast.CommonAstVisitor;
import de.walware.eclipsecommons.ltk.ast.IAstNode;



public abstract class Dummy extends RAstNode {

	
	static class Terminal extends Dummy {
		
		Terminal(IStatus status) {
			fStatus = status;
		}

		
		@Override
		public final NodeType getNodeType() {
			return NodeType.ERROR_TERM;
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
		public final RAstNode getChild(int index) {
			throw new IndexOutOfBoundsException();
		}

		@Override
		public final RAstNode[] getChildren() {
			return NO_CHILDREN;
		}

		@Override
		public final int getChildIndex(IAstNode child) {
			return -1;
		}

		@Override
		final Expression getExpr(RAstNode child) {
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
		public final boolean equalsSingle(RAstNode element) {
			return (element.getNodeType() == NodeType.ERROR_TERM);
		}

		@Override
		public final void accept(RAstVisitor visitor) {
			visitor.visit(this);
		}
		
		@Override
		public final void acceptInChildren(RAstVisitor visitor) {
		}

		public final void acceptInChildren(CommonAstVisitor visitor) {
		}
		
	}
	
	
	static class Operator extends Dummy {
		

		final Expression fLeftExpr = new Expression();
		final Expression fRightExpr = new Expression();

		
		Operator(IStatus status) {
			fStatus = status;
		}

		
		@Override
		public final NodeType getNodeType() {
			return NodeType.ERROR;
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
		public final RAstNode getChild(int index) {
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
		public final int getChildIndex(IAstNode child) {
			if (fLeftExpr.node == child) {
				return 0;
			}
			if (fRightExpr.node == child) {
				return 1;
			}
			return -1;
		}
		
		@Override
		final Expression getExpr(RAstNode child) {
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
		public final boolean equalsSingle(RAstNode element) {
			return (element.getNodeType() == NodeType.ERROR);
		}

		@Override
		public final void accept(RAstVisitor visitor) {
			visitor.visit(this);
		}
		
		@Override
		public final void acceptInChildren(RAstVisitor visitor) {
			if (fLeftExpr.node != null) {
				fLeftExpr.node.accept(visitor);
			}
			fRightExpr.node.accept(visitor);
		}
		
		public final void acceptInChildren(CommonAstVisitor visitor) {
			if (fLeftExpr.node != null) {
				fLeftExpr.node.accept(visitor);
			}
			fRightExpr.node.accept(visitor);
		}
	}
	
	String fText;
	
	
	final void updateStartOffset() {
		Expression left = getLeftExpr();
		if (left != null && left.node != null) {
			fStartOffset = left.node.fStartOffset;
		}
	}
	@Override
	final void updateStopOffset() {
		Expression right = getRightExpr();
		if (right != null && right.node != null) {
			fStopOffset = right.node.fStopOffset;
		}
	}

}