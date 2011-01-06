/*******************************************************************************
 * Copyright (c) 2008-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rdoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;

import de.walware.ecommons.io.TextContentDescriber;


/**
 * Detects encoding of Rd file based on optional \encoding element
 * The \encoding element must be the first command in Rd file
 */
public class RdTextContentDescriber extends TextContentDescriber {
	
	
	private static final QualifiedName[] SUPPORTED_OPTIONS = new QualifiedName[] {
		IContentDescription.CHARSET, 
		IContentDescription.BYTE_ORDER_MARK,
	};
	
	private static String ENCODING_COMMAND_NAME = "encoding"; //$NON-NLS-1$
	private static Pattern BRACKET_CONTENT_PATTERN = Pattern.compile("\\s*\\{\\s*(\\S*)\\s*\\}.*"); //$NON-NLS-1$
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public QualifiedName[] getSupportedOptions() {
		return SUPPORTED_OPTIONS;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int describe(final Reader contents, final IContentDescription description) throws IOException {
		if (description != null && description.isRequested(IContentDescription.CHARSET)) {
			final BufferedReader reader = new BufferedReader(contents);
			final String encoding = searchEncoding(reader);
			if (encoding == null) {
				return INDETERMINATE;
			}
			description.setProperty(IContentDescription.CHARSET, encoding);
		}
		return VALID;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int describe(final InputStream contents, final IContentDescription description) throws IOException {
		final byte[] bom = getByteOrderMark(contents);
		String baseEncoding = "UTF-8"; // "ISO-8859-1"; //$NON-NLS-1$
		if (bom != null) {
			if (bom == IContentDescription.BOM_UTF_16BE) {
				baseEncoding = "UTF-16BE"; //$NON-NLS-1$
			}
			else if (bom == IContentDescription.BOM_UTF_16LE) {
				baseEncoding = "UTF-16LE"; //$NON-NLS-1$
			}
			else if (bom == IContentDescription.BOM_UTF_8) {
				baseEncoding = "UTF-8"; //$NON-NLS-1$
			}
			
			if (description != null && description.isRequested(IContentDescription.BYTE_ORDER_MARK)) {
				description.setProperty(IContentDescription.BYTE_ORDER_MARK, bom);
			}
		}
		if (description != null && description.isRequested(IContentDescription.CHARSET)) {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(contents, baseEncoding));
			final String encoding = searchEncoding(reader);
			if (encoding == null) {
				return INDETERMINATE;
			}
			final IContentType contentType = description.getContentType();
			if (contentType == null || !encoding.equals(contentType.getDefaultCharset())) {
				description.setProperty(IContentDescription.CHARSET, encoding);
			}
		}
		return VALID;
	}
	
	/**
	 * {@inheritDoc}
	 */
	private String searchEncoding(final BufferedReader reader) throws IOException {
		String line;
		ITER_LINES: while ((line = reader.readLine()) != null) {
			ITER_CHARS: for (int i = 0; i < line.length(); i++) {
				switch (line.charAt(i)) {
				case ' ':
				case '\t':
					continue ITER_CHARS;
				case '%':
					continue ITER_LINES;
				case '\\':
					if (line.regionMatches(i+1, ENCODING_COMMAND_NAME, 0, ENCODING_COMMAND_NAME.length())) {
						final Matcher matcher = BRACKET_CONTENT_PATTERN.matcher(line.substring(i+1+ENCODING_COMMAND_NAME.length()));
						if (matcher.matches()) {
							return matcher.group(1);
						}
					}
					break ITER_LINES;
				default:
					break ITER_LINES;
				}
			}
		}
		return null;
	}
	
}
