/*=============================================================================#
 # Copyright (c) 2008-2016 Stephan Wahlbrink (WalWare.de) and others.
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

import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistProposalCollector;
import de.walware.ecommons.ltk.ui.sourceediting.assist.IQuickAssistComputer;
import de.walware.ecommons.ltk.ui.sourceediting.assist.QuickAssistProcessor;

import de.walware.statet.r.core.source.RHeuristicTokenScanner;
import de.walware.statet.r.internal.ui.editors.RQuickRefactoringComputer;
import de.walware.statet.r.ui.editors.IRSourceEditor;


public class RQuickAssistProcessor extends QuickAssistProcessor {
	
	
	private final IQuickAssistComputer computer= new RQuickRefactoringComputer();
	
	private RHeuristicTokenScanner scanner;
	
	
	public RQuickAssistProcessor(final IRSourceEditor editor) {
		super(editor);
	}
	
	
	@Override
	protected AssistInvocationContext createContext(final IQuickAssistInvocationContext invocationContext,
			final String contentType,
			final IProgressMonitor monitor) {
//		if (this.scanner == null) {
//			this.scanner= RHeuristicTokenScanner.create(getEditor().getDocumentContentInfo());
//		}
		return new RAssistInvocationContext((IRSourceEditor) getEditor(),
				invocationContext.getOffset(), contentType, true, this.scanner, monitor );
	}
	
	@Override
	protected void addModelAssistProposals(final AssistInvocationContext context,
			final AssistProposalCollector proposals, final IProgressMonitor monitor) {
		this.computer.computeAssistProposals(context, proposals, monitor);
	}
	
}
