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

import java.util.List;

import org.eclipse.core.runtime.IStatus;

import de.walware.eclipsecommons.ltk.ast.IAstNode;
import de.walware.eclipsecommons.ltk.ast.CommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;



/**
 *
 */
public abstract class RAstNode implements IAstNode {
	
	
	interface Assoc {
		static final int TERM = 1;
		static final int CONTAINER = 10;
		static final int NOSTD = 100;
		static final int LEFTSTD = 110;
		static final int LEFTMULTI = 111;
		static final int RIGHTSTD = 120;
	}
	
	
	static final RAstNode[] NO_CHILDREN = new RAstNode[0];
	
	
	RAstNode fParent;
	int fStartOffset;
	int fStopOffset;
	IStatus fStatus;

	
	public abstract NodeType getNodeType();
	
	public final RAstNode getParent() {
		return fParent;
	}

	public final RAstNode getRoot() {
		RAstNode candidate = this;
		RAstNode p;
		while ((p = candidate.getParent()) != null) {
			candidate = p;
		}
		return candidate;
	}
	
	public abstract boolean hasChildren();
	public abstract int getChildCount();
	public abstract RAstNode getChild(int index);
	public abstract RAstNode[] getChildren();
	
	public final int getStartOffset() {
		return fStartOffset;
	}
	
	public final int getStopOffset() {
		return fStopOffset;
	}
	
	public abstract int getChildIndex(IAstNode child);
	
	int getEqualsIndex(RAstNode element) {
		RAstNode[] children = getChildren();
		int index = 0;
		for (RAstNode child : children) {
			if (child == element) {
				return index;
			}
			if (child.equalsSingle(element)) {
				index++;
			}
		}
		return -1;
	}
	
	abstract Expression getExpr(RAstNode child);
	abstract Expression getLeftExpr();
	abstract Expression getRightExpr();

	@Override
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RAstNode) || !equalsSingle((RAstNode) obj)) {
			return false;
		}
		
		RAstNode me = this;
		RAstNode other = (RAstNode) obj;
		while (me != other) {
			if (me.fParent == null || other.fParent == null) {
				return (me.fParent == null && other.fParent == null);
			}
			if ((!me.fParent.equalsSingle(other.fParent))
					|| (me.fParent.getEqualsIndex(me) != other.fParent.getEqualsIndex(other))
					) {
				return false;
			}
			me = me.fParent;
			other = other.fParent;
		}
		return true;
	}
	
	public abstract boolean equalsSingle(RAstNode element);

	void appendPathElement(StringBuilder s) {
//		if (fParent != null) {
//			s.append(fParent.getEqualsIndex(this));
//		}
		s.append('$');
		s.append(getNodeType().ordinal());
	}
	
	@Override
	public int hashCode() {
		StringBuilder path = new StringBuilder();
		if (fParent != null) {
			if (fParent.fParent != null) {
				path.append(fParent.fParent.getNodeType().ordinal());
			}
			path.append('$');
			path.append(fParent.getNodeType().ordinal());
		}
		appendPathElement(path);
		return path.toString().hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		RAstNode parent = getParent();
		if (parent != null) {
			if (parent instanceof FlatMulti) {
				FlatMulti multi = (FlatMulti) parent;
				RTerminal operator = multi.getOperator(multi.getChildIndex(this));
				s.append(operator != null ? operator.text : "•");
				s.append("  ");
			}
		}
//		s.append("«");
		s.append(getNodeType().label);
//		s.append(" § " + fStartOffset+","+fStopOffset);
//		s.append("»");
		return s.toString();
	}
	
	public void accept(CommonAstVisitor visitor) {
		visitor.visit(this);
	}
	public abstract void accept(RAstVisitor visitor);
	public abstract void acceptInChildren(RAstVisitor visitor);
	
	protected final void acceptChildren(RAstVisitor visitor, List<? extends RAstNode> children) {
		for (RAstNode child : children) {
			child.accept(visitor);
		}
	}

	protected final void acceptChildren(CommonAstVisitor visitor, List<? extends RAstNode> children) {
		for (RAstNode child : children) {
			child.accept(visitor);
		}
	}

	protected final void acceptChildrenExpr(RAstVisitor visitor, List<Expression> children) {
		for (Expression expr : children) {
			expr.node.accept(visitor);
		}
	}
	
	protected final void acceptChildrenExpr(CommonAstVisitor visitor, List<Expression> children) {
		for (Expression expr : children) {
			expr.node.accept(visitor);
		}
	}
	
	abstract void updateStopOffset();

}
