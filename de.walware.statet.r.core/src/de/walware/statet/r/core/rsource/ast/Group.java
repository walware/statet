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

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_IN_GROUP_MISSING;

import java.lang.reflect.InvocationTargetException;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * <code>( ... )</code>
 */
public class Group extends RAstNode {
	
	
	final Expression fExpr = new Expression();
	int fGroupCloseOffset = Integer.MIN_VALUE;
	
	
	Group() {
	}
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.GROUP;
	}
	
	@Override
	public final RTerminal getOperator(final int index) {
		return RTerminal.GROUP_OPEN;
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
		switch (index) {
		case 0:
			return fExpr.node;
		default:
			throw new IndexOutOfBoundsException();
		}
	}
	
	@Override
	public final RAstNode[] getChildren() {
		return new RAstNode[] { fExpr.node };
	}
	
	@Override
	public final int getChildIndex(final IAstNode child) {
		if (fExpr.node == child) {
			return 0;
		}
		return -1;
	}
	
	public final RAstNode getExprChild() {
		return fExpr.node;
	}
	
	public final int getGroupCloseOffset() {
		return fGroupCloseOffset;
	}
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
		fExpr.node.acceptInR(visitor);
	}
	
	@Override
	public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(fExpr.node);
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
		return null;
	}
	
	@Override
	final Expression getRightExpr() {
		return null;
	}
	
	@Override
	public final boolean equalsSingle(final RAstNode element) {
		return (element.getNodeType() == NodeType.GROUP);
	}
	
	
	@Override
	final int getMissingExprStatus(final Expression expr) {
		if (fExpr == expr) {
			return STATUS2_SYNTAX_EXPR_IN_GROUP_MISSING;
		}
		throw new IllegalArgumentException();
	}
	
	@Override
	final void updateStopOffset() {
		if (fGroupCloseOffset != Integer.MIN_VALUE) {
			fStopOffset = fGroupCloseOffset+1;
		}
		else if (fExpr.node != null){
			fStopOffset = fExpr.node.fStopOffset;
		}
		else {
			fStopOffset = fStartOffset+1;
		}
	}
	
}
