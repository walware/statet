/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Point;

import de.walware.ecommons.ui.text.sourceediting.AssistInvocationContext;
import de.walware.ecommons.ui.text.sourceediting.SimpleCompletionProposal;

import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.ui.RUI;


public class RSimpleCompletionComputer extends SimpleCompletionProposal {
	
	
	public RSimpleCompletionComputer(final AssistInvocationContext context, final String replacementString, final int replacementOffset) {
		super(context, replacementString, replacementOffset);
	}
	
	
	@Override
	protected String getPluginId() {
		return RUI.PLUGIN_ID;
	}
	
	
	@Override
	protected int computeReplacementLength(final int replacementOffset, final Point selection, final int caretOffset, final boolean overwrite) {
		// keep in synch with RElementCompletionProposal
		final int end = Math.max(caretOffset, selection.x + selection.y);
		if (overwrite) {
			final RElementCompletionProposal.ApplyData data = new RElementCompletionProposal.ApplyData(fContext);
			final RHeuristicTokenScanner scanner = data.getScanner();
			scanner.configure(data.getDocument());
			final IRegion word = scanner.findRWord(end, false, true);
			if (word != null) {
				return (word.getOffset() + word.getLength() - replacementOffset);
			}
		}
		return (end - replacementOffset);
	}
	
	public boolean isAutoInsertable() {
		return false;
	}
	
	@Override
	public boolean validate(final IDocument document, final int offset, final DocumentEvent event) {
		// keep in synch with RElementCompletionProposal
		try {
			int start = getReplacementOffset();
			int length = offset - getReplacementOffset();
			if (length > 0 && document.getChar(start) == '`') {
				start++;
				length--;
			}
			if (length > 0 && document.getChar(start+length-1) == '`') {
				length--;
			}
			final String prefix = document.get(start, length);
			final String replacement = getReplacementString();
			if (new RElementsCompletionComputer.PrefixPattern(prefix).matches(replacement)) {
				return true;
			}
		}
		catch (final BadLocationException e) {
			// ignore concurrently modified document
		}
		return false;
	}
	
}
