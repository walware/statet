/*=============================================================================#
 # Copyright (c) 2008-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.editors;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import de.walware.jcommons.collections.ImCollections;

import de.walware.ecommons.ltk.ui.LTKUI;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistProposalCollector;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssist;
import de.walware.ecommons.ltk.ui.sourceediting.assist.IContentAssistComputer;


public class RoxygenCompletionComputer implements IContentAssistComputer {
	
	
	private static final List<String> TAG_COMMANDS;
	
	static {
		@SuppressWarnings("nls")
		final String[] tags= new String[] {
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
			"S3method", // deprecated 4.0
			"S3class",
			"listObject",
			"attributeObject",
			"environmentObject",
			"noRd",
			"useDynLib",
			"rdname", // 2.0
			"template", // 2.0
			"section", // 2.0
			"description", // 2.0
			"details", // 2.0
			"family", // 2.0
			"inheritParams", // 2.0
			"format", // 2.0
			"source", // 2.1
			"encoding", // 2.2
			"describeIn", // since 4.0
			"field", // since 4.0 (for fields of reference classes)
		};
		
		final String [] commands= new String[tags.length];
		for (int i= 0; i < commands.length; i++) {
			commands[i]= '@' + tags[i];
		}
		TAG_COMMANDS= ImCollections.newList(commands);
	}
	
	private static class TagProposal extends RKeywordCompletionProposal {
		
		
		public TagProposal(final AssistInvocationContext context, final String keyword, final int replacementOffset) {
			super(context, keyword, replacementOffset);
		}
		
		
		@Override
		public Image getImage() {
			return LTKUI.getImages().get(LTKUI.OBJ_TEXT_AT_TAG_IMAGE_ID);
		}
		
		@Override
		protected int computeReplacementLength(final int replacementOffset, final Point selection, final int caretOffset, final boolean overwrite) throws BadLocationException {
			int end= Math.max(caretOffset, selection.x + selection.y);
			if (overwrite) {
				final IDocument document= getInvocationContext().getSourceViewer().getDocument();
				while (end < document.getLength()) {
					if (Character.isLetterOrDigit(document.getChar(end))) {
						end++;
						continue;
					}
					break;
				}
			}
			return (end - replacementOffset);
		}
		
		@Override
		protected void doApply(final char trigger, final int stateMask,
				final int caretOffset, final int replacementOffset, final int replacementLength) throws BadLocationException {
			final AssistInvocationContext context= getInvocationContext();
			final SourceViewer viewer= context.getSourceViewer();
			final IDocument document= viewer.getDocument();
			try {
				String replacementString= getReplacementString();
				final int newCaretOffset= replacementOffset+replacementString.length()+1;
				if (replacementOffset+replacementLength == document.getLength() || document.getChar(replacementOffset+replacementLength) != ' ') {
					replacementString= replacementString + ' ';
				}
				document.replace(replacementOffset, replacementLength, replacementString);
				setCursorPosition(newCaretOffset);
			}
			catch (final BadLocationException e) {
			}
		}
		
	}
	
	
	public RoxygenCompletionComputer() {
	}
	
	
	@Override
	public void sessionStarted(final ISourceEditor editor, final ContentAssist assist) {
	}
	
	@Override
	public void sessionEnded() {
	}
	
	@Override
	public IStatus computeCompletionProposals(final AssistInvocationContext context, final int mode,
			final AssistProposalCollector proposals, final IProgressMonitor monitor) {
		final String tagPrefix= getTagPrefix(context);
		if (tagPrefix != null) {
			doComputeTagProposals(context, tagPrefix, proposals, monitor);
		}
		
		return Status.OK_STATUS;
	}
	
	@Override
	public IStatus computeInformationProposals(final AssistInvocationContext context,
			final AssistProposalCollector proposals, final IProgressMonitor monitor) {
		return null;
	}
	
	
	private String getTagPrefix(final AssistInvocationContext context) {
		try {
			final IDocument document= context.getSourceViewer().getDocument();
			final int start= Math.max(context.getInvocationOffset() - 20, 0); // max keyword length incl 
			final String s= document.get(start, context.getInvocationOffset()-start);
			final int last= s.length()-1;
			int i= last;
			while (i >= 0) {
				final char c= s.charAt(i);
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
		final int type= Character.getType(c);
		return (type > 0) && (type < 12 || type > 19);
	}
	
	private void doComputeTagProposals(final AssistInvocationContext context, final String prefix,
		final AssistProposalCollector proposals, final IProgressMonitor monitor) {
		final int offset= context.getInvocationOffset() - prefix.length();
		final List<String> keywords= TAG_COMMANDS;
		for (final String keyword : keywords) {
			if (keyword.regionMatches(true, 0, prefix, 0, prefix.length())) {
				proposals.add(new TagProposal(context, keyword, offset));
			}
		}
	}
	
}
