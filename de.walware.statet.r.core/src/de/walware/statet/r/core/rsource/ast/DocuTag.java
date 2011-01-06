/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


public class DocuTag extends RAstNode {
	
	
	private String fName;
	RAstNode[] fFragments = NO_CHILDREN;
	
	
	DocuTag(final String name) {
		fName = name;
	}
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.DOCU_TAG;
	}
	
	@Override
	public final RTerminal getOperator(final int index) {
		return null;
	}
	
	@Override
	public final String getText() {
		return fName;
	}
	
	
	@Override
	public final RAstNode[] getChildren() {
		final RAstNode[] children = new RAstNode[fFragments.length];
		System.arraycopy(fFragments, 0, children, 0, fFragments.length);
		return children;
	}
	
	@Override
	public final boolean hasChildren() {
		return (fFragments.length > 0);
	}
	
	@Override
	public final int getChildCount() {
		return fFragments.length;
	}
	
	@Override
	public final RAstNode getChild(final int index) {
		return fFragments[index];
	}
	
	@Override
	public final int getChildIndex(final IAstNode child) {
		for (int i = 0; i < fFragments.length; i++) {
			if (fFragments[i] == child) {
				return i;
			}
		}
		return -1;
	}
	
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
	}
	
	@Override
	public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
		for (int i = 0; i < fFragments.length; i++) {
			fFragments[i].acceptInR(visitor);
		}
	}
	
	public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		for (int i = 0; i < fFragments.length; i++) {
			visitor.visit(fFragments[i]);
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
		if (element.getNodeType() != NodeType.DOCU_TAG) {
			return false;
		}
		final DocuTag other = (DocuTag) element;
		return ((fName != null) ? fName.equals(other.fName) : (other.fName == null));
	}
	
	
	@Override
	final int getMissingExprStatus(final Expression expr) {
		throw new IllegalArgumentException();
	}
	
	@Override
	final void updateStopOffset() {
	}
	
}
