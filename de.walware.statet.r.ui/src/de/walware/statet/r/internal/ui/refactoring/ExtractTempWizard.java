/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.refactoring;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.ltk.ui.refactoring.RefactoringBasedStatus;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.core.refactoring.ExtractTempRefactoring;


public class ExtractTempWizard extends RefactoringWizard {
	
	
	private static class InputPage extends UserInputWizardPage {
		
		
		public static final String PAGE_NAME = "ExtractTemp.InputPage"; //$NON-NLS-1$
		
		
		private Text fVariableNameControl;
		private Button fReplaceAllControl;
		
		
		public InputPage() {
			super(PAGE_NAME);
		}
		
		@Override
		protected ExtractTempRefactoring getRefactoring() {
			return (ExtractTempRefactoring) super.getRefactoring();
		}
		
		public void createControl(final Composite parent) {
			final Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(LayoutUtil.applyDialogDefaults(new GridLayout(), 2));
			setControl(composite);
			
			final int count = getRefactoring().getAllOccurrencesCount();
			
			{	final Label label = new Label(composite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
				label.setText(Messages.ExtractTemp_Wizard_header);
				label.setFont(JFaceResources.getBannerFont());
			}
			
			LayoutUtil.addSmallFiller(composite, false);
			
			{	final Label label = new Label(composite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
				label.setText(Messages.ExtractTemp_Wizard_VariableName_label);
				
				fVariableNameControl = new Text(composite, SWT.BORDER);
				fVariableNameControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				fVariableNameControl.setFont(JFaceResources.getTextFont());
			}
			
			LayoutUtil.addSmallFiller(composite, false);
			
			{	
				if (count > 0) {
					final Label label = new Label(composite, SWT.WRAP);
					label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					if (count == 1) {
						label.setText("No other occurrences of the selected expression found.");
					}
					else {
						label.setText(NLS.bind("{0} other occurrences of the selected expression found.", count-1));
					}
				}
				
				fReplaceAllControl = new Button(composite, SWT.CHECK);
				fReplaceAllControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
				fReplaceAllControl.setText(Messages.ExtractTemp_Wizard_ReplaceAll_label);
				if (count <= 1) {
					fReplaceAllControl.setEnabled(false);
				}
			}
			
			LayoutUtil.addSmallFiller(composite, false);
			Dialog.applyDialogFont(composite);
			
			initBindings();
//			PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),);
		}
		
		protected void initBindings() {
			final Realm realm = Realm.getDefault();
			final DataBindingContext dbc = new DataBindingContext(realm);
			
			addBindings(dbc, realm);
			WizardPageSupport.create(this, dbc);
		}
		
		protected void addBindings(final DataBindingContext dbc, final Realm realm) {
			dbc.bindValue(SWTObservables.observeText(fVariableNameControl, SWT.Modify),
					PojoObservables.observeValue(realm, getRefactoring(), "tempName"), //$NON-NLS-1$
					new UpdateValueStrategy().setAfterGetValidator(new IValidator() {
						public IStatus validate(final Object value) {
							return new RefactoringBasedStatus(getRefactoring().checkTempName((String) value));
						}
					}), null);
			dbc.bindValue(SWTObservables.observeSelection(fReplaceAllControl), 
					PojoObservables.observeValue(realm, getRefactoring(), "replaceAllOccurrences"), null, null);
		}
		
		@Override
		public void setVisible(final boolean visible) {
			super.setVisible(visible);
			fVariableNameControl.setFocus();
		}
		
	}
	
	
	public ExtractTempWizard(final ExtractTempRefactoring ref) {
		super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE | NO_BACK_BUTTON_ON_STATUS_DIALOG);
		setDefaultPageTitle(Messages.ExtractTemp_Wizard_title);
	}
	
	
	@Override
	protected void addUserInputPages() {
		addPage(new InputPage());
	}
	
}
