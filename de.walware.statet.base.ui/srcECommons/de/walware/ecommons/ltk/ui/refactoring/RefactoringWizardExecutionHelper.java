/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.ui.refactoring;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;


/**
 * A helper class to activate the UI of a refactoring
 */
public class RefactoringWizardExecutionHelper {
	
	
	private RefactoringWizard fWizard;
	private int fSaveMode;
	private boolean fBuild;
	
	private RefactoringStatus fStatus;
	
	
	public RefactoringWizardExecutionHelper(final RefactoringWizard wizard, final int saveMode) {
		this(wizard, saveMode, false);
	}
	
	public RefactoringWizardExecutionHelper(final RefactoringWizard wizard, final int saveMode, final boolean build) {
		fWizard = wizard;
		fSaveMode = saveMode;
		fBuild = build;
	}
	
	
	public boolean perform(final Shell parent) {
		final RefactoringSaveHelper saveHelper = new RefactoringSaveHelper(fSaveMode);
		if (!saveHelper.saveEditors(parent)
				&& (fSaveMode & RefactoringSaveHelper.OPTIONAL) == 0) {
			return false;
		}
		try {
			if (fBuild) {
				saveHelper.triggerBuild();
			}
			final RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(fWizard);
			final int result = op.run(parent, fWizard.getRefactoring().getName());
			fStatus = op.getInitialConditionCheckingStatus();
			if (result == IDialogConstants.CANCEL_ID || result == RefactoringWizardOpenOperation.INITIAL_CONDITION_CHECKING_FAILED) {
				return false;
			}
			else {
				return true;
			}
		}
		catch (final InterruptedException e) {
			return false; // User action got cancelled
		}
	}
	
	public RefactoringStatus getInitialConditionCheckingStatus() {
		return fStatus;
	}
	
}
