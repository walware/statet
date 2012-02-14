/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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


public abstract class Comment extends RAstNode {
	
	
	static final class RoxygenLine extends Comment {
		
		
		public RoxygenLine() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.COMMENT;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.ROXYGEN_COMMENT;
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
		public final RAstNode[] getChildren() {
			return NO_CHILDREN;
		}
		
		@Override
		public final int getChildIndex(final IAstNode child) {
			return -1;
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			if (element.getNodeType() != NodeType.COMMENT
					|| element.getOperator(0) != RTerminal.ROXYGEN_COMMENT) {
				return false;
			}
			// ?
			return true;
		}
		
	}
	
	static final class CommonLine extends Comment {
		
		
		public CommonLine() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.COMMENT;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.COMMENT;
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
		public final RAstNode[] getChildren() {
			return NO_CHILDREN;
		}
		
		@Override
		public final int getChildIndex(final IAstNode child) {
			return -1;
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			if (element.getNodeType() != NodeType.COMMENT
					|| element.getOperator(0) != RTerminal.ROXYGEN_COMMENT) {
				return false;
			}
			// ?
			return true;
		}
		
	}
	
	
	Comment() {
	}
	
	
	@Override
	public void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
	}
	
	@Override
	public void acceptInRChildren(final RAstVisitor visitor) throws InvocationTargetException {
	}
	
	@Override
	public void acceptInChildren(final ICommonAstVisitor visitor) throws InvocationTargetException {
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
	
	@Override
	final void updateStopOffset() {
	}
	
}
