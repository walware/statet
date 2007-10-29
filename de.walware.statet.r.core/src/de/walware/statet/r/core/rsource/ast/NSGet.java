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

import de.walware.eclipsecommons.ltk.ast.CommonAstVisitor;
import de.walware.eclipsecommons.ltk.ast.IAstNode;



/**
 *
 */
public abstract class NSGet extends RAstNode {
	
	public static class Std extends NSGet {
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.NS_GET;
		}

		@Override
		public final boolean equalsSingle(RAstNode element) {
			return (element.getNodeType() == NodeType.NS_GET);
		}

	}

	public static class Internal extends NSGet {
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.NS_GET_INT;
		}

		@Override
		public final boolean equalsSingle(RAstNode element) {
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
	public final RAstNode getChild(int index) {
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
	public final int getChildIndex(IAstNode child) {
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
	public final void accept(RAstVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInChildren(RAstVisitor visitor) {
		fNamespace.accept(visitor);
		fElement.accept(visitor);
	}

	public final void acceptInChildren(CommonAstVisitor visitor) {
		fNamespace.accept(visitor);
		fElement.accept(visitor);
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
	
	final void updateStartOffset() {
		fStartOffset = fNamespace.fStartOffset;
	}
	
	@Override
	final void updateStopOffset() {
		fStopOffset = getElementChild().fStopOffset;
	}

}
