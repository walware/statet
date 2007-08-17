/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ltk.text;


/**
 *
 */
public class PartialStringParseInput extends SourceParseInput implements CharSequence {
	
	
	private final int fShift;
	

	public PartialStringParseInput(String content, int offsetInSource) {
		super(offsetInSource);
		fShift = offsetInSource;
		fBuffer = content.toCharArray();
	}

	
	public int length() {
		return fBuffer.length;
	}

	public char charAt(int index) {
		return fBuffer[index-fShift];
	}
	
	public CharSequence subSequence(int start, int end) {
		return new String(fBuffer, start-fShift, end-start-fShift);
	}
	
	@Override
	public String toString() {
		return new String(fBuffer);
	}
}
