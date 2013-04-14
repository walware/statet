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


class LayerDataSection extends EFFormSection {
	
	
	private final LayerDetail fDetail;
	
	private final EFPropertySet fProperties;
	
	
	public LayerDataSection(final LayerDetail detail, final Composite parent) {
		super(detail.getPage(), parent, (TITLE_STYLE | EXPANDABLE_STYLE));
		
		fDetail = detail;
		
		getSection().setText("Layer Data");
		
		final EClass eClass = fDetail.getEClass();
		
		fProperties = new EFPropertySet();
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.PROP_DATA_PROVIDER__DATA,
				"Data:", "The data source for this layer" ));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.PROP_XVAR_PROVIDER__XVAR,
				"x:", "The data for x" ));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.PROP_YVAR_PROVIDER__YVAR,
				"y:", "The data for y" ));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.GEOM_ABLINE_LAYER__INTERCEPT_VAR,
				"Intercept:", "The intercept of the line(s)" ));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.GEOM_ABLINE_LAYER__SLOPE_VAR,
				"Slope:", "The slope of the line(s)" ));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.PROP_STAT_PROVIDER__STAT,
				"Stat:", "The explicite statistical transformation" ));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.PROP_GROUP_VAR_PROVIDER__GROUP_VAR,
				"Group:", "The explicite group definition" ));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.GEOM_TEXT_LAYER__LABEL,
				"Label:", "The text to plot" ));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.GEOM_POINT_LAYER__POSITION_XJITTER,
				"Jitter x:", "The degree of jitter in x direction" ));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.GEOM_POINT_LAYER__POSITION_YJITTER,
				"Jitter y:", "The degree of jitter in y direction" ));
		
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
		
		new AutoExpandListener(getSection(), fProperties.getModelObservables());
	}
	
}
