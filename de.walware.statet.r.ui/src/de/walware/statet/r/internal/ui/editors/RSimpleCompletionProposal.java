/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import de.walware.jcommons.collections.CollectionUtils;

import de.walware.ecommons.ltk.ui.sourceediting.assist.SimpleCompletionProposal;

import de.walware.statet.r.core.RSymbolComparator;
import de.walware.statet.r.core.source.RHeuristicTokenScanner;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.sourceediting.RAssistInvocationContext;


public class RSimpleCompletionProposal extends SimpleCompletionProposal {
	
	
	public static class RHelpTopicCompletionProposal extends RSimpleCompletionProposal
			implements ICompletionProposalExtension6 {
		
		
		private final int relevance;
		
		private Object packages;
		
		
		public RHelpTopicCompletionProposal(final RAssistInvocationContext context,
				final String replacementString, final Object packages, final int replacementOffset,
				final int relevance) {
			super(context, replacementString, replacementOffset);
			
			this.relevance= relevance;
			this.packages= packages;
		}
		
		
		@Override
		public int getRelevance() {
			return this.relevance;
		}
		
		@Override
		public Image getImage() {
			return RUI.getImage(RUI.IMG_OBJ_R_HELP_TOPIC);
		}
		
		@Override
		public StyledString getStyledDisplayString() {
			final StyledString s= new StyledString(getReplacementString());
			if (this.packages != null) {
				if (this.packages instanceof List) {
					this.packages= CollectionUtils.toString((List<String>) this.packages, ", "); //$NON-NLS-1$
				}
				s.append(QUALIFIER_SEPARATOR, StyledString.QUALIFIER_STYLER);
				s.append((String) this.packages, StyledString.QUALIFIER_STYLER);
			}
			return s;
		}
		
	}
	
	
	public RSimpleCompletionProposal(final RAssistInvocationContext context,
			final String replacementString, final int replacementOffset) {
		super(context, replacementString, replacementOffset);
	}
	
	
	@Override
	protected String getPluginId() {
		return RUI.PLUGIN_ID;
	}
	
	
	@Override
	protected int computeReplacementLength(final int replacementOffset, final Point selection, final int caretOffset, final boolean overwrite) {
		// keep in synch with RElementCompletionProposal
		final int end= Math.max(caretOffset, selection.x + selection.y);
		if (overwrite) {
			final RElementCompletionProposal.ApplyData data= new RElementCompletionProposal.ApplyData(
					(RAssistInvocationContext) getInvocationContext() );
			final RHeuristicTokenScanner scanner= data.getScanner();
			scanner.configure(data.getDocument());
			final IRegion word= scanner.findRWord(end, false, true);
			if (word != null) {
				return (word.getOffset() + word.getLength() - replacementOffset);
			}
		}
		return (end - replacementOffset);
	}
	
	@Override
	public boolean isAutoInsertable() {
		return false;
	}
	
	@Override
	public boolean validate(final IDocument document, final int offset, final DocumentEvent event) {
		// keep in synch with RElementCompletionProposal
		try {
			int start= getReplacementOffset();
			int length= offset - getReplacementOffset();
			if (length > 0 && document.getChar(start) == '`') {
				start++;
				length--;
			}
			if (length > 0 && document.getChar(start+length-1) == '`') {
				length--;
			}
			final String prefix= document.get(start, length);
			final String replacement= getReplacementString();
			if (new RSymbolComparator.PrefixPattern(prefix).matches(replacement)) {
				return true;
			}
		}
		catch (final BadLocationException e) {
			// ignore concurrently modified document
		}
		return false;
	}
	
}
