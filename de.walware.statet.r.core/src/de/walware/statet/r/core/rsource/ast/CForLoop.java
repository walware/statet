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
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AS_FORSEQ_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS3_FOR;

import java.lang.reflect.InvocationTargetException;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * <code>for ( §var§ in §cond§ ) §cont§</code>
 */
public class CForLoop extends RAstNode {
	
	
	Symbol fVarSymbol;
	int fCondOpenOffset = Integer.MIN_VALUE;
	int fInOffset = Integer.MIN_VALUE;
	final Expression fCondExpr = new Expression();
	int fCondCloseOffset = Integer.MIN_VALUE;
	final Expression fLoopExpr = new Expression();
	
	
	CForLoop() {
	}
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.C_FOR;
	}
	
	@Override
	public final RTerminal getOperator(final int index) {
		return RTerminal.FOR;
	}
	
	
	@Override
	public final boolean hasChildren() {
		return true;
	}
	
	@Override
	public final int getChildCount() {
		return 3;
	}
	
	@Override
	public final RAstNode getChild(final int index) {
		switch (index) {
		case 0:
			return fVarSymbol;
		case 1:
			return fCondExpr.node;
		case 2:
			return fLoopExpr.node;
		default:
			throw new IndexOutOfBoundsException();
		}
	}
	
	@Override
	public final RAstNode[] getChildren() {
		return new RAstNode[] { fVarSymbol, fCondExpr.node, fLoopExpr.node };
	}
	
	@Override
	public final int getChildIndex(final IAstNode child) {
		if (fVarSymbol == child) {
			return 0;
		}
		if (fCondExpr.node == child) {
			return 1;
		}
		if (fLoopExpr.node == child) {
			return 2;
		}
		return -1;
	}
	
	public final int getCondOpenOffset() {
		return fCondOpenOffset;
	}
	
	public final Symbol getVarChild() {
		return fVarSymbol;
	}
	
	public final RAstNode getCondChild() {
		return fCondExpr.node;
	}
	
	public final int getCondCloseOffset() {
		return fCondCloseOffset;
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
		fVarSymbol.acceptInR(visitor);
		fCondExpr.node.acceptInR(visitor);
		fLoopExpr.node.acceptInR(visitor);
	}
	
	@Override
	public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(fVarSymbol);
		visitor.visit(fCondExpr.node);
		visitor.visit(fLoopExpr.node);
	}
	
	
	@Override
	final Expression getExpr(final RAstNode child) {
		if (fLoopExpr.node == child) {
			return fLoopExpr;
		}
		if (fCondExpr.node == child) {
			return fCondExpr;
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
		return (element.getNodeType() == NodeType.C_FOR);
	}
	
	
	@Override
	final int getMissingExprStatus(final Expression expr) {
		if (fCondExpr == expr) {
			return (STATUS2_SYNTAX_EXPR_AS_FORSEQ_MISSING | STATUS3_FOR);
		}
		if (fLoopExpr == expr) {
			return (STATUS2_SYNTAX_EXPR_AS_BODY_MISSING | STATUS3_FOR);
		}
		throw new IllegalArgumentException();
	}
	
	@Override
	final void updateStopOffset() {
		if (fLoopExpr.node != null) {
			fStopOffset = fLoopExpr.node.fStopOffset;
		}
		else if (fCondCloseOffset != Integer.MIN_VALUE) {
			fStopOffset = fCondCloseOffset+1;
		}
		else if (fCondExpr.node != null) {
			fStopOffset = fCondExpr.node.fStopOffset;
		}
		else if (fInOffset != Integer.MIN_VALUE) {
			fStopOffset = fInOffset+2;
		}
		else if (fVarSymbol != null) {
			fStopOffset = fVarSymbol.fStopOffset;
		}
		else if (fCondOpenOffset != Integer.MIN_VALUE) {
			fStopOffset = fCondOpenOffset+1;
		}
		else {
			fStopOffset = fStartOffset+3;
		}
	}
	
}
