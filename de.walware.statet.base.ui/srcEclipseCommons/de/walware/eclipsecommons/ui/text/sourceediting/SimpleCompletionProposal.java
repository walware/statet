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

package de.walware.eclipsecommons.ui.text.sourceediting;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;


/**
 * The standard implementation of the <code>ICompletionProposal</code> interface.
 */
public class SimpleCompletionProposal implements ICompletionProposal, ICompletionProposalExtension2, ICompletionProposalExtension4, 
		IRatedProposal {
	
	/** The replacement string. */
	private String fReplacementString;
	
	/** The replacement offset. */
	private int fReplacementOffset;
	
	/** The cursor position after this proposal has been applied. */
	private int fCursorPosition = -1;
	
	/** The context information of this proposal. */
	private IContextInformation fContextInformation;
	
	
	public SimpleCompletionProposal(final String replacementString, 
			final int replacementOffset) {
		fReplacementString = replacementString;
		fReplacementOffset = replacementOffset;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Image getImage() {
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getDisplayString() {
		return fReplacementString;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getAdditionalProposalInfo() {
		return null;
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
	 * {@value 50}
	 */
	public int getRelevance() {
		return 50;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean validate(final IDocument document, final int offset, final DocumentEvent event) {
		try {
			final String content = document.get(fReplacementOffset, offset - fReplacementOffset);
			if (fReplacementString.startsWith(content)) {
				return true;
			}
		}
		catch (final BadLocationException e) {
			// ignore concurrently modified document
		}
		return false;
	}
	
	public boolean isAutoInsertable() {
		return true;
	}
	
	/**
	 * not supported, use {@link #apply(ITextViewer, char, int, int)}
	 */
	public void apply(final IDocument document) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void apply(final ITextViewer viewer, final char trigger, final int stateMask, final int offset) {
		try {
			final IDocument document = viewer.getDocument();
			document.replace(fReplacementOffset, offset-fReplacementOffset, fReplacementString);
			fCursorPosition = fReplacementOffset + fReplacementString.length();
		}
		catch (final BadLocationException x) {
		}
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
