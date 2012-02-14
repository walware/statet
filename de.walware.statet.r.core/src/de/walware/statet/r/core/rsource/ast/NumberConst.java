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
 * 
 */
public class NumberConst extends SingleValue {
	
	
	private final RTerminal fType;
	
	
	NumberConst(final RTerminal type) {
		fType = type;
	}
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.NUM_CONST;
	}
	
	@Override
	public final RTerminal getOperator(final int index) {
		return fType;
	}
	
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	
	@Override
	public final boolean equalsSingle(final RAstNode element) {
		return (element.getNodeType() == NodeType.NUM_CONST);
	}
	
}
