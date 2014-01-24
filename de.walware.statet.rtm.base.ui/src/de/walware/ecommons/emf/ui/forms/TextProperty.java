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

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditValueProperty;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.emf.core.util.IEMFEditContext;



public class TextProperty extends EFProperty {
	
	
	private Text fWidget;
	
	private IObservableValue fModelObservable;
	
	
	public TextProperty(final String label, final String tooltip,
			final EClass eClass, final EStructuralFeature eFeature) {
		super(label, tooltip, eClass, eFeature);
	}
	
	
	@Override
	public void create(final Composite parent, final IEFFormPage page) {
		final EFToolkit toolkit = page.getToolkit();
		
		toolkit.createPropLabel(parent, getLabel(), getTooltip());
		fWidget = toolkit.createPropTextField(parent, 40);
	}
	
	@Override
	public Control getControl() {
		return fWidget;
	}
	
	@Override
	public void bind(final IEMFEditContext context) {
		super.bind(context);
		
		final IEMFEditValueProperty emfProperty = EMFEditProperties.value(getEditingDomain(),
				getEFeature() );
		fModelObservable = emfProperty.observeDetail(getBaseObservable());
		final IObservableValue swtObservable = WidgetProperties.text(SWT.Modify).observe(
				getRealm(), fWidget );
		
		getDataBindingContext().bindValue(swtObservable, fModelObservable);
	}
	
	@Override
	public IObservableValue getPropertyObservable() {
		return fModelObservable;
	}
	
}
