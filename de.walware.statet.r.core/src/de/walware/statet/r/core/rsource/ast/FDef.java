/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.rsource.ast;

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AS_ARGVALUE_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AS_BODY_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS3_FDEF;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * <code>function( §args§ ) §cont§</code>
 */
public class FDef extends RAstNode {
	
	
	public static class Args extends RAstNode {
		
		
		final List<Arg> fSpecs;
		
		
		Args(final FDef parent) {
			fRParent = parent;
			fSpecs= new ArrayList<>(0);
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.F_DEF_ARGS;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return null;
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
		public final Arg getChild(final int index) {
			return fSpecs.get(index);
		}
		
		@Override
		public final Arg[] getChildren() {
			return fSpecs.toArray(new Arg[fSpecs.size()]);
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
		public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
			visitor.visit(this);
		}
		
		@Override
		public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
			acceptChildren(visitor, fSpecs);
		}
		
		@Override
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
			return (element.getNodeType() == NodeType.F_DEF_ARGS);
		}
		
		
		@Override
		final int getMissingExprStatus(final Expression expr) {
			throw new IllegalArgumentException();
		}
		
		@Override
		final void updateStopOffset() {
		}
		
	}
	
	
	public static class Arg extends RAstNode {
		
		
		SingleValue fArgName;
		boolean fWithDefault;
		final Expression fDefaultExpr = new Expression();
		
		
		Arg(final FDef.Args parent) {
			fRParent = parent;
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.F_DEF_ARG;
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
				return fArgName;
			case 1:
				if (fWithDefault) {
					return fDefaultExpr.node;
				}
				//$FALL-THROUGH$
			default:
				throw new IndexOutOfBoundsException();
			}
		}
		
		@Override
		public final RAstNode[] getChildren() {
			if (fWithDefault) {
				return new RAstNode[] { fArgName, fDefaultExpr.node };
			}
			else {
				return new RAstNode[] { fArgName };
			}
		}
		
		@Override
		public final int getChildIndex(final IAstNode child) {
			if (fArgName == child) {
				return 0;
			}
			if (fDefaultExpr.node == child) {
				return 1;
			}
			return -1;
		}
		
		public final RAstNode getNameChild() {
			return fArgName;
		}
		
		public final boolean hasDefault() {
			return fWithDefault;
		}
		
		public final RAstNode getDefaultChild() {
			return fDefaultExpr.node;
		}
		
		@Override
		public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
			visitor.visit(this);
		}
		
		@Override
		public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
			fArgName.acceptInR(visitor);
			if (fWithDefault) {
				fDefaultExpr.node.acceptInR(visitor);
			}
		}
		
		@Override
		public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
			visitor.visit(fArgName);
			if (fWithDefault) {
				visitor.visit(fDefaultExpr.node);
			}
		}
		
		
		@Override
		final Expression getExpr(final RAstNode child) {
			if (fDefaultExpr.node == child) {
				return fDefaultExpr;
			}
			return null;
		}
		
		@Override
		final Expression getLeftExpr() {
			return null;
		}
		
		@Override
		final Expression getRightExpr() {
			return fDefaultExpr;
		}
		
		Expression addDefault() {
			fWithDefault = true;
			return fDefaultExpr;
		}
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.F_DEF_ARG);
		}
		
		
		@Override
		final int getMissingExprStatus(final Expression expr) {
			if (fWithDefault && fDefaultExpr == expr) {
				return (STATUS2_SYNTAX_EXPR_AS_ARGVALUE_MISSING | STATUS3_FDEF);
			}
			throw new IllegalArgumentException();
		}
		
		final void updateStartOffset() {
			fStartOffset = fArgName.fStartOffset;
		}
		
		@Override
		final void updateStopOffset() {
			if (fDefaultExpr.node != null) {
				fStopOffset = fDefaultExpr.node.fStopOffset;
			}
			else {
				fStopOffset = fArgName.fStopOffset;
			}
		}
		
	}
	
	
	int fArgsOpenOffset = Integer.MIN_VALUE;
	Args fArgs = new Args(this);
	int fArgsCloseOffset = Integer.MIN_VALUE;
	final Expression fExpr = new Expression();
	
	
	FDef() {
	}
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.F_DEF;
	}
	
	@Override
	public final RTerminal getOperator(final int index) {
		return RTerminal.FUNCTION;
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
			return fArgs;
		case 1:
			return fExpr.node;
		default:
			throw new IndexOutOfBoundsException();
		}
	}
	
	@Override
	public final RAstNode[] getChildren() {
		return new RAstNode[] { fArgs, fExpr.node };
	}
	
	@Override
	public final int getChildIndex(final IAstNode child) {
		if (fArgs == child) {
			return 0;
		}
		if (fExpr.node == child) {
			return 1;
		}
		return -1;
	}
	
	public final int getArgsOpenOffset() {
		return fArgsOpenOffset;
	}
	
	public final FDef.Args getArgsChild() {
		return fArgs;
	}
	
	public final int getArgsCloseOffset() {
		return fArgsCloseOffset;
	}
	
	public final RAstNode getContChild() {
		return fExpr.node;
	}
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
		fArgs.acceptInR(visitor);
		fExpr.node.acceptInR(visitor);
	}
	
	@Override
	public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(fArgs);
		visitor.visit(fExpr.node);
	}
	
	
	@Override
	final Expression getExpr(final RAstNode child) {
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
		return fExpr;
	}
	
	@Override
	public final boolean equalsSingle(final RAstNode element) {
		return (element.getNodeType() == NodeType.F_DEF);
	}
	
	@Override
	final int getMissingExprStatus(final Expression expr) {
		if (expr == fExpr) {
			return (STATUS2_SYNTAX_EXPR_AS_BODY_MISSING | STATUS3_FDEF);
		}
		throw new IllegalArgumentException();
	}
	
	@Override
	final void updateStopOffset() {
		if (fExpr.node != null) {
			fStopOffset = fExpr.node.fStopOffset;
		}
		else if (fArgsCloseOffset != Integer.MIN_VALUE) {
			fStopOffset = fArgsCloseOffset+1;
		}
		else if (fArgs != null) {
			fStopOffset = fArgs.fStopOffset;
		}
		else if (fArgsOpenOffset != Integer.MIN_VALUE) {
			fStopOffset = fArgsOpenOffset+1;
		}
		else {
			fStopOffset = fStartOffset+8;
		}
	}
	
}
