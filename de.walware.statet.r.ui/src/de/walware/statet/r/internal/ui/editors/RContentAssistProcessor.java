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
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import de.walware.ecommons.text.IPartitionConstraint;
import de.walware.ecommons.ui.text.sourceediting.AssistInvocationContext;
import de.walware.ecommons.ui.text.sourceediting.ContentAssist;
import de.walware.ecommons.ui.text.sourceediting.ContentAssistComputerRegistry;
import de.walware.ecommons.ui.text.sourceediting.ContentAssistProcessor;
import de.walware.ecommons.ui.text.sourceediting.ISourceEditor;

import de.walware.statet.nico.ui.console.InputDocument;

import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;


public class RContentAssistProcessor extends ContentAssistProcessor {
	
	
	private static IPartitionConstraint NONE_COMMENT_CONSTRAINT = new IPartitionConstraint() {
		public boolean matches(final String partitionType) {
			return (partitionType != IRDocumentPartitions.R_COMMENT);
		}
	};
	
	private RHeuristicTokenScanner fScanner;
	
	
	public RContentAssistProcessor(final ContentAssist assistant, final String partition, 
			final ContentAssistComputerRegistry registry, final ISourceEditor editor) {
		super(assistant, partition, registry, editor);
		fScanner = (RHeuristicTokenScanner) editor.getAdapter(RHeuristicTokenScanner.class);
	}
	
	
	@Override
	protected AssistInvocationContext createCompletionProposalContext(final int offset) {
		return new RAssistInvocationContext(getEditor(), offset, true);
	}
	
	@Override
	protected AssistInvocationContext createContextInformationContext(final int offset) {
		return new RAssistInvocationContext(getEditor(), offset, false);
	}
	
	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] { ',' };
	}
	
	@Override
	protected IContextInformationValidator createContextInformationValidator() {
		return new RContextInformationValidator();
	}
	
	@Override
	protected boolean forceContextInformation(final AssistInvocationContext context) {
		int offset = context.getInvocationOffset();
		if (context.getIdentifierPrefix().length() > 0 
				|| fScanner == null) {
			return false;
		}
		IDocument document = context.getSourceViewer().getDocument();
		if (document instanceof InputDocument) {
			final InputDocument inputDoc = (InputDocument) document;
			document = inputDoc.getMasterDocument();
			offset = offset + inputDoc.getOffsetInMasterDocument();
		}
		if (offset < 2) {
			return false;
		}
		fScanner.configure(document, NONE_COMMENT_CONSTRAINT);
		final int previousOffset = fScanner.findNonBlankBackward(offset-1, RHeuristicTokenScanner.UNBOUND, true);
		if (previousOffset > 0) {
			try {
				final char c = document.getChar(previousOffset);
				if (c == '(' || c == ',') {
					final String partitionType = fScanner.getPartition(previousOffset).getType();
					return (fScanner.getPartitioningConfig().getDefaultPartitionConstraint().matches(partitionType));
				}
			}
			catch (final BadLocationException e) {
			}
		}
		return false;
	}
	
}
