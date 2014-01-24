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
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.emf.core.util.IEMFEditContext;
import de.walware.ecommons.emf.ui.forms.EFFormSection;
import de.walware.ecommons.emf.ui.forms.EFPropertySet;
import de.walware.ecommons.emf.ui.forms.IEFFormPage;

import de.walware.statet.rtm.ggplot.GGPlotPackage.Literals;


public class PlotLabelSection extends EFFormSection {
	
	
	private final EFPropertySet fProperties;
	
	
	public PlotLabelSection(final IEFFormPage page, final Composite parent) {
		super(page, parent,
				"Labels",
				"Customize the labels of the plot." );
		
		final EClass eClass = Literals.GG_PLOT;
		fProperties = new EFPropertySet();
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.GG_PLOT__MAIN_TITLE,
				"Title:", "The overall title for the plot" ));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.GG_PLOT__AX_XLABEL,
				"x Label:", "The title for the x-axis" ));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.GG_PLOT__AX_YLABEL,
				"y Label:", "The title for the y-axis" ));
		
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
