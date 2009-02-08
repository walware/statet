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

package de.walware.ecommons.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;


/**
 * Accepts document ranges as input. Loads the content partial, small footprint.
 */
public class BufferedDocumentParseInput extends SourceParseInput {
	
	
	private static final int BUFFER_SIZE = 1024;
	
	
	private IDocument fDocument;
	
	
	public BufferedDocumentParseInput() {
		setBuffer(new char[BUFFER_SIZE], 0, 0);
	}
	
	
	public void setRange(final IDocument document, final int offset, final int length) {
		fDocument = document;
		init(offset, offset+length);
	}
	
	@Override
	protected void updateBuffer() {
		final int start = getIndex();
		final int length = Math.min(getStopIndex()-start, BUFFER_SIZE);
		try {
			fDocument.get(start, length).getChars(0, length, fBuffer, 0);
			setBuffer(fBuffer, length, 0);
		} catch (final BadLocationException e) {
			assert (false);
		}
	}
	
	
	public IDocument getDocument() {
		return fDocument;
	}
	
	public int getDocumentChar(final int offset) {
		try {
			return fDocument.getChar(offset);
		} catch (final BadLocationException e) {
			return -1;
		}
	}
	
}
