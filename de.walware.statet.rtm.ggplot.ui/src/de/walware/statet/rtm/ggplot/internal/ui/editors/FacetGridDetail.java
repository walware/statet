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

package de.walware.statet.rtm.ggplot.internal.ui.editors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.emf.core.util.IEMFEditContext;
import de.walware.ecommons.emf.ui.forms.DetailStack;
import de.walware.ecommons.emf.ui.forms.IEFFormPage;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.rtm.base.ui.rexpr.RExprListProperty;
import de.walware.statet.rtm.ggplot.GGPlotPackage.Literals;


class FacetGridDetail extends FacetDetail {
	
	
	private final RExprListProperty fColVarsProperty;
	private final RExprListProperty fRowVarsProperty;
	
	
	public FacetGridDetail(final DetailStack parent) {
		super(parent);
		
		final EClass eClass = Literals.GRID_FACET_LAYOUT;
		
		fColVarsProperty = (RExprListProperty) GGPlotProperties.createProperty(
				eClass, Literals.GRID_FACET_LAYOUT__COL_VARS,
				"Column Variables:", "The variables for facets in horizontal direction");
		fRowVarsProperty = (RExprListProperty) GGPlotProperties.createProperty(
				eClass, Literals.GRID_FACET_LAYOUT__ROW_VARS,
				"Row Variables:", "The variables for facets in vertical direction");
		
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
			
			fRowVarsProperty.create(column, page);
		}
		LayoutUtil.addGDDummy(composite, true);
	}
	
	@Override
	public void addBindings(final IEMFEditContext context) {
		fColVarsProperty.bind(context);
		
		fRowVarsProperty.bind(context);
	}
	
}
