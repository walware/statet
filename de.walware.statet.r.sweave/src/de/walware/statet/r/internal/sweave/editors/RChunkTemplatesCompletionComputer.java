/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.editors;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistProposalCollector;
import de.walware.ecommons.ltk.ui.sourceediting.assist.IAssistCompletionProposal;
import de.walware.ecommons.ltk.ui.sourceediting.assist.IContentAssistComputer;
import de.walware.ecommons.ltk.ui.sourceediting.assist.TemplatesCompletionComputer;

import de.walware.statet.r.internal.sweave.LtxRweaveTemplatesContextType;
import de.walware.statet.r.internal.sweave.SweavePlugin;


public class RChunkTemplatesCompletionComputer extends TemplatesCompletionComputer {
	
	
	public RChunkTemplatesCompletionComputer() {
		super(	SweavePlugin.getDefault().getRweaveTexGenerationTemplateStore(),
				SweavePlugin.getDefault().getRweaveTexGenerationTemplateContextRegistry() );
	}
	
	
	@Override
	public IStatus computeCompletionProposals(final AssistInvocationContext context, final int mode,
			final AssistProposalCollector<IAssistCompletionProposal> proposals, final IProgressMonitor monitor) {
		// Set to specific mode to force to include templates in default mode
		return super.computeCompletionProposals(context, IContentAssistComputer.SPECIFIC_MODE, proposals, monitor);
	}
	
	@Override
	protected boolean include(final Template template, final String prefix) {
		return true;
	}
	
	@Override
	protected String extractPrefix(final AssistInvocationContext context) {
		final IDocument document = context.getSourceViewer().getDocument();
		final int offset = context.getOffset();
		try {
			final int lineOffset = document.getLineOffset(document.getLineOfOffset(offset));
			if (offset-lineOffset == 2) {
				if (document.getChar(lineOffset) == '<' && document.getChar(lineOffset+1) == '<') {
					return "<<"; //$NON-NLS-1$
				}
			}
			else if (offset-lineOffset == 1) {
				if (document.getChar(lineOffset) == '<') {
					return "<"; //$NON-NLS-1$
				}
			}
			else if (offset-lineOffset == 0) {
				return ""; //$NON-NLS-1$
			}
		}
		catch (final BadLocationException e) {
		}
		return null;
	}
	
	@Override
	protected TemplateContextType getContextType(final AssistInvocationContext context, final IRegion region) {
		return getTypeRegistry().getContextType(LtxRweaveTemplatesContextType.RWEAVETEX_DEFAULT_CONTEXTTYPE);
	}
	
	@Override
	protected Image getImage(final Template template) {
		return SweavePlugin.getDefault().getImageRegistry().get(SweavePlugin.IMG_OBJ_RCHUNK);
	}
	
}
