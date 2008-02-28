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


/**
 *
 */
public class Symbol extends SingleValue {
	
	
	static class G extends Symbol {
		
	}
	
	
	@Override
	public final NodeType getNodeType() {
		return NodeType.SYMBOL;
	}
	
	@Override
	public final boolean equalsSingle(final RAstNode element) {
		if (element.getNodeType() != NodeType.SYMBOL) {
			return false;
		}
		final Symbol other = (Symbol) element;
		return (	(this.fText == other.fText
						|| (this.fText != null && other.fText != null && this.fText.equals(other.fText)) )
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
	
	
	@Override
	public final void acceptInR(final RAstVisitor visitor) throws InvocationTargetException {
		visitor.visit(this);
	}
	
}
