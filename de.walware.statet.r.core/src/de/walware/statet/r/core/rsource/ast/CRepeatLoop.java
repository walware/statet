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
public class CRepeatLoop extends RAstNode {
	
	
	final Expression fLoopExpr = new Expression();
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.C_REPEAT;
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
	public final RAstNode getChild(int index) {
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
	public final int getChildIndex(IAstNode child) {
		if (fLoopExpr.node == child) {
			return 0;
		}
		return -1;
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
		fLoopExpr.node.accept(visitor);
	}

	public final void acceptInChildren(CommonAstVisitor visitor) {
		fLoopExpr.node.accept(visitor);
	}
	
	@Override
	final Expression getExpr(RAstNode child) {
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
	public final boolean equalsSingle(RAstNode element) {
		return (element.getNodeType() == NodeType.C_REPEAT);
	}
	
	@Override
	final void updateStopOffset() {
		fStopOffset = fLoopExpr.node.fStopOffset;
	}

}
