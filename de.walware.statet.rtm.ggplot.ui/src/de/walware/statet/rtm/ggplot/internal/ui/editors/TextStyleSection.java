/*******************************************************************************
 * Copyright (c) 2013 WalWare/StatET-Project (www.walware.de/goto/statet).
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


public class TextStyleSection extends EFFormSection {
	
	
	private final EFPropertySet fProperties;
	
	
	public TextStyleSection(final IEFFormPage page, final Composite parent,
			final String label) {
		super(page, parent, "Text Style of " + label, null);
		
		final EClass eClass = Literals.TEXT_STYLE;
		
		fProperties = new EFPropertySet();
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.TEXT_STYLE__FONT_FAMILY, "Family:", "The font family"));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.TEXT_STYLE__FONT_FACE, "Face:", "The font face"));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.PROP_SIZE_PROVIDER__SIZE, "Size:", "The font size in pts"));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.PROP_COLOR_PROVIDER__COLOR, "Color:", "The color"));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.TEXT_STYLE__HJUST, "H Just:", "The horizontal justification (in [0, 1])"));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.TEXT_STYLE__HJUST, "V Just:", "The vertical justification (in [0, 1])"));
		fProperties.add(GGPlotProperties.createProperty(
				eClass, Literals.TEXT_STYLE__ANGLE, "Angle:", "The rotation angle (in [0, 360])"));
		
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
