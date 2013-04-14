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

package de.walware.statet.rtm.ggplot.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import de.walware.ecommons.emf.core.util.EFeatureReference;
import de.walware.ecommons.emf.core.util.RuleSet;

import de.walware.statet.rtm.ggplot.GGPlotPackage.Literals;
import de.walware.statet.rtm.ggplot.PropDataProvider;


public class GGPlotRuleSet extends RuleSet {
	
	
	public static final String DATA_PARENT_FEATURES_ID = "data" + PARENT_FEATURES_ID_SUFFIX; //$NON-NLS-1$
	
	public static final RuleSet INSTANCE = new GGPlotRuleSet();
	
	
	@Override
	public Object get(final EObject eObject, final EStructuralFeature eFeature, final String id) {
		if (id.equals(DISJOINT_FEATURES_ID)) {
			if (eObject != null && eObject.eClass() == Literals.GRID_FACET_LAYOUT
					&& (eFeature == Literals.GRID_FACET_LAYOUT__COL_VARS
							|| eFeature == Literals.GRID_FACET_LAYOUT__ROW_VARS)) {
				
				final List<EFeatureReference> features = new ArrayList<EFeatureReference>(2);
				features.add(new EFeatureReference(eObject, Literals.GRID_FACET_LAYOUT__COL_VARS));
				features.add(new EFeatureReference(eObject, Literals.GRID_FACET_LAYOUT__ROW_VARS));
				return features;
			}
			return null;
		}
		if (id.equals(DATA_PARENT_FEATURES_ID)) {
			if (eObject != null) {
				final List<EFeatureReference> features = new ArrayList<EFeatureReference>(2);
				EObject obj = eObject;
				do {
					if (obj instanceof PropDataProvider) {
						features.add(new EFeatureReference(obj, Literals.PROP_DATA_PROVIDER__DATA));
					}
					obj = obj.eContainer();
				} while (obj != null);
				if (!features.isEmpty()) {
					return features;
				}
			}
			return null;
		}
		return null;
	}
	
}
