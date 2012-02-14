/*******************************************************************************
 * Copyright (c) 2011-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.nico;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import de.walware.ecommons.text.TextUtil;


public class RSrcref implements IRSrcref {
	
	
	public static int getOffset(final AbstractDocument document, final int line, final int column)
			throws BadLocationException {
		int currentColumn = 0;
		int currentOffset = document.getLineOffset(line);
		final int l = document.getLength();
		while (currentColumn < column) {
			if (currentOffset >= l) {
				return -1;
			}
			final char c = document.getChar(currentOffset++);
			switch (c) {
			case '\n':
			case '\r':
				return -1;
			case '\t':
				currentColumn += 8 - (currentColumn % 8);
				continue;
			default:
				currentColumn++;
				continue;
			}
		}
		return currentOffset;
	}
	
	
	private final int fFirstLine;
	private final int fFirstColumn;
	private final int fLastLine;
	private final int fLastColumn;
	
	
	public RSrcref(final int beginLine, final int beginColumn, final int endLine, final int endColumn) {
		fFirstLine = beginLine;
		fFirstColumn = beginColumn;
		fLastLine = endLine;
		fLastColumn = endColumn;
	}
	
	public RSrcref(final IDocument document, final IRegion region) throws BadLocationException {
		final int offset = region.getOffset();
		fFirstLine = document.getLineOfOffset(offset);
		fFirstColumn = TextUtil.getColumn(document, offset, fFirstLine, 8);
		fLastLine = document.getLineOfOffset(offset+region.getLength());
		fLastColumn = TextUtil.getColumn(document, offset+region.getLength()-1, fLastLine, 8); // exclusive -> inclusive
	}
	
	
	@Override
	public boolean hasBeginDetail() {
		return (fFirstLine >= 0 && fFirstColumn >= 0);
	}
	
	@Override
	public int getFirstLine() {
		return fFirstLine;
	}
	
	@Override
	public int getFirstColumn() {
		return fFirstColumn;
	}
	
	@Override
	public boolean hasEndDetail() {
		return (fLastLine >= 0 && fLastColumn >= 0);
	}
	
	@Override
	public int getLastLine() {
		return fLastLine;
	}
	
	@Override
	public int getLastColumn() {
		return fLastColumn;
	}
	
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(this.getClass().getName());
		sb.append("\n\t").append("firstChar= ")
				.append("in line ").append(fFirstLine >= 0 ? fFirstLine : "NA")
				.append(" at column ").append(fFirstColumn >= 0 ? fFirstColumn : "NA");
		sb.append("\n\t").append("lastChar= ")
				.append("in line ").append(fLastLine >= 0 ? fLastLine : "NA")
				.append(" at column ").append(fLastColumn >= 0 ? fLastColumn : "NA");
		return sb.toString();
	}
	
}
