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

import de.walware.eclipsecommons.ltk.ast.CommonAstVisitor;
import de.walware.eclipsecommons.ltk.ast.IAstNode;



/**
 *
 */
abstract class SingleValue extends RAstNode {
	
	
	String fText;
	
	
	@Override
	public final boolean hasChildren() {
		return false;
	}
	
	@Override
	public final int getChildCount() {
		return 0;
	}
	
	@Override
	public final RAstNode getChild(int index) {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public final RAstNode[] getChildren() {
		return NO_CHILDREN;
	}
	
	@Override
	public final int getChildIndex(IAstNode child) {
		return -1;
	}
	
	@Override
	public final void acceptInChildren(RAstVisitor visitor) {
	}

	public final void acceptInChildren(CommonAstVisitor visitor) {
	}
	

	@Override
	final Expression getExpr(RAstNode child) {
		return null;
	}
	
	@Override
	final Expression getLeftExpr() {
		return null;
	}
	
	@Override
	final Expression getRightExpr() {
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder(super.toString());
		if (fText != null) {
			s.append(" ◊ ");
			appendEscaped(s, fText);
		}
		return s.toString();
	}
	
	void appendEscaped(StringBuilder builder, CharSequence text) {
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			switch (c) {
			case '\\':
				builder.append("\\\\");
				break;
			case '\n':
				builder.append("\\n");
				break;
			case '\r':
				builder.append("\\r");
				break;
			case '\t':
				builder.append("\\t");
				break;
			default:
				builder.append(c);
				break;
			}
		}
		
	}
	
	@Override
	void updateStopOffset() {
		if (fText != null) {
			fStopOffset = fStartOffset+fText.length();
		}
	}
}
