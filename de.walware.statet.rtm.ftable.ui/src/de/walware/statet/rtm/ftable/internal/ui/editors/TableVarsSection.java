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

package de.walware.statet.rtm.ftable.internal.ui.editors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.emf.core.util.IEMFEditContext;
import de.walware.ecommons.emf.ui.forms.EFFormSection;
import de.walware.ecommons.emf.ui.forms.IEFFormPage;
import de.walware.ecommons.ui.util.LayoutUtil;

import de.walware.statet.rtm.base.ui.rexpr.RExprListProperty;
import de.walware.statet.rtm.ftable.FTablePackage.Literals;


public class TableVarsSection extends EFFormSection {
	
	
	private final RExprListProperty fColVarsProperty;
	private final RExprListProperty fRowVarsProperty;
	
	
	public TableVarsSection(final IEFFormPage page, final Composite parent) {
		super(page, parent,
				"Variables",
				null );
		
		final EClass eClass = Literals.FTABLE;
		
		fColVarsProperty = (RExprListProperty) FTableProperties.createProperty(
				eClass, Literals.FTABLE__COL_VARS,
				"Column Variables:", "The variables for facets in horizontal direction");
		fRowVarsProperty = (RExprListProperty) FTableProperties.createProperty(
				eClass, Literals.FTABLE__ROW_VARS,
				"Row Variables:", "The variables for facets in vertical direction");
		
		createClient();
	}
	
	@Override
	protected void createContent(final Composite composite) {
		final IEFFormPage page = getPage();
		
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
