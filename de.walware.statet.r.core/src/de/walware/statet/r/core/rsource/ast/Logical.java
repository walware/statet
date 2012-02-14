/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
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
 * <code>&</code>
 * <code>|</code>
 */
public abstract class Logical extends StdBinary {
	
	
	static class Or extends Logical {
		
		
		Or() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.OR;
		}
		
		@Override
		public RTerminal getOperator(final int index) {
			return RTerminal.OR;
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.OR);
		}
		
	}
	
	static class OrD extends Logical {
		
		
		OrD() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.OR;
		}
		
		@Override
		public RTerminal getOperator(final int index) {
			return RTerminal.OR_D;
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.OR);
		}
		
	}
	
	static class And extends Logical {
		
		
		And() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.AND;
		}
		
		@Override
		public RTerminal getOperator(final int index) {
			return RTerminal.AND;
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.AND);
		}
		
	}
	
	static class AndD extends Logical {
		
		
		AndD() {
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.AND;
		}
		
		@Override
		public RTerminal getOperator(final int index) {
			return RTerminal.AND_D;
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.AND);
		}
		
	}
	
	
	protected Logical() {
	}
	
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
}
