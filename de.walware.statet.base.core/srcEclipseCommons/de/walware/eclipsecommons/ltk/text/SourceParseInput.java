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

package de.walware.eclipsecommons.ltk.text;


public class SourceParseInput {
	
	
	public final static int EOF = -1;
	
	
	protected char[] fBuffer = new char[0];
	private int fIndexInBuffer = 0;
	private int fIndex;
	
	
	protected SourceParseInput() {
		this(0);
	}
	
	protected SourceParseInput(int initialIndex) {
		fIndex = initialIndex;
	}
	
	public final int getIndex() {
		return fIndex;
	}
	
	public final int get(final int num) {
		int idx = fIndexInBuffer+num-1;
		if (idx < fBuffer.length) {
			return fBuffer[idx];
		}
		updateBuffer();
		idx = fIndexInBuffer+num-1;
		if (idx < fBuffer.length) {
			return fBuffer[idx];
		}
		return EOF;
	}
	
	public final boolean subequals(final int num, final char c1) {
		return (fBuffer[fIndexInBuffer+num-1] == c1);
	}

	public final boolean subequals(final int num, final char c1, final char c2) {
		int idx = fIndexInBuffer+num-1;
		return (fBuffer[idx] == c1 && fBuffer[++idx] == c2);
	}
	
	public final boolean subequals(final int num, final char c1, final char c2, final char c3) {
		int idx = fIndexInBuffer+num-1;
		return (fBuffer[idx] == c1 && fBuffer[++idx] == c2 && fBuffer[++idx] == c3);
	}

	public final boolean subequals(final int num, final char[] sequence, int start) {
		final int length = sequence.length;
		for (int idx = fIndexInBuffer+num-1; start < length; ) {
			if (fBuffer[idx++] != sequence[start++]) {
				return false;
			}
		}
		return true;
	}
	
	public final String substring(final int num, final int length) {
		return new String(fBuffer, fIndexInBuffer+num-1, length);
	}
	
	public final void consume(final int num) {
		fIndex += num;
		fIndexInBuffer += num;
	}
	
	protected void updateBuffer() {
	}
	
}
