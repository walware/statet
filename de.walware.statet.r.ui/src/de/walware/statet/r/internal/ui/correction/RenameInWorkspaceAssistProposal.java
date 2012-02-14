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

package de.walware.statet.r.internal.ui.correction;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.ltk.ui.refactoring.RefactoringSaveHelper;
import de.walware.ecommons.ltk.ui.refactoring.RefactoringWizardExecutionHelper;
import de.walware.ecommons.ltk.ui.sourceediting.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.CommandAssistProposal;

import de.walware.statet.r.core.model.IRWorkspaceSourceUnit;
import de.walware.statet.r.core.refactoring.RenameInWorkspaceRefactoring;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.ui.RUIMessages;
import de.walware.statet.r.internal.ui.refactoring.RenameInWorkspaceWizard;


public class RenameInWorkspaceAssistProposal extends CommandAssistProposal {
	
	
	private final RAstNode fNameNode;
	
	
	public RenameInWorkspaceAssistProposal(final AssistInvocationContext invocationContext,
			final RAstNode nameNode) {
		super(invocationContext, "de.walware.ecommons.ltk.commands.RefactorRenameInWorkspace"); //$NON-NLS-1$
		fLabel = RUIMessages.Proposal_RenameInWorkspace_label;
		fDescription = RUIMessages.Proposal_RenameInWorkspace_description;
		fRelevance = 8;
		
		fNameNode = nameNode;
	}
	
	
	@Override
	public void apply(final ITextViewer viewer, final char trigger, final int stateMask, final int offset) {
		RenameInWorkspaceRefactoring refactoring = null;
		{	refactoring = new RenameInWorkspaceRefactoring((IRWorkspaceSourceUnit) fContext.getSourceUnit(),
					fNameNode );
		}
		if (refactoring != null) {
			final RefactoringWizardExecutionHelper executionHelper = new RefactoringWizardExecutionHelper(
					new RenameInWorkspaceWizard(refactoring), RefactoringSaveHelper.SAVE_REFACTORING | RefactoringSaveHelper.EXCLUDE_ACTIVE_EDITOR, true);
			executionHelper.perform(viewer.getTextWidget().getShell());
		}
	}
	
	@Override
	public Image getImage() {
		return null;
	}
	
}
