/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
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
 * Accepts a common string as parse input.
 */
public class StringParseInput extends SourceParseInput implements CharSequence {
	
	
	final char[] fContent;
	
	
	public StringParseInput(final String content) {
		fContent = content.toCharArray();
	}
	
	
	@Override
	protected void updateBuffer(final int index, final int min) {
		setBuffer(fContent, fContent.length, index);
	}
	
	
	public int length() {
		return fContent.length;
	}
	
	public char charAt(final int index) {
		return fBuffer[index];
	}
	
	public CharSequence subSequence(final int start, final int end) {
		return new String(fBuffer, start, end-start);
	}
	
	
	@Override
	protected char[] getCharInput() {
		return fContent;
	}
	
}
