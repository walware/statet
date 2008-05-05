/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
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
 * <code>§left§ &lt; §right§</code>
 * <code>§left§ &lt;= §right§</code>
 * <code>§left§ == §right§</code>
 * <code>§left§ =&gt; §right§</code>
 * <code>§left§ &gt; §right§</code>
 * <code>§left§ != §right§</code>
 */
public abstract class Relational extends StdBinary {
	
	
	static class LT extends Relational {
		
		
		LT() {
		}
		
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.REL_LT;
		}
		
	}
	
	static class LE extends Relational {
		
		
		LE() {
		}
		
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.REL_LE;
		}
		
	}
	
	static class EQ extends Relational {
		
		
		EQ() {
		}
		
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.REL_EQ;
		}
		
	}
	
	static class GE extends Relational {
		
		
		GE() {
		}
		
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.REL_GE;
		}
		
	}
	
	static class GT extends Relational {
		
		
		GT() {
		}
		
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.REL_GT;
		}
		
	}
	
	static class NE extends Relational {
		
		
		NE() {
		}
		
		
		@Override
		public final RTerminal getOperator(final int index) {
			return RTerminal.REL_NE;
		}
		
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
