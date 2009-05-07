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

import java.lang.reflect.InvocationTargetException;

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * <code>§namespace§ :: §element§</code>
 * <code>§namespace§ ::: §element§</code>
 */
public abstract class NSGet extends RAstNode {
	
	static class Std extends NSGet {
		
		
		Std() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.NS_GET;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.NS_GET;
		}
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.NS_GET);
		}
		
	}
	
	static class Internal extends NSGet {
		
		
		Internal() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.NS_GET_INT;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.NS_GET_INT;
		}
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.NS_GET_INT);
		}
		
	}
	
	
	SingleValue fNamespace;
	int fOperatorOffset;
	SingleValue fElement;
	
	
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
			return fNamespace;
		case 1:
			return fElement;
		default:
			throw new IndexOutOfBoundsException();
		}
	}
	
	@Override
	public final RAstNode[] getChildren() {
		return new RAstNode[] { fNamespace, fElement };
	}
	
	@Override
	public final int getChildIndex(final IAstNode child) {
		if (fNamespace == child) {
			return 0;
		}
		if (fElement == child) {
			return 1;
		}
		return -1;
	}
	
	public final RAstNode getNamespaceChild() {
		return fNamespace;
	}
	
	public final RAstNode getElementChild() {
		return fElement;
	}
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
		fNamespace.acceptInR(visitor);
		fElement.acceptInR(visitor);
	}
	
	public final void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(fNamespace);
		visitor.visit(fElement);
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
	final int getMissingExprStatus(final Expression expr) {
		throw new IllegalArgumentException();
	}
	
	final void updateStartOffset() {
		fStartOffset = fNamespace.fStartOffset;
	}
	
	@Override
	final void updateStopOffset() {
		fStopOffset = fElement.fStopOffset;
	}
	
}
