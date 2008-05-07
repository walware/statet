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

package de.walware.statet.r.ui.editors.templates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.icu.text.Collator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Image;

import de.walware.statet.base.ui.StatetImages;
import de.walware.statet.ext.templates.StatextTemplateProposal;
import de.walware.statet.ext.templates.StatextTemplateProposal.TemplateComparator;
import de.walware.statet.r.core.rlang.RTokens;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.REditor;


public class REditorTemplatesCompletionProcessor extends TemplateCompletionProcessor {
	
	
	private static final TemplateComparator fgTemplateComparator = new StatextTemplateProposal.TemplateComparator();
	
	private static final String[] fgKeywords;
	static {
		final ArrayList<String> list = new ArrayList<String>();
		Collections.addAll(list, RTokens.CONSTANT_WORDS);
		Collections.addAll(list, RTokens.FLOWCONTROL_WORDS);
		Collections.sort(list, Collator.getInstance());
		fgKeywords = list.toArray(new String[list.size()]);
	}
	
	
	private TemplateStore fTemplateStore;
	private ContextTypeRegistry fTypeRegistry;
	private REditor fEditor;
	
	
	public REditorTemplatesCompletionProcessor(final REditor editor) {
		fTemplateStore = RUIPlugin.getDefault().getREditorTemplateStore();
		fTypeRegistry = RUIPlugin.getDefault().getREditorTemplateContextRegistry();
		fEditor = editor;
	}
	
	@Override
	public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {
		
		final ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();
		
		// adjust offset to end of normalized selection
//		if (selection.getOffset() == offset)
//			offset = selection.getOffset() + selection.getLength();
		
		String prefix = extractPrefix(viewer, offset);
		IRegion region;
		if (selection.getLength() == 0) {
			region = new Region(offset - prefix.length(), prefix.length());
		} else {
			region = new Region(selection.getOffset(), selection.getLength());
		}
		final REditorContext context = createContext(viewer, region);
		if (context == null)
			return new ICompletionProposal[0];
		
		final List<ICompletionProposal> matches = new ArrayList<ICompletionProposal>();
		
		if (prefix.length() > 0 && prefix.length() == selection.getLength()) {
			doComputeProposals(matches, context, prefix, region);
			prefix = ""; // wenn erfolglos, dann ohne prefix //$NON-NLS-1$
		}
		
		if (matches.isEmpty()) {
			context.setVariable("selection", selection.getText()); // name of the selection variables {line, word}_selection //$NON-NLS-1$
			doComputeProposals(matches, context, prefix, region);
		}
		
		return matches.toArray(new ICompletionProposal[matches.size()]);
	}
	
	private void doComputeProposals(final List<ICompletionProposal> matches, final REditorContext context, final String prefix, final IRegion replacementRegion) {
		// Add Templates
		final Template[] templates = getTemplates(context.getContextType().getId());
		final List<StatextTemplateProposal> templateMatches = new ArrayList<StatextTemplateProposal>();
		for (int i = 0; i < templates.length; i++) {
			final Template template = templates[i];
			try {
				context.getContextType().validate(template.getPattern());
			} catch (final TemplateException e) {
				continue;
			}
			if (template.getContextTypeId().equals(context.getContextType().getId())
					&& template.getName().startsWith(prefix)) // Change <-> super
				templateMatches.add(createProposal(template, context, replacementRegion, getRelevance(template, prefix)));
		}
		if (templateMatches.size() > 0) {
			Collections.sort(templateMatches, fgTemplateComparator);
			matches.addAll(templateMatches);
		}
		
		// Add R keywords (allready sorted)
		if (prefix.length() > 0) {
			for (int i = 0; i < fgKeywords.length; i++) {
				if (fgKeywords[i].startsWith(prefix))
					matches.add(new CompletionProposal(fgKeywords[i],
							replacementRegion.getOffset(), replacementRegion.getLength(), replacementRegion.getOffset()+fgKeywords[i].length()));
			}
		}
	}
	
	@Override
	protected String extractPrefix(final ITextViewer viewer, final int offset) {
		int i = offset;
		final IDocument document = viewer.getDocument();
		if (i > document.getLength())
			return ""; //$NON-NLS-1$
		
		try {
			while (i > 0) {
				final char ch = document.getChar(i - 1);
				if (RTokens.isSeparator(ch))
					break;
				i--;
			}
			
			return document.get(i, offset - i);
		} catch (final BadLocationException e) {
			return ""; //$NON-NLS-1$
		}
	}
	
	@Override
	protected Template[] getTemplates(final String contextTypeId) {
		return fTemplateStore.getTemplates();
	}
	
	@Override
	protected TemplateContextType getContextType(final ITextViewer viewer, final IRegion region) {
		return fTypeRegistry.getContextType(REditorTemplatesContextType.RCODE_CONTEXTTYPE);
	}
	
	@Override
	protected Image getImage(final Template template) {
		return StatetImages.getImage(StatetImages.CONTENTASSIST_TEMPLATE);
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
	protected StatextTemplateProposal createProposal(final Template template, final TemplateContext context, final IRegion region, final int relevance) {
		return new StatextTemplateProposal(template, context, region, getImage(template), relevance);
	}
	
}
