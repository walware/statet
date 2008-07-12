/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;

import de.walware.eclipsecommons.ui.text.sourceediting.AssistInvocationContext;
import de.walware.eclipsecommons.ui.text.sourceediting.TemplatesCompletionComputer;

import de.walware.statet.r.core.rlang.RTokens;
import de.walware.statet.r.internal.ui.RUIPlugin;


/**
 * 
 */
public class REditorTemplatesCompletionComputer extends TemplatesCompletionComputer {
	
	
	private static final List<String> fgKeywords;
	static {
		final ArrayList<String> list = new ArrayList<String>();
		Collections.addAll(list, RTokens.CONSTANT_WORDS);
		Collections.addAll(list, RTokens.FLOWCONTROL_WORDS);
		Collections.sort(list, Collator.getInstance());
		list.trimToSize();
		fgKeywords = Collections.unmodifiableList(list);
	}
	
	
	/**
	 * 
	 */
	public REditorTemplatesCompletionComputer() {
		super(RUIPlugin.getDefault().getREditorTemplateStore(), RUIPlugin.getDefault().getREditorTemplateContextRegistry());
	}
	
	
	@Override
	protected List<String> getKeywords() {
		return fgKeywords;
	}
	
	@Override
	protected TemplateContextType getContextType(final ITextViewer viewer, final IRegion region) {
		return fTypeRegistry.getContextType(REditorTemplatesContextType.RCODE_CONTEXTTYPE);
	}
	
	protected DocumentTemplateContext createTemplateContext(final AssistInvocationContext context, final IRegion region) {
		final ISourceViewer viewer = context.getSourceViewer();
		final TemplateContextType contextType = getContextType(viewer, region);
		if (contextType != null) {
			
			final IDocument document = viewer.getDocument();
			return new REditorContext(contextType, document, region.getOffset(), region.getLength(), context.getEditor());
		}
		return null;
	}
	
}
