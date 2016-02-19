/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;

import de.walware.ecommons.ltk.ui.sourceediting.EditorTextInfoHoverProxy;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfiguration;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.assist.InfoHoverDescriptor;

import de.walware.statet.r.core.source.IRDocumentConstants;
import de.walware.statet.r.core.source.RHeuristicTokenScanner;
import de.walware.statet.r.ui.editors.IRSourceEditor;
import de.walware.statet.r.ui.sourceediting.RAssistInvocationContext;


public class REditorTextHover extends EditorTextInfoHoverProxy {
	
	
	private RHeuristicTokenScanner scanner;
	
	
	public REditorTextHover(final IRSourceEditor editor,
			final InfoHoverDescriptor descriptor, final SourceEditorViewerConfiguration config) {
		super(descriptor, config);
	}
	
	
	@Override
	public IRegion getHoverRegion(final ITextViewer textViewer, final int offset) {
		if (this.scanner == null) {
			this.scanner= RHeuristicTokenScanner.create(getEditor().getDocumentContentInfo());
		}
		try {
			final IDocument document= getEditor().getViewer().getDocument();
			this.scanner.configure(document);
			final IRegion word= this.scanner.findRWord(offset, false, true);
			if (word != null) {
				final ITypedRegion partition= this.scanner.getPartition(word.getOffset());
				if (IRDocumentConstants.R_DEFAULT_CONTENT_CONSTRAINT.matches(partition.getType())
						|| partition.getType() == IRDocumentConstants.R_STRING_CONTENT_TYPE
						|| partition.getType() == IRDocumentConstants.R_QUOTED_SYMBOL_CONTENT_TYPE) {
					return word;
				}
			}
			final char c= document.getChar(offset);
			if (c == '[') {
				final ITypedRegion partition= this.scanner.getPartition(offset);
				if (IRDocumentConstants.R_DEFAULT_CONTENT_CONSTRAINT.matches(partition.getType())) {
					return new Region(offset, 1);
				}
			}
		}
		catch (final Exception e) {
		}
		return null;
	}
	
	@Override
	protected AssistInvocationContext createContext(final IRegion region, final String contentType,
			final IProgressMonitor monitor) {
		// we are not in UI thread
		final RAssistInvocationContext context= new RAssistInvocationContext(
				(IRSourceEditor) getEditor(), region, contentType, this.scanner, monitor );
		if (context.getAstSelection() == null) {
			return null;
		}
		return context;
	}
	
}
