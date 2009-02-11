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

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.ui.text.sourceediting.AssistInvocationContext;
import de.walware.ecommons.ui.text.sourceediting.IContentAssistComputer;
import de.walware.ecommons.ui.text.sourceediting.ISourceEditor;
import de.walware.ecommons.ui.text.sourceediting.KeywordCompletionProposal;

import de.walware.statet.base.ui.StatetImages;


public class RoxygenCompletionComputer implements IContentAssistComputer {
	
	
	private static final String[] TAGS = new String[] {
		"docType",
		"export",
		"exportClass",
		"exportMethod",
		"exportPattern",
		"import",
		"importFrom",
		"importClassesFrom",
		"importMethodsFrom",
		"name",
		"aliases",
		"title",
		"usage",
		"references",
		"note",
		"include",
		"slot",
		"param",
		"return",
		"returnType",
		"seealso",
		"example",
		"examples",
		"author",
		"concept",
		"keywords",
		"method",
		"prototype",
		"S3method",
		"S3class",
		"listObject",
		"attributeObject",
		"environmentObject",
		"source",
		"format",
	};
	
	private static final List<String> TAG_COMMANDS;
	static {
		final String [] commands = new String[TAGS.length];
		for (int i = 0; i < commands.length; i++) {
			commands[i] = '@' + TAGS[i];
		}
		TAG_COMMANDS = Arrays.asList(commands);
	}
	
	private static class TagProposal extends KeywordCompletionProposal {
		
		public TagProposal(final String keyword, final int replacementOffset) {
			super(keyword, replacementOffset);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Image getImage() {
			return StatetImages.getImage(StatetImages.OBJ_TEXT_AT_TAG);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void apply(final ITextViewer viewer, final char trigger, final int stateMask, final int offset) {
			try {
				final IDocument document = viewer.getDocument();
				final int replacementOffset = getReplacementOffset();
				String replacementString = getReplacementString();
				if (offset == document.getLength() || document.getChar(offset) != ' ') {
					replacementString = replacementString + ' ';
				}
				document.replace(replacementOffset, offset-replacementOffset, replacementString);
				setCursorPosition(replacementOffset + replacementString.length());
			}
			catch (final BadLocationException e) {
			}
		}
		
	}
	
	
	public RoxygenCompletionComputer() {
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void sessionStarted(final ISourceEditor editor) {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void sessionEnded() {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IStatus computeCompletionProposals(final AssistInvocationContext context,
			final List<ICompletionProposal> tenders, final IProgressMonitor monitor) {
		final String tagPrefix = getTagPrefix(context);
		if (tagPrefix != null) {
			doComputeTagProposals(context, tagPrefix, tenders, monitor);
		}
		return null;
	}
	
	private String getTagPrefix(final AssistInvocationContext context) {
		try {
			final IDocument document = context.getSourceViewer().getDocument();
			final int start = Math.max(context.getInvocationOffset()-20, 0); // max keyword length incl 
			final String s = document.get(start, context.getInvocationOffset()-start);
			final int last = s.length()-1;
			int i = last;
			while (i >= 0) {
				final char c = s.charAt(i);
				if (c == '@') {
					return s.substring(i);
				}
				if (!isRoxygenTagChar(c)) {
					return (i == last) ? "" : null; //$NON-NLS-1$
				}
				i--;
			}
			return null;
		}
		catch (final BadLocationException e) {
			return null;
		}
	}
	
	private boolean isRoxygenTagChar(final int c) {
		if ((c >= 0x41 && c <= 0x5A) || (c >= 0x61 && c <= 0x7A)) {
			return true;
		}
		final int type = Character.getType(c);
		return (type > 0) && (type < 12 || type > 19);
	}
	
	private void doComputeTagProposals(final AssistInvocationContext context, final String prefix,
		final List<ICompletionProposal> tenders, final IProgressMonitor monitor) {
		final int offset = context.getInvocationOffset()-prefix.length();
		final List<String> keywords = TAG_COMMANDS;
		for (final String keyword : keywords) {
			if (keyword.regionMatches(true, 0, prefix, 0, prefix.length())) {
				tenders.add(new TagProposal(keyword, offset));
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IStatus computeContextInformation(final AssistInvocationContext context,
			final List<IContextInformation> tenders, final IProgressMonitor monitor) {
		return null;
	}
	
}
