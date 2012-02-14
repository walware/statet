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

import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;


/**
 * 
 */
abstract class SingleValue extends RAstNode {
	
	
	String fText;
	
	
	protected SingleValue() {
	}
	
	
	@Override
	public final String getText() {
		return fText;
	}
	
	@Override
	public final boolean hasChildren() {
		return false;
	}
	
	@Override
	public final int getChildCount() {
		return 0;
	}
	
	@Override
	public final RAstNode getChild(final int index) {
		throw new IndexOutOfBoundsException();
	}
	
	@Override
	public final RAstNode[] getChildren() {
		return NO_CHILDREN;
	}
	
	@Override
	public final int getChildIndex(final IAstNode child) {
		return -1;
	}
	
	@Override
	public final void acceptInRChildren(final RAstVisitor visitor) {
	}
	
	@Override
	public final void acceptInChildren(final ICommonAstVisitor visitor) {
	}
	
	
	@Override
	final Expression getExpr(final RAstNode child) {
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
	public boolean equalsValue(final RAstNode element) {
		return ((element.getNodeType() == getNodeType())
				&& ((fText != null) ? fText.equals(element.getText()) : element.getText() == null) );
	}
	
	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder(super.toString());
		if (fText != null) {
			s.append(" ◊ "); //$NON-NLS-1$
			appendEscaped(s, fText);
		}
		return s.toString();
	}
	
	void appendEscaped(final StringBuilder builder, final CharSequence text) {
		for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			switch (c) {
			case '\\':
				builder.append("\\\\"); //$NON-NLS-1$
				break;
			case '\n':
				builder.append("\\n"); //$NON-NLS-1$
				break;
			case '\r':
				builder.append("\\r"); //$NON-NLS-1$
				break;
			case '\t':
				builder.append("\\t"); //$NON-NLS-1$
				break;
			default:
				builder.append(c);
				break;
			}
		}
		
	}
	
	
	@Override
	final int getMissingExprStatus(final Expression expr) {
		throw new IllegalArgumentException();
	}
	
	@Override
	void updateStopOffset() {
		if (fText != null) {
			fStopOffset = fStartOffset+fText.length();
		}
	}
	
}
