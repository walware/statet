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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

import de.walware.statet.rtm.ggplot.GGPlotPackage;
import de.walware.statet.rtm.ggplot.GeomBarLayer;
import de.walware.statet.rtm.ggplot.PropAlphaProvider;
import de.walware.statet.rtm.ggplot.PropColorProvider;
import de.walware.statet.rtm.ggplot.PropFillProvider;
import de.walware.statet.rtm.ggplot.PropStatProvider;
import de.walware.statet.rtm.ggplot.Stat;
import de.walware.statet.rtm.rtdata.RtDataFactory;
import de.walware.statet.rtm.rtdata.RtDataPackage;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;


/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Geom Bar Layer</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomBarLayerImpl#getStat <em>Stat</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomBarLayerImpl#getColor <em>Color</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomBarLayerImpl#getFill <em>Fill</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomBarLayerImpl#getAlpha <em>Alpha</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class GeomBarLayerImpl extends XYVarLayerImpl implements GeomBarLayer {
	/**
	 * The cached value of the '{@link #getStat() <em>Stat</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStat()
	 * @generated
	 * @ordered
	 */
	protected Stat stat;

	/**
	 * The default value of the '{@link #getColor() <em>Color</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColor()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr COLOR_EDEFAULT = (RTypedExpr)RtDataFactory.eINSTANCE.createFromString(RtDataPackage.eINSTANCE.getRColor(), ""); //$NON-NLS-1$

	/**
	 * The cached value of the '{@link #getColor() <em>Color</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColor()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr color = COLOR_EDEFAULT;

	/**
	 * The default value of the '{@link #getFill() <em>Fill</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFill()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr FILL_EDEFAULT = (RTypedExpr)RtDataFactory.eINSTANCE.createFromString(RtDataPackage.eINSTANCE.getRColor(), "");

	/**
	 * The cached value of the '{@link #getFill() <em>Fill</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFill()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr fill = FILL_EDEFAULT;

	/**
	 * The default value of the '{@link #getAlpha() <em>Alpha</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAlpha()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr ALPHA_EDEFAULT = (RTypedExpr)RtDataFactory.eINSTANCE.createFromString(RtDataPackage.eINSTANCE.getRAlpha(), ""); //$NON-NLS-1$

	/**
	 * The cached value of the '{@link #getAlpha() <em>Alpha</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAlpha()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr alpha = ALPHA_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GeomBarLayerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GGPlotPackage.Literals.GEOM_BAR_LAYER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getColor() {
		return color;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setColor(RTypedExpr newColor) {
		RTypedExpr oldColor = color;
		color = newColor;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_BAR_LAYER__COLOR, oldColor, color));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getFill() {
		return fill;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFill(RTypedExpr newFill) {
		RTypedExpr oldFill = fill;
		fill = newFill;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_BAR_LAYER__FILL, oldFill, fill));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getAlpha() {
		return alpha;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAlpha(RTypedExpr newAlpha) {
		RTypedExpr oldAlpha = alpha;
		alpha = newAlpha;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_BAR_LAYER__ALPHA, oldAlpha, alpha));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case GGPlotPackage.GEOM_BAR_LAYER__STAT:
				return basicSetStat(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Stat getStat() {
		return stat;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetStat(Stat newStat, NotificationChain msgs) {
		Stat oldStat = stat;
		stat = newStat;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_BAR_LAYER__STAT, oldStat, newStat);
			if (msgs == null) {
				msgs = notification;
			}
			else {
				msgs.add(notification);
			}
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStat(Stat newStat) {
		if (newStat != stat) {
			NotificationChain msgs = null;
			if (stat != null) {
				msgs = ((InternalEObject)stat).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GEOM_BAR_LAYER__STAT, null, msgs);
			}
			if (newStat != null) {
				msgs = ((InternalEObject)newStat).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GEOM_BAR_LAYER__STAT, null, msgs);
			}
			msgs = basicSetStat(newStat, msgs);
			if (msgs != null) {
				msgs.dispatch();
			}
		}
		else if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_BAR_LAYER__STAT, newStat, newStat));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case GGPlotPackage.GEOM_BAR_LAYER__STAT:
				return getStat();
			case GGPlotPackage.GEOM_BAR_LAYER__COLOR:
				return getColor();
			case GGPlotPackage.GEOM_BAR_LAYER__FILL:
				return getFill();
			case GGPlotPackage.GEOM_BAR_LAYER__ALPHA:
				return getAlpha();
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
			case GGPlotPackage.GEOM_BAR_LAYER__STAT:
				setStat((Stat)newValue);
				return;
			case GGPlotPackage.GEOM_BAR_LAYER__COLOR:
				setColor((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_BAR_LAYER__FILL:
				setFill((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_BAR_LAYER__ALPHA:
				setAlpha((RTypedExpr)newValue);
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
			case GGPlotPackage.GEOM_BAR_LAYER__STAT:
				setStat((Stat)null);
				return;
			case GGPlotPackage.GEOM_BAR_LAYER__COLOR:
				setColor(COLOR_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_BAR_LAYER__FILL:
				setFill(FILL_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_BAR_LAYER__ALPHA:
				setAlpha(ALPHA_EDEFAULT);
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
			case GGPlotPackage.GEOM_BAR_LAYER__STAT:
				return stat != null;
			case GGPlotPackage.GEOM_BAR_LAYER__COLOR:
				return COLOR_EDEFAULT == null ? color != null : !COLOR_EDEFAULT.equals(color);
			case GGPlotPackage.GEOM_BAR_LAYER__FILL:
				return FILL_EDEFAULT == null ? fill != null : !FILL_EDEFAULT.equals(fill);
			case GGPlotPackage.GEOM_BAR_LAYER__ALPHA:
				return ALPHA_EDEFAULT == null ? alpha != null : !ALPHA_EDEFAULT.equals(alpha);
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
		if (baseClass == PropStatProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_BAR_LAYER__STAT: return GGPlotPackage.PROP_STAT_PROVIDER__STAT;
				default: return -1;
			}
		}
		if (baseClass == PropColorProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_BAR_LAYER__COLOR: return GGPlotPackage.PROP_COLOR_PROVIDER__COLOR;
				default: return -1;
			}
		}
		if (baseClass == PropFillProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_BAR_LAYER__FILL: return GGPlotPackage.PROP_FILL_PROVIDER__FILL;
				default: return -1;
			}
		}
		if (baseClass == PropAlphaProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_BAR_LAYER__ALPHA: return GGPlotPackage.PROP_ALPHA_PROVIDER__ALPHA;
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
		if (baseClass == PropStatProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_STAT_PROVIDER__STAT: return GGPlotPackage.GEOM_BAR_LAYER__STAT;
				default: return -1;
			}
		}
		if (baseClass == PropColorProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_COLOR_PROVIDER__COLOR: return GGPlotPackage.GEOM_BAR_LAYER__COLOR;
				default: return -1;
			}
		}
		if (baseClass == PropFillProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_FILL_PROVIDER__FILL: return GGPlotPackage.GEOM_BAR_LAYER__FILL;
				default: return -1;
			}
		}
		if (baseClass == PropAlphaProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_ALPHA_PROVIDER__ALPHA: return GGPlotPackage.GEOM_BAR_LAYER__ALPHA;
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
		if (eIsProxy()) {
			return super.toString();
		}

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (color: "); //$NON-NLS-1$
		result.append(color);
		result.append(", fill: "); //$NON-NLS-1$
		result.append(fill);
		result.append(", alpha: "); //$NON-NLS-1$
		result.append(alpha);
		result.append(')');
		return result.toString();
	}

} //GeomBarLayerImpl
