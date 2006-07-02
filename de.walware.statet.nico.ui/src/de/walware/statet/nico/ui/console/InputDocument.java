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

package de.walware.statet.nico.ui.console;

import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;


public class InputDocument extends Document {

	
	private static Pattern gLineSeparatorPattern = Pattern.compile("\\r[\\n]?|\\n"); //$NON-NLS-1$

	
	InputDocument() {
		super();
	}
	
	
	@Override
	public void replace(int pos, int length, String text, long modificationStamp) throws BadLocationException {
		
		text = gLineSeparatorPattern.matcher(text).replaceAll(""); //$NON-NLS-1$
		super.replace(pos, length, text, modificationStamp);
	}
}
