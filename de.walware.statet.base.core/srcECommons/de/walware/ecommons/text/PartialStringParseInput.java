/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.text;


/**
 * The string represents a range of the source.
 */
public class PartialStringParseInput extends SourceParseInput {
	
	
	private final int fShift;
	private final char[] fStringContent;
	
	
	public PartialStringParseInput(final String content, final int offsetInSource) {
		fShift = offsetInSource;
		fStringContent = content.toCharArray();
	}
	
	
	@Override
	protected void updateBuffer() {
		if (getIndex() < fShift) {
			throw new IllegalStateException();
		}
		setBuffer(fStringContent, fStringContent.length, getIndex()-fShift);
	}
	
	
	public int length() {
		return fShift+fStringContent.length;
	}
	
	public char charAt(final int index) {
		return fStringContent[index-fShift];
	}
	
	public CharSequence subSequence(final int start, final int end) {
		return new String(fStringContent, start-fShift, end-(start-fShift));
	}
	
	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		s.ensureCapacity(fShift+fStringContent.length);
		s.setLength(fShift);
		s.append(fStringContent);
		return s.toString();
	}
	
}
