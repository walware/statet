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

import de.walware.statet.r.core.rlang.RTerminal;


/**
 *
 */
public abstract class Assignment extends StdBinary {
	
	
	static class LeftS extends Assignment {
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.A_LEFT_S;
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
		
		public final RTerminal getOperator() {
			return RTerminal.ARROW_LEFT_S;
		}

		@Override
		public final boolean equalsSingle(RAstNode element) {
			switch (element.getNodeType()) {
			case A_LEFT_S:
			case A_RIGHT_S:
			case A_LEFT_E:
				return super.equalsSingle(element);
			default:
				return false;
			}
		}
		
	}

	static class LeftD extends Assignment {
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.A_LEFT_D;
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
		
		public final RTerminal getOperator() {
			return RTerminal.ARROW_LEFT_S;
		}
		
		@Override
		public final boolean equalsSingle(RAstNode element) {
			switch (element.getNodeType()) {
			case A_LEFT_D:
			case A_RIGHT_D:
				return super.equalsSingle(element);
			default:
				return false;
			}
		}
		
	}

	static class LeftE extends Assignment {
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.A_LEFT_E;
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
		
		public final RTerminal getOperator() {
			return RTerminal.EQUAL;
		}
		
		@Override
		public final boolean equalsSingle(RAstNode element) {
			switch (element.getNodeType()) {
			case A_LEFT_S:
			case A_RIGHT_S:
			case A_LEFT_E:
				return super.equalsSingle(element);
			default:
				return false;
			}
		}
		
	}
	
	static class RightS extends Assignment {
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.A_RIGHT_S;
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
		
		public final RTerminal getOperator() {
			return RTerminal.ARROW_RIGHT_S;
		}
		
		@Override
		public final boolean equalsSingle(RAstNode element) {
			switch (element.getNodeType()) {
			case A_LEFT_S:
			case A_RIGHT_S:
			case A_LEFT_E:
				return super.equalsSingle(element);
			default:
				return false;
			}
		}
		
	}
	
	static class RightD extends Assignment {
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.A_RIGHT_D;
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
		
		public final RTerminal getOperator() {
			return RTerminal.ARROW_RIGHT_D;
		}
		
		@Override
		public final boolean equalsSingle(RAstNode element) {
			switch (element.getNodeType()) {
			case A_LEFT_D:
			case A_RIGHT_D:
				return super.equalsSingle(element);
			default:
				return false;
			}
		}
		
	}
	
	
	public abstract RAstNode getTargetChild();
	public abstract RAstNode getSourceChild();

	@Override
	public final void accept(RAstVisitor visitor) {
		visitor.visit(this);
	}
	
	abstract Expression getTargetExpr();
	abstract Expression getSourceExpr();

	
	@Override
	public boolean equalsSingle(RAstNode element) {
		RAstNode thisTarget = getTargetExpr().node;
		RAstNode otherTarget = ((Assignment) element).getTargetExpr().node;
		return (	((thisTarget == otherTarget)
						|| (thisTarget != null && otherTarget != null && thisTarget.equalsSingle(otherTarget)) )
				);
	}
	
}
