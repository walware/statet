/*******************************************************************************
 * Copyright (c) 2008-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.editors;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.assist.QuickAssistProcessor;

import de.walware.docmlet.tex.ui.editors.LtxQuickAssistProcessor;

import de.walware.statet.r.sweave.text.LtxRweaveSwitch;
import de.walware.statet.r.ui.sourceediting.RQuickAssistProcessor;


public class LtxRweaveQuickAssistProcessor implements IQuickAssistProcessor {
	
	
	private final QuickAssistProcessor fRProcessor;
	private final QuickAssistProcessor fTexProcessor;
	
	private String fErrorMessage;
	
	
	public LtxRweaveQuickAssistProcessor(final ISourceEditor editor) {
		fRProcessor = new RQuickAssistProcessor(editor);
		fTexProcessor = new LtxQuickAssistProcessor(editor);
	}
	
	
	@Override
	public boolean canFix(final Annotation annotation) {
		return false;
	}
	
	@Override
	public boolean canAssist(final IQuickAssistInvocationContext invocationContext) {
		return false;
	}
	
	@Override
	public ICompletionProposal[] computeQuickAssistProposals(final IQuickAssistInvocationContext invocationContext) {
		fErrorMessage = null;
		switch (LtxRweaveSwitch.get(invocationContext.getSourceViewer().getDocument(),
				invocationContext.getOffset())) {
		case LTX:
			try {
				return fTexProcessor.computeQuickAssistProposals(invocationContext);
			}
			finally {
				fErrorMessage = fTexProcessor.getErrorMessage();
			}
		case R:
			try {
				return fRProcessor.computeQuickAssistProposals(invocationContext);
			}
			finally {
				fErrorMessage = fRProcessor.getErrorMessage();
			}
		default:
			return null;
		}
	}
	
	@Override
	public String getErrorMessage() {
		return fErrorMessage;
	}
	
}
