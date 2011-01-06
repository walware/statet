/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * <code>§target§ &lt;- §source§</code>
 * <code>§target§ &lt;&lt;- §source§</code>
 * <code>§source§ -&gt; §target§</code>
 * <code>§source§ -&gt;&gt; §target§</code>
 * <code>§target§ = §source§</code>
 */
public abstract class Assignment extends StdBinary {
	
	
	static class LeftS extends Assignment {
		
		
		LeftS() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.A_LEFT;
		}
		
		@Override
		public final RAstNode getTargetChild() {
			return fLeftExpr.node;
		}
		
		@Override
		public final RAstNode getSourceChild() {
			return fRightExpr.node;
		}
		
		@Override
		final Expression getTargetExpr() {
			return fLeftExpr;
		}
		
		@Override
		final Expression getSourceExpr() {
			return fRightExpr;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.ARROW_LEFT_S;
		}
		
		@Override
		public final boolean isSearchOperator() {
			return false;
		}
		
	}
	
	
	static class LeftD extends Assignment {
		
		
		LeftD() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.A_LEFT;
		}
		
		@Override
		public final RAstNode getTargetChild() {
			return fLeftExpr.node;
		}
		
		@Override
		public final RAstNode getSourceChild() {
			return fRightExpr.node;
		}
		
		@Override
		final Expression getTargetExpr() {
			return fLeftExpr;
		}
		
		@Override
		final Expression getSourceExpr() {
			return fRightExpr;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.ARROW_LEFT_D;
		}
		
		@Override
		public final boolean isSearchOperator() {
			return true;
		}
		
	}
	
	
	static class LeftE extends Assignment {
		
		
		LeftE() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.A_EQUALS;
		}
		
		@Override
		public final RAstNode getTargetChild() {
			return fLeftExpr.node;
		}
		
		@Override
		public final RAstNode getSourceChild() {
			return fRightExpr.node;
		}
		
		@Override
		final Expression getTargetExpr() {
			return fLeftExpr;
		}
		
		@Override
		final Expression getSourceExpr() {
			return fRightExpr;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.EQUAL;
		}
		
		@Override
		public final boolean isSearchOperator() {
			return false;
		}
		
	}
	
	
	static class RightS extends Assignment {
		
		
		RightS() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.A_RIGHT;
		}
		
		@Override
		public final RAstNode getTargetChild() {
			return fRightExpr.node;
		}
		
		@Override
		public final RAstNode getSourceChild() {
			return fLeftExpr.node;
		}
		
		@Override
		final Expression getTargetExpr() {
			return fRightExpr;
		}
		
		@Override
		final Expression getSourceExpr() {
			return fRightExpr;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.ARROW_RIGHT_S;
		}
		
		@Override
		public final boolean isSearchOperator() {
			return false;
		}
		
	}
	
	
	static class RightD extends Assignment {
		
		
		RightD() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.A_RIGHT;
		}
		
		@Override
		public final RAstNode getTargetChild() {
			return fRightExpr.node;
		}
		
		@Override
		public final RAstNode getSourceChild() {
			return fLeftExpr.node;
		}
		
		@Override
		final Expression getTargetExpr() {
			return fRightExpr;
		}
		
		@Override
		final Expression getSourceExpr() {
			return fRightExpr;
		}
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.ARROW_RIGHT_D;
		}
		
		@Override
		public final boolean isSearchOperator() {
			return true;
		}
		
	}
	
	
	public abstract RAstNode getTargetChild();
	
	public abstract RAstNode getSourceChild();
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	
	public abstract boolean isSearchOperator();
	
	abstract Expression getTargetExpr();
	
	abstract Expression getSourceExpr();
	
	@Override
	public final boolean equalsSingle(final RAstNode element) {
		if (!(element instanceof Assignment)) {
			return false;
		}
		final Assignment other = (Assignment) element;
		final RAstNode thisTarget = getTargetExpr().node;
		final RAstNode otherTarget = other.getTargetExpr().node;
		return (	(isSearchOperator() == other.isSearchOperator())
				&& 	((thisTarget == otherTarget)
						|| (thisTarget != null && otherTarget != null && thisTarget.equalsSingle(otherTarget)) )
				);
	}
	
	@Override
	public final boolean equalsValue(final RAstNode element) {
		return ((getOperator(0) == element.getOperator(0))
				&& fLeftExpr.node.equalsValue(element.getLeftExpr().node)
				&& fRightExpr.node.equalsValue(element.getRightExpr().node) );
	}
	
}
