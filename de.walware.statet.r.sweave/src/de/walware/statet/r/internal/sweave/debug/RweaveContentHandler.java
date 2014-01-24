/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.debug;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;

import de.walware.ecommons.text.TextUtil;

import de.walware.statet.r.internal.sweave.editors.LtxRweaveDocumentSetupParticipant;
import de.walware.statet.r.launching.ICodeSubmitContentHandler;
import de.walware.statet.r.sweave.text.Rweave;


public class RweaveContentHandler implements ICodeSubmitContentHandler {
	
	
	static IDocumentSetupParticipant DOC_SETUP = new LtxRweaveDocumentSetupParticipant();
	
	
	public RweaveContentHandler() {
	}
	
	
	@Override
	public List<String> getCodeLines(final IDocument document) throws BadLocationException, CoreException {
		DOC_SETUP.setup(document);
		
		final ArrayList<String> lines = new ArrayList<String>(document.getNumberOfLines() / 2);
		
		final ITypedRegion[] cats = Rweave.R_TEX_CAT_UTIL.getCats(document, 0, document.getLength());
		for (final ITypedRegion cat : cats) {
			if (cat.getType() == Rweave.R_CAT) {
				TextUtil.addLines(document, cat.getOffset(), cat.getLength(), lines);
			}
		}
		
		return lines;
	}
	
	@Override
	public List<String> getCodeLines(final IDocument document, final int offset, final int length)
			throws CoreException, BadLocationException {
		DOC_SETUP.setup(document);
		
		final ArrayList<String> lines = new ArrayList<String>(Math.min(
				document.getNumberOfLines(0, length) + 1, 64) );
		
		final ITypedRegion[] cats = Rweave.R_TEX_CAT_UTIL.getCats(document, offset, length);
		for (final ITypedRegion cat : cats) {
			if (cat.getType() == Rweave.R_CAT) {
				TextUtil.addLines(document, cat.getOffset(), cat.getLength(), lines);
			}
		}
		
		return lines;
	}
	
}
