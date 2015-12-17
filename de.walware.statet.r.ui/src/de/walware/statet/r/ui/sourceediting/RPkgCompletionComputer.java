/*=============================================================================#
 # Copyright (c) 2015 Stephan Wahlbrink (WalWare.de) and others.
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
import org.eclipse.core.runtime.IStatus;

import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistProposalCollector;

import de.walware.statet.r.core.model.RElementName;


public class RPkgCompletionComputer extends RElementsCompletionComputer {
	
	
	public RPkgCompletionComputer() {
	}
	
	
	@Override
	protected void computeCompletionProposals(final RAssistInvocationContext context, final int mode,
			final AssistProposalCollector proposals, final IProgressMonitor monitor) {
		final RElementName prefixName= context.getIdentifierElementName();
		if (prefixName == null || !isPackageName(prefixName)) {
			return;
		}
		
		doComputePkgNameProposals(context, ARG_TYPE_NO_PRIO, proposals);
	}
	
	@Override
	public IStatus computeInformationProposals(final AssistInvocationContext context,
			final AssistProposalCollector proposals, final IProgressMonitor monitor) {
		return null;
	}
	
}
