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
 * <code>&</code>
 * <code>|</code>
 */
public abstract class Logical extends FlatMulti {
	
	
	static class Or extends Logical {
		
		
		Or(final RTerminal firstOperator) {
			super(firstOperator);
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.OR;
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.OR);
		}
		
	}
	
	static class And extends Logical {
		
		
		And(final RTerminal firstOperator) {
			super(firstOperator);
		}
		
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.AND;
		}
		
		
		@Override
		public final boolean equalsSingle(final RAstNode element) {
			return (element.getNodeType() == NodeType.AND);
		}
		
	}
	
	
	protected Logical(final RTerminal firstOperator) {
		super(firstOperator);
	}
	
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
}
