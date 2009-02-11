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
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
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
public class ElementNameCompletionProposal implements ICompletionProposal, 
		ICompletionProposalExtension2, ICompletionProposalExtension4, ICompletionProposalExtension6,
		IRatedProposal {
	
	protected final AssistInvocationContext fContext;
	
	protected IElementName fReplacementName;
	
	/** The replacement offset. */
	protected int fReplacementOffset;
	
	/** The cursor position after this proposal has been applied. */
	private int fCursorPosition = -1;
	
	/** The context information of this proposal. */
	private IContextInformation fContextInformation;
	
	/** The additional info of this proposal. */
	private String fAdditionalProposalInfo;
	
	private IModelElement fElement;
	
	private IElementLabelProvider fLabelProvider;
	
	private int fRelevance;
	
	
	public ElementNameCompletionProposal(final AssistInvocationContext context, final IElementName elementName, 
			final int replacementOffset, final IModelElement element, final int defDistance, 
			final IElementLabelProvider labelProvider) {
		fContext = context;
		fReplacementName = elementName;
		fReplacementOffset = replacementOffset;
		fElement = element;
		fLabelProvider = labelProvider;
		fRelevance = 60 - defDistance;
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
		final StringBuilder text = new StringBuilder(fElement.getElementName().getDisplayName());
		fLabelProvider.decorateText(text, fElement);
		return text.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public StyledString getStyledDisplayString() {
		final StyledString text = new StyledString(fElement.getElementName().getDisplayName());
		fLabelProvider.decorateStyledText(text, fElement);
		return text;
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
	public void selected(final ITextViewer viewer, final boolean smartToggle) {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void unselected(final ITextViewer viewer) {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getRelevance() {
		return fRelevance;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean validate(final IDocument document, final int offset, final DocumentEvent event) {
		try {
			final String content = document.get(fReplacementOffset, offset - fReplacementOffset);
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
	
	/**
	 * not supported, use {@link #apply(ITextViewer, char, int, int)}
	 */
	public void apply(final IDocument document) {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void apply(final ITextViewer viewer, final char trigger, final int stateMask, final int offset) {
		assert (fContext.getSourceViewer() == viewer);
		try {
			apply(trigger, stateMask, offset);
		}
		catch (final BadLocationException x) {
			// TODO
		}
	}
	
	protected void apply(final char trigger, final int stateMask, final int offset) throws BadLocationException {
		final SourceViewer viewer = fContext.getSourceViewer();
		final IDocument document = viewer.getDocument();
		final StringBuilder replacement = new StringBuilder(fReplacementName.getDisplayName());
		document.replace(fReplacementOffset, offset-fReplacementOffset, replacement.toString());
		setCursorPosition(fReplacementOffset + replacement.length());
	}
	
	
	protected void setCursorPosition(final int offset) {
		fCursorPosition = offset;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IContextInformation getContextInformation() {
		return fContextInformation;
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
