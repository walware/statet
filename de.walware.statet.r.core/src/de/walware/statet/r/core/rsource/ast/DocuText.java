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


public class DocuText extends RAstNode {
	
	
	private final String fText;
	
	
	DocuText(final String text) {
		fText = text;
	}
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.DOCU_TEXT;
	}
	
	@Override
	public final RTerminal getOperator(final int index) {
		return null;
	}
	
	@Override
	public final String getText() {
		return fText;
	}
	
	
	@Override
	public final RAstNode[] getChildren() {
		return NO_CHILDREN;
	}
	
	@Override
	public final boolean hasChildren() {
		return false;
	}
	
	@Override
	public final int getChildCount() {
		return 0;
	}
	
	@Override
	public final RAstNode getChild(final int index) {
		throw new IndexOutOfBoundsException();
	}
	
	@Override
	public final int getChildIndex(final IAstNode child) {
		return -1;
	}
	
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
	}
	
	@Override
	public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
	}
	
	public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
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
		final DocuText other = (DocuText) element;
		return ((fText != null) ? fText.equals(other.fText) : (null == other.fText));
	}
	
	
	@Override
	final int getMissingExprStatus(final Expression expr) {
		throw new IllegalArgumentException();
	}
	
	@Override
	final void updateStopOffset() {
	}
	
}
