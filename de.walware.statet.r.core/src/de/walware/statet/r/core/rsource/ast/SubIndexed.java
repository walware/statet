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



/**
 *
 */
public abstract class SubIndexed extends RAstNode {
	

	static class S extends SubIndexed {
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.SUB_INDEXED_S;
		}

		@Override
		public final boolean equalsSingle(RAstNode element) {
			return (element.getNodeType() == NodeType.SUB_INDEXED_S && super.equalsSingle(element));
		}
		
	}

	static class D extends SubIndexed {
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.SUB_INDEXED_D;
		}

		@Override
		public final boolean equalsSingle(RAstNode element) {
			return (element.getNodeType() == NodeType.SUB_INDEXED_D && super.equalsSingle(element));
		}
		
	}

	public static class Sublist extends SpecList {
		
		Sublist(SubIndexed parent) {
			fParent = parent;
		}
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.SUB_INDEXED_ARGS;
		}

		@Override
		public final void accept(RAstVisitor visitor) {
			visitor.visit(this);
		}
		
		@Override
		public final boolean equalsSingle(RAstNode element) {
			return (element.getNodeType() == NodeType.SUB_INDEXED_ARGS);
		}
		
		@Override
		final SpecItem createItem() {
			return new SubIndexed.Arg(this);
		}
	}
	
	public static class Arg extends SpecItem {
		
		
		Arg(SubIndexed.Sublist parent) {
			fParent = parent;
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.SUB_INDEXED_ARG;
		}

		@Override
		public final boolean equalsSingle(RAstNode element) {
			return (element.getNodeType() == NodeType.SUB_INDEXED_ARG);
		}
		
		@Override
		public final void accept(RAstVisitor visitor) {
			visitor.visit(this);
		}
		
	}

	
	final Expression fExpr = new Expression();
	final Sublist fSublist = new Sublist(this);
	int fOpenOffset = Integer.MIN_VALUE;
	int fCloseOffset = Integer.MIN_VALUE;
	int fClose2Offset = Integer.MIN_VALUE;
	
	
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
			return fSublist;
		default:
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public final RAstNode[] getChildren() {
		return new RAstNode[] { fExpr.node, fSublist };
	}
	
	public final RAstNode getRefChild() {
		return fExpr.node;
	}
	
	public final Sublist getSublistChild() {
		return fSublist;
	}

	public final int getSublistOpenOffset() {
		return fOpenOffset;
	}
	
	public final int getSublistCloseOffset() {
		return fCloseOffset;
	}
	
	@Override
	public final int getChildIndex(IAstNode child) {
		if (fExpr.node == child) {
			return 0;
		}
		if (fSublist == child) {
			return 1;
		}
		return -1;
	}
	
	@Override
	public final void accept(RAstVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInChildren(RAstVisitor visitor) {
		fExpr.node.accept(visitor);
		fSublist.accept(visitor);
	}

	public final void acceptInChildren(CommonAstVisitor visitor) {
		fExpr.node.accept(visitor);
		fSublist.accept(visitor);
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
		SubIndexed other = (SubIndexed) element;
		return (	(this.fExpr.node == other.fExpr.node
						|| (this.fExpr.node != null && other.fExpr.node != null && this.fExpr.node.equalsSingle(other.fExpr.node)) )
				);
	}

	final void updateStartOffset() {
		fStartOffset = fExpr.node.fStartOffset;
	}
	
	@Override
	final void updateStopOffset() {
		if (fClose2Offset >= 0) {
			fStopOffset = fClose2Offset+1;
		}
		else if (fCloseOffset >= 0) {
			fStopOffset = fCloseOffset+1;
		}
		else {
			fStopOffset = fSublist.fStopOffset;
		}
	}

}
