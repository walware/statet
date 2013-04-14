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
package de.walware.statet.rtm.ftable.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import de.walware.statet.rtm.ftable.FTable;
import de.walware.statet.rtm.ftable.FTablePackage;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>FTable</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ftable.impl.FTableImpl#getData <em>Data</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ftable.impl.FTableImpl#getDataFilter <em>Data Filter</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ftable.impl.FTableImpl#getColVars <em>Col Vars</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ftable.impl.FTableImpl#getRowVars <em>Row Vars</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class FTableImpl extends EObjectImpl implements FTable {
	/**
	 * The default value of the '{@link #getData() <em>Data</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getData()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr DATA_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getData() <em>Data</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getData()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr data = DATA_EDEFAULT;
	/**
	 * The default value of the '{@link #getDataFilter() <em>Data Filter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataFilter()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr DATA_FILTER_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getDataFilter() <em>Data Filter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataFilter()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr dataFilter = DATA_FILTER_EDEFAULT;
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
	protected FTableImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return FTablePackage.Literals.FTABLE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RTypedExpr getData() {
		return data;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setData(RTypedExpr newData) {
		RTypedExpr oldData = data;
		data = newData;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, FTablePackage.FTABLE__DATA, oldData, data));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RTypedExpr getDataFilter() {
		return dataFilter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDataFilter(RTypedExpr newDataFilter) {
		RTypedExpr oldDataFilter = dataFilter;
		dataFilter = newDataFilter;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, FTablePackage.FTABLE__DATA_FILTER, oldDataFilter, dataFilter));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<RTypedExpr> getColVars() {
		if (colVars == null) {
			colVars = new EDataTypeUniqueEList<RTypedExpr>(RTypedExpr.class, this, FTablePackage.FTABLE__COL_VARS);
		}
		return colVars;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<RTypedExpr> getRowVars() {
		if (rowVars == null) {
			rowVars = new EDataTypeUniqueEList<RTypedExpr>(RTypedExpr.class, this, FTablePackage.FTABLE__ROW_VARS);
		}
		return rowVars;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case FTablePackage.FTABLE__DATA:
				return getData();
			case FTablePackage.FTABLE__DATA_FILTER:
				return getDataFilter();
			case FTablePackage.FTABLE__COL_VARS:
				return getColVars();
			case FTablePackage.FTABLE__ROW_VARS:
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
			case FTablePackage.FTABLE__DATA:
				setData((RTypedExpr)newValue);
				return;
			case FTablePackage.FTABLE__DATA_FILTER:
				setDataFilter((RTypedExpr)newValue);
				return;
			case FTablePackage.FTABLE__COL_VARS:
				getColVars().clear();
				getColVars().addAll((Collection<? extends RTypedExpr>)newValue);
				return;
			case FTablePackage.FTABLE__ROW_VARS:
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
			case FTablePackage.FTABLE__DATA:
				setData(DATA_EDEFAULT);
				return;
			case FTablePackage.FTABLE__DATA_FILTER:
				setDataFilter(DATA_FILTER_EDEFAULT);
				return;
			case FTablePackage.FTABLE__COL_VARS:
				getColVars().clear();
				return;
			case FTablePackage.FTABLE__ROW_VARS:
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
			case FTablePackage.FTABLE__DATA:
				return DATA_EDEFAULT == null ? data != null : !DATA_EDEFAULT.equals(data);
			case FTablePackage.FTABLE__DATA_FILTER:
				return DATA_FILTER_EDEFAULT == null ? dataFilter != null : !DATA_FILTER_EDEFAULT.equals(dataFilter);
			case FTablePackage.FTABLE__COL_VARS:
				return colVars != null && !colVars.isEmpty();
			case FTablePackage.FTABLE__ROW_VARS:
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
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (data: "); //$NON-NLS-1$
		result.append(data);
		result.append(", dataFilter: "); //$NON-NLS-1$
		result.append(dataFilter);
		result.append(", colVars: "); //$NON-NLS-1$
		result.append(colVars);
		result.append(", rowVars: "); //$NON-NLS-1$
		result.append(rowVars);
		result.append(')');
		return result.toString();
	}

} //FTableImpl
