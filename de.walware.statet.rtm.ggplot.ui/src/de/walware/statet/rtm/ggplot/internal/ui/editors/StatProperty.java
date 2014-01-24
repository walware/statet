/*=============================================================================#
 # Copyright (c) 2013-2014 Stephan Wahlbrink (WalWare.de) and others.
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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.emf.core.util.IEMFEditContext;
import de.walware.ecommons.emf.ui.forms.Detail;
import de.walware.ecommons.emf.ui.forms.DetailStack;
import de.walware.ecommons.emf.ui.forms.DropDownProperty;
import de.walware.ecommons.emf.ui.forms.IEFFormPage;
import de.walware.ecommons.emf.ui.forms.PropertyDetail;

import de.walware.statet.rtm.base.ui.rexpr.RExprValueProperty;
import de.walware.statet.rtm.base.ui.rexpr.RExprWidget;
import de.walware.statet.rtm.ggplot.GGPlotPackage;
import de.walware.statet.rtm.ggplot.GGPlotPackage.Literals;


public class StatProperty extends DropDownProperty {
	
	
	private static class Details extends DetailStack {
		
		
		public Details(final IEFFormPage page, final Composite parent) {
			super(page, parent);
		}
		
		
		@Override
		protected Detail createDetail(final EObject value) {
			if (value != null) {
				switch (value.eClass().getClassifierID()) {
				case GGPlotPackage.SUMMARY_STAT:
					return new SummaryDetail(this);
				default:
					break;
				}
			}
			return super.createDetail(value);
		}
		
	}
	
	private static class SummaryDetail extends PropertyDetail {
		
		
		private final RExprValueProperty fProperty;
		
		
		public SummaryDetail(final DetailStack parent) {
			super(parent);
			
			final EClass eClass = Literals.SUMMARY_STAT;
			
			fProperty = (RExprValueProperty) GGPlotProperties.createProperty(
					eClass, Literals.SUMMARY_STAT__YFUN,
					"f(x):", "Summary function for y" );
			
			createContent();
		}
		
		@Override
		protected void createContent(final Composite composite) {
			final IEFFormPage page = getPage();
			
			fProperty.create(composite, page, RExprWidget.MIN_SIZE);
		}
		
		@Override
		public void addBindings(final IEMFEditContext context) {
			fProperty.bind(context);
		}
		
	}
	
	
	public StatProperty(final String label, final String tooltip, final EClass eClass, final EStructuralFeature eFeature) {
		super(label, tooltip, eClass, eFeature);
	}
	
	
	@Override
	protected List<Object> createInitialInput() {
		final List<Object> list = new ArrayList<Object>();
		list.add(""); //$NON-NLS-1$
		list.add(GGPlotPackage.eINSTANCE.getGGPlotFactory().createIdentityStat());
		list.add(GGPlotPackage.eINSTANCE.getGGPlotFactory().createSummaryStat());
		return list;
	}
	
	@Override
	protected ILabelProvider createLabelProvider(final IEFFormPage page) {
		return new AdapterFactoryLabelProvider(page.getEditor().getAdapterFactory());
	}
	
	@Override
	protected DetailStack createDetails(final Composite parent, final IEFFormPage page) {
		return new Details(page, parent);
	}
	
}
