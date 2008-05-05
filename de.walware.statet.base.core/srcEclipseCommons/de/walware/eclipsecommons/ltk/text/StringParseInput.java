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

package de.walware.eclipsecommons.ltk.text;


/**
 * Accepts a common string as parse input.
 */
public class StringParseInput extends SourceParseInput implements CharSequence {
	
	
	private final char[] fStringContent;
	
	
	public StringParseInput(final String content) {
		fStringContent = content.toCharArray();
	}
	
	
	@Override
	protected void updateBuffer() {
		setBuffer(fStringContent, fStringContent.length, getIndex());
	}
	
	
	public int length() {
		return fBufferLength;
	}
	
	public char charAt(final int index) {
		return fBuffer[index];
	}
	
	public CharSequence subSequence(final int start, final int end) {
		return new String(fBuffer, start, end-start);
	}
	
	@Override
	public String toString() {
		return new String(fBuffer);
	}
	
}
