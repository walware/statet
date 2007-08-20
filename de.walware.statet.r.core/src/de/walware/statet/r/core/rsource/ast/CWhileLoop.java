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

import de.walware.eclipsecommons.ltk.ast.CommonAstVisitor;
import de.walware.eclipsecommons.ltk.ast.IAstNode;




/**
 *
 */
public class CWhileLoop extends RAstNode {
	

	int fCondOpenOffset = Integer.MIN_VALUE;
	final Expression fCondExpr = new Expression();
	int fCondCloseOffset = Integer.MIN_VALUE;
	final Expression fLoopExpr = new Expression();
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.C_WHILE;
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
	public final RAstNode getChild(int index) {
		switch (index) {
		case 0:
			return fCondExpr.node;
		case 1:
			return fLoopExpr.node;
		default:
			throw new IndexOutOfBoundsException();
		}
	}
	
	@Override
	public final RAstNode[] getChildren() {
		return new RAstNode[] { fCondExpr.node, fLoopExpr.node };
	}
	
	@Override
	public final int getChildIndex(IAstNode child) {
		if (fCondExpr.node == child) {
			return 0;
		}
		return -1;
	}

	@Override
	final Expression getExpr(RAstNode child) {
		if (fLoopExpr.node == child) {
			return fLoopExpr;
		}
		if (fCondExpr.node == child) {
			return fCondExpr;
		}
		return null;
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
	
	public final RAstNode getContChild() {
		return fLoopExpr.node;
	}

	@Override
	public final void accept(RAstVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInChildren(RAstVisitor visitor) {
		fCondExpr.node.accept(visitor);
		fLoopExpr.node.accept(visitor);
	}
	
	public final void acceptInChildren(CommonAstVisitor visitor) {
		fCondExpr.node.accept(visitor);
		fLoopExpr.node.accept(visitor);
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
	public final boolean equalsSingle(RAstNode element) {
		return (element.getNodeType() == NodeType.C_WHILE);
	}
	
	
	@Override
	void updateStopOffset() {
		if (fLoopExpr.node != null) {
			fStopOffset = fLoopExpr.node.fStopOffset;
		}
		else if (fCondCloseOffset >= 0) {
			fStopOffset = fCondCloseOffset+1;
		}
		else if (fCondExpr.node != null) {
			fStopOffset = fCondExpr.node.fStopOffset;
		}
		else if (fCondOpenOffset >= 0) {
			fStopOffset = fCondOpenOffset+1;
		}
		else {
			fStopOffset = fStartOffset+5;
		}
	}
	
}
