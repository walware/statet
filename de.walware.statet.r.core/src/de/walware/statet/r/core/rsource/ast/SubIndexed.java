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

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AS_REF_MISSING;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * <code>§ref§ [ §args§ ]</code>
 * <code>§ref§ [[ §args§ ]]</code>
 */
public abstract class SubIndexed extends RAstNode {
	
	
	static final class S extends SubIndexed {
		
		
		S() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.SUB_INDEXED_S;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.SUB_INDEXED_S_OPEN;
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.SUB_INDEXED_S && super.equalsSingle(element));
		}
		
	}
	
	static final class D extends SubIndexed {
		
		
		D() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.SUB_INDEXED_D;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.SUB_INDEXED_D_OPEN;
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.SUB_INDEXED_D && super.equalsSingle(element));
		}
		
	}
	
	public static final class Args extends RAstNode {
		
		
		final List<SubIndexed.Arg> fSpecs;
		
		
		Args(final SubIndexed parent) {
			fRParent = parent;
			fSpecs= new ArrayList<>();
		}
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.SUB_INDEXED_ARGS;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return null;
		}
		
		
		@Override
		public final boolean hasChildren() {
			return (!fSpecs.isEmpty());
		}
		
		@Override
		public final int getChildCount() {
			return fSpecs.size();
		}
		
		@Override
		public final RAstNode getChild(final int index) {
			return fSpecs.get(index);
		}
		
		@Override
		public final RAstNode[] getChildren() {
			return fSpecs.toArray(new RAstNode[fSpecs.size()]);
		}
		
		@Override
		public final int getChildIndex(final IAstNode child) {
			for (int i = fSpecs.size()-1; i >= 0; i--) {
				if (fSpecs.get(i) == child) {
					return i;
				}
			}
			return -1;
		}
		
		@Override
		public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
			visitor.visit(this);
		}
		
		@Override
		public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
			acceptChildren(visitor, fSpecs);
		}
		
		@Override
		public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
			for (final RAstNode child : fSpecs) {
				visitor.visit(child);
			}
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
		final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.SUB_INDEXED_ARGS);
		}
		
		@Override
		final int getMissingExprStatus(final Expression expr) {
			throw new IllegalArgumentException();
		}
		
		@Override
		final void updateStopOffset() {
		}
		
	}
	
	public static final class Arg extends SpecItem {
		
		
		Arg(final SubIndexed.Args parent) {
			fRParent = parent;
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.SUB_INDEXED_ARG;
		}
		
		
		@Override
		public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
			visitor.visit(this);
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.SUB_INDEXED_ARG);
		}
		
	}
	
	
	final Expression fExpr = new Expression();
	final Args fSublist = new Args(this);
	int fOpenOffset = NA_OFFSET;
	int fCloseOffset = NA_OFFSET;
	int fClose2Offset = NA_OFFSET;
	
	
	protected SubIndexed() {
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
	
	public final Args getArgsChild() {
		return fSublist;
	}
	
	public final int getSublistOpenOffset() {
		return fOpenOffset;
	}
	
	public final int getSublistCloseOffset() {
		return fCloseOffset;
	}
	
	public final int getSublistClose2Offset() {
		return fCloseOffset;
	}
	
	@Override
	public final int getChildIndex(final IAstNode child) {
		if (fExpr.node == child) {
			return 0;
		}
		if (fSublist == child) {
			return 1;
		}
		return -1;
	}
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
		fExpr.node.acceptInR(visitor);
		fSublist.acceptInR(visitor);
	}
	
	@Override
	public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(fExpr.node);
		visitor.visit(fSublist);
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
		final SubIndexed other = (SubIndexed) element;
		return ((fExpr.node == other.fExpr.node
						|| (fExpr.node != null && fExpr.node.equalsSingle(other.fExpr.node)) )
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
		if (fClose2Offset != NA_OFFSET) {
			fStopOffset = fClose2Offset+1;
		}
		else if (fCloseOffset != NA_OFFSET) {
			fStopOffset = fCloseOffset+1;
		}
		else {
			fStopOffset = fSublist.fStopOffset;
		}
	}
	
}
