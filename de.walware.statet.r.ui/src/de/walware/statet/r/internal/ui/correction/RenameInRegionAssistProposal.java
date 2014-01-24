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

package de.walware.statet.r.internal.ui.correction;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;

import de.walware.ecommons.ltk.ui.refactoring.RefactoringSaveHelper;
import de.walware.ecommons.ltk.ui.refactoring.RefactoringWizardExecutionHelper;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.assist.CommandAssistProposal;

import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.refactoring.RenameInRegionRefactoring;
import de.walware.statet.r.internal.ui.RUIMessages;
import de.walware.statet.r.internal.ui.refactoring.RenameInRegionWizard;


public class RenameInRegionAssistProposal extends CommandAssistProposal {
	
	
	public RenameInRegionAssistProposal(final AssistInvocationContext invocationContext) {
		super(invocationContext, "de.walware.ecommons.ltk.commands.RefactorRenameInSelectedRegion"); //$NON-NLS-1$
		fLabel = RUIMessages.Proposal_RenameInRegion_label;
		fDescription = RUIMessages.Proposal_RenameInRegion_description;
	}
	
	
	@Override
	public void apply(final ITextViewer viewer, final char trigger, final int stateMask, final int offset) {
		RenameInRegionRefactoring refactoring = null;
		{	final ITextSelection textSelection = (ITextSelection) viewer.getSelectionProvider().getSelection();
			refactoring = new RenameInRegionRefactoring((IRSourceUnit) fContext.getSourceUnit(),
					new Region(textSelection.getOffset(), textSelection.getLength()) );
		}
		if (refactoring != null) {
			final RefactoringWizardExecutionHelper executionHelper = new RefactoringWizardExecutionHelper(
					new RenameInRegionWizard(refactoring), RefactoringSaveHelper.SAVE_NOTHING);
			executionHelper.perform(viewer.getTextWidget().getShell());
		}
	}
	
}
