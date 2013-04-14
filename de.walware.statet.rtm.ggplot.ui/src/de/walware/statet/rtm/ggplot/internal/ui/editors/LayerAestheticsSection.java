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
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.emf.core.util.IEMFEditContext;
import de.walware.ecommons.emf.ui.forms.EFFormSection;
import de.walware.ecommons.emf.ui.forms.EFPropertySet;
import de.walware.ecommons.emf.ui.forms.IEFFormPage;

import de.walware.statet.rtm.ggplot.GGPlotPackage.Literals;


class LayerAestheticsSection extends EFFormSection {
	
	
	private final LayerDetail fDetail;
	
	private final EFPropertySet fProperties;
	
	
	public LayerAestheticsSection(final LayerDetail detail, final Composite parent) {
		super(detail.getPage(), parent, (TITLE_DESCRIPTION_STYLE | EXPANDABLE_STYLE));
		
		fDetail = detail;
		
		getSection().setText("Layer Aesthetics");
		getSection().setDescription("Customize the style of the layer.");
		
		final EClass eClass = fDetail.getEClass();
		
		fProperties = new EFPropertySet();
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.PROP_SHAPE_PROVIDER__SHAPE,
				"Shape:", null ));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.PROP_LINE_TYPE_PROVIDER__LINE_TYPE,
				"Linetype:", null ));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.PROP_SIZE_PROVIDER__SIZE,
				"Size:", null ));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.PROP_COLOR_PROVIDER__COLOR,
				"Color:", "The color" ));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.PROP_FILL_PROVIDER__FILL,
				"Fill:", "The fill color" ));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.PROP_ALPHA_PROVIDER__ALPHA,
				"Alpha:", "The opacity level (in [0, 1]" ));
		
		createClient();
	}
	
	
	@Override
	protected void createContent(final Composite composite) {
		final IEFFormPage page = getPage();
		
		fProperties.createControls(composite, page);
	}
	
	@Override
	public void addBindings(final IEMFEditContext context) {
		fProperties.bind(context);
	}
	
}
