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

import java.util.List;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditValueProperty;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.databinding.jface.ObjValueObservable;
import de.walware.ecommons.emf.core.util.IEMFEditContext;
import de.walware.ecommons.emf.ui.forms.EFProperty;
import de.walware.ecommons.emf.ui.forms.IEFFormPage;

import de.walware.statet.rtm.base.ui.editors.RtFormToolkit;
import de.walware.statet.rtm.base.util.RExprTypes;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;


public class RExprValueProperty extends EFProperty {
	
	
	private final IRExprTypesUIProvider fProvider;
	private final RExprTypes fTypes;
	
	private RExprWidget fWidget;
	
	private IObservableValue fModelObservable;
	
	
	public RExprValueProperty(final String label, final String tooltip,
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
		create(parent, page, 0);
	}
	
	public void create(final Composite parent, final IEFFormPage page, final int options) {
		final IRExprTypesUIProvider provider = getProvider();
		final List<RExprTypeUIAdapter> uiAdapters = provider.getUIAdapters(fTypes,
				getEClass(), getEFeature() );
		final RtFormToolkit toolkit = (RtFormToolkit) page.getToolkit();
		
		toolkit.createPropLabel(parent, getLabel(), getTooltip());
		
		fWidget = toolkit.createPropRTypedExpr(parent, options, fTypes, uiAdapters);
	}
	
	@Override
	public RExprWidget getControl() {
		return fWidget;
	}
	
	@Override
	public void bind(final IEMFEditContext context) {
		super.bind(context);
		
		fWidget.setContext(this);
		
		final IEMFEditValueProperty emfProperty = EMFEditProperties.value(getEditingDomain(),
				getEFeature() );
		fModelObservable = emfProperty.observeDetail(getBaseObservable());
		final IObservableValue swtObservable = new ObjValueObservable<RTypedExpr>(getRealm(),
				fWidget );
		
		getDataBindingContext().bindValue(swtObservable, fModelObservable);
	}
	
	@Override
	public IObservableValue getPropertyObservable() {
		return fModelObservable;
	}
	
}
