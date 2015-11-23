/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
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

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * <code>next</code>
 * <code>break</code>
 */
public abstract class CLoopCommand extends RAstNode {
	
	
	static final class Break extends CLoopCommand {
		
		
		Break() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.C_BREAK;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.BREAK;
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.C_BREAK);
		}
		
		@Override
		public final boolean equalsValue(final RAstNode element) {
			return (element.getNodeType() == NodeType.C_BREAK);
		}
		
		@Override
		public final RTerminal getTerminal() {
			return RTerminal.BREAK;
		}
		
	}
	
	static final class Next extends CLoopCommand {
		
		
		Next() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.C_NEXT;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.NEXT;
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.C_NEXT);
		}
		
		@Override
		public boolean equalsValue(final RAstNode element) {
			return (element.getNodeType() == NodeType.C_NEXT);
		}
		
		@Override
		public final RTerminal getTerminal() {
			return RTerminal.NEXT;
		}
		
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
	
	public abstract RTerminal getTerminal();
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	@Override
	public final void acceptInRChildren(final RAstVisitor visitor) {
	}
	
	@Override
	public final void acceptInChildren(final ICommonAstVisitor visitor) {
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
