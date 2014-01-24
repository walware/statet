/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.rtm.base.ui.rexpr;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.databinding.IEMFObservable;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditListProperty;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.walware.ecommons.emf.core.util.IContext;
import de.walware.ecommons.emf.core.util.IEMFEditContext;
import de.walware.ecommons.emf.core.util.IEMFEditPropertyContext;
import de.walware.ecommons.emf.ui.forms.EFProperty;
import de.walware.ecommons.emf.ui.forms.IEFFormPage;
import de.walware.ecommons.ui.components.ButtonGroup;
import de.walware.ecommons.ui.content.ElementSourceSelectionProvider;
import de.walware.ecommons.ui.util.SelectionTransferDragAdapter;
import de.walware.ecommons.ui.util.ViewerUtil;
import de.walware.ecommons.ui.util.ViewerUtil.TableComposite;

import de.walware.statet.rtm.base.ui.editors.RtFormToolkit;
import de.walware.statet.rtm.base.util.RExprTypes;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;


public class RExprListProperty extends EFProperty implements ButtonGroup.IActions<RTypedExpr> {
	
	
	private class RExprEditing extends ObservableValueEditingSupport {
		
		
		private CellEditor fCellEditor;
		
		
		public RExprEditing() {
			super(fWidget.viewer, getDataBindingContext());
		}
		
		
		@Override
		protected CellEditor getCellEditor(final Object element) {
			if (element instanceof RTypedExpr) {
				if (fCellEditor == null) {
					fCellEditor = createRExprCellEditor();
				}
				return fCellEditor;
			}
			return null;
		}
		
		protected CellEditor createRExprCellEditor() {
			final TextCellEditor editor = new TextCellEditor((Composite) getViewer().getControl());
//			editor.setValidator(new RIdentifierCellValidator());
			return editor;
		}
		
		@Override
		protected IObservableValue doCreateCellEditorObservable(final CellEditor cellEditor) {
//			return new ObjValueObservable<RTypedExpr>(fModelObservable.getRealm(),
//					(IObjValueWidget<RTypedExpr>) fCellEditor.getControl() );
			return SWTObservables.observeText(((TextCellEditor) fCellEditor).getControl(), SWT.Modify);
		}
		
		@Override
		protected Binding createBinding(final IObservableValue target, final IObservableValue model) {
			return getDataBindingContext().bindValue(target, model,
					new UpdateValueStrategy(UpdateValueStrategy.POLICY_CONVERT)
							.setConverter(new String2RTypedExprConverter(fTypes.getDefaultTypeKey())),
					new UpdateValueStrategy()
							.setConverter(new RExpr2StringConverter()) );
		}
		
		@Override
		protected IObservableValue doCreateElementObservable(final Object element, final ViewerCell cell) {
			return new AbstractObservableValue() {
				@Override
				public Object getValueType() {
					return RTypedExpr.class;
				}
				@Override
				protected Object doGetValue() {
					return element;
				}
				@Override
				protected void doSetValue(final Object value) {
					final int index = getIndex(element);
					final CommandStack commandStack = getEditingDomain().getCommandStack();
					final IEMFObservable emfObservable = (IEMFObservable) fModelObservable;
					Command command;
					if (value == null) {
						if (index >= 0) {
							fModelObservable.remove(index);
						}
						return;
					}
					else {
						if (index >= 0) {
							command = SetCommand.create(getEditingDomain(),
									emfObservable.getObserved(), getEFeature(),
									value, index );
	//						fModelObservable.set(index, value);
						}
						else {
	//						command = AddCommand.create(fContext.getEditingDomain(),
	//								emfObservable.getObserved(), emfObservable.getStructuralFeature(),
	//								value );
							fModelObservable.add(index);
							return;
						}
					}
					commandStack.execute(command);
				}
			};
		}
		
	}
	
	
	private final IRExprTypesUIProvider fProvider;
	private final RExprTypes fTypes;
	private List<RExprTypeUIAdapter> fTypeUIAdapters;
	
	private TableComposite fWidget;
	private ButtonGroup<RTypedExpr> fButtonGroup;
	
	private IObservableList fModelObservable;
	
	
	public RExprListProperty(final String label, final String tooltip,
			final EClass eClass, final EStructuralFeature eFeature,
			final IRExprTypesUIProvider provider) {
		super(label, tooltip, eClass, eFeature);
		
		fProvider = provider;
		fTypes = provider.getTypes(getEClass(), getEFeature());
	}
	
	
	protected IRExprTypesUIProvider getProvider() {
		return fProvider;
	}
	
	@Override
	public void create(final Composite parent, final IEFFormPage page) {
		final IRExprTypesUIProvider provider = getProvider();
		fTypeUIAdapters = provider.getUIAdapters(fTypes, getEClass(), getEFeature() );
		final RtFormToolkit toolkit = (RtFormToolkit) page.getToolkit();
		
		toolkit.createPropLabel(parent, getLabel(), getTooltip(), 3);
		
		fWidget = toolkit.createPropSingleColumnTable(parent, 6, 25);
		
		fWidget.viewer.setContentProvider(new ObservableListContentProvider());
		final ViewerColumn column = fWidget.getViewerColumn(0);
		column.setLabelProvider(new RExprLabelProvider(fTypeUIAdapters));
		
		fButtonGroup = new ButtonGroup<RTypedExpr>(parent, this, true);
		fButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		customizeButtonGroup(fButtonGroup);
	}
	
	protected void customizeButtonGroup(final ButtonGroup<RTypedExpr> buttonGroup) {
		buttonGroup.addAddButton(null);
		buttonGroup.addDeleteButton(null);
		buttonGroup.addSeparator();
		buttonGroup.addUpButton(null);
		buttonGroup.addDownButton(null);
	}
	
	@Override
	public Control getControl() {
		return fWidget;
	}
	
	public TableViewer getViewer() {
		return fWidget.viewer;
	}
	
	
	@Override
	public RTypedExpr edit(final int command, final RTypedExpr item, final Object parent) {
		switch(command) {
		case ButtonGroup.ADD_NEW:
			return new RTypedExpr(fTypes.getDefaultTypeKey(), ""); //$NON-NLS-1$
		case ButtonGroup.EDIT:
			return item;
		default:
			return null;
		}
	}
	
	@Override
	public void updateState(final IStructuredSelection selection) {
	}
	
	@Override
	public void bind(final IEMFEditContext context) {
		super.bind(context);
		
		final IEMFEditListProperty emfProperty = EMFEditProperties.list(getEditingDomain(),
				getEFeature() );
		fModelObservable = emfProperty.observeDetail(getBaseObservable());
		
		fWidget.viewer.setInput(fModelObservable);
		fButtonGroup.connectTo(fWidget.viewer, fModelObservable, null);
		
		final ViewerColumn column = fWidget.getViewerColumn(0);
		column.setEditingSupport(new RExprEditing());
		ViewerUtil.installDefaultEditBehaviour2(fWidget.viewer);
		
		final ElementSourceSelectionProvider extSelectionProvider = new ElementSourceSelectionProvider(
				fWidget.viewer, this );
		{	final int operations = (DND.DROP_DEFAULT | DND.DROP_COPY | DND.DROP_MOVE);
			final Transfer[] transfers = new Transfer[] { LocalSelectionTransfer.getTransfer() };
			fWidget.viewer.addDragSupport(operations, transfers, new SelectionTransferDragAdapter(
					extSelectionProvider ));
			fWidget.viewer.addDropSupport(operations, transfers, new RExprViewerDropAdapter(
					fWidget.viewer, fTypeUIAdapters, this ) {
				@Override
				protected boolean canMove(final IContext source, final Object input) {
					final IEMFEditPropertyContext sourcePropertyContext = (IEMFEditPropertyContext) source.getAdapter(IEMFEditPropertyContext.class);
					return (sourcePropertyContext != null
							&& getEditingDomain() == sourcePropertyContext.getEditingDomain());
				}
				@Override
				protected int getIndex(final Object element) {
					return fModelObservable.indexOf(element);
				}
				@Override
				protected void insertExprs(final List<RTypedExpr> exprs, final int index, final int time) {
					if (index >= 0) {
						fModelObservable.addAll(index, exprs);
					}
					else {
						fModelObservable.addAll(exprs);
					}
				}
				@Override
				protected void moveExprs(final IContext source, final List<RTypedExpr> exprs, final int index, final int time) {
					final IEMFEditPropertyContext sourceContext = (IEMFEditPropertyContext) source;
					
					final EObject owner = (EObject) ((IObserving) fModelObservable).getObserved();
					final IObservable sourceObservable = sourceContext.getPropertyObservable();
					if (owner == null || sourceObservable == null) {
						return;
					}
					Command command;
					
					if (sourceObservable == fModelObservable) {
						if (exprs.size() == 1) {
							final RTypedExpr expr = exprs.get(0);
							final int oldIndex = getIndex(expr);
							if (oldIndex < 0) {
								return;
							}
							command = MoveCommand.create(getEditingDomain(),
									owner, getEFeature(), expr, (oldIndex < index) ? index - 1 : index );
						}
						else {
							final CompoundCommand compound = new CompoundCommand();
							final List<RTypedExpr> after = new ArrayList<RTypedExpr>(exprs.size());
							for (final RTypedExpr expr : exprs) {
								final int oldIndex = getIndex(expr);
								if (oldIndex < 0) {
									continue;
								}
								if (oldIndex < index) {
									compound.append(MoveCommand.create(getEditingDomain(),
											owner, getEFeature(), expr, index - 1 ));
								}
								else {
									after.add(expr);
								}
							}
							for (final RTypedExpr expr : after) {
								compound.append(MoveCommand.create(getEditingDomain(),
										owner, getEFeature(), expr, index ));
							}
							command = compound;
						}
					}
					else {
						final EObject oldOwner = (EObject) ((IObserving) sourceObservable).getObserved();
						final CompoundCommand compound = new CompoundCommand();
						compound.append(AddCommand.create(getEditingDomain(),
								owner, getEFeature(), exprs));
						compound.append(RemoveCommand.create(getEditingDomain(),
								oldOwner, sourceContext.getEFeature(), exprs));
						command = compound;
					}
					
					getEditingDomain().getCommandStack().execute(command);
				}
			});
		}
		
		if (!fModelObservable.isEmpty()) {
			fWidget.viewer.setSelection(new StructuredSelection(fModelObservable.get(0)));
		}
		else {
			fButtonGroup.updateState();
		}
	}
	
	protected int getIndex(final Object element) {
		for (int i = 0; i < fModelObservable.size(); i++) {
			if (fModelObservable.get(i) == element) {
				return i;
			}
		}
		return fModelObservable.indexOf(element);
	}
	
	@Override
	public IObservableList getPropertyObservable() {
		return fModelObservable;
	}
	
}
