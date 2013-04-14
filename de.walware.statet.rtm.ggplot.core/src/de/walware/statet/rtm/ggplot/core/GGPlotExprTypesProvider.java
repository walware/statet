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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import de.walware.ecommons.collections.ConstList;

import de.walware.statet.rtm.base.util.IRExprTypesProvider;
import de.walware.statet.rtm.base.util.RExprType;
import de.walware.statet.rtm.base.util.RExprTypes;
import de.walware.statet.rtm.ggplot.GGPlotPackage;
import de.walware.statet.rtm.rtdata.RtDataPackage;


public class GGPlotExprTypesProvider implements IRExprTypesProvider {
	
	
	private static final RExprTypes T_DATA = new RExprTypes(
			RExprType.DATAFRAME_TYPE );
	
	private static final RExprTypes T_DATA_VAR = new RExprTypes(
			RExprType.DATAFRAME_COLUMN_TYPE );
	
	private static final RExprTypes T_LABEL = new RExprTypes(new ConstList<RExprType>(
			RExprType.TEXT_VALUE_TYPE,
			RExprType.EXPR_LABEL_VALUE_TYPE ), 0 );
	
	private static final RExprTypes T_LABEL_VAR = new RExprTypes(new ConstList<RExprType>(
			RExprType.TEXT_VALUE_TYPE,
			RExprType.EXPR_LABEL_VALUE_TYPE,
			RExprType.DATAFRAME_COLUMN_TYPE), 2 );
	
	private static final RExprTypes T_COLOR_VAR = new RExprTypes(new ConstList<RExprType>(
			RExprType.EXPR_COLOR_VALUE_TYPE,
			RExprType.DATAFRAME_COLUMN_TYPE ), 0 );
	
	private static final RExprTypes T_ALPHA_VAR = new RExprTypes(new ConstList<RExprType>(
			RExprType.EXPR_ALPHA_VALUE_TYPE,
			RExprType.DATAFRAME_COLUMN_TYPE ), 0 );
	
	private static final RExprTypes T_FONT_FAMILY_VAR = new RExprTypes(new ConstList<RExprType>(
			RExprType.EXPR_FONT_FAMILY_VALUE_TYPE/*,
			RExprType.DATAFRAME_COLUMN_TYPE*/ ), 0 );
	
	private static final RExprTypes T_OTHER_VAR = new RExprTypes(new ConstList<RExprType>(
			RExprType.EXPR_VALUE_TYPE,
			RExprType.DATAFRAME_COLUMN_TYPE ), 0 );
	
	private static final RExprTypes T_FUN = new RExprTypes(
			RExprType.EXPR_FUNCTION_TYPE );
	
	private static final RExprTypes T_OTHER = new RExprTypes(
			RExprType.EXPR_VALUE_TYPE );
	
	
	public static GGPlotExprTypesProvider INSTANCE = new GGPlotExprTypesProvider();
	
	
	@Override
	public RExprTypes getTypes(final EClass eClass, final EStructuralFeature eFeature) {
		final EClass eFeatureClass = eFeature.getEContainingClass();
		switch (eFeatureClass.getClassifierID()) {
		case GGPlotPackage.PROP_XVAR_PROVIDER:
		case GGPlotPackage.PROP_YVAR_PROVIDER:
			return T_DATA_VAR;
		case GGPlotPackage.PROP_SHAPE_PROVIDER:
			return T_OTHER_VAR;
		case GGPlotPackage.PROP_LINE_TYPE_PROVIDER:
			return T_OTHER_VAR;
		case GGPlotPackage.PROP_SIZE_PROVIDER:
			return T_OTHER_VAR;
		case GGPlotPackage.PROP_COLOR_PROVIDER:
		case GGPlotPackage.PROP_FILL_PROVIDER:
			return T_COLOR_VAR;
		case GGPlotPackage.PROP_ALPHA_PROVIDER:
			return T_ALPHA_VAR;
		case GGPlotPackage.PROP_GROUP_VAR_PROVIDER:
			return T_DATA_VAR;
		default:
			break;
		}
		
		switch (eClass.getClassifierID()) {
//		case GGPlotPackage.GG_PLOT:
		case GGPlotPackage.GEOM_ABLINE_LAYER:
			switch (eFeature.getFeatureID()) {
			case GGPlotPackage.GEOM_ABLINE_LAYER__INTERCEPT_VAR:
			case GGPlotPackage.GEOM_ABLINE_LAYER__SLOPE_VAR:
				return T_OTHER_VAR;
			default:
				break;
			}
			break;
//		case GGPlotPackage.GEOM_BAR_LAYER:
//		case GGPlotPackage.GEOM_BOXPLOT_LAYER:
//		case GGPlotPackage.GEOM_HISTOGRAM_LAYER:
//		case GGPlotPackage.GEOM_LINE_LAYER:
//		case GGPlotPackage.GEOM_POINT_LAYER:
//		case GGPlotPackage.GEOM_SMOOTH_LAYER:
		case GGPlotPackage.GEOM_TEXT_LAYER:
			switch (eFeature.getFeatureID()) {
			case GGPlotPackage.GEOM_TEXT_LAYER__LABEL:
				return T_LABEL_VAR;
			default:
				break;
			}
			break;
//		case GGPlotPackage.GEOM_TILE_LAYER:
//		case GGPlotPackage.GEOM_VIOLIN_LAYER:
//		case GGPlotPackage.TEXT_STYLE:
		case GGPlotPackage.GRID_FACET_LAYOUT:
			switch (eFeature.getFeatureID()) {
			case GGPlotPackage.GRID_FACET_LAYOUT__COL_VARS:
			case GGPlotPackage.GRID_FACET_LAYOUT__ROW_VARS:
				return T_DATA_VAR;
			default:
				break;
			}
			break;
		case GGPlotPackage.WRAP_FACET_LAYOUT:
			switch (eFeature.getFeatureID()) {
			case GGPlotPackage.WRAP_FACET_LAYOUT__COL_VARS:
				return T_DATA_VAR;
			default:
				break;
			}
			break;
		default:
			break;
		}
		
		if (eFeature.getEType() == RtDataPackage.Literals.RDATA_FRAME) {
			return T_DATA;
		}
		if (eFeature.getEType() == RtDataPackage.Literals.RDATA_FILTER) {
			return T_OTHER;
		}
		if (eFeature.getEType() == RtDataPackage.Literals.RLABEL) {
			return T_LABEL;
		}
		if (eFeature.getEType() == RtDataPackage.Literals.RFUNCTION) {
			return T_FUN;
		}
		if (eFeature.getEType() == RtDataPackage.Literals.RFONT_FAMILY) {
			return T_FONT_FAMILY_VAR;
		}
		return T_OTHER;
	}
	
}
