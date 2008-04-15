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

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_OK;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import de.walware.eclipsecommons.ltk.ast.IAstNode;
import de.walware.eclipsecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * A node of a R AST
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
	static final Object[] NO_ATTACHMENT = new Object[0];
	
	
	RAstNode fRParent;
	int fStartOffset;
	int fStopOffset;
	int fStatus;
	private Object[] fAttachments;
	
	
	protected RAstNode() {
		fStatus = STATUS_OK;
		fAttachments = NO_ATTACHMENT;
	}
	
	protected RAstNode(final int status) {
		fStatus = status;
		fAttachments = NO_ATTACHMENT;
	}
	
	
	public abstract NodeType getNodeType();
	
	public abstract RTerminal getOperator(final int index);
	
	public final int getStatusCode() {
		return fStatus;
	}
	
	public String getText() {
		return null;
	}
	
	
	public final RAstNode getParent() {
		return fRParent;
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
	public abstract int getChildIndex(IAstNode child);
	
	
	public final int getOffset() {
		return fStartOffset;
	}
	
	public final int getStopOffset() {
		return fStopOffset;
	}
	
	public final int getLength() {
		return fStopOffset-fStartOffset;
	}
	
	
	int getEqualsIndex(final RAstNode element) {
		final RAstNode[] children = getChildren();
		int index = 0;
		for (final RAstNode child : children) {
			if (child == element) {
				return index;
			}
			if (child.equalsSingle(element)) {
				index++;
			}
		}
		return -1;
	}
	
	
	public void accept(final ICommonAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	public abstract void acceptInR(RAstVisitor visitor) throws InvocationTargetException;
	public abstract void acceptInRChildren(RAstVisitor visitor) throws InvocationTargetException;
	
	protected final void acceptChildren(final RAstVisitor visitor, final List<? extends RAstNode> children) throws InvocationTargetException {
		for (final RAstNode child : children) {
			child.acceptInR(visitor);
		}
	}
	
	protected final void acceptChildren(final ICommonAstVisitor visitor, final List<? extends RAstNode> children) throws InvocationTargetException {
		for (final RAstNode child : children) {
			child.accept(visitor);
		}
	}
	
	protected final void acceptChildrenExpr(final RAstVisitor visitor, final List<Expression> children) throws InvocationTargetException {
		for (final Expression expr : children) {
			expr.node.acceptInR(visitor);
		}
	}
	
	protected final void acceptChildrenExpr(final ICommonAstVisitor visitor, final List<Expression> children) throws InvocationTargetException {
		for (final Expression expr : children) {
			expr.node.accept(visitor);
		}
	}
	
	
	abstract Expression getExpr(RAstNode child);
	abstract Expression getLeftExpr();
	abstract Expression getRightExpr();
	
	public final boolean equalsIgnoreAst(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RAstNode) || !equalsSingle((RAstNode) obj)) {
			return false;
		}
		
		RAstNode me = this;
		RAstNode other = (RAstNode) obj;
		while (me != other) {
			if (me.fRParent == null || other.fRParent == null) {
				return (me.fRParent == null && other.fRParent == null);
			}
			if ((!me.fRParent.equalsSingle(other.fRParent))
					|| (me.fRParent.getEqualsIndex(me) != other.fRParent.getEqualsIndex(other))
					) {
				return false;
			}
			me = me.fRParent;
			other = other.fRParent;
		}
		return true;
	}
	
	abstract boolean equalsSingle(RAstNode element);
	
	
	void appendPathElement(final StringBuilder s) {
//		if (fParent != null) {
//			s.append(fParent.getEqualsIndex(this));
//		}
		s.append('$');
		s.append(getNodeType().ordinal());
	}
	
	public int hashCodeIgnoreAst() {
		final StringBuilder path = new StringBuilder();
		if (fRParent != null) {
			if (fRParent.fRParent != null) {
				path.append(fRParent.fRParent.getNodeType().ordinal());
			}
			path.append('$');
			path.append(fRParent.getNodeType().ordinal());
		}
		appendPathElement(path);
		return path.toString().hashCode();
	}
	
	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		final RAstNode parent = getParent();
		if (parent != null) {
			if (parent instanceof FlatMulti) {
				final FlatMulti multi = (FlatMulti) parent;
				final RTerminal operator = multi.getOperator(multi.getChildIndex(this));
				s.append(operator != null ? operator.text : "•"); //$NON-NLS-1$
				s.append("  "); //$NON-NLS-1$
			}
		}
//		s.append("«");
		s.append(getNodeType().label);
//		s.append(" § " + fStartOffset+","+fStopOffset);
//		s.append("»");
		return s.toString();
	}
	
	
	abstract int getMissingExprStatus(Expression expr);
	
	abstract void updateStopOffset();
	
	
	public void addAttachment(final Object data) {
		if (fAttachments == NO_ATTACHMENT) {
			fAttachments = new Object[] { data };
		}
		else {
			final Object[] newArray = new Object[fAttachments.length+1];
			System.arraycopy(fAttachments, 0, newArray, 0, fAttachments.length);
			newArray[fAttachments.length] = data;
			fAttachments = newArray;
		}
	}
	
	public Object[] getAttachments() {
		return fAttachments;
	}
	
}
