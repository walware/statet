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

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AFTER_OP_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_OK;

import java.lang.reflect.InvocationTargetException;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * <code>? §topic§</code>
 * <code>§type§ ? §topic§</code>
 */
public class Help extends RAstNode {
	
	
	final Expression fLeftExpr = new Expression();
	final Expression fRightExpr = new Expression();
	
	
	Help() {
	}
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.HELP;
	}
	
	@Override
	public final RTerminal getOperator(final int index) {
		return RTerminal.QUESTIONMARK;
	}
	
	@Override
	public final boolean hasChildren() {
		return true;
	}
	
	@Override
	public final int getChildCount() {
		return (fLeftExpr.node != null) ? 2 : 1;
	}
	
	@Override
	public final RAstNode getChild(final int index) {
		if (fLeftExpr.node != null) {
			switch (index) {
			case 0:
				return fLeftExpr.node;
			case 1:
				return fRightExpr.node;
			default:
				break;
			}
		}
		else if (index == 0) {
			return fRightExpr.node;
		}
		throw new IndexOutOfBoundsException();
	}
	
	@Override
	public final RAstNode[] getChildren() {
		if (fLeftExpr.node != null) {
			return new RAstNode[] { fLeftExpr.node, fRightExpr.node };
		}
		return new RAstNode[] { fRightExpr.node };
	}
	
	@Override
	public final int getChildIndex(final IAstNode child) {
		if (fLeftExpr.node == child) {
			return 0;
		}
		if (fRightExpr.node == child) {
			return 1;
		}
		return -1;
	}
	
	public final RAstNode getTypeChild() {
		return fLeftExpr.node;
	}
	
	public final boolean hasType() {
		return (fLeftExpr.node != null);
	}
	
	public final RAstNode getTopicChild() {
		return fRightExpr.node;
	}
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
		if (fLeftExpr.node != null) {
			fLeftExpr.node.acceptInR(visitor);
		}
		fRightExpr.node.acceptInR(visitor);
	}
	
	@Override
	public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		if (fLeftExpr.node != null) {
			visitor.visit(fLeftExpr.node);
		}
		fRightExpr.node.accept(visitor);
	}
	
	
	@Override
	final Expression getExpr(final RAstNode child) {
		if (fRightExpr.node == child) {
			return fRightExpr;
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
		return fRightExpr;
	}
	
	@Override
	public final boolean equalsSingle(final RAstNode element) {
		return (element.getNodeType() == NodeType.HELP);
	}
	
	
	@Override
	final int getMissingExprStatus(final Expression expr) {
		if (fRightExpr == expr) {
			return STATUS2_SYNTAX_EXPR_AFTER_OP_MISSING;
		}
		if (fLeftExpr == expr) {
			return STATUS_OK;
		}
		throw new IllegalArgumentException();
	}
	
	@Override
	final void updateStopOffset() {
		fStopOffset = getTopicChild().fStopOffset;
	}
	
}