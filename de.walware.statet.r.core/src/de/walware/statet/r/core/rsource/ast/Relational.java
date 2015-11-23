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

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * <code>§left§ &lt; §right§</code>
 * <code>§left§ &lt;= §right§</code>
 * <code>§left§ == §right§</code>
 * <code>§left§ =&gt; §right§</code>
 * <code>§left§ &gt; §right§</code>
 * <code>§left§ != §right§</code>
 */
public abstract class Relational extends StdBinary {
	
	
	static final class LT extends Relational {
		
		
		LT() {
		}
		
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.REL_LT;
		}
		
		
		@Override
		public boolean equalsValue(final RAstNode element) {
			return ((NodeType.RELATIONAL == element.getNodeType())
					&& ((	(element.getOperator(0) == RTerminal.REL_LT)
								&& fLeftExpr.node.equalsValue(element.getLeftExpr().node)
								&& fRightExpr.node.equalsValue(element.getRightExpr().node) )
						||	(element.getOperator(0) == RTerminal.REL_GE)
								&& fLeftExpr.node.equalsValue(element.getRightExpr().node)
								&& fRightExpr.node.equalsValue(element.getLeftExpr().node) ) );
		}
		
	}
	
	static final class LE extends Relational {
		
		
		LE() {
		}
		
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.REL_LE;
		}
		
		
		@Override
		public boolean equalsValue(final RAstNode element) {
			return ((NodeType.RELATIONAL == element.getNodeType())
					&& ((	(element.getOperator(0) == RTerminal.REL_LE)
								&& fLeftExpr.node.equalsValue(element.getLeftExpr().node)
								&& fRightExpr.node.equalsValue(element.getRightExpr().node) )
						||	(element.getOperator(0) == RTerminal.REL_GT)
								&& fLeftExpr.node.equalsValue(element.getRightExpr().node)
								&& fRightExpr.node.equalsValue(element.getLeftExpr().node) ) );
		}
		
	}
	
	static final class EQ extends Relational {
		
		
		EQ() {
		}
		
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.REL_EQ;
		}
		
		
		@Override
		public boolean equalsValue(final RAstNode element) {
			return ((NodeType.RELATIONAL == element.getNodeType())
					&& (element.getOperator(0) == RTerminal.REL_EQ)
					&& fLeftExpr.node.equalsValue(element.getLeftExpr().node)
					&& fRightExpr.node.equalsValue(element.getRightExpr().node) );
		}
		
	}
	
	static final class GE extends Relational {
		
		
		GE() {
		}
		
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.REL_GE;
		}
		
		
		@Override
		public boolean equalsValue(final RAstNode element) {
			return ((NodeType.RELATIONAL == element.getNodeType())
					&& ((	(element.getOperator(0) == RTerminal.REL_GE)
								&& fLeftExpr.node.equalsValue(element.getLeftExpr().node)
								&& fRightExpr.node.equalsValue(element.getRightExpr().node) )
						||	(element.getOperator(0) == RTerminal.REL_LT)
								&& fLeftExpr.node.equalsValue(element.getRightExpr().node)
								&& fRightExpr.node.equalsValue(element.getLeftExpr().node) ) );
		}
		
	}
	
	static final class GT extends Relational {
		
		
		GT() {
		}
		
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.REL_GT;
		}
		
		
		@Override
		public boolean equalsValue(final RAstNode element) {
			return ((NodeType.RELATIONAL == element.getNodeType())
					&& ((	(element.getOperator(0) == RTerminal.REL_GT)
								&& fLeftExpr.node.equalsValue(element.getLeftExpr().node)
								&& fRightExpr.node.equalsValue(element.getRightExpr().node) )
						||	(element.getOperator(0) == RTerminal.REL_LE)
								&& fLeftExpr.node.equalsValue(element.getRightExpr().node)
								&& fRightExpr.node.equalsValue(element.getLeftExpr().node) ) );
		}
		
	}
	
	static final class NE extends Relational {
		
		
		NE() {
		}
		
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.REL_NE;
		}
		
		
		@Override
		public boolean equalsValue(final RAstNode element) {
			return ((NodeType.RELATIONAL == element.getNodeType())
					&& (element.getOperator(0) == RTerminal.REL_NE)
					&& fLeftExpr.node.equalsValue(element.getLeftExpr().node)
					&& fRightExpr.node.equalsValue(element.getRightExpr().node) );
		}
		
	}
	
	
	protected Relational() {
	}
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.RELATIONAL;
	}
	
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	
	@Override
	public final boolean equalsSingle(final RAstNode element) {
		return (element.getNodeType() == NodeType.RELATIONAL);
	}
	
}
