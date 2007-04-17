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

package de.walware.statet.r.ui;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import de.walware.statet.r.core.rlang.RTokens;


/**
 *
 */
public class RUIUtil {

	public static IRegion getRWord(IDocument document, int anchor, boolean isDotSeparator) {
	
		try {
	
			int offset = anchor;
			char c;
	
			while (offset >= 0) {
				c = document.getChar(offset);
				if (RTokens.isRobustSeparator(c, isDotSeparator))
					break;
				--offset;
			}
			
			int start = offset;
	
			offset = anchor;
			int length = document.getLength();
	
			while (offset < length) {
				c = document.getChar(offset);
				if (RTokens.isRobustSeparator(c, isDotSeparator))
					break;
				++offset;
			}
			
			int end = offset;
			
			if (start < end)
				return new Region (start + 1, end - start - 1);
			
		} catch (BadLocationException x) {
		}
		return new Region(anchor, 0);
	}

	public static IRegion getDefaultWord(IDocument document, int anchor) {
		
		try {
	
			int offset = anchor;
			char c;
	
			while (offset >= 0) {
				c = document.getChar(offset);
				if (!Character.isLetterOrDigit(c))
					break;
				--offset;
			}
	
			int start = offset;
	
			offset = anchor;
			int length = document.getLength();
	
			while (offset < length) {
				c = document.getChar(offset);
				if (!Character.isLetterOrDigit(c))
					break;
				++offset;
			}
			
			int end = offset;
			
			if (start < end)
				return new Region (start + 1, end - start - 1);
			
		} catch (BadLocationException x) {
		}
		return new Region(anchor, 0);
	}
	
}
