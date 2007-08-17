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
public abstract class Arithmetic extends FlatMulti {
	

	static class Add extends Arithmetic {
		
		public Add(RTerminal firstOperator) {
			super(firstOperator);
		}
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.ADD;
		}
		
		@Override
		public final boolean equalsSingle(RAstNode element) {
			return (element.getNodeType() == NodeType.ADD);
		}
		
	}

	static class Mult extends Arithmetic {
		
		public Mult(RTerminal firstOperator) {
			super(firstOperator);
		}
		
		@Override
		public final NodeType getNodeType() {
			return NodeType.MULT;
		}
		
		@Override
		public final boolean equalsSingle(RAstNode element) {
			return (element.getNodeType() == NodeType.MULT);
		}
		
	}


	protected Arithmetic(RTerminal firstOperator) {
		super(firstOperator);
	}
	
	
	@Override
	public final void accept(RAstVisitor visitor) {
		visitor.visit(this);
	}
	
}
