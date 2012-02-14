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
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AS_CONDITION_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS3_ELSE;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS3_IF;

import java.lang.reflect.InvocationTargetException;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * <code>if ( §cond§ ) §then§</code>
 * <code>if ( §cond§ ) §then§ else §else§</code>
 */
public class CIfElse extends RAstNode {
	
	boolean fWithElse = false;
	
	int fCondOpenOffset = Integer.MIN_VALUE;
	final Expression fCondExpr = new Expression();
	int fCondCloseOffset = Integer.MIN_VALUE;
	final Expression fThenExpr = new Expression();
	int fElseOffset = Integer.MIN_VALUE;
	final Expression fElseExpr = new Expression();
	
	
	CIfElse() {
	}
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.C_IF;
	}
	
	@Override
	public final RTerminal getOperator(final int index) {
		return null;
	}
	
	
	public final boolean hasElse() {
		return fWithElse;
	}
	
	@Override
	public final boolean hasChildren() {
		return true;
	}
		
	@Override
	public final int getChildCount() {
		return fWithElse ? 3 : 2;
	}
	
	@Override
	public final RAstNode getChild(final int index) {
		switch (index) {
		case 0:
			return fCondExpr.node;
		case 1:
			return fThenExpr.node;
		case 2:
			if (fWithElse) {
				return fElseExpr.node;
			}
			//$FALL-THROUGH$
		default:
			throw new IndexOutOfBoundsException();
		}
	}
	
	@Override
	public final RAstNode[] getChildren() {
		if (fWithElse) {
			return new RAstNode[] { fCondExpr.node, fThenExpr.node, fElseExpr.node };
		}
		else {
			return new RAstNode[] { fCondExpr.node, fThenExpr.node };
		}
	}
	
	@Override
	public final int getChildIndex(final IAstNode child) {
		if (fCondExpr.node == child) {
			return 0;
		}
		if (fThenExpr.node == child) {
			return 1;
		}
		if (fElseExpr.node == child) {
			return 2;
		}
		return -1;
	}
	
	public final int getCondOpenOffset() {
		return fCondOpenOffset;
	}
	
	public final RAstNode getCondChild() {
		return fCondExpr.node;
	}
	
	public final int getCondCloseOffset() {
		return fCondCloseOffset;
	}
	
	public final RAstNode getThenChild() {
		return fThenExpr.node;
	}
	
	public final int getElseOffset() {
		return fElseOffset;
	}
	
	public final RAstNode getElseChild() {
		return fElseExpr.node;
	}
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
		fCondExpr.node.acceptInR(visitor);
		fThenExpr.node.acceptInR(visitor);
		if (fWithElse) {
			fElseExpr.node.acceptInR(visitor);
		}
	}
	
	@Override
	public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(fCondExpr.node);
		visitor.visit(fThenExpr.node);
		if (fWithElse) {
			visitor.visit(fElseExpr.node);
		}
	}
	
	
	@Override
	final Expression getExpr(final RAstNode child) {
		if (fThenExpr.node == child) {
			return fThenExpr;
		}
		if (fElseExpr.node == child) {
			return fElseExpr;
		}
		if (fCondExpr.node == child) {
			return fCondExpr;
		}
		return null;
	}
	
	@Override
	final Expression getLeftExpr() {
		return fThenExpr;
	}
	
	@Override
	final Expression getRightExpr() {
		if (fWithElse) {
			return fElseExpr;
		}
		return fThenExpr;
	}
	
	@Override
	public final boolean equalsSingle(final RAstNode element) {
		return (element.getNodeType() == NodeType.C_IF);
	}
	
	
	@Override
	final int getMissingExprStatus(final Expression expr) {
		if (fCondExpr == expr) {
			return (STATUS2_SYNTAX_EXPR_AS_CONDITION_MISSING | STATUS3_IF);
		}
		if (fThenExpr == expr) {
			return (STATUS2_SYNTAX_EXPR_AS_BODY_MISSING | STATUS3_IF);
		}
		if (fWithElse && fElseExpr == expr) {
			return (STATUS2_SYNTAX_EXPR_AS_BODY_MISSING | STATUS3_ELSE);
		}
		throw new IllegalArgumentException();
	}
	
	@Override
	final void updateStopOffset() {
		if (fWithElse && fElseExpr.node != null) {
			fStopOffset = fElseExpr.node.fStopOffset;
		}
		else if (fWithElse && fElseOffset != Integer.MIN_VALUE) {
			fStopOffset = fElseOffset+4;
		}
		else if (fThenExpr.node != null) {
			fStopOffset = fThenExpr.node.fStopOffset;
		}
		else if (fCondCloseOffset != Integer.MIN_VALUE) {
			fStopOffset = fCondCloseOffset+1;
		}
		else if (fCondExpr.node != null) {
			fStopOffset = fCondExpr.node.fStopOffset;
		}
		else if (fCondOpenOffset != Integer.MIN_VALUE) {
			fStopOffset = fCondOpenOffset+1;
		}
		else {
			fStopOffset = fStartOffset+2;
		}
	}
	
}
