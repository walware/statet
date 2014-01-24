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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.swt.IFocusService;

import de.walware.ecommons.emf.core.util.IEMFEditContext;
import de.walware.ecommons.emf.core.util.IEMFEditPropertyContext;


public class EFProperty implements IEMFEditPropertyContext {
	
	
	protected static void register(final Control control, final String id) {
		final IFocusService service = (IFocusService) PlatformUI.getWorkbench().getService(IFocusService.class);
		if (service != null) {
			service.addFocusTracker(control, id);
		}
	}
	
	
	private final String fLabel;
	private final String fTooltip;
	
	private final EClass fEClass;
	private final EStructuralFeature fEFeature;
	
	private IEMFEditContext fContext;
	
	
	public EFProperty(final String label, final String tooltip,
			final EClass eClass, final EStructuralFeature eFeature) {
		fLabel = label;
		fTooltip = tooltip;
		
		fEClass = eClass;
		fEFeature = eFeature;
	}
	
	
	public String getLabel() {
		return fLabel;
	}
	
	public String getTooltip() {
		return fTooltip;
	}
	
	@Override
	public EClass getEClass() {
		return fEClass;
	}
	
	@Override
	public EStructuralFeature getEFeature() {
		return fEFeature;
	}
	
	
	public void create(final Composite parent, final IEFFormPage page) {
	}
	
	public Control getControl() {
		return null;
	}
	
	
	public void bind(final IEMFEditContext context) {
		fContext = context;
	}
	
	@Override
	public IObservable getPropertyObservable() {
		return null;
	}
	
	@Override
	public EditingDomain getEditingDomain() {
		return fContext.getEditingDomain();
	}
	
	@Override
	public DataBindingContext getDataBindingContext() {
		return fContext.getDataBindingContext();
	}
	
	@Override
	public Realm getRealm() {
		return fContext.getRealm();
	}
	
	@Override
	public IObservableValue getBaseObservable() {
		return fContext.getBaseObservable();
	}
	
	@Override
	public Object getAdapter(final Class required) {
		if (required.isInstance(this)) {
			return this;
		}
		return fContext.getAdapter(required);
	}
	
}
