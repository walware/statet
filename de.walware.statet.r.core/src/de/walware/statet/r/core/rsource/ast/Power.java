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


class Power extends StdBinary {
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.POWER;
	}
	
	public final RTerminal getOperator() {
		return RTerminal.POWER;
	}
	
	public final RAstNode getBaseChild() {
		return fLeftExpr.node;
	}

	public final RAstNode getExpChild() {
		return fRightExpr.node;
	}
	
	@Override
	public final void accept(RAstVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public final boolean equalsSingle(RAstNode element) {
		return (element.getNodeType() == NodeType.POWER);
	}
	
}