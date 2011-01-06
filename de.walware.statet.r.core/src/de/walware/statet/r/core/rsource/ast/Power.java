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
 * <code>§base§ ^ §power§</code>
 */
public class Power extends StdBinary {
	
	
	Power() {
	}
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.POWER;
	}
	
	@Override
	public final RTerminal getOperator(final int index) {
		return RTerminal.POWER;
	}
	
	public final RAstNode getBaseChild() {
		return fLeftExpr.node;
	}
	
	public final RAstNode getExpChild() {
		return fRightExpr.node;
	}
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	
	@Override
	public final boolean equalsSingle(final RAstNode element) {
		return (element.getNodeType() == NodeType.POWER);
	}
	
	@Override
	public final boolean equalsValue(final RAstNode element) {
		return ((NodeType.POWER == element.getNodeType())
				&& fLeftExpr.node.equalsValue(element.getLeftExpr().node)
				&& fRightExpr.node.equalsValue(element.getRightExpr().node) );
	}
	
}
