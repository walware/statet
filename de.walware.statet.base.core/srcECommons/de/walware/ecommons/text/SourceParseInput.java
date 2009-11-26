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
 * Generic API for input of lexers etc.
 * Subclasses have to fill and update the buffer.
 * 
 * Note:
 * All public methods have 1-based index (args called num).
 * Internal methods uses 0-based index (args call index).
 */
public abstract class SourceParseInput {
	
	
	public final static int EOF = -1;
	
	protected final static char[] NO_INPUT = new char[0];
	
	
	protected char[] fBuffer = NO_INPUT;
	private int fBufferLength = 0;
	private int fIndexInBuffer = 0;
	private int fIndex;
	private int fStop;
	
	
	protected SourceParseInput() {
		fIndex = Integer.MIN_VALUE;
		fStop = Integer.MIN_VALUE;
	}
	
//	protected SourceParseInput(final int initialIndex) {
//		fIndex = initialIndex;
//		fStop = -1;
//	}
	
	public final void init() {
		init(0, Integer.MIN_VALUE);
	}
	
	public void init(final int start, final int stop) {
		fIndex = start;
		fStop = stop;
		fIndexInBuffer = 0;
		fBufferLength = 0;
		updateBuffer(fIndex, 0);
	}
	
	public final int getIndex() {
		return fIndex;
	}
	
	public final int getStopIndex() {
		return fStop;
	}
	
	public final int get(final int num) {
		int idx = fIndexInBuffer+num-1;
		if (idx < fBufferLength) {
			return fBuffer[idx];
		}
		updateBuffer(fIndex, num);
		idx = fIndexInBuffer+num-1;
		if (idx < fBufferLength) {
			return fBuffer[idx];
		}
		return EOF;
	}
	
	public final boolean subequals(final int num, final char c1) {
		int idx = fIndexInBuffer+num-1; // -1
		if (idx < fBufferLength) {
			return (fBuffer[idx] == c1);
		}
		updateBuffer(fIndex, num);
		idx = fIndexInBuffer+num;
		return (idx < fBufferLength
				&& fBuffer[idx] == c1);
	}
	
	public final boolean subequals(final int num, final char c1, final char c2) {
		int idx = fIndexInBuffer+num; // -1 +1
		if (idx < fBufferLength) {
			return (fBuffer[idx] == c2 && fBuffer[--idx] == c1);
		}
		updateBuffer(fIndex, num);
		idx = fIndexInBuffer+num;
		return (idx < fBufferLength
				&& fBuffer[idx] == c2 && fBuffer[++idx] == c1);
	}
	
	public final boolean subequals(final int num, final char c1, final char c2, final char c3) {
		int idx = fIndexInBuffer+num+1; // -1 +2
		if (idx < fBufferLength) {
			return (fBuffer[idx] == c3 && fBuffer[--idx] == c2 && fBuffer[--idx] == c1);
		}
		updateBuffer(fIndex, num);
		idx = fIndexInBuffer+num+1;
		return (idx < fBufferLength
				&& fBuffer[idx] == c3 && fBuffer[--idx] == c2 && fBuffer[--idx] == c1);
	}
	
	public final boolean subequals(final int num, final char[] sequence) {
		int idx = fIndexInBuffer+num-1;
		final int length = sequence.length;
		if (idx+length > fBufferLength) {
			updateBuffer(fIndex, num);
			idx = fIndexInBuffer+num-1;
			if (idx+length > fBufferLength) {
				return false;
			}
		}
		int offset = 0;
		while (offset < length) {
			if (fBuffer[idx++] != sequence[offset++]) {
				return false;
			}
		}
		return true;
	}
	
	public final String substring(final int num, final int length) {
		return new String(fBuffer, fIndexInBuffer+num-1, length);
	}
	
	public int getLength(final int num) {
		return num;
	}
	
	public final void consume(final int num) {
		fIndex += getLength(num);
		fIndexInBuffer += num;
	}
	
	protected abstract void updateBuffer(int index, int min);
	
	protected final void setBuffer(final char[] buffer, final int length, final int indexInBuffer) {
		fIndexInBuffer = indexInBuffer;
		fBuffer = buffer;
		final int stopInBuffer = fIndexInBuffer+fStop-fIndex;
		fBufferLength = (fStop > 0 && stopInBuffer < length) ?
			stopInBuffer : length;
	}
	
	protected int getIndexInBuffer() {
		return fIndexInBuffer;
	}
	
	/**
	 * Underlying input if it is a string
	 */
	protected String getStringInput() {
		return null;
	}
	
	/**
	 * Underlying input if it is a char array
	 */
	protected char[] getCharInput() {
		return null;
	}
	
	/**
	 * Offset of underlying input (string or char array).
	 * @return offset of the input as index {@link #getIndex()}
	 */
	protected int getInputOffset() {
		return 0;
	}
	
	@Override
	public String toString() {
		if (getCharInput() != null) {
			final int offset = getInputOffset();
			if (offset == 0) {
				return new String(getCharInput());
			}
			if (offset > 0) {
				final StringBuilder s = new StringBuilder();
				s.ensureCapacity(offset+getCharInput().length);
				s.setLength(offset);
				s.append(getCharInput());
				return s.toString();
			}
			else {
				return new String(getCharInput(), -offset, getCharInput().length+offset);
			}
		}
		else if (getStringInput() != null) {
			final int offset = getInputOffset();
			if (offset == 0) {
				return getStringInput();
			}
			else if (offset > 0) {
				final StringBuilder s = new StringBuilder();
				s.ensureCapacity(offset+getStringInput().length());
				s.setLength(offset);
				s.append(getStringInput());
				return s.toString();
			}
			else {
				return getStringInput().substring(-offset);
			}
		}
		return super.toString();
	}
	
}
