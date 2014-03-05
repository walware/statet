/*=============================================================================#
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.workbench.search.ui;


public class LineElement<E> implements IMatchGroup<E> {
	
	
	private final E element;
	
	private final int lineNumber;
	private final int startOffset;
	private final String text;
	
	
	public LineElement(final E element,
			final int lineNumber, final int lineStartOffset, final String text) {
		this.element= element;
		this.lineNumber= lineNumber;
		this.startOffset= lineStartOffset;
		this.text= text;
	}
	
	
	@Override
	public E getElement() {
		return this.element;
	}
	
	public int getLine() {
		return this.lineNumber;
	}
	
	public String getText() {
		return this.text;
	}
	
	public int getOffset() {
		return this.startOffset;
	}
	
	public boolean contains(final int offset) {
		return this.startOffset <= offset && offset < this.startOffset + this.text.length();
	}
	
	public int getLength() {
		return this.text.length();
	}
	
}
