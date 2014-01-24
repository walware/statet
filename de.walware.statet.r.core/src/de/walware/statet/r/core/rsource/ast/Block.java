/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
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
 * <code>{ ... }</code>
 */
public class Block extends ExpressionList {
	
	
	int fBlockCloseOffset = Integer.MIN_VALUE;
	
	
	Block() {
	}
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.BLOCK;
	}
	
	@Override
	public final RTerminal getOperator(final int index) {
		return RTerminal.BLOCK_OPEN;
	}
	
	public int getBlockCloseOffset() {
		return fBlockCloseOffset;
	}
	
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
	
	@Override
	public final boolean equalsSingle(final RAstNode element) {
		return (element.getNodeType() == NodeType.BLOCK);
	}
	
	
	@Override
	final void updateStopOffset() {
		if (fBlockCloseOffset != Integer.MIN_VALUE) {
			fStopOffset = fBlockCloseOffset+1;
		}
		else {
			final int count = getChildCount();
			if (count > 0) {
				fStopOffset = getChild(count-1).fStopOffset;
			}
			else {
				fStopOffset = fStartOffset+1;
			}
		}
	}
	
}
