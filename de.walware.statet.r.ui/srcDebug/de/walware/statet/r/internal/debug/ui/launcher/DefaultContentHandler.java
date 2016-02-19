/*=============================================================================#
 # Copyright (c) 2007-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.launcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import de.walware.ecommons.text.TextUtil;

import de.walware.statet.r.launching.ICodeSubmitContentHandler;


/**
 * Handler for R script files.
 */
public class DefaultContentHandler implements ICodeSubmitContentHandler {
	
	
	public DefaultContentHandler() {
	}
	
	
	@Override
	public void setup(final IDocument document) {
	}
	
	@Override
	public List<String> getCodeLines(final IDocument document)
			throws BadLocationException, CoreException {
		final ArrayList<String> lines= new ArrayList<>(document.getNumberOfLines() + 1);
		
		TextUtil.addLines(document, 0, document.getLength(), lines);
		
		return lines;
	}
	
	@Override
	public List<String> getCodeLines(final IDocument document, final int offset, final int length)
			throws CoreException, BadLocationException {
		final ArrayList<String> lines= new ArrayList<>(
				document.getNumberOfLines(0, length) + 1 );
		
		TextUtil.addLines(document, offset, length, lines);
		
		return lines;
	}
	
}
