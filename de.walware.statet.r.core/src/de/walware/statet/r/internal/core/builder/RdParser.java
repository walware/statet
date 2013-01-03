/*******************************************************************************
 * Copyright (c) 2005-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.builder;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;

import de.walware.ecommons.text.ILineInformation;


public class RdParser {
	
	public class LineInformation implements ILineInformation {
		
		
		private final IntList fOffsets;
		
		
		public LineInformation() {
			fOffsets = new ArrayIntList();
		}
		
		public void addLine(final int offset) {
			fOffsets.add(offset);
		}
		
		@Override
		public int getNumberOfLines() {
			return fOffsets.size();
		}
		
		@Override
		public int getLineOfOffset(final int offset) {
			if (fOffsets.size() == 0) {
				return -1;
			}
			int low = 0;
			int high = fOffsets.size()-1;
			
			while (low <= high) {
				final int mid = (low + high) >> 1;
				final int lineOffset = fOffsets.get(mid);
				
				if (lineOffset < offset) {
					low = mid + 1;
				}
				else if (lineOffset > offset) {
					high = mid - 1;
				}
				else {
					return mid;
				}
			}
			return low-1;
		}
		
		@Override
		public int getLineOffset(final int line) {
			if (line < 0 || line >= fOffsets.size()) {
				return -1;
			}
			return fOffsets.get(line);
		}
		
	}
	
	
	private static final char[][] PLATFORM_KEYWORDS = {			// without '#'
		"ifdef".toCharArray(), "ifndef".toCharArray(), "endif".toCharArray() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	private enum Last { NONE, NEWLINE, BACKSLASH };
	
	private TaskMarkerHandler fMarkers;
	private char[] fContent;
	
	private int fCurrentOffset = 0;
	private int fCurrentLine = 1;
	private Last fLastChar = Last.NONE;
	private LineInformation fLineStructure;
	
	
	public RdParser(final char[] content, final TaskMarkerHandler markers) {
		fContent = content;
		fMarkers = markers;
		fLineStructure = new LineInformation();
	}
	
	public void check() throws CoreException {
		READ: for (; fCurrentOffset < fContent.length; fCurrentOffset++) {
				
			if (checkNewLine()) {
				continue READ;
			}
			
			if (checkBackslash()) {
				continue READ;
			}
					
			final char current = fContent[fCurrentOffset];
			switch (current) {
			case '%':
				readComment();
				continue READ;
			
			case '#':
				if (fLastChar == Last.NEWLINE) {
					CHECK_KEYS: for (int i = 0; i < PLATFORM_KEYWORDS.length; i++) {
						int offset = fCurrentOffset+1;
						CHECK_KEYCHARS: for (int j = 0; j < PLATFORM_KEYWORDS[i].length; j++) {
							if (offset < fContent.length && PLATFORM_KEYWORDS[i][j] == fContent[offset++]) {
								continue CHECK_KEYCHARS;
							}
							continue CHECK_KEYS;
						}
						readPlatformInstruction(PLATFORM_KEYWORDS[i]);
					}
				}
				continue READ;
			}
		}
	}
	
	private void readPlatformInstruction(final char[] keyword) {
		final int start = fCurrentOffset;
		int end = fCurrentOffset;
		
		READ: for (fCurrentOffset++; fCurrentOffset < fContent.length; fCurrentOffset++) {
			
			end = fCurrentOffset;
			if (checkNewLine()) {
				break READ;
			}
		}
	}
	
	private void readComment() throws CoreException {
		final int start = fCurrentOffset;
		int end = fCurrentOffset;
		
		READ: for (fCurrentOffset++; fCurrentOffset < fContent.length; fCurrentOffset++) {
			end = fCurrentOffset;
			if (checkNewLine()) {
				end--;
				break READ;
			}
		}
		try {
			fMarkers.checkForTasks(new String(fContent, start, end-start+1), start, fLineStructure);
		}
		catch (final BadLocationException e) {
		}
	}
	
	private boolean checkNewLine() {
		final char current = fContent[fCurrentOffset];
		if (current == '\r' || current == '\n') {
			
			if (current == '\r' && fCurrentOffset+1 < fContent.length && fContent[fCurrentOffset+1] == '\n') {
				fCurrentOffset++;
			}
			
			fLineStructure.addLine(fCurrentOffset);
			fLastChar = Last.NEWLINE;
			return true;
		}
		return false;
	}
	
	private boolean checkBackslash() {
		if (fContent[fCurrentOffset] == '\\') {
			fLastChar = Last.BACKSLASH;
			return true;
		}
		if (fLastChar == Last.BACKSLASH) {
			fLastChar = Last.NONE;
			return true;
		}
		return false;
	}
	
}
