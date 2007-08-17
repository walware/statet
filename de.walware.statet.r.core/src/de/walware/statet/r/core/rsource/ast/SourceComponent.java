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



/**
 *
 */
public class SourceComponent extends ExpressionList {
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.SOURCELINES;
	}
	
	@Override
	public final boolean equalsSingle(RAstNode element) {
		return (element.getNodeType() == NodeType.SOURCELINES);
	}

	@Override
	public final void accept(RAstVisitor visitor) {
		visitor.visit(this);
	}
	
	final void updateStartOffset() {
		if (getChildCount() > 0) {
			fStartOffset = getChild(0).fStartOffset;
		}
		else {
			fStartOffset = 0;
		}
	}
	
	@Override
	final void updateStopOffset() {
		final int count = getChildCount();
		if (count > 0) {
			fStopOffset = getChild(count-1).fStopOffset;
		}
		else {
			fStopOffset = 0;
		}
	}
	
}
