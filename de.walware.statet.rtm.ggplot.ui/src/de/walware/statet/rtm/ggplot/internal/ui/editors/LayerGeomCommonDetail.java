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

import de.walware.ecommons.emf.ui.forms.DetailStack;

import de.walware.statet.rtm.ggplot.GGPlotPackage;


public class LayerGeomCommonDetail extends LayerDetail {
	
	
	private LayerDataSection fDataSection;
	private LayerAestheticsSection fAestheticsSection;
	
	
	public LayerGeomCommonDetail(final DetailStack parent, final EClass eClass) {
		super(parent, eClass);
	}
	
	
	@Override
	protected void createContent(final Composite composite) {
		fDataSection = new LayerDataSection(this, composite);
		fDataSection.getSection().setLayoutData(createSectionLayoutData());
		registerSection(fDataSection);
		switch (getEClass().getClassifierID()) {
		case GGPlotPackage.GEOM_ABLINE_LAYER:
		case GGPlotPackage.GEOM_TEXT_LAYER:
			fDataSection.getSection().setExpanded(true);
			break;
		}
		
		fAestheticsSection = new LayerAestheticsSection(this, composite);
		fAestheticsSection.getSection().setLayoutData(createSectionLayoutData());
		registerSection(fAestheticsSection);
		fAestheticsSection.getSection().setExpanded(true);
	}
	
}
