/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.rsource.ast;

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_OK;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import de.walware.ecommons.collections.ImCollections;
import de.walware.ecommons.collections.ImList;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * A node of a R AST
 */
public abstract class RAstNode implements IAstNode {
	
	
	interface Assoc {
		byte TERM = 1;
		byte CONTAINER = 2;
		byte NOSTD = 3;
		byte LEFTSTD = 4;
		byte RIGHTSTD = 5;
	}
	
	
	static final RAstNode[] NO_CHILDREN = new RAstNode[0];
	
	private static final ImList<Object> NO_ATTACHMENT= ImCollections.emptyList();
	
	
	RAstNode fRParent;
	int fStartOffset;
	int fStopOffset;
	int fStatus;
	
	private ImList<Object> attachments;
	
	
	protected RAstNode() {
		fStatus = STATUS_OK;
		this.attachments= NO_ATTACHMENT;
	}
	
	protected RAstNode(final int status) {
		fStatus = status;
		this.attachments= NO_ATTACHMENT;
	}
	
	
	public abstract NodeType getNodeType();
	
	public abstract RTerminal getOperator(final int index);
	
	@Override
	public final int getStatusCode() {
		return fStatus;
	}
	
	public String getText() {
		return null;
	}
	
	/**
	 * @return the parent node, if it is an RAstNode too, otherwise <code>null</code>
	 */
	public final RAstNode getRParent() {
		return fRParent;
	}
	
	@Override
	public IAstNode getParent() {
		return fRParent;
	};
	
	public final RAstNode getRRoot() {
		RAstNode candidate = this;
		RAstNode p;
		while ((p = candidate.fRParent) != null) {
			candidate = p;
		}
		return candidate;
	}
	
	@Override
	public final IAstNode getRoot() {
		IAstNode candidate = this;
		IAstNode p;
		while ((p = candidate.getParent()) != null) {
			candidate = p;
		}
		return candidate;
	}
	
	@Override
	public abstract boolean hasChildren();
	@Override
	public abstract int getChildCount();
	@Override
	public abstract RAstNode getChild(int index);
	public abstract RAstNode[] getChildren();
	@Override
	public abstract int getChildIndex(IAstNode child);
	
	
	@Override
	public final int getOffset() {
		return fStartOffset;
	}
	
	@Override
	public final int getStopOffset() {
		return fStopOffset;
	}
	
	@Override
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
	
	
	@Override
	public final void accept(final ICommonAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	public abstract void acceptInR(RAstVisitor visitor) throws InvocationTargetException;
	
	public abstract void acceptInRChildren(RAstVisitor visitor) throws InvocationTargetException;
	
	protected final void acceptChildren(final RAstVisitor visitor, final List<? extends RAstNode> children) throws InvocationTargetException {
		for (final RAstNode child : children) {
			child.acceptInR(visitor);
		}
	}
	
	protected final void acceptChildrenExpr(final RAstVisitor visitor, final List<Expression> children) throws InvocationTargetException {
		for (final Expression expr : children) {
			expr.node.acceptInR(visitor);
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
	
	public boolean equalsValue(final RAstNode element) {
		if (getNodeType() != element.getNodeType()) {
			return false;
		}
		final int count = getChildCount();
		if (count != element.getChildCount()) {
			return false;
		}
		for (int i = 0; i < count; i++) {
			if (!getChild(i).equalsValue(element.getChild(i))) {
				return false;
			}
		}
		return true;
	}
	
	
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
//		s.append("«");
		s.append(getNodeType().label);
//		s.append(" § " + fStartOffset+","+fStopOffset);
//		s.append("»");
		return s.toString();
	}
	
	
	abstract int getMissingExprStatus(Expression expr);
	
	abstract void updateStopOffset();
	
	
	@Override
	public synchronized void addAttachment(final Object data) {
		this.attachments= ImCollections.addElement(this.attachments, data);
	}
	
	@Override
	public synchronized void removeAttachment(final Object data) {
		this.attachments= ImCollections.removeElement(this.attachments, data);
	}
	
	@Override
	public ImList<Object> getAttachments() {
		return this.attachments;
	}
	
}
