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

package de.walware.statet.rtm.ftable.core;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import de.walware.ecommons.collections.ConstArrayList;

import de.walware.statet.rtm.base.util.IRExprTypesProvider;
import de.walware.statet.rtm.base.util.RExprType;
import de.walware.statet.rtm.base.util.RExprTypes;
import de.walware.statet.rtm.ftable.FTablePackage;
import de.walware.statet.rtm.rtdata.RtDataPackage;


public class FTableExprTypesProvider implements IRExprTypesProvider {
	
	
	private static final RExprTypes T_DATA = new RExprTypes(
			RExprType.DATAFRAME_TYPE );
	
	private static final RExprTypes T_DATA_VAR = new RExprTypes(
			RExprType.DATAFRAME_COLUMN_TYPE );
	
	private static final RExprTypes T_LABEL = new RExprTypes(new ConstArrayList<RExprType>(
			RExprType.TEXT_VALUE_TYPE,
			RExprType.EXPR_VALUE_TYPE ), 0 );
	
	private static final RExprTypes T_LABEL_VAR = new RExprTypes(new ConstArrayList<RExprType>(
			RExprType.TEXT_VALUE_TYPE,
			RExprType.EXPR_VALUE_TYPE,
			RExprType.DATAFRAME_COLUMN_TYPE), 2 );
	
	private static final RExprTypes T_COLOR_VAR = new RExprTypes(new ConstArrayList<RExprType>(
			RExprType.EXPR_COLOR_VALUE_TYPE,
			RExprType.DATAFRAME_COLUMN_TYPE ), 0 );
	
	private static final RExprTypes T_OTHER_VAR = new RExprTypes(new ConstArrayList<RExprType>(
			RExprType.EXPR_VALUE_TYPE,
			RExprType.DATAFRAME_COLUMN_TYPE ), 0 );
	
	private static final RExprTypes T_OTHER = new RExprTypes(
			RExprType.EXPR_VALUE_TYPE );
	
	
	public static FTableExprTypesProvider INSTANCE = new FTableExprTypesProvider();
	
	
	@Override
	public RExprTypes getTypes(final EClass eClass, final EStructuralFeature eFeature) {
		switch (eClass.getClassifierID()) {
		case FTablePackage.FTABLE:
			switch (eFeature.getFeatureID()) {
			case FTablePackage.FTABLE__COL_VARS:
			case FTablePackage.FTABLE__ROW_VARS:
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
		if (eFeature.getEType() == RtDataPackage.Literals.RTEXT) {
			return T_LABEL;
		}
		if (eFeature.getEType() == RtDataPackage.Literals.RLABEL) {
			return T_LABEL;
		}
		return T_OTHER;
	}
	
}
