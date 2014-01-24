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

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.emf.core.util.IEMFEditContext;
import de.walware.ecommons.emf.ui.databinding.DetailContext;
import de.walware.ecommons.emf.ui.forms.Detail;
import de.walware.ecommons.emf.ui.forms.DetailStack;
import de.walware.ecommons.emf.ui.forms.EFFormSection;
import de.walware.ecommons.emf.ui.forms.EObjectListProperty;
import de.walware.ecommons.emf.ui.forms.IEFFormPage;

import de.walware.statet.rtm.ggplot.GGPlotPackage;
import de.walware.statet.rtm.ggplot.GGPlotPackage.Literals;


public class LayersSection extends EFFormSection {
	
	
	private static class Details extends DetailStack {
		
		
		public Details(final IEFFormPage page, final Composite parent) {
			super(page, parent);
		}
		
		
		@Override
		protected Detail createDetail(final EObject value) {
			if (value != null) {
				switch (value.eClass().getClassifierID()) {
				case GGPlotPackage.GEOM_ABLINE_LAYER:
				case GGPlotPackage.GEOM_BAR_LAYER:
				case GGPlotPackage.GEOM_BOXPLOT_LAYER:
				case GGPlotPackage.GEOM_HISTOGRAM_LAYER:
				case GGPlotPackage.GEOM_LINE_LAYER:
				case GGPlotPackage.GEOM_POINT_LAYER:
				case GGPlotPackage.GEOM_SMOOTH_LAYER:
				case GGPlotPackage.GEOM_TEXT_LAYER:
				case GGPlotPackage.GEOM_TILE_LAYER:
				case GGPlotPackage.GEOM_VIOLIN_LAYER:
					return new LayerGeomCommonDetail(this, value.eClass());
				default:
					return super.createDetail(value);
				}
			}
			return new LayerNoSelectionDetail(this);
		}
		
	}
	
	
	private final EObjectListProperty fLayersProperty;
	
	private Details fDetails;
	
	
	public LayersSection(final IEFFormPage page, final Composite parent) {
		super(page, parent,
				"Layers",
				null ); 
		
		final EClass eClass = Literals.GG_PLOT;
		fLayersProperty = new EObjectListProperty("Layers", null,
				eClass, Literals.GG_PLOT__LAYERS );
		
		createClient();
	}
	
	
	@Override
	protected void createContent(final Composite composite) {
		final IEFFormPage page = getPage();
		
		fLayersProperty.create(composite, page);
		((GridData) fLayersProperty.getControl().getLayoutData()).verticalAlignment = SWT.TOP;
	}
	
	protected DetailStack createDetails(final Composite parent) {
		fDetails = new Details(getPage(), parent);
		return fDetails;
	}
	
	@Override
	public void addBindings(final IEMFEditContext context) {
		fLayersProperty.bind(context);
		
		final IEMFEditContext detailContext = new DetailContext(context,
				fLayersProperty.getSingleSelectionObservable() );
		fDetails.setContext(detailContext);
		
		getSection().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				final IObservableValue selection = fLayersProperty.getSingleSelectionObservable();
				final IObservableList layers = fLayersProperty.getPropertyObservable();
				if (selection.getValue() == null & !layers.isEmpty()) {
					selection.setValue(layers.get(0));
				}
			}
		});
	}
	
	public ISelectionProvider getSelectionProvider() {
		return fLayersProperty.getSelectionProvider();
	}
	
}
