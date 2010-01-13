/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.text.sourceediting;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;


/**
 * A content assist executor can invoke content assist for a specific proposal category on an editor.
 */
public final class SpecificContentAssistHandler extends AbstractHandler {
	
	
	public static final String PAR_CATEGORY_ID = "categoryId"; //$NON-NLS-1$
	
	
	private ISourceEditor fEditor;
	private ContentAssistComputerRegistry fRegistry;
	
	
	/**
	 * Creates a new handler.
	 *
	 * @param registry the computer registry to use for the enablement of proposal categories
	 */
	public SpecificContentAssistHandler(final ISourceEditor editor, final ContentAssistComputerRegistry registry) {
		fEditor = editor;
		fRegistry = registry;
	}
	
	
	/**
	 * Invokes content assist on <code>editor</code>, showing only proposals computed by the
	 * <code>CompletionProposalCategory</code> with the given <code>categoryId</code>.
	 *
	 * @param editor the editor to invoke code assist on
	 * @param categoryId the id of the proposal category to show proposals for
	 */
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final String par = event.getParameter(PAR_CATEGORY_ID);
		if (par == null) {
			return null;
		}
		fRegistry.startSpecificMode(par);
		
		final ITextOperationTarget target = (ITextOperationTarget) fEditor.getAdapter(ITextOperationTarget.class);
		try {
			if (target != null && target.canDoOperation(ISourceViewer.CONTENTASSIST_PROPOSALS)) {
				target.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
			}
		}
		finally {
			fRegistry.stopSpecificMode();
		}
		return null;
	}
	
}
