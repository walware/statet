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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.emf.core.util.IEMFEditContext;
import de.walware.ecommons.emf.ui.forms.DetailStack;
import de.walware.ecommons.emf.ui.forms.EFPropertySet;
import de.walware.ecommons.emf.ui.forms.IEFFormPage;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.rtm.base.ui.rexpr.RExprListProperty;
import de.walware.statet.rtm.ggplot.GGPlotPackage;


class FacetWrapDetail extends FacetDetail {
	
	
	private final RExprListProperty fColVarsProperty;
	private final EFPropertySet fOtherProperties;
	
	
	public FacetWrapDetail(final DetailStack parent) {
		super(parent);
		
		final EClass eClass = GGPlotPackage.Literals.WRAP_FACET_LAYOUT;
		
		fColVarsProperty = (RExprListProperty) GGPlotProperties.createProperty(
				eClass, GGPlotPackage.Literals.WRAP_FACET_LAYOUT__COL_VARS,
				"Variables:", "The variables for facets" );
		
		fOtherProperties = new EFPropertySet();
		fOtherProperties.add(GGPlotProperties.createProperty(
				eClass, GGPlotPackage.Literals.WRAP_FACET_LAYOUT__COL_NUM,
				"Number of Columns:", "The number of facets in a single row" ));
		
		createContent();
	}
	
	
	@Override
	protected void createContent(final Composite composite) {
		final IEFFormPage page = getPage();
		
		LayoutUtil.addSmallFiller(composite, false);
		
		{	final Composite column = new Composite(composite, SWT.NONE);
			column.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
			column.setLayout(LayoutUtil.createCompositeGrid(3));
			
			fColVarsProperty.create(column, page);
		}
		{	final Composite column = new Composite(composite, SWT.NONE);
			column.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
			column.setLayout(LayoutUtil.createCompositeGrid(3));
			
			fOtherProperties.createControls(column, page);
		}
		LayoutUtil.addGDDummy(composite, true);
	}
	
	@Override
	public void addBindings(final IEMFEditContext context) {
		fColVarsProperty.bind(context);
		
		fOtherProperties.bind(context);
	}
	
}
