/*******************************************************************************
 * Copyright (c) 2008-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;

import de.walware.ecommons.ui.text.sourceediting.AssistInvocationContext;
import de.walware.ecommons.ui.text.sourceediting.ISourceEditor;
import de.walware.ecommons.ui.text.sourceediting.TemplatesCompletionComputer;

import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.templates.REditorContext;
import de.walware.statet.r.ui.editors.templates.REditorTemplatesContextType;


public class REditorTemplatesCompletionComputer extends TemplatesCompletionComputer {
	
	
	public REditorTemplatesCompletionComputer() {
		super(RUIPlugin.getDefault().getREditorTemplateStore(), RUIPlugin.getDefault().getREditorTemplateContextRegistry());
	}
	
	
	@Override
	protected String extractPrefix(final AssistInvocationContext context) {
		try {
			final IDocument document = context.getSourceViewer().getDocument();
			final int end = context.getInvocationOffset();
			final int start = Math.max(end-50, 0);
			final String text = document.get(start, end-start);
			int i = text.length()-1;
			while (i >= 0) {
				final char c = text.charAt(i);
				if (Character.isLetterOrDigit(c) || c == '.' || c == '_') {
					i--;
					continue;
				}
				if (c == '\\' || c == '@') {
					return text.substring(i);
				}
				return text.substring(i+1);
			}
		}
		catch (final BadLocationException e) {}
		return ""; //$NON-NLS-1$
	}
	
	@Override
	protected TemplateContextType getContextType(final AssistInvocationContext context, final IRegion region) {
		try {
			final ISourceEditor editor = context.getEditor();
			final AbstractDocument document = (AbstractDocument) context.getSourceViewer().getDocument();
			final ITypedRegion partition = document.getPartition(editor.getPartitioning().getPartitioning(), region.getOffset(), true);
			if (partition.getType() == IRDocumentPartitions.R_ROXYGEN) {
				return fTypeRegistry.getContextType(REditorTemplatesContextType.ROXYGEN_CONTEXTTYPE);
			}
			else {
				return fTypeRegistry.getContextType(REditorTemplatesContextType.RCODE_CONTEXTTYPE);
			}
		}
		catch (final BadPartitioningException e) {} 
		catch (final BadLocationException e) {}
		return null;
	}
	
	@Override
	protected DocumentTemplateContext createTemplateContext(final AssistInvocationContext context, final IRegion region) {
		final ISourceViewer viewer = context.getSourceViewer();
		final TemplateContextType contextType = getContextType(context, region);
		if (contextType != null) {
			final IDocument document = viewer.getDocument();
			return new REditorContext(contextType, document, region.getOffset(), region.getLength(), context.getEditor());
		}
		return null;
	}
	
}
