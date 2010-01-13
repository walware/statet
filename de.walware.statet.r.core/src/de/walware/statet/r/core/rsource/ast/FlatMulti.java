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

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AFTER_OP_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_BEFORE_OP_MISSING;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


abstract class FlatMulti extends RAstNode {
	
	
	private final Expression fLeftExpr = new Expression();
	private final List<RTerminal> fOperators = new ArrayList<RTerminal>(1);
	private final List<Expression> fMultExpr = new ArrayList<Expression>(1);
	
	
	protected FlatMulti(final RTerminal firstOperator) {
		fMultExpr.add(new Expression());
		fOperators.add(firstOperator);
	}
	
	
	@Override
	public final boolean hasChildren() {
		return true;
	}
	
	@Override
	public final RAstNode[] getChildren() {
		final int n = fMultExpr.size()+1;
		final RAstNode[] children = new RAstNode[n];
		children[0] = fLeftExpr.node;
		for (int i = 1; i < n; i++) {
			children[i] = fMultExpr.get(i-1).node;
		}
		return children;
	}
	
	@Override
	public final int getChildIndex(final IAstNode child) {
		for (int i = fMultExpr.size()-1; i >= 0; i--) {
			if (fMultExpr.get(i).node == child) {
				return i+1;
			}
		}
		if (fLeftExpr.node == child) {
			return 0;
		}
		return -1;
	}
	
	@Override
	public final int getChildCount() {
		return 1+fMultExpr.size();
	}
	
	@Override
	public final RAstNode getChild(final int i) {
		if (i == 0) {
			return fLeftExpr.node;
		}
		return fMultExpr.get(i-1).node;
	}
	
	@Override
	public final RTerminal getOperator(final int i) {
		if (i == 0) {
			return null;
		}
		return fOperators.get(i-1);
	}
	
	@Override
	public void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
		fLeftExpr.node.acceptInR(visitor);
		acceptChildrenExpr(visitor, fMultExpr);
	}
	
	public void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(fLeftExpr.node);
		for (final Expression expr : fMultExpr) {
			visitor.visit(expr.node);
		}
	}
	
	
	@Override
	final Expression getExpr(final RAstNode child) {
		for (int i = fMultExpr.size()-1; i >= 0; i--) {
			if (fMultExpr.get(i).node == child) {
				return fMultExpr.get(i);
			}
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
		return fMultExpr.get(fMultExpr.size()-1);
	}
	
	final Expression appendComponent(final int stopOffset, final RTerminal operator) {
		fStopOffset = stopOffset;
		fOperators.add(operator);
		final Expression expr = new Expression();
		fMultExpr.add(expr);
		return expr;
	}
	
	
	@Override
	final int getMissingExprStatus(final Expression expr) {
		if (fLeftExpr == expr) {
			return STATUS2_SYNTAX_EXPR_BEFORE_OP_MISSING;
		}
		// if (fMultExpr.indexOf(expr) != Integer.MIN_VALUE) {
		return STATUS2_SYNTAX_EXPR_AFTER_OP_MISSING;
	}
	
	final void updateStartOffset() {
		fStartOffset = fLeftExpr.node.fStartOffset;
	}
	
	@Override
	final void updateStopOffset() {
		if (fMultExpr.size() > 0) {
			fStopOffset = getChild(getChildCount()-1).fStopOffset;
		}
	}
	
	
	@Override
	public final boolean equalsValue(final RAstNode element) {
		if (getNodeType() != element.getNodeType()) {
			return false;
		}
		final int count = getChildCount();
		if ((count != element.getChildCount())
				|| !getChild(0).equalsValue(element.getChild(0))) {
			return false;
		}
		for (int i = 1; i < count; i++) {
			if ((getOperator(i) != element.getOperator(i))
					|| !getChild(i).equalsValue(element.getChild(i))) {
				return false;
			}
		}
		return true;
	}
	
}
