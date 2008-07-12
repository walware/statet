/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;

import de.walware.eclipsecommons.ltk.IElementName;
import de.walware.eclipsecommons.ltk.IModelElement;
import de.walware.eclipsecommons.ltk.ui.IElementLabelProvider;
import de.walware.eclipsecommons.ui.text.sourceediting.AssistInvocationContext;
import de.walware.eclipsecommons.ui.text.sourceediting.ElementNameCompletionProposal;


public class RCompletionProposal extends ElementNameCompletionProposal {
	
	
	public RCompletionProposal(final AssistInvocationContext context, final IElementName elementName, 
			final int replacementOffset, final IModelElement element, final int defDistance, 
			final IElementLabelProvider labelProvider) {
		super(context, elementName, replacementOffset, element, defDistance, labelProvider);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void apply(final char trigger, final int stateMask, final int offset) throws BadLocationException {
		final int length = offset - fReplacementOffset;
		final SourceViewer viewer = fContext.getSourceViewer();
		final IDocument document = viewer.getDocument();
		final StringBuilder replacement = new StringBuilder(fReplacementName.getDisplayName());
		int cursor = replacement.length();
		if (length > 0 && document.getChar(fReplacementOffset) == '`' && replacement.charAt(0) != '`') {
			if (replacement.length() != fReplacementName.getSegmentName().length() ||
					fReplacementOffset+length >= document.getLength() || 
					document.getChar(fReplacementOffset+length) != '`') {
				replacement.insert(fReplacementName.getSegmentName().length(), '`');
			}
			replacement.insert(0, '`');
			cursor += 2;
		}
		document.replace(fReplacementOffset, length, replacement.toString());
		setCursorPosition(fReplacementOffset + cursor);
	}
	
}
