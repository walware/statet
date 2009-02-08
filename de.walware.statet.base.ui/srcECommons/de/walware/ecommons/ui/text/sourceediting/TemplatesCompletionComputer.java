/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.text.sourceediting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.ui.text.sourceediting.TemplateProposal.TemplateComparator;

import de.walware.statet.base.ui.StatetImages;


/**
 * Content assist computer for editor templates
 */
public abstract class TemplatesCompletionComputer implements IContentAssistComputer {
	
	
	private static final TemplateComparator fgTemplateComparator = new TemplateProposal.TemplateComparator();
	
	
	protected final TemplateStore fTemplateStore;
	protected final ContextTypeRegistry fTypeRegistry;
	
	
	public TemplatesCompletionComputer(final TemplateStore templateStore, final ContextTypeRegistry contextTypes) {
		fTemplateStore = templateStore;
		fTypeRegistry = contextTypes;
	}
	
	
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
		final ISourceViewer viewer = context.getSourceViewer();
		
		String prefix = extractPrefix(context);
		IRegion region;
		if (context.getLength() == 0) {
			region = new Region(context.getInvocationOffset() - prefix.length(), prefix.length());
		}
		else {
			region = new Region(context.getOffset(), context.getLength());
		}
		final DocumentTemplateContext templateContext = createTemplateContext(context, region);
		if (templateContext == null)
			return null;
		
		int count = 0;
		if (prefix.length() > 0 && prefix.length() == context.getLength()) {
			count = doComputeProposals(tenders, templateContext, prefix, region);
			prefix = ""; // wenn erfolglos, dann ohne prefix //$NON-NLS-1$
		}
		
		if (count == 0) {
			try {
				final String text = viewer.getDocument().get(context.getOffset(), context.getLength());
				templateContext.setVariable("selection", text); // name of the selection variables {line, word}_selection //$NON-NLS-1$
				doComputeProposals(tenders, templateContext, prefix, region);
			}
			catch (final BadLocationException e) {
			}
		}
		return null;
	}
	
	private int doComputeProposals(final List<ICompletionProposal> tenders, final DocumentTemplateContext context, final String prefix, final IRegion replacementRegion) {
		// Add Templates
		int count = 0;
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
					&& template.getName().regionMatches(true, 0, prefix, 0, prefix.length())) { // Change <-> super
				templateMatches.add(createProposal(template, context, replacementRegion, getRelevance(template, prefix)));
			}
		}
		if (templateMatches.size() > 0) {
			Collections.sort(templateMatches, fgTemplateComparator);
			tenders.addAll(templateMatches);
			count += templateMatches.size();
		}
		
		return count;
	}
	
	public IStatus computeContextInformation(final AssistInvocationContext context,
			final List<IContextInformation> tenders, final IProgressMonitor monitor) {
		return null;
	}
	
	
	protected String extractPrefix(final AssistInvocationContext context) {
		return context.computeIdentifierPrefix();
	}
	
	protected Template[] getTemplates(final String contextTypeId) {
		return fTemplateStore.getTemplates();
	}
	
	protected abstract TemplateContextType getContextType(final AssistInvocationContext context, final IRegion region);
	
	protected DocumentTemplateContext createTemplateContext(final AssistInvocationContext context, final IRegion region) {
		final ISourceViewer viewer = context.getSourceViewer();
		final TemplateContextType contextType = getContextType(context, region);
		if (contextType != null) {
			final IDocument document = viewer.getDocument();
			return new DocumentTemplateContext(contextType, document, region.getOffset(), region.getLength());
		}
		return null;
	}
	
	protected TemplateProposal createProposal(final Template template, final TemplateContext context, final IRegion region, final int relevance) {
		return new TemplateProposal(template, context, region, getImage(template), relevance);
	}
	
	protected Image getImage(final Template template) {
		return StatetImages.getImage(StatetImages.OBJ_TEXT_TEMPLATE);
	}
	
	/**
	 * Returns the relevance of a template given a prefix. The default
	 * implementation returns a number greater than zero if the template name
	 * starts with the prefix, and zero otherwise.
	 *
	 * @param template the template to compute the relevance for
	 * @param prefix the prefix after which content assist was requested
	 * @return the relevance of <code>template</code>
	 * @see #extractPrefix(ITextViewer, int)
	 */
	protected int getRelevance(final Template template, final String prefix) {
		if (template.getName().regionMatches(true, 0, prefix, 0, prefix.length())) {
			return 90;
		}
		return 0;
	}
	
}
