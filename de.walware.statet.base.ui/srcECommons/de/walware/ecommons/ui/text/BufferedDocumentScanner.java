/*******************************************************************************
 * Copyright (c) 2000-2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.text;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;


/**
 * A buffered document scanner. The buffer always contains a section
 * of a fixed size of the document to be scanned.
 */
public final class BufferedDocumentScanner implements ICharacterScanner {
	
	/** The document being scanned. */
	private IDocument fDocument;
	/** The offset of the document range to scan. */
	private int fRangeOffset;
	/** The length of the document range to scan. */
	private int fRangeLength;
	/** The delimiters of the document. */
	private char[][] fDelimiters;
	
	/** The buffer. */
	private final char[] fBuffer;
	/** The offset of the buffer within the document. */
	private int fBufferOffset;
	/** The valid length of the buffer for access. */
	private int fBufferLength;
	/** The offset of the scanner within the buffer. */
	private int fOffset;
	
	
	/**
	 * Creates a new buffered document scanner.
	 * The buffer size is set to the given number of characters.
	 * 
	 * @param size the buffer size
	 */
	public BufferedDocumentScanner(final int size) {
		Assert.isTrue(size >= 1);
		fBuffer= new char[size];
	}
	
	/**
	 * Fills the buffer with the contens of the document starting at the given offset.
	 * 
	 * @param offset the document offset at which the buffer starts
	 */
	private final void updateBuffer(final int offset) {
		fBufferOffset= offset;
		
		if (fBufferOffset + fBuffer.length > fRangeOffset + fRangeLength)
			fBufferLength= fRangeLength - (fBufferOffset - fRangeOffset);
		else
			fBufferLength= fBuffer.length;
		
		try {
			final String content= fDocument.get(fBufferOffset, fBufferLength);
			content.getChars(0, fBufferLength, fBuffer, 0);
		} catch (final BadLocationException e) {
		}
	}
	
	/**
	 * Configures the scanner by providing access to the document range over which to scan.
	 * 
	 * @param document the document to scan
	 * @param offset the offset of the document range to scan
	 * @param length the length of the document range to scan
	 */
	public final void setRange(final IDocument document, final int offset, final int length) {
		fDocument= document;
		fRangeOffset= offset;
		fRangeLength= length;
		
		final String[] delimiters= document.getLegalLineDelimiters();
		fDelimiters= new char[delimiters.length][];
		for (int i= 0; i < delimiters.length; i++)
			fDelimiters[i]= delimiters[i].toCharArray();
		
		updateBuffer(offset);
		fOffset= 0;
	}
	
	public final int read() {
		if (fOffset >= fBufferLength) {
			if (fBufferOffset + fBufferLength >= fRangeOffset + fRangeLength)
				return EOF;
			else {
				updateBuffer(fBufferOffset + fBufferLength);
				fOffset= 0;
			}
		}
		return fBuffer[fOffset++];
	}
	
	public final void unread() {
		if (fOffset <= 0) {
			if (fBufferOffset <= fRangeOffset) {
				// error: BOF
			} else {
				updateBuffer(fBufferOffset - fBuffer.length);
				fOffset= fBuffer.length - 1;
			}
		} else {
			--fOffset;
		}
	}
	
	public final int getColumn() {
		try {
			final int offset= fBufferOffset + fOffset;
			final int line= fDocument.getLineOfOffset(offset);
			final int start= fDocument.getLineOffset(line);
			return offset - start;
		} catch (final BadLocationException e) {
		}
		
		return -1;
	}
	
	public final char[][] getLegalLineDelimiters() {
		return fDelimiters;
	}
	
}
