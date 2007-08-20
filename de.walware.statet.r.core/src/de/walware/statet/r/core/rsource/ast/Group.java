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
public class Group extends RAstNode {
	
	
	final Expression fExpr = new Expression();
	int fGroupCloseOffset = Integer.MIN_VALUE;
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.GROUP;
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
			return fExpr.node;
		default:
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public final RAstNode[] getChildren() {
		return new RAstNode[] { fExpr.node };
	}
	
	@Override
	public final int getChildIndex(IAstNode child) {
		if (fExpr.node == child) {
			return 0;
		}
		return -1;
	}
	
	public final RAstNode getExprChild() {
		return fExpr.node;
	}
	
	public final int getGroupCloseOffset() {
		return fGroupCloseOffset;
	}
	
	@Override
	public final void accept(RAstVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInChildren(RAstVisitor visitor) {
		fExpr.node.accept(visitor);
	}
	
	public final void acceptInChildren(CommonAstVisitor visitor) {
		fExpr.node.accept(visitor);
	}

	
	@Override
	final Expression getExpr(RAstNode child) {
		if (fExpr.node == child) {
			return fExpr;
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
	
	@Override
	public final boolean equalsSingle(RAstNode element) {
		return (element.getNodeType() == NodeType.GROUP);
	}


	@Override
	final void updateStopOffset() {
		if (fGroupCloseOffset >= 0) {
			fStopOffset = fGroupCloseOffset+1;
		}
		else if (fExpr.node != null){
			fStopOffset = fExpr.node.fStopOffset;
		}
		else {
			fStopOffset = fStartOffset+1;
		}
	}

}
