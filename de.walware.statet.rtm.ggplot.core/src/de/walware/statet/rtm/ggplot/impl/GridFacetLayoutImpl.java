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

package de.walware.statet.rtm.ggplot.impl;

import java.util.Collection;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import de.walware.statet.rtm.ggplot.GGPlotPackage;
import de.walware.statet.rtm.ggplot.GridFacetLayout;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;


/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Grid Facet Layout</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GridFacetLayoutImpl#getColVars <em>Col Vars</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GridFacetLayoutImpl#getRowVars <em>Row Vars</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class GridFacetLayoutImpl extends EObjectImpl implements GridFacetLayout {
	/**
	 * The cached value of the '{@link #getColVars() <em>Col Vars</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColVars()
	 * @generated
	 * @ordered
	 */
	protected EList<RTypedExpr> colVars;

	/**
	 * The cached value of the '{@link #getRowVars() <em>Row Vars</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRowVars()
	 * @generated
	 * @ordered
	 */
	protected EList<RTypedExpr> rowVars;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GridFacetLayoutImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GGPlotPackage.Literals.GRID_FACET_LAYOUT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<RTypedExpr> getRowVars() {
		if (rowVars == null) {
			rowVars = new EDataTypeUniqueEList<RTypedExpr>(RTypedExpr.class, this, GGPlotPackage.GRID_FACET_LAYOUT__ROW_VARS);
		}
		return rowVars;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<RTypedExpr> getColVars() {
		if (colVars == null) {
			colVars = new EDataTypeUniqueEList<RTypedExpr>(RTypedExpr.class, this, GGPlotPackage.GRID_FACET_LAYOUT__COL_VARS);
		}
		return colVars;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case GGPlotPackage.GRID_FACET_LAYOUT__COL_VARS:
				return getColVars();
			case GGPlotPackage.GRID_FACET_LAYOUT__ROW_VARS:
				return getRowVars();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case GGPlotPackage.GRID_FACET_LAYOUT__COL_VARS:
				getColVars().clear();
				getColVars().addAll((Collection<? extends RTypedExpr>)newValue);
				return;
			case GGPlotPackage.GRID_FACET_LAYOUT__ROW_VARS:
				getRowVars().clear();
				getRowVars().addAll((Collection<? extends RTypedExpr>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case GGPlotPackage.GRID_FACET_LAYOUT__COL_VARS:
				getColVars().clear();
				return;
			case GGPlotPackage.GRID_FACET_LAYOUT__ROW_VARS:
				getRowVars().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case GGPlotPackage.GRID_FACET_LAYOUT__COL_VARS:
				return colVars != null && !colVars.isEmpty();
			case GGPlotPackage.GRID_FACET_LAYOUT__ROW_VARS:
				return rowVars != null && !rowVars.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) {
			return super.toString();
		}

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (colVars: "); //$NON-NLS-1$
		result.append(colVars);
		result.append(", rowVars: "); //$NON-NLS-1$
		result.append(rowVars);
		result.append(')');
		return result.toString();
	}

} //GridFacetLayoutImpl
