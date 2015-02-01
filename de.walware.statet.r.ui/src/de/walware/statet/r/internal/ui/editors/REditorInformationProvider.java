/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.editors;

import static de.walware.ecommons.ltk.ui.sourceediting.assist.IInfoHover.MODE_FOCUS;
import static de.walware.ecommons.ltk.ui.sourceediting.assist.IInfoHover.MODE_TOOLTIP;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;

import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.ui.sourceediting.EditorInformationProvider;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.assist.IInfoHover;

import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.internal.ui.rhelp.RHelpHover;
import de.walware.statet.r.ui.sourceediting.RAssistInvocationContext;


public class REditorInformationProvider extends EditorInformationProvider {
	
	
	private RHeuristicTokenScanner fScanner;
	
	
	public REditorInformationProvider(final ISourceEditor editor) {
		super(editor, new IInfoHover[] { new RHelpHover(MODE_TOOLTIP | MODE_FOCUS) });
	}
	
	
	@Override
	public IRegion getSubject(final ITextViewer textViewer, final int offset) {
		if (fScanner == null) {
			fScanner = (RHeuristicTokenScanner) LTK.getModelAdapter(
					getEditor().getModelTypeId(), RHeuristicTokenScanner.class );
		}
		try {
			final IDocument document = getEditor().getViewer().getDocument();
			fScanner.configure(document);
			final IRegion word = fScanner.findRWord(offset, false, true);
			if (word != null) {
				final ITypedRegion partition = fScanner.getPartition(word.getOffset());
				if (fScanner.getPartitioningConfig().getDefaultPartitionConstraint().matches(partition.getType())
						|| partition.getType() == IRDocumentPartitions.R_STRING
						|| partition.getType() == IRDocumentPartitions.R_QUOTED_SYMBOL) {
					return word;
				}
			}
		}
		catch (final Exception e) {
		}
		return new Region(offset, 0);
	}
	
	@Override
	protected AssistInvocationContext createContext(final IRegion region,
			final IProgressMonitor monitor) {
		final RAssistInvocationContext context = new RAssistInvocationContext(getEditor(), region, monitor);
		if (context.getAstSelection() == null) {
			return null;
		}
		return context;
	}
	
}
