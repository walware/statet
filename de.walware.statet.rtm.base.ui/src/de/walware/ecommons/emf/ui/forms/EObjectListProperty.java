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

package de.walware.ecommons.emf.ui.forms;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditListProperty;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.CommandParameter;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.IViewerNotification;
import org.eclipse.emf.edit.ui.dnd.EditingDomainViewerDropAdapter;
import org.eclipse.emf.edit.ui.dnd.LocalTransfer;
import org.eclipse.emf.edit.ui.dnd.ViewerDragAdapter;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import de.walware.ecommons.emf.core.util.IEMFEditContext;
import de.walware.ecommons.ui.components.ButtonGroup;
import de.walware.ecommons.ui.content.ElementSourceSelectionProvider;
import de.walware.ecommons.ui.util.MenuUtil;
import de.walware.ecommons.ui.util.ViewerUtil;
import de.walware.ecommons.ui.util.ViewerUtil.TableComposite;


public class EObjectListProperty extends EFProperty {
	
	
	private class LabelProvider extends AdapterFactoryLabelProvider {
		
		protected Viewer fViewer;
		protected AdapterFactoryContentProvider.ViewerRefresh fViewerRefresh;
		
		public LabelProvider(final AdapterFactory adapterFactory, final Viewer viewer) {
			super(adapterFactory);
			
			fViewer = viewer;
		}
		
		@Override
		public void notifyChanged(final Notification notification) {
			if (fViewer != null && fViewer.getControl() != null && !fViewer.getControl().isDisposed()
					&& notification instanceof IViewerNotification) {
				final IViewerNotification viewerNotification = (IViewerNotification) notification;
				if (viewerNotification.isLabelUpdate()) {
					if (fViewerRefresh == null) {
						fViewerRefresh = new AdapterFactoryContentProvider.ViewerRefresh(fViewer);
					}
					if (fViewerRefresh.addNotification(viewerNotification)) {
						fViewer.getControl().getDisplay().asyncExec(fViewerRefresh);
					}
				}
			}
			super.notifyChanged(notification);
		}
		
	}
	
	private class AddHandler extends ButtonGroup.AddHandler {
		
		
		private MenuManager fMenuManager;
		
		
		public AddHandler() {
		}
		
		
		@Override
		public boolean run(final IStructuredSelection selection) {
			if (fMenuManager == null) {
				fMenuManager = new MenuManager();
			}
			else {
				fMenuManager.removeAll();
			}
			fillMenu(fMenuManager);
			
			final Menu menu = fMenuManager.createContextMenu(getControl());
			MenuUtil.setPullDownPosition(menu, getControl());
			menu.setVisible(true);
			return false;
		}
		
		protected void fillMenu(final MenuManager menuManager) {
			final EditingDomain editingDomain = getEditingDomain();
			final EObject base = (EObject) getBaseObservable().getValue();
			if (base == null) {
				return;
			}
			final Collection<?> descriptors = editingDomain.getNewChildDescriptors(base, null);
			for (final Object descriptor : descriptors) {
				if (!(descriptor instanceof CommandParameter)) {
					continue;
				}
				final CommandParameter parameter = (CommandParameter) descriptor;
				if (parameter.getEStructuralFeature() != getEFeature()) {
					continue;
				}
				final IContributionItem item = new EditCommandContributionItem(parameter,
						editingDomain, getBaseObservable() ) {
					@Override
					protected void executed(final Collection<?> result) {
						final Iterator<?> iter = result.iterator();
						if (iter.hasNext()) {
							final Object value = iter.next();
							if (fModelObservable.contains(value)) {
								fSingleSelectionObservable.setValue(value);
							}
						}
					}
				};
				menuManager.add(item);
			}
		}
		
		@Override
		public void widgetDisposed(final DisposeEvent e) {
			if (fMenuManager != null) {
				fMenuManager.dispose();
			}
		}
		
	}
	
	
	private TableComposite fWidget;
	private ButtonGroup<EObject> fButtonGroup;
	
	private IObservableList fModelObservable;
	
	private ElementSourceSelectionProvider fSelectionProvider;
	private IObservableValue fSingleSelectionObservable;
	
	
	public EObjectListProperty(final String label, final String tooltip,
			final EClass eClass, final EStructuralFeature eFeature) {
		super(label, tooltip, eClass, eFeature);
	}
	
	
	@Override
	public void create(final Composite parent, final IEFFormPage page) {
		fWidget = page.getToolkit().createPropSingleColumnTable(parent, 10, 25);
		
		fWidget.viewer.setContentProvider(new ObservableListContentProvider());
		fWidget.viewer.setLabelProvider(new LabelProvider(
				page.getEditor().getAdapterFactory(), fWidget.viewer ));
		
		fButtonGroup = new ButtonGroup<EObject>(parent);
		fButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		customizeButtonGroup(fButtonGroup);
	}
	
	protected void customizeButtonGroup(final ButtonGroup<EObject> buttonGroup) {
		buttonGroup.addAddButton(new AddHandler());
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
	public void bind(final IEMFEditContext context) {
		super.bind(context);
		
		final IEMFEditListProperty emfProperty = EMFEditProperties.list(getEditingDomain(),
				getEFeature() );
		fModelObservable = emfProperty.observeDetail(getBaseObservable());
		((IObserving) fModelObservable).getObserved();
		
		fWidget.viewer.setInput(fModelObservable);
		fButtonGroup.connectTo(fWidget.viewer, fModelObservable, null);
		
		register(fWidget.table, IEFPropertyExpressions.EOBJECT_LIST_ID);
		fSelectionProvider = new ElementSourceSelectionProvider(fWidget.viewer, this);
		ViewerUtil.setSelectionProvider(fWidget.table, fSelectionProvider);
		
		{	final int operations = (DND.DROP_DEFAULT | DND.DROP_COPY | DND.DROP_MOVE);
			final Transfer[] transfers = new Transfer[] { LocalTransfer.getInstance() };
			fWidget.viewer.addDragSupport(operations, transfers, new ViewerDragAdapter(fWidget.viewer));
			fWidget.viewer.addDropSupport(operations, transfers, new EditingDomainViewerDropAdapter(
					getEditingDomain(), fWidget.viewer ));
		}
		
		fSingleSelectionObservable = ViewersObservables.observeSinglePostSelection(fWidget.viewer);
		
		fButtonGroup.updateState();
	}
	
	@Override
	public IObservableList getPropertyObservable() {
		return fModelObservable;
	}
	
	public IObservableValue getSingleSelectionObservable() {
		return fSingleSelectionObservable;
	}
	
	public ISelectionProvider getSelectionProvider() {
		return fSelectionProvider;
	}
	
}
