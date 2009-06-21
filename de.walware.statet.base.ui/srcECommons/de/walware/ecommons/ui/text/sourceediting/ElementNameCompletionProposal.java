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

package de.walware.ecommons.ui.text.sourceediting;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ui.IElementLabelProvider;


/**
 * The standard implementation of the <code>ICompletionProposal</code> interface.
 */
public abstract class ElementNameCompletionProposal extends CompletionProposalWithOverwrite
		implements ICompletionProposalExtension6 {
	
	
	protected final IElementName fReplacementName;
	
	/** The cursor position after this proposal has been applied. */
	private int fCursorPosition = -1;
	
	/** The additional info of this proposal. */
	private String fAdditionalProposalInfo;
	
	protected final IModelElement fElement;
	
	private final IElementLabelProvider fLabelProvider;
	
	private int fRelevance;
	
	
	public ElementNameCompletionProposal(final AssistInvocationContext context, 
			final IElementName replacementName, final int replacementOffset,
			final IModelElement element, final int relevance, 
			final IElementLabelProvider labelProvider) {
		super(context, replacementOffset);
		fReplacementName = replacementName;
		fElement = element;
		fLabelProvider = labelProvider;
		fRelevance = relevance;
	}
	
	
	protected IElementLabelProvider getLabelProvider() {
		return fLabelProvider;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Image getImage() {
		return fLabelProvider.getImage(fElement);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getDisplayString() {
		return fLabelProvider.getText(fElement);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public StyledString getStyledDisplayString() {
		return fLabelProvider.getStyledText(fElement);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getAdditionalProposalInfo() {
		return fAdditionalProposalInfo;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getRelevance() {
		return fRelevance;
	}
	
	public String getSortingString() {
		return fReplacementName.getSegmentName();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean validate(final IDocument document, final int offset, final DocumentEvent event) {
		try {
			final String content = document.get(getReplacementOffset(), offset - getReplacementOffset());
			if (fReplacementName.getSegmentName().regionMatches(true, 0, content, 0, content.length())) {
				return true;
			}
		}
		catch (final BadLocationException e) {
			// ignore concurrently modified document
		}
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isAutoInsertable() {
		return false;
	}
	
	@Override
	protected void doApply(final char trigger, final int stateMask, final int caretOffset, final int replacementOffset, final int replacementLength) throws BadLocationException {
		final SourceViewer viewer = fContext.getSourceViewer();
		final IDocument document = viewer.getDocument();
		final StringBuilder replacement = new StringBuilder(fReplacementName.getDisplayName());
		document.replace(replacementOffset, replacementLength, replacement.toString());
		setCursorPosition(replacementOffset + replacement.length());
	}
	
	
	protected void setCursorPosition(final int offset) {
		fCursorPosition = offset;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * This implementation returns <code>null</code>
	 */
	public IContextInformation getContextInformation() {
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Point getSelection(final IDocument document) {
		if (fCursorPosition >= 0) {
			return new Point(fCursorPosition, 0);
		}
		return null;
	}
	
	
}
