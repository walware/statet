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

package de.walware.statet.r.internal.ui.refactoring;

import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.ltk.ui.refactoring.RefactoringBasedStatus;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.r.core.refactoring.RElementSearchProcessor.Mode;
import de.walware.statet.r.core.refactoring.RRefactoringAdapter;
import de.walware.statet.r.core.refactoring.RenameInWorkspaceRefactoring;


public class RenameInWorkspaceWizard extends RefactoringWizard {
	
	
	private static class InputPage extends UserInputWizardPage {
		
		
		public static final String PAGE_NAME = "RenameInWorkspace.InputPage"; //$NON-NLS-1$
		
		
		private Text fVariableNameControl;
		
		
		public InputPage() {
			super(PAGE_NAME);
		}
		
		@Override
		protected RenameInWorkspaceRefactoring getRefactoring() {
			return (RenameInWorkspaceRefactoring) super.getRefactoring();
		}
		
		@Override
		public void createControl(final Composite parent) {
			final Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(LayoutUtil.applyDialogDefaults(new GridLayout(), 2));
			setControl(composite);
			
			final RenameInWorkspaceRefactoring refactoring = getRefactoring();
			final String name = refactoring.getNewName();
			
			{	final Label label = new Label(composite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
				label.setText(NLS.bind(Messages.RenameInWorkspace_Wizard_header, 
						RRefactoringAdapter.getQuotedIdentifier(name) ));
				label.setFont(JFaceResources.getBannerFont());
			}
			
			LayoutUtil.addSmallFiller(composite, false);
			
			{	final Label label = new Label(composite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
				label.setText(Messages.RenameInWorkspace_Wizard_VariableName_label);
				
				fVariableNameControl = new Text(composite, SWT.BORDER);
				fVariableNameControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				fVariableNameControl.setFont(JFaceResources.getTextFont());
			}
			
			{	final Group group = new Group(composite, SWT.NONE);
				group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
				group.setText("Scope:");
				group.setLayout(LayoutUtil.createGroupGrid(1));
				
				final List<Mode> modes = refactoring.getAvailableModes();
				if (modes.contains(Mode.WORKSPACE)) {
					final Button button = new Button(group, SWT.RADIO);
					button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
					button.setText("&Workspace (complete project tree)");
					button.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent e) {
							getRefactoring().setMode(Mode.WORKSPACE);
						}
					});
					if (refactoring.getMode() == Mode.WORKSPACE) {
						button.setSelection(true);
					}
				}
				if (modes.contains(Mode.CURRENT_AND_REFERENCING_PROJECTS)) {
					final Button button = new Button(group, SWT.RADIO);
					button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
					button.setText("Current and &referencing projects");
					button.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent e) {
							getRefactoring().setMode(Mode.CURRENT_AND_REFERENCING_PROJECTS);
						}
					});
					if (refactoring.getMode() == Mode.CURRENT_AND_REFERENCING_PROJECTS) {
						button.setSelection(true);
					}
				}
				if (modes.contains(Mode.CURRENT_PROJECT)) {
					final Button button = new Button(group, SWT.RADIO);
					button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
					button.setText("C&urrent project");
					button.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent e) {
							getRefactoring().setMode(Mode.CURRENT_PROJECT);
						}
					});
					if (refactoring.getMode() == Mode.CURRENT_PROJECT) {
						button.setSelection(true);
					}
				}
				if (modes.contains(Mode.LOCAL_FRAME)) {
					final Button button = new Button(group, SWT.RADIO);
					button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
					button.setText("&Local frame.");
					button.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent e) {
							getRefactoring().setMode(Mode.LOCAL_FRAME);
						}
					});
					if (refactoring.getMode() == Mode.LOCAL_FRAME) {
						button.setSelection(true);
					}
				}
			}
			
			LayoutUtil.addSmallFiller(composite, false);
			Dialog.applyDialogFont(composite);
			
			initBindings();
			fVariableNameControl.selectAll();
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
					PojoObservables.observeValue(realm, getRefactoring(), "newName"), //$NON-NLS-1$
					new UpdateValueStrategy().setAfterGetValidator(new IValidator() {
						@Override
						public IStatus validate(final Object value) {
							final RenameInWorkspaceRefactoring refactoring = getRefactoring();
							final RefactoringStatus status = refactoring.checkNewName((String) value);
							if (status.isOK() && refactoring.getCurrentName().equals(value)) {
								return Status.CANCEL_STATUS;
							}
							return new RefactoringBasedStatus(status);
						}
					}), null);
		}
		
		@Override
		public void setVisible(final boolean visible) {
			super.setVisible(visible);
			fVariableNameControl.setFocus();
		}
		
	}
	
	
	public RenameInWorkspaceWizard(final RenameInWorkspaceRefactoring ref) {
		super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE | NO_BACK_BUTTON_ON_STATUS_DIALOG);
		setDefaultPageTitle(Messages.RenameInWorkspace_Wizard_title);
	}
	
	
	@Override
	protected void addUserInputPages() {
		addPage(new InputPage());
	}
	
}
