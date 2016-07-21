/*=============================================================================#
 # Copyright (c) 2008-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;

import de.walware.ecommons.io.TextContentDescriber;


/**
 * Detects encoding of R package DESCRIPTION based on optional encoding field
 */
public class RPkgDescriptionTextContentDescriber extends TextContentDescriber {
	
	
	private static final QualifiedName[] SUPPORTED_OPTIONS= new QualifiedName[] {
		IContentDescription.CHARSET, 
		IContentDescription.BYTE_ORDER_MARK,
	};
	
	
	private static final String PACKAGE_FIELD= "Package:"; //$NON-NLS-1$
	private static final String ENCODING_FIELD= "Encoding:"; //$NON-NLS-1$
	
	private static class ParseResult {
		
		public static final int PKG_FIELD= 1 << 0;
		
		public static final int ERROR= 1 << 28;
		
		
		private int state;
		private String encoding;
		
		
		public ParseResult() {
		}
		
		
		public boolean isIndeterminate() {
			return (this.state == ERROR
					|| (this.state == 0 && this.encoding == null) );
		}
		
		public boolean hasCharsetSpec() {
			return (this.encoding != null);
		}
		
		public String getCharsetName() {
			String name= this.encoding;
			try {
				final Charset charset= Charset.forName(this.encoding);
				name= charset.name();
			}
			catch (final Exception e) {}
			return name;
		}
		
	}
	
	
	public RPkgDescriptionTextContentDescriber() {
	}
	
	
	@Override
	public QualifiedName[] getSupportedOptions() {
		return SUPPORTED_OPTIONS;
	}
	
	@Override
	public int describe(final Reader contents, final IContentDescription description) throws IOException {
		if (description != null && description.isRequested(IContentDescription.CHARSET)) {
			final BufferedReader reader= new BufferedReader(contents);
			final ParseResult result= parse(reader);
			if (result.isIndeterminate()) {
				return INDETERMINATE;
			}
			if (result.hasCharsetSpec()) {
				description.setProperty(IContentDescription.CHARSET, result.getCharsetName());
			}
		}
		return VALID;
	}
	
	@Override
	public int describe(final InputStream contents, final IContentDescription description) throws IOException {
		final byte[] bom= getByteOrderMark(contents);
		String baseEncoding= "ISO-8859-1"; //$NON-NLS-1$
		if (bom != null) {
			if (bom == IContentDescription.BOM_UTF_16BE) {
				baseEncoding= "UTF-16BE"; //$NON-NLS-1$
			}
			else if (bom == IContentDescription.BOM_UTF_16LE) {
				baseEncoding= "UTF-16LE"; //$NON-NLS-1$
			}
			else if (bom == IContentDescription.BOM_UTF_8) {
				baseEncoding= "UTF-8"; //$NON-NLS-1$
			}
			
			if (description != null && description.isRequested(IContentDescription.BYTE_ORDER_MARK)) {
				description.setProperty(IContentDescription.BYTE_ORDER_MARK, bom);
			}
		}
		if (description != null && description.isRequested(IContentDescription.CHARSET)) {
			final BufferedReader reader= new BufferedReader(
					new InputStreamReader(contents, baseEncoding) );
			final ParseResult result= parse(reader);
			if (result.isIndeterminate()) {
				return INDETERMINATE;
			}
			if (result.hasCharsetSpec()) {
				description.setProperty(IContentDescription.CHARSET, result.getCharsetName());
			}
		}
		return VALID;
	}
	
	private ParseResult parse(final BufferedReader reader) throws IOException {
		final ParseResult result= new ParseResult();
		String line;
		while ((line= reader.readLine()) != null) {
			if (line.startsWith(PACKAGE_FIELD)) {
				if ((result.state & ParseResult.PKG_FIELD) == 0) {
					result.state= ParseResult.PKG_FIELD;
				}
				else {
					result.state= ParseResult.ERROR;
					break;
				}
			}
			else if (line.startsWith(ENCODING_FIELD)) {
				final String value= line.substring(ENCODING_FIELD.length()).trim();
				if (result.encoding == null && !value.isEmpty()) {
					result.encoding= value;
				}
			}
		}
		return result;
	}
	
}
