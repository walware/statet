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

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS1_SYNTAX_MISSING_TOKEN;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;


/**
 * <code>expr; expr; ...</code>
 */
abstract class ExpressionList extends RAstNode {
	
	
	final List<Expression> fExpressions = new ArrayList<Expression>();
	
	
	ExpressionList() {
	}
	
	
	@Override
	public final boolean hasChildren() {
		return (fExpressions.size() > 0);
	}
	
	@Override
	public final RAstNode[] getChildren() {
		final int n = fExpressions.size();
		final RAstNode[] children = new RAstNode[n];
		for (int i = 0; i < n; i++) {
			children[i] = fExpressions.get(i).node;
		}
		return children;
	}
	
	@Override
	public final int getChildIndex(final IAstNode child) {
		for (int i = fExpressions.size()-1; i >= 0; i--) {
			if (fExpressions.get(i).node == child) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public final int getChildCount() {
		return fExpressions.size();
	}
	
	@Override
	public final RAstNode getChild(final int i) {
		return fExpressions.get(i).node;
	}
	
	@Override
	public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
		acceptChildrenExpr(visitor, fExpressions);
	}
	
	public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		acceptChildrenExpr(visitor, fExpressions);
	}
	
	
	@Override
	final Expression getExpr(final RAstNode child) {
		for (int i = fExpressions.size()-1; i >= 0; i--) {
			if (fExpressions.get(i).node == child) {
				return fExpressions.get(i);
			}
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
	
	Expression appendNewExpr() {
		final Expression expr = new Expression();
		fExpressions.add(expr);
		return expr;
	}
	
	void setSeparator(final int offset) {
	}
	
	
	@Override
	final int getMissingExprStatus(final Expression expr) {
		return STATUS1_SYNTAX_MISSING_TOKEN;
	}
	
}
