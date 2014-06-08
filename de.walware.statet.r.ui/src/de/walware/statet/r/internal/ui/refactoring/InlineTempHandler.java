/*=============================================================================#
 # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.refactoring;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ltk.ui.refactoring.RefactoringSaveHelper;
import de.walware.ecommons.ltk.ui.refactoring.RefactoringWizardExecutionHelper;
import de.walware.ecommons.ltk.ui.util.LTKSelectionUtil;
import de.walware.ecommons.workbench.ui.WorkbenchUIUtil;

import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.refactoring.InlineTempRefactoring;


public class InlineTempHandler extends AbstractHandler {
	
	
	public InlineTempHandler() {
	}
	
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ISelection selection = WorkbenchUIUtil.getCurrentSelection(event.getApplicationContext());
		final IWorkbenchPart activePart = WorkbenchUIUtil.getActivePart(event.getApplicationContext());
		if (selection == null || activePart == null) {
			return null;
		}
		final ISourceUnit su = LTKSelectionUtil.getSingleSourceUnit(activePart);
		if (!(su instanceof IRSourceUnit)) {
			return null;
		}
		InlineTempRefactoring refactoring = null;
		if (selection instanceof ITextSelection) {
			final ITextSelection textSelection = (ITextSelection) selection;
			refactoring = new InlineTempRefactoring((IRSourceUnit) su,
					new Region(textSelection.getOffset(), textSelection.getLength()) );
		}
		if (refactoring != null) {
			final RefactoringWizardExecutionHelper executionHelper = new RefactoringWizardExecutionHelper(
					new InlineTempWizard(refactoring), RefactoringSaveHelper.SAVE_NOTHING);
			executionHelper.perform(activePart.getSite().getShell());
		}
//			}
//			catch (final CoreException e) {
//				StatusManager.getManager().handle(new Status(
//						IStatus.ERROR, RUI.PLUGIN_ID, -1,
//						Messages.InlineTemp_Wizard_title, e),
//						StatusManager.LOG | StatusManager.SHOW);
//			}
		return null;
	}
	
}
