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
public abstract class Sign extends RAstNode {
	

	static class PlusSign extends Sign {
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.SIGN;
		}
		
		public final RTerminal getOperator() {
			return RTerminal.PLUS;
		}

		@Override
		public final boolean equalsSingle(RAstNode element) {
			return (element.getNodeType() == NodeType.SIGN);
		}
		
	}

	static class MinusSign extends Sign {
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.SIGN;
		}
		
		public final RTerminal getOperator() {
			return RTerminal.MINUS;
		}

		@Override
		public final boolean equalsSingle(RAstNode element) {
			return (element.getNodeType() == NodeType.SIGN);
		}
		
	}
	
	static class Not extends Sign {
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.NOT;
		}
		
		public final RTerminal getOperator() {
			return RTerminal.NOT;
		}

		@Override
		public final boolean equalsSingle(RAstNode element) {
			return (element.getNodeType() == NodeType.NOT);
		}
		
	}
	
	
	final Expression fRightExpr = new Expression();
	
	
	@Override
	public final boolean hasChildren() {
		return true;
	}
	
	@Override
	public final int getChildCount() {
		return 1;
	}
	
	@Override
	public final RAstNode getChild(int index) {
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
	public final int getChildIndex(IAstNode child) {
		if (fRightExpr.node == child) {
			return 0;
		}
		return -1;
	}

	public final RAstNode getRightChild() {
		return fRightExpr.node;
	}
	
	@Override
	public final void accept(RAstVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInChildren(RAstVisitor visitor) {
		fRightExpr.node.accept(visitor);
	}

	public final void acceptInChildren(CommonAstVisitor visitor) {
		fRightExpr.node.accept(visitor);
	}
	
	
	@Override
	final Expression getExpr(RAstNode child) {
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
	final void updateStopOffset() {
		fStopOffset = fRightExpr.node.fStopOffset;
	}

}
