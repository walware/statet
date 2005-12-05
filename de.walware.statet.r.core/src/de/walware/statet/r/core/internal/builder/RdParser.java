/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.internal.builder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.eclipse.core.runtime.CoreException;


public class RdParser {

	private static class LineManager {
		
		IntList fList = new ArrayIntList();
		
		public void setLineStart(int line, int startOffset) {
			
			fList.add(line-1, startOffset);
		}
		
		public int getLineOfOffset(int offset) {
			
			for (int i = 0; i < fList.size(); i++) {
				if (fList.get(i) > offset)
					return i;
			}
			return fList.size();
		}
	}
	
	
	private static final char[][] PLATFORM_KEYWORDS = {			// without '#'
		"ifdef".toCharArray(), "ifndef".toCharArray(), "endif".toCharArray() };

	private enum Last { NONE, NEWLINE, BACKSLASH };
	
	private MarkerHandler fMarkers;
	private char[] fContent;
	
	private int fCurrentOffset = 0;
	private int fCurrentLine = 1;
	private Last fLastChar = Last.NONE;
	private LineManager fLineStructure;
	

	public RdParser(char[] content, MarkerHandler markers) {

		fContent = content;
		fMarkers = markers;
		fLineStructure = new LineManager();
	}
	
	public void check() throws CoreException {
		
		fLineStructure.setLineStart(1, 0);
		
			READ: for (; fCurrentOffset < fContent.length; fCurrentOffset++) {
				
				if (checkNewLine())
					continue READ;

				if (checkBackslash())
					continue READ;
				
				char current = fContent[fCurrentOffset];
				switch (current) {
				case '%':
					readComment();
					continue READ;

				case '#':
					if (fLastChar == Last.NEWLINE) {
						CHECK_KEYS: for (int i = 0; i < PLATFORM_KEYWORDS.length; i++) {
							int offset = fCurrentOffset+1;
							CHECK_KEYCHARS: for (int j = 0; j < PLATFORM_KEYWORDS[i].length; j++) {
								if (offset < fContent.length && PLATFORM_KEYWORDS[i][j] == fContent[offset++])
									continue CHECK_KEYCHARS;
								continue CHECK_KEYS;
							}
							readPlatformInstruction(PLATFORM_KEYWORDS[i]);
						}
					}
					continue READ;
				}
			}
	}
	
	private void readPlatformInstruction(char[] keyword) {
		
		int start = fCurrentOffset;
		int end = fCurrentOffset;
		
		READ: for (fCurrentOffset++; fCurrentOffset < fContent.length; fCurrentOffset++) {
			
			end = fCurrentOffset;
			if (checkNewLine())
				break READ;
		}
	}

	private void readComment() throws CoreException {
		
		int start = fCurrentOffset;
		int end = fCurrentOffset;
		
		READ: for (fCurrentOffset++; fCurrentOffset < fContent.length; fCurrentOffset++) {
			
			end = fCurrentOffset;
			if (checkNewLine()) {
				end--;
				break READ;
			}
		}
		provisoriHandleComment(start, end);
	}

	private boolean checkNewLine() {

		char current = fContent[fCurrentOffset];
		if (current == '\r' || current == '\n') {
			
			if (current == '\r' && fCurrentOffset+1 < fContent.length && fContent[fCurrentOffset+1] == '\n') {
				fCurrentOffset++;
			}
			
			fLineStructure.setLineStart(++fCurrentLine, fCurrentOffset);
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
	
	
	private void provisoriHandleComment(int start, int end) throws CoreException {
		
		String content = new String(fContent, start, end-start+1);

		Pattern pattern = fMarkers.getTaskPattern();
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			String text = content.substring(matcher.start(1));
			fMarkers.addTaskMarker(text, fLineStructure.getLineOfOffset(start), matcher.group(1));
		}
		
	}
}
