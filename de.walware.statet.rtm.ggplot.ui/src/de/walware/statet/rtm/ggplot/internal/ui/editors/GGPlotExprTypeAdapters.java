/*=============================================================================#
 # Copyright (c) 2013-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.rtm.ggplot.internal.ui.editors;

import org.eclipse.emf.ecore.EStructuralFeature;

import de.walware.statet.rtm.base.ui.rexpr.DefaultRExprTypeUIAdapters;
import de.walware.statet.rtm.base.ui.rexpr.RExprTypeUIAdapter;
import de.walware.statet.rtm.base.util.RExprType;
import de.walware.statet.rtm.ggplot.GGPlotPackage.Literals;


public class GGPlotExprTypeAdapters extends DefaultRExprTypeUIAdapters {
	
	
	public GGPlotExprTypeAdapters() {
	}
	
	
	@Override
	public RExprTypeUIAdapter getUIAdapter(final RExprType type, final EStructuralFeature eFeature) {
		if (eFeature == Literals.GG_PLOT__MAIN_TITLE
				|| eFeature == Literals.GG_PLOT__AX_XLABEL
				|| eFeature == Literals.GG_PLOT__AX_YLABEL
				|| eFeature == Literals.GEOM_TEXT_LAYER__LABEL) {
			if (type == RExprType.TEXT_VALUE_TYPE) {
				return LABEL_TEXT_VALUE_LINK_ADAPTER;
			}
			if (type == RExprType.EXPR_LABEL_VALUE_TYPE) {
				return LABEL_EXPR_VALUE_LINK_ADAPTER;
			}
			if (type == RExprType.DATAFRAME_COLUMN_TYPE) {
				return DATAFRAME_COLUMN_LINK_ADAPTER;
			}
		}
		return super.getUIAdapter(type, eFeature);
	}
	
}
