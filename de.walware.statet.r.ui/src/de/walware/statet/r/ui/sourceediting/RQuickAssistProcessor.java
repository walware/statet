/*=============================================================================#
 # Copyright (c) 2008-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.sourceediting;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistProposalCollector;
import de.walware.ecommons.ltk.ui.sourceediting.assist.IAssistCompletionProposal;
import de.walware.ecommons.ltk.ui.sourceediting.assist.IQuickAssistComputer;
import de.walware.ecommons.ltk.ui.sourceediting.assist.QuickAssistProcessor;

import de.walware.statet.r.internal.ui.editors.RQuickRefactoringComputer;


public class RQuickAssistProcessor extends QuickAssistProcessor {
	
	
	private final IQuickAssistComputer fComputer = new RQuickRefactoringComputer();
	
	
	public RQuickAssistProcessor(final ISourceEditor editor) {
		super(editor);
	}
	
	
	@Override
	protected AssistInvocationContext createContext(final IQuickAssistInvocationContext invocationContext,
			final IProgressMonitor monitor) {
		return new RAssistInvocationContext(getEditor(), invocationContext.getOffset(),
				true, monitor);
	}
	
	@Override
	protected void addModelAssistProposals(final AssistInvocationContext context,
			final AssistProposalCollector<IAssistCompletionProposal> proposals,
			final IProgressMonitor monitor) {
		fComputer.computeAssistProposals(context, proposals, monitor);
	}
	
	
}
