/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.emf.ui.forms;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditValueProperty;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.walware.ecommons.emf.core.util.IEMFEditContext;
import de.walware.ecommons.emf.ui.databinding.CustomViewerObservables;
import de.walware.ecommons.emf.ui.databinding.DetailContext;
import de.walware.ecommons.ui.util.LayoutUtil;


public class DropDownProperty extends EFProperty {
	
	
	private Composite fComposite;
	
	private ComboViewer fComboViewer;
	
	private DetailStack fDetailStack;
	
	private IObservableValue fModelObservable;
	
	private final List<Object> fInitialInput;
	
	
	public DropDownProperty(final String label, final String tooltip,
			final EClass eClass, final EStructuralFeature eFeature) {
		super(label, tooltip, eClass, eFeature);
		
		fInitialInput = createInitialInput();
	}
	
	
	protected List<Object> createInitialInput() {
		return Collections.emptyList();
	}
	
	@Override
	public void create(final Composite parent, final IEFFormPage page) {
		final EFToolkit toolkit = page.getToolkit();
		
		toolkit.createPropLabel(parent, getLabel(), getTooltip());
		
		fComposite = new Composite(parent, SWT.NULL);
		toolkit.adapt(fComposite);
		fComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		fComposite.setLayout(LayoutUtil.createCompositeGrid(3));
		
		{	final ComboViewer viewer = new ComboViewer(fComposite, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.SINGLE);
			fComboViewer = viewer;
			
			final ILabelProvider labelProvider = createLabelProvider(page);
			
			viewer.setLabelProvider(labelProvider);
			viewer.setContentProvider(ArrayContentProvider.getInstance());
			
			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
			gd.widthHint = LayoutUtil.hintWidth(getCombo(), fInitialInput, labelProvider);
			viewer.getControl().setLayoutData(gd);
		}
		
		fDetailStack = createDetails(fComposite, page);
		fDetailStack.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
	}
	
	@Override
	public Control getControl() {
		return fComposite;
	}
	
	protected Combo getCombo() {
		return fComboViewer.getCombo();
	}
	
	protected ILabelProvider createLabelProvider(final IEFFormPage page) {
		return new AdapterFactoryLabelProvider(page.getEditor().getAdapterFactory());
	}
	
	protected DetailStack createDetails(final Composite parent, final IEFFormPage page) {
		return new DetailStack(page, fComposite);
	}
	
	
	@Override
	public void bind(final IEMFEditContext context) {
		super.bind(context);
		
		final IEMFEditValueProperty emfProperty = EMFEditProperties.value(getEditingDomain(),
				getEFeature() );
		fModelObservable = emfProperty.observeDetail(getBaseObservable());
		
		context.getDataBindingContext().bindValue(
				CustomViewerObservables.observeComboSelection(fComboViewer, fInitialInput),
				fModelObservable );
		
		final IEMFEditContext detailContext = new DetailContext(context, fModelObservable);
		fDetailStack.setContext(detailContext);
	}
	
	@Override
	public IObservableValue getPropertyObservable() {
		return fModelObservable;
	}
	
}
