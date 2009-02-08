/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.ui.text.sourceediting.TemplateProposal;
import de.walware.ecommons.ui.text.sourceediting.TemplateProposal.TemplateComparator;

import de.walware.statet.base.ui.StatetImages;

import de.walware.statet.r.ui.editors.REditor;
import de.walware.statet.r.ui.editors.templates.REditorContext;


public class RChunkTemplatesCompletionProcessor extends TemplateCompletionProcessor {
	
	
	private static final TemplateComparator fgTemplateComparator = new TemplateProposal.TemplateComparator();
	
	
	private TemplateStore fTemplateStore;
	private ContextTypeRegistry fTypeRegistry;
	private REditor fEditor;
	
	
	public RChunkTemplatesCompletionProcessor(final REditor editor) {
		fTemplateStore = SweavePlugin.getDefault().getRweaveTexGenerationTemplateStore();
		fTypeRegistry = SweavePlugin.getDefault().getRweaveTexGenerationTemplateContextRegistry();
		fEditor = editor;
	}
	
	
	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '<' };
	}
	
	@Override
	public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {
		final ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();
		
		// adjust offset to end of normalized selection
//		if (selection.getOffset() == offset)
//			offset = selection.getOffset() + selection.getLength();
		
		final String prefix = extractChunkPrefix(viewer, offset);
		if (prefix == null) {
			return new ICompletionProposal[0];
		}
		IRegion region;
		if (selection.getLength() == 0) {
			region = new Region(offset - prefix.length(), prefix.length());
		} else {
			region = new Region(selection.getOffset(), selection.getLength());
		}
		final REditorContext context = createContext(viewer, region);
		if (context == null) {
			return new ICompletionProposal[0];
		}
		
		final List<ICompletionProposal> matches = new ArrayList<ICompletionProposal>();
		
		context.setVariable("selection", selection.getText()); //$NON-NLS-1$
		doComputeProposals(matches, context, prefix, region);
		
		return matches.toArray(new ICompletionProposal[matches.size()]);
	}
	
	private void doComputeProposals(final List<ICompletionProposal> matches, final REditorContext context, final String prefix, final IRegion replacementRegion) {
		// Add Templates
		final Template[] templates = getTemplates(context.getContextType().getId());
		final List<TemplateProposal> templateMatches = new ArrayList<TemplateProposal>();
		for (int i = 0; i < templates.length; i++) {
			final Template template = templates[i];
			try {
				context.getContextType().validate(template.getPattern());
			} catch (final TemplateException e) {
				continue;
			}
			if (template.getContextTypeId().equals(context.getContextType().getId())
//					&& template.getPattern().startsWith(prefix)
					) // Change <-> super
				templateMatches.add(createProposal(template, context, replacementRegion, getRelevance(template, prefix)));
		}
		if (templateMatches.size() > 0) {
			Collections.sort(templateMatches, fgTemplateComparator);
			matches.addAll(templateMatches);
		}
	}
	
	protected String extractChunkPrefix(final ITextViewer viewer, final int offset) {
		final IDocument document = viewer.getDocument();
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
		} catch (final BadLocationException e) {
		}
		return null;
	}
	
	@Override
	protected Template[] getTemplates(final String contextTypeId) {
		return fTemplateStore.getTemplates(RweaveTexTemplatesContextType.RWEAVETEX_DEFAULT_CONTEXTTYPE);
	}
	
	@Override
	protected TemplateContextType getContextType(final ITextViewer viewer, final IRegion region) {
		return fTypeRegistry.getContextType(RweaveTexTemplatesContextType.RWEAVETEX_DEFAULT_CONTEXTTYPE);
	}
	
	@Override
	protected Image getImage(final Template template) {
		return StatetImages.getImage(StatetImages.OBJ_TEXT_TEMPLATE);
	}
	
	
	@Override
	protected REditorContext createContext(final ITextViewer contextViewer, final IRegion region) {
		final TemplateContextType contextType = getContextType(contextViewer, region);
		if (contextType != null) {
			final IDocument document = contextViewer.getDocument();
			return new REditorContext(contextType, document, region.getOffset(), region.getLength(), fEditor);
		}
		return null;
	}
	
	@Override
	protected TemplateProposal createProposal(final Template template, final TemplateContext context, final IRegion region, final int relevance) {
		return new TemplateProposal(template, context, region, getImage(template), relevance);
	}
	
}
