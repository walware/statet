/**
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 */
package de.walware.statet.rtm.ggplot.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import de.walware.statet.rtm.ggplot.GGPlotPackage;
import de.walware.statet.rtm.ggplot.WrapFacetLayout;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Wrap Facet Layout</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.WrapFacetLayoutImpl#getColVars <em>Col Vars</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.WrapFacetLayoutImpl#getColNum <em>Col Num</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class WrapFacetLayoutImpl extends EObjectImpl implements WrapFacetLayout {
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
	 * The default value of the '{@link #getColNum() <em>Col Num</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColNum()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr COL_NUM_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getColNum() <em>Col Num</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColNum()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr colNum = COL_NUM_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected WrapFacetLayoutImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GGPlotPackage.Literals.WRAP_FACET_LAYOUT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<RTypedExpr> getColVars() {
		if (colVars == null) {
			colVars = new EDataTypeUniqueEList<RTypedExpr>(RTypedExpr.class, this, GGPlotPackage.WRAP_FACET_LAYOUT__COL_VARS);
		}
		return colVars;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RTypedExpr getColNum() {
		return colNum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setColNum(RTypedExpr newColNum) {
		RTypedExpr oldColNum = colNum;
		colNum = newColNum;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.WRAP_FACET_LAYOUT__COL_NUM, oldColNum, colNum));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case GGPlotPackage.WRAP_FACET_LAYOUT__COL_VARS:
				return getColVars();
			case GGPlotPackage.WRAP_FACET_LAYOUT__COL_NUM:
				return getColNum();
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
			case GGPlotPackage.WRAP_FACET_LAYOUT__COL_VARS:
				getColVars().clear();
				getColVars().addAll((Collection<? extends RTypedExpr>)newValue);
				return;
			case GGPlotPackage.WRAP_FACET_LAYOUT__COL_NUM:
				setColNum((RTypedExpr)newValue);
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
			case GGPlotPackage.WRAP_FACET_LAYOUT__COL_VARS:
				getColVars().clear();
				return;
			case GGPlotPackage.WRAP_FACET_LAYOUT__COL_NUM:
				setColNum(COL_NUM_EDEFAULT);
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
			case GGPlotPackage.WRAP_FACET_LAYOUT__COL_VARS:
				return colVars != null && !colVars.isEmpty();
			case GGPlotPackage.WRAP_FACET_LAYOUT__COL_NUM:
				return COL_NUM_EDEFAULT == null ? colNum != null : !COL_NUM_EDEFAULT.equals(colNum);
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
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (colVars: "); //$NON-NLS-1$
		result.append(colVars);
		result.append(", colNum: "); //$NON-NLS-1$
		result.append(colNum);
		result.append(')');
		return result.toString();
	}

} //WrapFacetLayoutImpl
