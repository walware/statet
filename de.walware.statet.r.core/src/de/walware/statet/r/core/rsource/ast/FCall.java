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


/**
 * <code>§ref§ ( §args§ )</code>
 */
public class FCall extends RAstNode {
	
	
	public static class Args extends SpecList {
		
		
		Args(final FCall parent) {
			fRParent = parent;
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.F_CALL_ARGS;
		}
		
		@Override
		public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
			visitor.visit(this);
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.F_CALL_ARGS);
		}
		
		@Override
		final SpecItem createItem() {
			return new FCall.Arg(this);
		}
		
	}
	
	public static class Arg extends SpecItem {
		
		
		Arg(final FCall.Args parent) {
			fRParent = parent;
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.F_CALL_ARG;
		}
		
		@Override
		public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
			visitor.visit(this);
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.F_CALL_ARG);
		}
		
	}
	
	
	final Expression fRefExpr = new Expression();
	int fArgsOpenOffset = Integer.MIN_VALUE;
	final Args fArgs = new Args(this);
	int fArgsCloseOffset = Integer.MIN_VALUE;
	
	
	FCall() {
	}
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.F_CALL;
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
	public final RAstNode getChild(final int index) {
		switch (index) {
		case 0:
			return fRefExpr.node;
		case 1:
			return fArgs;
		default:
			throw new IndexOutOfBoundsException();
		}
	}
	
	@Override
	public final RAstNode[] getChildren() {
		return new RAstNode[] { fRefExpr.node, fArgs };
	}
	
	@Override
	public final int getChildIndex(final IAstNode child) {
		if (fRefExpr.node == child) {
			return 0;
		}
		if (fArgs == child) {
			return 1;
		}
		return -1;
	}
	
	public final RAstNode getRefChild() {
		return fRefExpr.node;
	}
	
	public final int getArgsOpenOffset() {
		return fArgsOpenOffset;
	}
	
	public final FCall.Args getArgsChild() {
		return fArgs;
	}
	
	public final int getArgsCloseOffset() {
		return fArgsCloseOffset;
	}
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
		fRefExpr.node.acceptInR(visitor);
		fArgs.acceptInR(visitor);
	}
	
	public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		fRefExpr.node.accept(visitor);
		fArgs.accept(visitor);
	}
	
	
	@Override
	final Expression getExpr(final RAstNode child) {
		if (fRefExpr.node == child) {
			return fRefExpr;
		}
		return null;
	}
	
	@Override
	final Expression getLeftExpr() {
		return fRefExpr;
	}
	
	@Override
	final Expression getRightExpr() {
		return null;
	}
	
	@Override
	public final boolean equalsSingle(final RAstNode element) {
		if (element.getNodeType() != NodeType.F_CALL) {
			return false;
		}
		final RAstNode otherExprNode = ((FCall) element).fRefExpr.node;
		return ((fRefExpr.node == otherExprNode
				|| fRefExpr.node != null && fRefExpr.node.equalsSingle(otherExprNode)) );
	}
	
	
	final void updateStartOffset() {
		fStartOffset = fRefExpr.node.fStartOffset;
	}
	
	@Override
	final void updateStopOffset() {
		if (fArgsCloseOffset >= 0) {
			fStopOffset = fArgsCloseOffset+1;
		}
		else {
			fStopOffset = fArgs.fStopOffset;
		}
	}
	
}
