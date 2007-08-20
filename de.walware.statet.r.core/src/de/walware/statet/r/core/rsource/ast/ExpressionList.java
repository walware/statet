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

import java.util.ArrayList;
import java.util.List;

import de.walware.eclipsecommons.ltk.ast.CommonAstVisitor;
import de.walware.eclipsecommons.ltk.ast.IAstNode;

import de.walware.statet.r.core.rsource.RSourceToken;


/**
 *
 */
public abstract class ExpressionList extends RAstNode {
	
	
	List<Expression> fExpressions = new ArrayList<Expression>();
	List<RSourceToken> fSeparators;
	
	
	@Override
	public final boolean hasChildren() {
		return (fExpressions.size() > 0);
	}
	
	@Override
	public final RAstNode[] getChildren() {
		final int n = fExpressions.size();
		RAstNode[] children = new RAstNode[n];
		for (int i = 0; i < n; i++) {
			children[i] = fExpressions.get(i).node;
		}
		return children;
	}
	
	@Override
	public final int getChildIndex(IAstNode child) {
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
	public final RAstNode getChild(int i) {
		return fExpressions.get(i).node;
	}
	
	@Override
	public final void acceptInChildren(RAstVisitor visitor) {
		acceptChildrenExpr(visitor, fExpressions);
	}

	public final void acceptInChildren(CommonAstVisitor visitor) {
		acceptChildrenExpr(visitor, fExpressions);
	}

	
	@Override
	final Expression getExpr(RAstNode child) {
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
		Expression expr = new Expression();
		fExpressions.add(expr);
		return expr;
	}
	
	void setSeparator(int offset) {
	}

}
