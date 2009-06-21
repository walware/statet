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

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AS_REF_MISSING;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * <code>§ref§ ( §args§ )</code>
 */
public class FCall extends RAstNode {
	
	
	public static class Args extends RAstNode {
		
		
		final List<FCall.Arg> fSpecs;
		
		
		Args(final FCall parent) {
			fRParent = parent;
			fSpecs = new ArrayList<FCall.Arg>(0);
		}
		
		Args(final List<FCall.Arg> args) {
			fRParent = null;
			fSpecs = args;
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.F_CALL_ARGS;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return null;
		}
		
		@Override
		public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
			visitor.visit(this);
		}
		
		
		@Override
		public final boolean hasChildren() {
			return (!fSpecs.isEmpty());
		}
		
		@Override
		public final int getChildCount() {
			return fSpecs.size();
		}
		
		@Override
		public final FCall.Arg getChild(final int index) {
			return fSpecs.get(index);
		}
		
		@Override
		public final FCall.Arg[] getChildren() {
			return fSpecs.toArray(new FCall.Arg[fSpecs.size()]);
		}
		
		@Override
		public final int getChildIndex(final IAstNode child) {
			for (int i = fSpecs.size()-1; i >= 0; i--) {
				if (fSpecs.get(i) == child) {
					return i;
				}
			}
			return -1;
		}
		
		@Override
		public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
			acceptChildren(visitor, fSpecs);
		}
		
		public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
			for (final RAstNode child : fSpecs) {
				visitor.visit(child);
			}
		}
		
		
		@Override
		final Expression getExpr(final RAstNode child) {
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
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.F_CALL_ARGS);
		}
		
		@Override
		final int getMissingExprStatus(final Expression expr) {
			throw new IllegalArgumentException();
		}
		
		@Override
		final void updateStopOffset() {
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
	public final RTerminal getOperator(final int index) {
		return null;
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
		visitor.visit(fRefExpr.node);
		visitor.visit(fArgs);
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
	
	
	@Override
	final int getMissingExprStatus(final Expression expr) {
		if (expr == fRefExpr) {
			return STATUS2_SYNTAX_EXPR_AS_REF_MISSING;
		}
		throw new IllegalArgumentException();
	}
	
	final void updateStartOffset() {
		fStartOffset = fRefExpr.node.fStartOffset;
	}
	
	@Override
	final void updateStopOffset() {
		if (fArgsCloseOffset != Integer.MIN_VALUE) {
			fStopOffset = fArgsCloseOffset+1;
		}
		else {
			fStopOffset = fArgs.fStopOffset;
		}
	}
	
}
