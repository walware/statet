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
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
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
import de.walware.ecommons.ui.util.ViewerUtil;
import de.walware.ecommons.ui.util.ViewerUtil.CheckTableComposite;

import de.walware.statet.r.core.refactoring.FunctionToS4MethodRefactoring;
import de.walware.statet.r.core.refactoring.FunctionToS4MethodRefactoring.Variable;


public class FunctionToS4MethodWizard extends RefactoringWizard {
	
	
	private static class InputPage extends UserInputWizardPage {
		
		
		public static final String PAGE_NAME = "FunctionToS4Method.InputPage"; //$NON-NLS-1$
		
		private static class TypeEditing extends EditingSupport {
			
			private final TextCellEditor fCellEditor;
			
			public TypeEditing(final ColumnViewer viewer) {
				super(viewer);
				fCellEditor = new TextCellEditor((Composite) viewer.getControl());
				fCellEditor.getControl().setFont(JFaceResources.getTextFont());
			}
			
			@Override
			protected boolean canEdit(final Object element) {
				return (element instanceof Variable);
			}
			
			@Override
			protected CellEditor getCellEditor(final Object element) {
				if (element instanceof Variable) {
					return fCellEditor;
				}
				return null;
			}
			
			@Override
			protected Object getValue(final Object element) {
				if (element instanceof Variable) {
					final Variable variable = (Variable) element;
					final String type = variable.getArgumentType();
					return (type != null) ? type : "";
				}
				return null;
			}
			
			@Override
			protected void setValue(final Object element, final Object value) {
				if (element instanceof Variable) {
					final Variable variable = (Variable) element;
					variable.setArgumentType((String) value);
					getViewer().update(element, null);
				}
			}
			
		}
		
		private Text fFunctionNameControl;
		private CheckboxTableViewer fArgumentsViewer;
		private ButtonGroup<Variable> fArgumentsButtons;
		
		private Button fGenerateGenericControl;
		
		
		public InputPage() {
			super(PAGE_NAME);
		}
		
		@Override
		protected FunctionToS4MethodRefactoring getRefactoring() {
			return (FunctionToS4MethodRefactoring) super.getRefactoring();
		}
		
		@Override
		public void createControl(final Composite parent) {
			final Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(LayoutUtil.applyDialogDefaults(new GridLayout(), 2));
			setControl(composite);
			
			{	final String title = Messages.FunctionToS4Method_Wizard_header;
				final Label label = new Label(composite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
				label.setText(title);
				label.setFont(JFaceResources.getBannerFont());
			}
			
			LayoutUtil.addSmallFiller(composite, false);
			
			{	final Label label = new Label(composite, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
				label.setText(Messages.FunctionToS4Method_Wizard_VariableName_label);
				
				fFunctionNameControl = new Text(composite, SWT.BORDER);
				fFunctionNameControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				fFunctionNameControl.setFont(JFaceResources.getTextFont());
			}
			
			LayoutUtil.addSmallFiller(composite, false);
			
			final Control table = createArgumentsTable(composite);
			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			
			LayoutUtil.addSmallFiller(composite, false);
			
			{	fGenerateGenericControl = new Button(composite, SWT.CHECK);
				fGenerateGenericControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
				fGenerateGenericControl.setText(Messages.FunctionToS4Method_Wizard_GenerateGeneric_label);
			}
			
			LayoutUtil.addSmallFiller(composite, false);
			Dialog.applyDialogFont(composite);
//			PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),);
			
			initBindings();
			for (final Variable variable : getRefactoring().getVariables()) {
				if (!variable.getName().equals("...")) { //$NON-NLS-1$
					fArgumentsViewer.setChecked(variable, variable.getUseAsGenericArgumentDefault());
				}
				else {
					fArgumentsViewer.setGrayed(variable, true);
					fArgumentsViewer.setChecked(variable, true);
				}
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
				label.setText("Method &parameters:");
				label.addTraverseListener(new TraverseListener() {
					@Override
					public void keyTraversed(final TraverseEvent e) {
						if (e.detail == SWT.TRAVERSE_MNEMONIC) {
							e.doit = false;
							fArgumentsViewer.getControl().setFocus();
						}
					}
				});
			}
			
			final CheckTableComposite table = new CheckTableComposite(composite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			gd.heightHint = LayoutUtil.hintHeight(table.table, 12);
			table.setLayoutData(gd);
			table.setLayoutData(gd);
			table.table.setHeaderVisible(true);
			table.table.setLinesVisible(true);
			fArgumentsViewer = table.viewer;
			
			{	final TableViewerColumn column = table.addColumn("Name", SWT.LEFT, new ColumnWeightData(1));
				column.setLabelProvider(new CellLabelProvider() {
					@Override
					public void update(final ViewerCell cell) {
						final Object element = cell.getElement();
						if (element instanceof Variable) {
							cell.setFont(JFaceResources.getDialogFont());
							final Variable variable = (Variable) element;
							cell.setText(variable.getName());
							return;
						}
						cell.setText(""); //$NON-NLS-1$
					}
				});
			}
			
			{	final TableViewerColumn column = table.addColumn("Type", SWT.LEFT, new ColumnWeightData(1));
				column.setLabelProvider(new CellLabelProvider() {
					@Override
					public void update(final ViewerCell cell) {
						final Object element = cell.getElement();
						if (element instanceof Variable) {
							cell.setFont(JFaceResources.getTextFont());
							final Variable variable = (Variable) element;
							final String type = variable.getArgumentType();
							cell.setText((type != null) ? type : ""); //$NON-NLS-1$
							return;
						}
						cell.setText(""); //$NON-NLS-1$
					}
				});
				final TypeEditing editing = new TypeEditing(table.viewer);
				
				column.setEditingSupport(editing);
			}
			
			ViewerUtil.installDefaultEditBehaviour(fArgumentsViewer);
			fArgumentsViewer.setContentProvider(new ArrayContentProvider());
			
			fArgumentsButtons = new ButtonGroup<Variable>(composite) {
				@Override
				protected void move1(final int oldIdx, final int newIdx) {
					final Object upperElement = fArgumentsViewer.getElementAt(Math.min(oldIdx, newIdx));
					final Object lowerElement = fArgumentsViewer.getElementAt(Math.max(oldIdx, newIdx));
					super.move1(oldIdx, newIdx);
					if ((upperElement instanceof Variable)
							&& !((Variable) upperElement).getName().equals("...") //$NON-NLS-1$
							&& (lowerElement instanceof Variable)
							&& !((Variable) lowerElement).getName().equals("...")) { //$NON-NLS-1$
						final boolean checked = fArgumentsViewer.getChecked(upperElement);
						((Variable) lowerElement).setUseAsGenericArgument(checked);
						fArgumentsViewer.setChecked(lowerElement, checked);
					}
				}
			};
			fArgumentsButtons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));
			fArgumentsButtons.addUpButton();
			fArgumentsButtons.addDownButton();
			
			return composite;
		}
		
		protected void initBindings() {
			final Realm realm = Realm.getDefault();
			final DataBindingContext dbc = new DataBindingContext(realm);
			
			addBindings(dbc, realm);
			WizardPageSupport.create(this, dbc);
		}
		
		protected void addBindings(final DataBindingContext dbc, final Realm realm) {
			dbc.bindValue(SWTObservables.observeText(fFunctionNameControl, SWT.Modify),
					PojoObservables.observeValue(realm, getRefactoring(), "functionName"), //$NON-NLS-1$
					new UpdateValueStrategy().setAfterGetValidator(new IValidator() {
						@Override
						public IStatus validate(final Object value) {
							return new RefactoringBasedStatus(getRefactoring().checkFunctionName((String) value));
						}
					}), null);
			final IObservableList argumentsList = new WritableList(realm, getRefactoring().getVariables(), Variable.class);
			fArgumentsViewer.addCheckStateListener(new ICheckStateListener() {
				@Override
				public void checkStateChanged(final CheckStateChangedEvent event) {
					final Object element = event.getElement();
					if (element instanceof Variable) {
						final Variable variable = (Variable) element;
						if (!variable.getName().equals("...")) { //$NON-NLS-1$
							final boolean check = event.getChecked();
							int idx = argumentsList.indexOf(element);
							if (check) {
								Variable previousVariable = null;
								while (idx > 0) {
									final Object previousElement = argumentsList.get(idx-1);
									if (previousElement instanceof Variable
											&& !((Variable) previousElement).getName().equals("...")) { //$NON-NLS-1$
										previousVariable = (Variable) previousElement;
										break;
									}
									idx--;
								}
								if (previousVariable == null || previousVariable.getUseAsGenericArgument() == event.getChecked()) {
									variable.setUseAsGenericArgument(event.getChecked());
									fArgumentsViewer.setChecked(variable, event.getChecked());
									return;
								}
							}
							else {
								Variable nextVariable = null;
								while (idx < argumentsList.size()-1) {
									final Object nextElement = argumentsList.get(idx+1);
									if (nextElement instanceof Variable
											&& !((Variable) nextElement).getName().equals("...")) { //$NON-NLS-1$
										nextVariable = (Variable) nextElement;
										break;
									}
									idx++;
								}
								if (nextVariable == null || nextVariable.getUseAsGenericArgument() == event.getChecked()) {
									variable.setUseAsGenericArgument(event.getChecked());
									fArgumentsViewer.setChecked(variable, event.getChecked());
									return;
								}
							}
							fArgumentsViewer.setChecked(variable, !event.getChecked());
						}
						else {
							fArgumentsViewer.setChecked(variable, true);
						}
					}
				}
			});
			fArgumentsViewer.setInput(argumentsList);
			fArgumentsButtons.connectTo(fArgumentsViewer, argumentsList, null);
			
			dbc.bindValue(SWTObservables.observeSelection(fGenerateGenericControl), 
					PojoObservables.observeValue(realm, getRefactoring(), "generateGeneric"), null, null); //$NON-NLS-1$
		}
		
		@Override
		public void setVisible(final boolean visible) {
			super.setVisible(visible);
			fFunctionNameControl.setFocus();
		}
		
	}
	
	
	public FunctionToS4MethodWizard(final FunctionToS4MethodRefactoring ref) {
		super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE | NO_BACK_BUTTON_ON_STATUS_DIALOG);
		setDefaultPageTitle(Messages.FunctionToS4Method_Wizard_title);
	}
	
	
	@Override
	protected void addUserInputPages() {
		addPage(new InputPage());
	}
	
}
