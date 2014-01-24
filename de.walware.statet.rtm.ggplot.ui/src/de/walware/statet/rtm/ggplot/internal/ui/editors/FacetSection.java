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

package de.walware.statet.rtm.ggplot.internal.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import de.walware.ecommons.emf.core.util.IEMFEditContext;
import de.walware.ecommons.emf.ui.databinding.CustomViewerObservables;
import de.walware.ecommons.emf.ui.databinding.DetailContext;
import de.walware.ecommons.emf.ui.forms.Detail;
import de.walware.ecommons.emf.ui.forms.DetailStack;
import de.walware.ecommons.emf.ui.forms.EFFormSection;
import de.walware.ecommons.emf.ui.forms.EFToolkit;
import de.walware.ecommons.emf.ui.forms.IEFFormPage;
import de.walware.ecommons.ui.components.WaCombo;
import de.walware.ecommons.ui.components.WaComboViewer;

import de.walware.statet.rtm.ggplot.GGPlotPackage;
import de.walware.statet.rtm.ggplot.core.RtGGPlotCorePlugin;


public class FacetSection extends EFFormSection {
	
	
	private static class Details extends DetailStack {
		
		public Details(final IEFFormPage page, final Composite parent) {
			super(page, parent);
		}
		
		
		@Override
		protected Detail createDetail(final EObject value) {
			if (value != null) {
				switch (value.eClass().getClassifierID()) {
				case GGPlotPackage.WRAP_FACET_LAYOUT:
					return new FacetWrapDetail(this);
				case GGPlotPackage.GRID_FACET_LAYOUT:
					return new FacetGridDetail(this);
				default:
					break;
				}
			}
			return super.createDetail(value);
		}
		
	}
	
	
	private WaCombo fTypeControl;
	private TableViewer fTypeViewer;
	
	private DetailStack fDetailStack;
	
	
	public FacetSection(final IEFFormPage page, final Composite parent) {
		super(page, parent,
				"Layout / Facets",
				null );
		
		createClient();
	}
	
	@Override
	protected Layout createClientLayout() {
		final GridLayout layout = (GridLayout) super.createClientLayout();
		
		layout.marginTop = 0;
		
		return layout;
	}
	
	@Override
	protected void createContent(final Composite composite) {
		final IEFFormPage page = getPage();
		final EFToolkit toolkit = page.getToolkit();
		
		fTypeControl = new WaCombo(composite, SWT.BORDER);
		fTypeViewer = new WaComboViewer(fTypeControl);
		toolkit.adapt(fTypeControl);
		fTypeControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		fTypeViewer.setLabelProvider(new AdapterFactoryLabelProvider(
				page.getEditor().getAdapterFactory()) {
			@Override
			public Image getColumnImage(final Object object, final int columnIndex) {
				if (object instanceof String && columnIndex == 0) {
					return getImageFromObject(RtGGPlotCorePlugin.INSTANCE.getImage("full/obj16/NoFacetLayout"));
				}
				return super.getColumnImage(object, columnIndex);
			}
		});
		fTypeViewer.setContentProvider(ArrayContentProvider.getInstance());
		
		fDetailStack = new Details(page, composite);
		fDetailStack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
	}
	
	@Override
	public void addBindings(final IEMFEditContext context) {
		final IObservableValue facetObservable = EMFEditProperties.value(context.getEditingDomain(),
						GGPlotPackage.Literals.GG_PLOT__FACET )
				.observeDetail(context.getBaseObservable());
		{	final List<Object> options = new ArrayList<Object>();
			options.add("Single Plot / No Facets");
			options.add(GGPlotPackage.eINSTANCE.getGGPlotFactory().createWrapFacetLayout());
			options.add(GGPlotPackage.eINSTANCE.getGGPlotFactory().createGridFacetLayout());
			context.getDataBindingContext().bindValue(
					CustomViewerObservables.observeComboSelection(fTypeViewer, options),
					facetObservable );
		}
		
		final IEMFEditContext detailContext = new DetailContext(context, facetObservable);
		fDetailStack.setContext(detailContext);
	}
	
}
