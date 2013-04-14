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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

import de.walware.statet.rtm.ggplot.GGPlotPackage;
import de.walware.statet.rtm.ggplot.PropGroupVarProvider;
import de.walware.statet.rtm.ggplot.PropXVarProvider;
import de.walware.statet.rtm.ggplot.PropYVarProvider;
import de.walware.statet.rtm.ggplot.XYVarLayer;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>XY Var Layer</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.XYVarLayerImpl#getXVar <em>XVar</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.XYVarLayerImpl#getYVar <em>YVar</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.XYVarLayerImpl#getGroupVar <em>Group Var</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public abstract class XYVarLayerImpl extends LayerImpl implements XYVarLayer {
	/**
	 * The default value of the '{@link #getXVar() <em>XVar</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getXVar()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr XVAR_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getXVar() <em>XVar</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getXVar()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr xVar = XVAR_EDEFAULT;

	/**
	 * The default value of the '{@link #getYVar() <em>YVar</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getYVar()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr YVAR_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getYVar() <em>YVar</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getYVar()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr yVar = YVAR_EDEFAULT;

	/**
	 * The default value of the '{@link #getGroupVar() <em>Group Var</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGroupVar()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr GROUP_VAR_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getGroupVar() <em>Group Var</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGroupVar()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr groupVar = GROUP_VAR_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected XYVarLayerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GGPlotPackage.Literals.XY_VAR_LAYER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RTypedExpr getGroupVar() {
		return groupVar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setGroupVar(RTypedExpr newGroupVar) {
		RTypedExpr oldGroupVar = groupVar;
		groupVar = newGroupVar;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.XY_VAR_LAYER__GROUP_VAR, oldGroupVar, groupVar));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RTypedExpr getXVar() {
		return xVar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setXVar(RTypedExpr newXVar) {
		RTypedExpr oldXVar = xVar;
		xVar = newXVar;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.XY_VAR_LAYER__XVAR, oldXVar, xVar));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RTypedExpr getYVar() {
		return yVar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setYVar(RTypedExpr newYVar) {
		RTypedExpr oldYVar = yVar;
		yVar = newYVar;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.XY_VAR_LAYER__YVAR, oldYVar, yVar));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case GGPlotPackage.XY_VAR_LAYER__XVAR:
				return getXVar();
			case GGPlotPackage.XY_VAR_LAYER__YVAR:
				return getYVar();
			case GGPlotPackage.XY_VAR_LAYER__GROUP_VAR:
				return getGroupVar();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case GGPlotPackage.XY_VAR_LAYER__XVAR:
				setXVar((RTypedExpr)newValue);
				return;
			case GGPlotPackage.XY_VAR_LAYER__YVAR:
				setYVar((RTypedExpr)newValue);
				return;
			case GGPlotPackage.XY_VAR_LAYER__GROUP_VAR:
				setGroupVar((RTypedExpr)newValue);
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
			case GGPlotPackage.XY_VAR_LAYER__XVAR:
				setXVar(XVAR_EDEFAULT);
				return;
			case GGPlotPackage.XY_VAR_LAYER__YVAR:
				setYVar(YVAR_EDEFAULT);
				return;
			case GGPlotPackage.XY_VAR_LAYER__GROUP_VAR:
				setGroupVar(GROUP_VAR_EDEFAULT);
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
			case GGPlotPackage.XY_VAR_LAYER__XVAR:
				return XVAR_EDEFAULT == null ? xVar != null : !XVAR_EDEFAULT.equals(xVar);
			case GGPlotPackage.XY_VAR_LAYER__YVAR:
				return YVAR_EDEFAULT == null ? yVar != null : !YVAR_EDEFAULT.equals(yVar);
			case GGPlotPackage.XY_VAR_LAYER__GROUP_VAR:
				return GROUP_VAR_EDEFAULT == null ? groupVar != null : !GROUP_VAR_EDEFAULT.equals(groupVar);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == PropXVarProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.XY_VAR_LAYER__XVAR: return GGPlotPackage.PROP_XVAR_PROVIDER__XVAR;
				default: return -1;
			}
		}
		if (baseClass == PropYVarProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.XY_VAR_LAYER__YVAR: return GGPlotPackage.PROP_YVAR_PROVIDER__YVAR;
				default: return -1;
			}
		}
		if (baseClass == PropGroupVarProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.XY_VAR_LAYER__GROUP_VAR: return GGPlotPackage.PROP_GROUP_VAR_PROVIDER__GROUP_VAR;
				default: return -1;
			}
		}
		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
		if (baseClass == PropXVarProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_XVAR_PROVIDER__XVAR: return GGPlotPackage.XY_VAR_LAYER__XVAR;
				default: return -1;
			}
		}
		if (baseClass == PropYVarProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_YVAR_PROVIDER__YVAR: return GGPlotPackage.XY_VAR_LAYER__YVAR;
				default: return -1;
			}
		}
		if (baseClass == PropGroupVarProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_GROUP_VAR_PROVIDER__GROUP_VAR: return GGPlotPackage.XY_VAR_LAYER__GROUP_VAR;
				default: return -1;
			}
		}
		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
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
		result.append(" (xVar: "); //$NON-NLS-1$
		result.append(xVar);
		result.append(", yVar: "); //$NON-NLS-1$
		result.append(yVar);
		result.append(", groupVar: "); //$NON-NLS-1$
		result.append(groupVar);
		result.append(')');
		return result.toString();
	}

} //XYVarLayerImpl
