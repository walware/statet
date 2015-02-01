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
 * 
 */
public abstract class StringConst extends SingleValue {
	
	
	static class S extends StringConst {
		
		
		S() {
		}
		
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.STRING_S;
		}
		
	}
	
	static class D extends StringConst {
		
		
		D() {
		}
		
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.STRING_D;
		}
		
	}
	
	
	protected StringConst() {
	}
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.STRING_CONST;
	}
	
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	
	@Override
	public final boolean equalsSingle(final RAstNode element) {
		return (element.getNodeType() == NodeType.STRING_CONST);
	}
	
}
