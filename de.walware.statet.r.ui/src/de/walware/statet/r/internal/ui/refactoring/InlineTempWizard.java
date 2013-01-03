/*******************************************************************************
 * Copyright (c) 2008-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.refactoring;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.core.refactoring.InlineTempRefactoring;


public class InlineTempWizard extends RefactoringWizard {
	
	
	private static class InlineTempInputPage extends UserInputWizardPage {
		
		public static final String PAGE_NAME = "InlineTemp.InputPage"; //$NON-NLS-1$
		
		public InlineTempInputPage() {
			super(PAGE_NAME);
		}
		
		@Override
		protected InlineTempRefactoring getRefactoring() {
			return (InlineTempRefactoring) super.getRefactoring();
		}
		
		@Override
		public void createControl(final Composite parent) {
			final Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(LayoutUtil.applyDialogDefaults(new GridLayout(), 2));
			setControl(composite);
			
			final int count = getRefactoring().getReferencesCount();
			final String variableName = getRefactoring().getVariableName();
			
			{	final String title = NLS.bind(Messages.InlineTemp_Wizard_header, '`'+variableName+'`');
				final Label label = new Label(composite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
				label.setText(title);
				label.setFont(JFaceResources.getBannerFont());
			}
			
			LayoutUtil.addSmallFiller(composite, false);
			
			{	final Label label = new Label(composite, SWT.WRAP);
				final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
				gd.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
				gd.heightHint = LayoutUtil.hintHeight(label, 3);
				label.setLayoutData(gd);
				if (count == 1) {
					label.setText("No other references besides the selected assignment to the local variable found.");
				}
				else {
					label.setText(NLS.bind("An assignment and {0} references to the local variable found.", count-1));
				}
			}
			
			LayoutUtil.addSmallFiller(composite, false);
			Dialog.applyDialogFont(composite);
//			PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), );
		}
		
	}
	
	
	public InlineTempWizard(final InlineTempRefactoring ref) {
		super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE | NO_BACK_BUTTON_ON_STATUS_DIALOG);
		setDefaultPageTitle(Messages.InlineTemp_Wizard_title);
	}
	
	
	@Override
	protected void addUserInputPages() {
		addPage(new InlineTempInputPage());
	}
	
}
