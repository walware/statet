/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource.ast;

import java.lang.reflect.InvocationTargetException;

import de.walware.eclipsecommons.ltk.ast.IAstNode;
import de.walware.eclipsecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * <code>~</code>
 */
public class Model extends RAstNode {
	
	
	final Expression fLeftExpr = new Expression();
	final Expression fRightExpr = new Expression();
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.MODEL;
	}
	
	public final RTerminal getOperator() {
		return RTerminal.TILDE;
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
	
	public final boolean hasLeft() {
		return (fLeftExpr.node != null);
	}
	
	public final RAstNode getLeftChild() {
		return fLeftExpr.node;
	}
	
	public final RAstNode getRightChild() {
		return fRightExpr.node;
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
	
	public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		if (fLeftExpr.node != null) {
			fLeftExpr.node.accept(visitor);
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
		return (element.getNodeType() == NodeType.MODEL);
		
	}
	final void updateStartOffset() {
		if (fLeftExpr.node != null) {
			fStartOffset = fLeftExpr.node.fStartOffset;
		}
	}
	
	@Override
	final void updateStopOffset() {
		fStopOffset = getRightChild().fStopOffset;
	}
	
}
