/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.builder;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.eclipse.core.runtime.CoreException;


public class RParser {

	private static class LineManager implements ILineResolver {
		
		IntList fList = new ArrayIntList();
		
		public void addLine(int startOffset) {
			
			fList.add(startOffset);
		}
		
		public int getLineOfOffset(int offset) {
			
			for (int i = 0; i < fList.size(); i++) {
				if (fList.get(i) > offset)
					return i;
			}
			return fList.size();
		}
	}
	
	
	private enum Last { NONE, NEWLINE, BACKSLASH };
	
	private MarkerHandler fMarkers;
	private char[] fContent;
	
	private int fCurrentOffset = 0;
	private int fCurrentLine = 1;
	private Last fLastChar = Last.NONE;
	private LineManager fLineStructure;
	

	public RParser(char[] content, MarkerHandler markers) {

		fContent = content;
		fMarkers = markers;
		fLineStructure = new LineManager();
	}
	
	public void check() throws CoreException {
		
		READ: for (; fCurrentOffset < fContent.length; fCurrentOffset++) {
			
			if (checkNewLine()) {
				continue READ;
			}

			if (checkBackslash()) {
				continue READ;
			}
				
			char current = fContent[fCurrentOffset];
			switch (current) {
			case '#':
				readComment();
				continue READ;

			case '"':
			case '\'':
				readString(current);
				continue READ;
			}
		}
	}
	
	private void readString(char b) {
		
		int start = fCurrentOffset;
		int end = -1;
		
		READ: for (fCurrentOffset++; fCurrentOffset < fContent.length; fCurrentOffset++) {
			
			if (checkNewLine())
				continue READ;

			if (checkBackslash())
				continue READ;
				
			if (fContent[fCurrentOffset] == b) {
				end = fCurrentOffset;
				break READ;
			}
		}
		if (end == -1)
			end = fCurrentOffset-1;
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
		fMarkers.checkForTasks(new String(fContent, start, end-start+1), start, fLineStructure);
	}

	private boolean checkNewLine() {

		char current = fContent[fCurrentOffset];
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
