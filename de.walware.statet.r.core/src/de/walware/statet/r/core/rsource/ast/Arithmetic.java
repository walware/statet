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
 * <code>§expr§ + §expr§ - §expr§</code>
 * <code>§expr§ * §expr§ / §expr§</code>
 */
public abstract class Arithmetic extends StdBinary {
	
	
	static class Plus extends Arithmetic {
		
		
		public Plus() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.ADD;
		}
		
		@Override
		public RTerminal getOperator(final int index) {
			return RTerminal.PLUS;
		}
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.ADD);
		}
		
	}
	
	static class Minus extends Arithmetic {
		
		
		public Minus() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.ADD;
		}
		
		@Override
		public RTerminal getOperator(final int index) {
			return RTerminal.MINUS;
		}
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.ADD);
		}
		
	}
	
	static class Mult extends Arithmetic {
		
		
		public Mult() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.MULT;
		}
		
		@Override
		public RTerminal getOperator(final int index) {
			return RTerminal.MULT;
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.MULT);
		}
		
	}
	
	static class Div extends Arithmetic {
		
		
		public Div() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.MULT;
		}
		
		@Override
		public RTerminal getOperator(final int index) {
			return RTerminal.DIV;
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.MULT);
		}
		
	}
	
	
	protected Arithmetic() {
	}
	
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
}
