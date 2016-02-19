/*=============================================================================#
 # Copyright (c) 2008-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.sourceediting;

import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssist;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssistComputerRegistry;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ContentAssistProcessor;
import de.walware.ecommons.text.core.IFragmentDocument;
import de.walware.ecommons.text.core.IPartitionConstraint;

import de.walware.statet.r.console.core.util.LoadReferencesUtil;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.core.source.RHeuristicTokenScanner;
import de.walware.statet.r.internal.ui.editors.RContextInformationValidator;
import de.walware.statet.r.ui.editors.IRSourceEditor;


public class RContentAssistProcessor extends ContentAssistProcessor {
	
	
	private static IPartitionConstraint NO_R_COMMENT_CONSTRAINT = new IPartitionConstraint() {
		@Override
		public boolean matches(final String partitionType) {
			return (partitionType != IRDocumentConstants.R_COMMENT_CONTENT_TYPE);
		}
	};
	
	
	private class Context extends RAssistInvocationContext {
		
		
		public Context(final int offset, final boolean isProposal,
				final IProgressMonitor monitor) {
			super((IRSourceEditor) RContentAssistProcessor.this.getEditor(),
					offset, getContentType(),
					isProposal,
					RContentAssistProcessor.this.scanner,
					monitor );
		}
		
		
		@Override
		protected int getToolReferencesWaitTimeout() {
			return (getAssistant().isCompletionProposalAutoRequest() ?
					LoadReferencesUtil.MAX_AUTO_WAIT : LoadReferencesUtil.MAX_EXPLICITE_WAIT );
		}
		
		@Override
		protected void toolReferencesResolved(final ImList<ICombinedRElement> resolvedElements) {
			reloadPossibleCompletions(this);
		}
		
	}
	
	
	private final RHeuristicTokenScanner scanner;
	
	private int timeoutCounter;
	
	
	public RContentAssistProcessor(final ContentAssist assistant, final String partition, 
			final ContentAssistComputerRegistry registry, final IRSourceEditor editor) {
		super(assistant, partition, registry, editor);
		this.scanner= RHeuristicTokenScanner.create(editor.getDocumentContentInfo());
	}
	
	
	@Override
	protected AssistInvocationContext createCompletionProposalContext(final int offset,
			final IProgressMonitor monitor) {
		return new Context(offset, true, monitor);
	}
	
	@Override
	protected AssistInvocationContext createContextInformationContext(final int offset,
			final IProgressMonitor monitor) {
		final Context context;
		if (this.timeoutCounter <= 3) {
			final long startTime= System.nanoTime();
			
			context= new Context(offset, true, monitor);
			
			if (TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) > 50) {
				this.timeoutCounter= Math.min(this.timeoutCounter + 1, 10);
			}
			else {
				this.timeoutCounter= Math.max(this.timeoutCounter - 1, 0);
			}
		}
		else {
			context= new Context(offset, false, monitor);
		}
		return context;
	}
	
	
	@Override
	protected IContextInformationValidator createContextInformationValidator() {
		return new RContextInformationValidator();
	}
	
	@Override
	protected boolean forceContextInformation(final AssistInvocationContext context) {
		try {
			int offset = context.getInvocationOffset();
			if (context.getIdentifierPrefix().length() > 0 
					|| this.scanner == null) {
				return false;
			}
			IDocument document = context.getSourceViewer().getDocument();
			if (document instanceof IFragmentDocument) {
				final IFragmentDocument inputDoc = (IFragmentDocument) document;
				document = inputDoc.getMasterDocument();
				offset = offset + inputDoc.getOffsetInMasterDocument();
			}
			if (offset < 2) {
				return false;
			}
			this.scanner.configure(document, NO_R_COMMENT_CONSTRAINT);
			final int previousOffset = this.scanner.findNonBlankBackward(offset, RHeuristicTokenScanner.UNBOUND, true);
			if (previousOffset > 0) {
				final char c = document.getChar(previousOffset);
				if (c == '(' || c == ',') {
					final String partitionType = this.scanner.getPartition(previousOffset).getType();
					return (IRDocumentConstants.R_DEFAULT_CONTENT_CONSTRAINT.matches(partitionType));
				}
			}
		}
		catch (final BadLocationException e) {}
		
		return false;
	}
	
}
