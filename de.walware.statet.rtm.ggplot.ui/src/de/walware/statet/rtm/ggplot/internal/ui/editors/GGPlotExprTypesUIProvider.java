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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import de.walware.statet.rtm.base.ui.rexpr.IRExprTypesUIProvider;
import de.walware.statet.rtm.base.ui.rexpr.RExprTypeUIAdapter;
import de.walware.statet.rtm.base.util.RExprType;
import de.walware.statet.rtm.base.util.RExprTypes;
import de.walware.statet.rtm.ggplot.core.GGPlotExprTypesProvider;


public class GGPlotExprTypesUIProvider extends GGPlotExprTypesProvider
		implements IRExprTypesUIProvider {
	
	
	private static final GGPlotExprTypeAdapters ADAPTERS = new GGPlotExprTypeAdapters();
	
	
	@Override
	public List<RExprTypeUIAdapter> getUIAdapters(final RExprTypes types,
			final EClass eClass, final EStructuralFeature eFeature) {
		final List<RExprTypeUIAdapter> uiAdapters = new ArrayList<RExprTypeUIAdapter>();
		for (final RExprType type : types.getTypes()) {
			RExprTypeUIAdapter uiAdapter = null;
			uiAdapter = ADAPTERS.getUIAdapter(type, eFeature);
			if (uiAdapter != null) {
				uiAdapters.add(uiAdapter);
			}
		}
		return uiAdapters;
	}
	
}
