/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ui.IElementLabelProvider;
import de.walware.ecommons.ui.text.sourceediting.AssistInvocationContext;
import de.walware.ecommons.ui.text.sourceediting.ElementNameCompletionProposal;

import de.walware.statet.r.ui.RUI;


public class RCompletionProposal extends ElementNameCompletionProposal {
	
	
	public RCompletionProposal(final AssistInvocationContext context, final IElementName elementName, 
			final int replacementOffset, final IModelElement replacementName,
			final int defDistance, final IElementLabelProvider labelProvider) {
		super(context, elementName, replacementOffset, replacementName, defDistance, labelProvider);
	}
	
	
	@Override
	protected String getPluginId() {
		return RUI.PLUGIN_ID;
	}
	
	@Override
	protected void doApply(final char trigger, final int stateMask, final int caretOffset, final int replacementOffset, final int replacementLength) throws BadLocationException {
		final IDocument document = fContext.getSourceViewer().getDocument();
		
		final StringBuilder replacement = new StringBuilder(fReplacementName.getDisplayName());
		int cursor = replacement.length();
		if (replacementLength > 0 && document.getChar(replacementOffset) == '`' && replacement.charAt(0) != '`') {
			if (replacement.length() != fReplacementName.getSegmentName().length() ||
					replacementOffset+replacementLength >= document.getLength() || 
					document.getChar(replacementOffset+replacementLength) != '`') {
				replacement.insert(fReplacementName.getSegmentName().length(), '`');
			}
			replacement.insert(0, '`');
			cursor += 2;
		}
		document.replace(replacementOffset, replacementLength, replacement.toString());
		setCursorPosition(replacementOffset + cursor);
	}
	
}
