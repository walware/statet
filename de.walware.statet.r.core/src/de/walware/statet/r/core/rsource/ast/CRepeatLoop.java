/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource.ast;

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AS_BODY_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS3_REPEAT;

import java.lang.reflect.InvocationTargetException;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * <code>repeat §cont§</code>
 */
public class CRepeatLoop extends RAstNode {
	
	
	final Expression fLoopExpr = new Expression();
	
	
	CRepeatLoop() {
	}
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.C_REPEAT;
	}
	
	@Override
	public final RTerminal getOperator(final int index) {
		return RTerminal.WHILE;
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
			return fLoopExpr.node;
		default:
			throw new IndexOutOfBoundsException();
		}
	}
	
	@Override
	public final RAstNode[] getChildren() {
		return new RAstNode[] { fLoopExpr.node };
	}
	
	@Override
	public final int getChildIndex(final IAstNode child) {
		if (fLoopExpr.node == child) {
			return 0;
		}
		return -1;
	}
	
	public final RAstNode getContChild() {
		return fLoopExpr.node;
	}
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
		fLoopExpr.node.acceptInR(visitor);
	}
	
	@Override
	public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(fLoopExpr.node);
	}
	
	
	@Override
	final Expression getExpr(final RAstNode child) {
		if (fLoopExpr.node == child) {
			return fLoopExpr;
		}
		return null;
	}
	
	@Override
	final Expression getLeftExpr() {
		return null;
	}
	
	@Override
	final Expression getRightExpr() {
		return fLoopExpr;
	}
	
	@Override
	public final boolean equalsSingle(final RAstNode element) {
		return (element.getNodeType() == NodeType.C_REPEAT);
	}
	
	
	@Override
	final int getMissingExprStatus(final Expression expr) {
		if (fLoopExpr == expr) {
			return (STATUS2_SYNTAX_EXPR_AS_BODY_MISSING | STATUS3_REPEAT);
		}
		throw new IllegalArgumentException();
	}
	
	@Override
	final void updateStopOffset() {
		fStopOffset = fLoopExpr.node.fStopOffset;
	}
	
}
