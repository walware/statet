/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.internal.fileutil;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescription;

import de.walware.eclipsecommons.FileUtil;
import de.walware.eclipsecommons.FileUtil.ReadTextFileOperation;
import de.walware.eclipsecommons.FileUtil.ReaderAction;
import de.walware.eclipsecommons.FileUtil.WriteTextFileOperation;


/**
 * 
 */
public abstract class FileUtilProvider {
	
	public static class FileInput implements Closeable {
		
		private String fEncoding;
		private String fDefaultEncoding;
		private InputStream fStream;
		
		public FileInput(final InputStream input, final String expliciteCharsetHint) throws IOException, CoreException {
			fStream = input;
			if (expliciteCharsetHint != null) {
				fDefaultEncoding = expliciteCharsetHint;
			}
			else {
				read(input);
			}
			fEncoding = (fDefaultEncoding != null) ? fDefaultEncoding : FileUtil.UTF_8;
		}
		
		void read(final InputStream input) throws IOException {
			
			try {
				final int n = 3;
				final byte[] bytes = new byte[n];
				final int readed = input.read(bytes, 0, n);
				if (readed == 0) {
					return;
				}
				int next = 0;
				if (startsWith(bytes, IContentDescription.BOM_UTF_8)) {
					next = IContentDescription.BOM_UTF_8.length;
					fDefaultEncoding = FileUtil.UTF_8;
				}
				else if (startsWith(bytes, IContentDescription.BOM_UTF_16BE)) {
					next = IContentDescription.BOM_UTF_16BE.length;
					fDefaultEncoding = FileUtil.UTF_16_BE;
				}
				else if (startsWith(bytes, IContentDescription.BOM_UTF_16LE)) {
					next = IContentDescription.BOM_UTF_16LE.length;
					fDefaultEncoding = FileUtil.UTF_16_LE;
				}
				if (readed-next > 0) {
					fStream = new SequenceInputStream(new ByteArrayInputStream(
							bytes, next, readed-next), input);
				}
			}
			catch (final IOException e) {
				saveClose(input);
				throw e;
			}
		}
		
		private boolean startsWith(final byte[] array, final byte[] start) {
			for (int i = 0; i < start.length; i++) {
				if (array[i] != start[i]) {
					return false;
				}
			}
			return true;
		}
		
		public void setEncoding(final String encoding, final boolean force) {
			if (encoding == null && fDefaultEncoding != null) {
				fEncoding = fDefaultEncoding;
			}
			if (force || fDefaultEncoding == null) {
				fEncoding = encoding;
			}
		}
		
		public void close() throws IOException {
			if (fStream != null) {
				fStream.close();
			}
		}
		
		public String getDefaultCharset() {
			return fDefaultEncoding;
		}
		
		public Reader getReader() throws UnsupportedEncodingException {
			return new InputStreamReader(fStream, fEncoding);
		}
		
	}
	
	
	public abstract long getTimeStamp(Object file, IProgressMonitor monitor) throws CoreException;
	public abstract ReadTextFileOperation createReadTextFileOp(ReaderAction action, Object file);
	public abstract WriteTextFileOperation createWriteTextFileOp(String content, Object file);
	
	
	public static void saveClose(final Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (final IOException e) {}
		}
	}
	
}
