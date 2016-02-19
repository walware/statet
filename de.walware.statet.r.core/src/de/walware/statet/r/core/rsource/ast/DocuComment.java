/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.rsource.ast;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * Node for a documentation comment (currently always Roxygen)
 * 
 * The children are its comment lines.
 * The documentation structure is accessible via {@link #getTags()}.
 */
public final class DocuComment extends RAstNode {
	
	
	int fNextOffset = NA_OFFSET;
	Comment[] fLines;
	ImList<DocuTag> tags;
	
	
	public DocuComment() {
	}
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.DOCU_AGGREGATION;
	}
	
	@Override
	public final RTerminal getOperator(final int index) {
		return RTerminal.ROXYGEN_COMMENT;
	}
	
	public final int getSubsequentNodeOffset() {
		return fNextOffset;
	}
	
	
	public List<DocuTag> getTags() {
		return this.tags;
	}
	
	@Override
	public final boolean hasChildren() {
		return (fLines.length > 0);
	}
	
	@Override
	public final int getChildCount() {
		return fLines.length;
	}
	
	@Override
	public final Comment getChild(final int index) {
		return fLines[index];
	}
	
	@Override
	public final RAstNode[] getChildren() {
		final RAstNode[] children = new RAstNode[fLines.length];
		System.arraycopy(fLines, 0, children, 0, fLines.length);
		return children;
	}
	
	@Override
	public final int getChildIndex(final IAstNode child) {
		for (int i = 0; i < fLines.length; i++) {
			if (fLines[i] == child) {
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
		for (final Comment child : fLines) {
			visitor.visit(child);
		}
	}
	
	public final void acceptInRDocu(final RAstVisitor visitor) throws InvocationTargetException {
		for (final DocuTag tag : this.tags) {
			visitor.visit(tag);
		}
	}
	
	@Override
	public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		for (int i = 0; i < fLines.length; i++) {
			visitor.visit(fLines[i]);
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
		if (element.getNodeType() != NodeType.F_CALL) {
			return false;
		}
		return true;
	}
	
	
	@Override
	final int getMissingExprStatus(final Expression expr) {
		throw new IllegalArgumentException();
	}
	
	@Override
	final void updateStopOffset() {
	}
	
}
