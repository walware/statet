/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.ui.launcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import de.walware.eclipsecommons.ltk.text.TextUtil;

import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.launching.ICodeLaunchContentHandler;


/**
 * Handler for R-script files.
 */
public class DefaultCodeLaunchHandler implements ICodeLaunchContentHandler {
	
	public String[] getCodeLines(final IDocument document) {
		try {
			final List<String> lines = new ArrayList<String>(document.getNumberOfLines()+1);
			TextUtil.getLines(document, 0, document.getLength(), lines);
			return lines.toArray(new String[lines.size()]);
		} catch (final BadLocationException e) {
			RUIPlugin.logError(-1, "Error occurred when reading R code lines.", e); //$NON-NLS-1$
		}
		return null;
	}
	
}
