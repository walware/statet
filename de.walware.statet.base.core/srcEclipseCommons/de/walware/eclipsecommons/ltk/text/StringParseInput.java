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
public class StringParseInput extends SourceParseInput implements CharSequence {
	

	public StringParseInput(String content) {
		fBuffer = content.toCharArray();
	}

	
	public int length() {
		return fBuffer.length;
	}

	public char charAt(int index) {
		return fBuffer[index];
	}
	
	public CharSequence subSequence(int start, int end) {
		return new String(fBuffer, start, end-start);
	}
	
	@Override
	public String toString() {
		return new String(fBuffer);
	}
}
