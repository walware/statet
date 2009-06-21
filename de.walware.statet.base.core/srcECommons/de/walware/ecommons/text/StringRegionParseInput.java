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

import org.eclipse.jface.text.IRegion;


/**
 * Accepts a common string as parse input.
 */
public class StringRegionParseInput extends SourceParseInput implements CharSequence {
	
	
	private static final int BUFFER_LENGTH = 512;
	private int fOffset;
	private char[] fContent;
	private final IRegion[] fRegions;
	private int fFirstRegionIdx;
	private final char[] fBuffer = new char[BUFFER_LENGTH];
	private final int[] fLength = new int[BUFFER_LENGTH+1];
	
	
	/**
	 * 
	 * @param content StringParseInput
	 * @param regions the regions in the content, must be sorted
	 */
	public StringRegionParseInput(final SourceParseInput content, final IRegion[] regions) {
		fContent = content.getCharInput();
		fOffset = content.getInputOffset();
		if (fContent == null) {
			fContent = content.getStringInput().toCharArray();
		}
		if (regions == null) {
			throw new NullPointerException();
		}
		fRegions = regions;
		fLength[0] = 0;
	}
	
	
	@Override
	public void init(int start, int stop) {
		fFirstRegionIdx = 0;
		if (fRegions.length > 0) {
			start = Math.max(start, fRegions[0].getOffset());
			stop = Math.min(stop, fRegions[fRegions.length-1].getOffset()+fRegions[fRegions.length-1].getLength());
		}
		super.init(start, stop);
	}
	
	
	@Override
	protected void updateBuffer(final int index, int min) {
		if (index < fOffset) {
			throw new IllegalStateException();
		}
		int startIdx = -1;
		for (int i = fFirstRegionIdx; i < fRegions.length; i++) {
			if (index < fRegions[i].getOffset() + fRegions[i].getLength()) {
				startIdx = i;
				break;
			}
		}
		fFirstRegionIdx = startIdx;
		if (startIdx < 0) {
			fLength[1] = 1;
			setBuffer(NO_INPUT, 0, 0);
			return;
		}
		int bufferLength;
		int realLength;
		final int indexInBuffer;
		if (startIdx > 0) {
			bufferLength = 1;
			fBuffer[0] = '\n';
			if (index < fRegions[startIdx].getOffset()) {
				indexInBuffer = 0;
				realLength = fRegions[startIdx].getOffset() - index;
			}
			else {
				indexInBuffer = index - fRegions[startIdx].getOffset() + 1;
				realLength = 0;
			}
		}
		else {
			bufferLength = 0;
			indexInBuffer = index - fRegions[startIdx].getOffset();
			realLength = 0;
		}
		fLength[indexInBuffer] = realLength;
		for (int i = startIdx; ; ) {
//		for (int i = startIdx; bufferLength-indexInBuffer < min; ) { // only min
			final int add = Math.min(BUFFER_LENGTH-bufferLength, fRegions[i].getLength());
			System.arraycopy(fContent, fRegions[i].getOffset()-fOffset, fBuffer, bufferLength, add);
			int j = Math.max(bufferLength, indexInBuffer)+1;
			bufferLength += add;
			for (; j <= bufferLength; j++) {
				fLength[j] = ++realLength;
			}
			if (++i >= fRegions.length) {
				break;
			}
			if (bufferLength < BUFFER_LENGTH) {
				fBuffer[bufferLength++] = '\n';
				fLength[j] = realLength += fRegions[i].getOffset() - (fRegions[i-1].getOffset()+fRegions[i-1].getLength());
			}
		}
		setBuffer(fBuffer, bufferLength, indexInBuffer);
	}
	
	@Override
	public int getLength(int num) {
		return fLength[getIndexInBuffer()+num]-fLength[getIndexInBuffer()];
	}
	
	
	public int length() {
		return fOffset+fContent.length;
	}
	
	public char charAt(final int index) {
		return fContent[index-fOffset];
	}
	
	public CharSequence subSequence(final int start, final int end) {
		return new String(fContent, start-fOffset, end-(start-fOffset));
	}
	
}
