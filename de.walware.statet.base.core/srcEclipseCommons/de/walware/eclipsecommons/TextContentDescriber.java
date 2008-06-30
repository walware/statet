/*******************************************************************************
 * Copyright (c) 2004-2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.ITextContentDescriber;


/**
 * This class provides basis for text-based content describers. 
 */
public class TextContentDescriber implements ITextContentDescriber {
	
	
	private final static QualifiedName[] SUPPORTED_OPTIONS = { IContentDescription.BYTE_ORDER_MARK };
	
	
	/**
	 * {@inheritDoc}
	 */
	public QualifiedName[] getSupportedOptions() {
		return SUPPORTED_OPTIONS;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int describe(final Reader contents, final IContentDescription description) throws IOException {
		// we want to be pretty loose on detecting the text content type  
		return INDETERMINATE;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int describe(final InputStream contents, final IContentDescription description) throws IOException {
		if (description == null || !description.isRequested(IContentDescription.BYTE_ORDER_MARK))
			return INDETERMINATE;
		final byte[] bom = getByteOrderMark(contents);
		if (bom != null)
			description.setProperty(IContentDescription.BYTE_ORDER_MARK, bom);
		// we want to be pretty loose on detecting the text content type  		
		return INDETERMINATE;
	}
	
	/**
	 * Return the byte order mark of the input and 
	 * moves input pointer to position after the BOM, if available or otherwise to 0.
	 * @param input the input to check
	 * @return the BOM or null, if none common BOM
	 * @throws IOException
	 */
	protected final byte[] getByteOrderMark(final InputStream input) throws IOException {
		final int first = input.read();
		byte[] bom = null;
		if (first == 0xEF) {
			//look for the UTF-8 Byte Order Mark (BOM)
			final int second = input.read();
			final int third = input.read();
			if (second == 0xBB && third == 0xBF)
				bom = IContentDescription.BOM_UTF_8;
		} else if (first == 0xFE) {
			//look for the UTF-16 BOM
			if (input.read() == 0xFF)
				bom = IContentDescription.BOM_UTF_16BE;
		} else if (first == 0xFF) {
			if (input.read() == 0xFE)
				bom = IContentDescription.BOM_UTF_16LE;
		}
		
		input.reset();
		if (bom != null) {
			input.skip(bom.length);
		}
		return bom;
	}
	
}
