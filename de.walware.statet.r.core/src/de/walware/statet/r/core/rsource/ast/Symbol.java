/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
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
public abstract class Symbol extends SingleValue {
	
	
	static final class Std extends Symbol {
		
		
		Std() {
		}
		
		
		@Override
		public final RTerminal getOperator(final int index) {
			return null;
		}
		
	}
	
	
	static final class G extends Symbol {
		
		
		G() {
		}
		
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.SYMBOL_G;
		}
		
	}
	
	
	protected Symbol() {
	}
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.SYMBOL;
	}
	
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	
	@Override
	public final boolean equalsSingle(final RAstNode element) {
		if (element.getNodeType() != NodeType.SYMBOL) {
			return false;
		}
		final Symbol other = (Symbol) element;
		return ((fText == other.fText
						|| (fText != null && fText.equals(other.fText)) )
				);
	}
	
	@Override
	void appendPathElement(final StringBuilder s) {
//		if (fParent != null) {
//			s.append(fParent.getEqualsIndex(this));
//		}
		s.append('$');
		s.append(fText);
	}
	
}
