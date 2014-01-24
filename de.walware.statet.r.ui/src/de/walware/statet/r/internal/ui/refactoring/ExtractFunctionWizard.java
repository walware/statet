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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.ltk.ui.refactoring.RefactoringBasedStatus;
import de.walware.ecommons.ui.components.ButtonGroup;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.ViewerUtil.CheckboxTableComposite;

import de.walware.statet.r.core.refactoring.ExtractFunctionRefactoring;
import de.walware.statet.r.core.refactoring.ExtractFunctionRefactoring.Variable;


public class ExtractFunctionWizard extends RefactoringWizard {
	
	
	private static class InputPage extends UserInputWizardPage {
		
		
		public static final String PAGE_NAME = "ExtractFunction.InputPage"; //$NON-NLS-1$
		
		
		private static final ViewerFilter[] FILTER_DEFAULT = new ViewerFilter[] {
			new ViewerFilter() {
				@Override
				public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
					if (element instanceof Variable) {
						final Variable variable = (Variable) element;
						return (variable.getUseAsArgumentDefault() || variable.getUseAsArgument());
					}
					return true;
				}
			}
		};
		
		private static final ViewerFilter[] FILTER_OFF = new ViewerFilter[0];
		
		
		private Text fVariableNameControl;
		private CheckboxTableViewer fArgumentsViewer;
		private ButtonGroup<Variable> fArgumentsButtons;
		
		
		public InputPage() {
			super(PAGE_NAME);
		}
		
		@Override
		protected ExtractFunctionRefactoring getRefactoring() {
			return (ExtractFunctionRefactoring) super.getRefactoring();
		}
		
		@Override
		public void createControl(final Composite parent) {
			final Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(LayoutUtil.applyDialogDefaults(new GridLayout(), 2));
			setControl(composite);
			
			{	final String title = Messages.ExtractFunction_Wizard_header;
				final Label label = new Label(composite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
				label.setText(title);
				label.setFont(JFaceResources.getBannerFont());
			}
			
			LayoutUtil.addSmallFiller(composite, false);
			
			{	final Label label = new Label(composite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
				label.setText(Messages.ExtractFunction_Wizard_VariableName_label);
				
				fVariableNameControl = new Text(composite, SWT.BORDER);
				fVariableNameControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				fVariableNameControl.setFont(JFaceResources.getTextFont());
			}
			
			LayoutUtil.addSmallFiller(composite, false);
			
			final Control table = createArgumentsTable(composite);
			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			
			LayoutUtil.addSmallFiller(composite, false);
			Dialog.applyDialogFont(composite);
//			PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),);
			
			fArgumentsViewer.setFilters(FILTER_DEFAULT);
			initBindings();
			for (final Variable variable : getRefactoring().getVariables()) {
				fArgumentsViewer.setChecked(variable, variable.getUseAsArgumentDefault());
			}
			fArgumentsButtons.updateState();
		}
		
		private Control createArgumentsTable(final Composite parent) {
			final Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
			
			{	final Composite above = new Composite(composite, SWT.NONE);
				above.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 2, 1));
				above.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 2));
				
				final Label label = new Label(above, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
				label.setText("Function &parameters:");
				label.addTraverseListener(new TraverseListener() {
					@Override
					public void keyTraversed(final TraverseEvent e) {
						if (e.detail == SWT.TRAVERSE_MNEMONIC) {
							e.doit = false;
							fArgumentsViewer.getControl().setFocus();
						}
					}
				});
				final Button showAll = new Button(above, SWT.CHECK);
				showAll.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
				showAll.setText("Show &all identifiers");
				showAll.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						if (showAll.getSelection()) {
							fArgumentsViewer.setFilters(FILTER_OFF);
						}
						else {
							fArgumentsViewer.setFilters(FILTER_DEFAULT);
						}
					}
				});
			}
			
			final CheckboxTableComposite table = new CheckboxTableComposite(composite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			gd.heightHint = LayoutUtil.hintHeight(table.table, 12);
			table.setLayoutData(gd);
			table.table.setHeaderVisible(true);
			table.table.setLinesVisible(true);
			fArgumentsViewer = table.viewer;
			
			{	final TableViewerColumn column = table.addColumn("Variable", SWT.LEFT, new ColumnWeightData(1));
				column.setLabelProvider(new CellLabelProvider() {
					@Override
					public void update(final ViewerCell cell) {
						final Object element = cell.getElement();
						if (element instanceof Variable) {
							cell.setFont(JFaceResources.getTextFont());
							final Variable variable = (Variable) element;
							cell.setText(variable.getName());
							return;
						}
						cell.setText("");
					}
				});
			}
			
			fArgumentsViewer.setContentProvider(new ArrayContentProvider());
			
			fArgumentsButtons = new ButtonGroup<Variable>(composite);
			fArgumentsButtons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));
			fArgumentsButtons.addUpButton(null);
			fArgumentsButtons.addDownButton(null);
			
			return composite;
		}
		
		protected void initBindings() {
			final Realm realm = Realm.getDefault();
			final DataBindingContext dbc = new DataBindingContext(realm);
			
			addBindings(dbc, realm);
			WizardPageSupport.create(this, dbc);
		}
		
		protected void addBindings(final DataBindingContext dbc, final Realm realm) {
			dbc.bindValue(SWTObservables.observeText(fVariableNameControl, SWT.Modify),
					PojoObservables.observeValue(realm, getRefactoring(), "functionName"),
					new UpdateValueStrategy().setAfterGetValidator(new IValidator() {
						@Override
						public IStatus validate(final Object value) {
							return new RefactoringBasedStatus(getRefactoring().checkFunctionName((String) value));
						}
					}), null);
			fArgumentsViewer.addCheckStateListener(new ICheckStateListener() {
				@Override
				public void checkStateChanged(final CheckStateChangedEvent event) {
					final Object element = event.getElement();
					if (element instanceof Variable) {
						final Variable variable = (Variable) element;
						variable.setUseAsArgument(event.getChecked());
					}
				}
			});
			final IObservableList argumentsList = new WritableList(realm, getRefactoring().getVariables(), Variable.class);
			fArgumentsViewer.setInput(argumentsList);
			fArgumentsButtons.connectTo(fArgumentsViewer, argumentsList, null);
			
//			dbc.bindValue(SWTObservables.observeSelection(fReplaceAllControl), 
//					PojoObservables.observeValue(realm, getRefactoring(), "replaceAllOccurrences"), null, null);
		}
		
		@Override
		public void setVisible(final boolean visible) {
			super.setVisible(visible);
			fVariableNameControl.setFocus();
		}
		
	}
	
	
	public ExtractFunctionWizard(final ExtractFunctionRefactoring ref) {
		super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE | NO_BACK_BUTTON_ON_STATUS_DIALOG);
		setDefaultPageTitle(Messages.ExtractFunction_Wizard_title);
	}
	
	
	@Override
	protected void addUserInputPages() {
		addPage(new InputPage());
	}
	
}
