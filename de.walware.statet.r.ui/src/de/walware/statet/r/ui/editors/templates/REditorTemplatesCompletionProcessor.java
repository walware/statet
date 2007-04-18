/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors.templates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.icu.text.Collator;

import org.eclipse.core.resources.IFile;
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
import org.eclipse.ui.IEditorPart;

import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.core.rlang.RTokens;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.templates.RTemplateProposal.RTemplateComparator;
import de.walware.statet.ui.StatetImages;


public class REditorTemplatesCompletionProcessor extends TemplateCompletionProcessor {

	
	private static final RTemplateComparator fgTemplateComparator = new RTemplateProposal.RTemplateComparator();

	private static final String[] fgKeywords;
	static {
		ArrayList<String> list = new ArrayList<String>();
		Collections.addAll(list, RTokens.SPECIAL_CONSTANTS);
		Collections.addAll(list, RTokens.LOGICAL_CONSTANTS);
		Collections.addAll(list, RTokens.FLOWCONTROL_RESERVED_WORDS);
		Collections.sort(list, Collator.getInstance());
		fgKeywords = list.toArray(new String[list.size()]);
	}
	
	
	private TemplateStore fTemplateStore;
	private ContextTypeRegistry fTypeRegistry;
	private IEditorPart fEditor;
	
	public REditorTemplatesCompletionProcessor(IEditorPart editor) {
		
		fTemplateStore = RUIPlugin.getDefault().getREditorTemplateStore();
		fTypeRegistry = RUIPlugin.getDefault().getREditorTemplateContextRegistry();
		fEditor = editor;
	}

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {

		ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();

		// adjust offset to end of normalized selection
		if (selection.getOffset() == offset)
			offset = selection.getOffset() + selection.getLength();

		String prefix = extractPrefix(viewer, offset);
		IRegion region;
		if (selection.getLength() == 0) {
			region = new Region(offset - prefix.length(), prefix.length());
		} else { 
			region = new Region(selection.getOffset(), selection.getLength());
		}
		REditorContext context = createContext(viewer, region);
		if (context == null)
			return new ICompletionProposal[0];

		List<ICompletionProposal> matches = new ArrayList<ICompletionProposal>();

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
	
	private void doComputeProposals(List<ICompletionProposal> matches, REditorContext context, String prefix, IRegion replacementRegion) {
		
		// Add Templates
		Template[] templates = getTemplates(context.getContextType().getId());
		List<RTemplateProposal> templateMatches = new ArrayList<RTemplateProposal>();
		for (int i = 0; i < templates.length; i++) {
			Template template = templates[i];
			try {
				context.getContextType().validate(template.getPattern());
			} catch (TemplateException e) {
				continue;
			}
			if (template.getContextTypeId().equals(context.getContextType().getId()) 
					&& template.getName().startsWith(prefix)) // <- �nderung gegen�ger super
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
	protected String extractPrefix(ITextViewer viewer, int offset) {
		
		int i = offset;
		IDocument document = viewer.getDocument();
		if (i > document.getLength())
			return ""; //$NON-NLS-1$

		try {
			while (i > 0) {
				char ch = document.getChar(i - 1);
				if (RTokens.isSeparator(ch))
					break;
				i--;
			}

			return document.get(i, offset - i);
		} catch (BadLocationException e) {
			return ""; //$NON-NLS-1$
		}
	}
	
	@Override
	protected Template[] getTemplates(String contextTypeId) {
		
		return fTemplateStore.getTemplates();
	}

	@Override
	protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {

		return fTypeRegistry.getContextType(REditorTemplatesContextType.RSCIRPT_CONTEXTTYPE);
	}

	@Override
	protected Image getImage(Template template) {

		return StatetImages.getDefault().getImage(StatetImages.IMG_CONTENTASSIST_TEMPLATE);
	}
	
	
	@Override
	protected REditorContext createContext(ITextViewer contextViewer, IRegion region) {
		
		TemplateContextType contextType = getContextType(contextViewer, region);
		if (contextType != null) {
			
			IDocument document = contextViewer.getDocument();
			RResourceUnit unit = new RResourceUnit((IFile) fEditor.getEditorInput().getAdapter(IFile.class));
			return new REditorContext(contextType, document, region.getOffset(), region.getLength(), unit);
		}
		return null;
	}

	@Override
	protected RTemplateProposal createProposal(Template template, TemplateContext context, IRegion region, int relevance) {
		return new RTemplateProposal(template, context, region, getImage(template), relevance);
	}
	
}
