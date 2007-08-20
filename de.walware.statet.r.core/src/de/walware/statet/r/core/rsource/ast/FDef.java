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

import java.util.ArrayList;
import java.util.List;

import de.walware.eclipsecommons.ltk.ast.CommonAstVisitor;
import de.walware.eclipsecommons.ltk.ast.IAstNode;

import de.walware.statet.r.core.rsource.RSourceToken;


/**
 *
 */
public class FDef extends RAstNode {
	
	
	public static class Args extends RAstNode {
		
		List<Arg> fSpecs = new ArrayList<Arg>(0);
		List<RSourceToken> fSeparatorSources;
		
		
		Args(FDef parent) {
			fParent = parent;
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.F_DEF_ARGS;
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
		public final RAstNode getChild(int index) {
			return fSpecs.get(index);
		}
		
		@Override
		public final RAstNode[] getChildren() {
			return fSpecs.toArray(new RAstNode[fSpecs.size()]);
		}
		
		@Override
		public final int getChildIndex(IAstNode child) {
			for (int i = fSpecs.size()-1; i >= 0; i--) {
				if (fSpecs.get(i) == child) {
					return i;
				}
			}
			return -1;
		}
		
		@Override
		final Expression getExpr(RAstNode child) {
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
			return (element.getNodeType() == NodeType.F_DEF_ARGS);
		}

		@Override
		public final void accept(RAstVisitor visitor) {
			visitor.visit(this);
		}
		
		@Override
		public final void acceptInChildren(RAstVisitor visitor) {
			acceptChildren(visitor, fSpecs);
		}

		public final void acceptInChildren(CommonAstVisitor visitor) {
			acceptChildren(visitor, fSpecs);
		}
		
		@Override
		final void updateStopOffset() {
		}

	}
	

	public static class Arg extends RAstNode {

		
		SingleValue fArgName;
		boolean fWithDefault;
		Expression fDefaultExpr;
		
		
		Arg(FDef.Args parent) {
			fParent = parent;
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.F_DEF_ARG;
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
				return fArgName;
			case 1:
				if (fWithDefault) {
					return fDefaultExpr.node;
				}
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
		public final int getChildIndex(IAstNode child) {
			if (fArgName == child) {
				return 0;
			}
			if (fDefaultExpr.node == child) {
				return 1;
			}
			return -1;
		}
		
		@Override
		final Expression getExpr(RAstNode child) {
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
			return fDefaultExpr = new Expression();
		}
		
		@Override
		public final boolean equalsSingle(RAstNode element) {
			return (element.getNodeType() == NodeType.F_DEF_ARG);
		}

		@Override
		public final void accept(RAstVisitor visitor) {
			visitor.visit(this);
		}
		
		@Override
		public final void acceptInChildren(RAstVisitor visitor) {
			fArgName.accept(visitor);
			if (fWithDefault) {
				fDefaultExpr.node.accept(visitor);
			}
		}

		public final void acceptInChildren(CommonAstVisitor visitor) {
			fArgName.accept(visitor);
			if (fWithDefault) {
				fDefaultExpr.node.accept(visitor);
			}
		}

		final void updateStartOffset() {
			fStartOffset = fArgName.fStartOffset;
		}
		
		@Override
		final void updateStopOffset() {
			if (fDefaultExpr != null) {
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
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.F_DEF;
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
	public final int getChildIndex(IAstNode child) {
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
	public final void accept(RAstVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInChildren(RAstVisitor visitor) {
		fArgs.accept(visitor);
		fExpr.node.accept(visitor);
	}

	public final void acceptInChildren(CommonAstVisitor visitor) {
		fArgs.accept(visitor);
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
		return fExpr;
	}
	
	@Override
	public final boolean equalsSingle(RAstNode element) {
		return (element.getNodeType() == NodeType.F_DEF);
	}

	@Override
	final void updateStopOffset() {
		if (fExpr.node != null) {
			fStopOffset = fExpr.node.fStopOffset;
		}
		else if (fArgsCloseOffset >= 0) {
			fStopOffset = fArgsCloseOffset+1;
		}
		else if (fArgs != null) {
			fStopOffset = fArgs.fStopOffset;
		}
		else if (fArgsOpenOffset >= 0) {
			fStopOffset = fArgsOpenOffset+1;
		}
		else {
			fStopOffset = fStartOffset+8;
		}
	}

}
