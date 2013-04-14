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
import de.walware.statet.rtm.ggplot.GeomViolinLayer;
import de.walware.statet.rtm.ggplot.PropAlphaProvider;
import de.walware.statet.rtm.ggplot.PropColorProvider;
import de.walware.statet.rtm.ggplot.PropFillProvider;
import de.walware.statet.rtm.ggplot.PropLineTypeProvider;
import de.walware.statet.rtm.rtdata.RtDataFactory;
import de.walware.statet.rtm.rtdata.RtDataPackage;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Geom Violin Layer</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomViolinLayerImpl#getLineType <em>Line Type</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomViolinLayerImpl#getColor <em>Color</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomViolinLayerImpl#getFill <em>Fill</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomViolinLayerImpl#getAlpha <em>Alpha</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class GeomViolinLayerImpl extends XYVarLayerImpl implements GeomViolinLayer {
	/**
	 * The default value of the '{@link #getLineType() <em>Line Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLineType()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr LINE_TYPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLineType() <em>Line Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLineType()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr lineType = LINE_TYPE_EDEFAULT;

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
	protected static final RTypedExpr FILL_EDEFAULT = (RTypedExpr)RtDataFactory.eINSTANCE.createFromString(RtDataPackage.eINSTANCE.getRColor(), ""); //$NON-NLS-1$

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
	protected GeomViolinLayerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GGPlotPackage.Literals.GEOM_VIOLIN_LAYER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RTypedExpr getLineType() {
		return lineType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLineType(RTypedExpr newLineType) {
		RTypedExpr oldLineType = lineType;
		lineType = newLineType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_VIOLIN_LAYER__LINE_TYPE, oldLineType, lineType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RTypedExpr getColor() {
		return color;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setColor(RTypedExpr newColor) {
		RTypedExpr oldColor = color;
		color = newColor;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_VIOLIN_LAYER__COLOR, oldColor, color));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RTypedExpr getFill() {
		return fill;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFill(RTypedExpr newFill) {
		RTypedExpr oldFill = fill;
		fill = newFill;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_VIOLIN_LAYER__FILL, oldFill, fill));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RTypedExpr getAlpha() {
		return alpha;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setAlpha(RTypedExpr newAlpha) {
		RTypedExpr oldAlpha = alpha;
		alpha = newAlpha;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_VIOLIN_LAYER__ALPHA, oldAlpha, alpha));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case GGPlotPackage.GEOM_VIOLIN_LAYER__LINE_TYPE:
				return getLineType();
			case GGPlotPackage.GEOM_VIOLIN_LAYER__COLOR:
				return getColor();
			case GGPlotPackage.GEOM_VIOLIN_LAYER__FILL:
				return getFill();
			case GGPlotPackage.GEOM_VIOLIN_LAYER__ALPHA:
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
			case GGPlotPackage.GEOM_VIOLIN_LAYER__LINE_TYPE:
				setLineType((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_VIOLIN_LAYER__COLOR:
				setColor((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_VIOLIN_LAYER__FILL:
				setFill((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_VIOLIN_LAYER__ALPHA:
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
			case GGPlotPackage.GEOM_VIOLIN_LAYER__LINE_TYPE:
				setLineType(LINE_TYPE_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_VIOLIN_LAYER__COLOR:
				setColor(COLOR_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_VIOLIN_LAYER__FILL:
				setFill(FILL_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_VIOLIN_LAYER__ALPHA:
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
			case GGPlotPackage.GEOM_VIOLIN_LAYER__LINE_TYPE:
				return LINE_TYPE_EDEFAULT == null ? lineType != null : !LINE_TYPE_EDEFAULT.equals(lineType);
			case GGPlotPackage.GEOM_VIOLIN_LAYER__COLOR:
				return COLOR_EDEFAULT == null ? color != null : !COLOR_EDEFAULT.equals(color);
			case GGPlotPackage.GEOM_VIOLIN_LAYER__FILL:
				return FILL_EDEFAULT == null ? fill != null : !FILL_EDEFAULT.equals(fill);
			case GGPlotPackage.GEOM_VIOLIN_LAYER__ALPHA:
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
		if (baseClass == PropLineTypeProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_VIOLIN_LAYER__LINE_TYPE: return GGPlotPackage.PROP_LINE_TYPE_PROVIDER__LINE_TYPE;
				default: return -1;
			}
		}
		if (baseClass == PropColorProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_VIOLIN_LAYER__COLOR: return GGPlotPackage.PROP_COLOR_PROVIDER__COLOR;
				default: return -1;
			}
		}
		if (baseClass == PropFillProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_VIOLIN_LAYER__FILL: return GGPlotPackage.PROP_FILL_PROVIDER__FILL;
				default: return -1;
			}
		}
		if (baseClass == PropAlphaProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_VIOLIN_LAYER__ALPHA: return GGPlotPackage.PROP_ALPHA_PROVIDER__ALPHA;
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
		if (baseClass == PropLineTypeProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_LINE_TYPE_PROVIDER__LINE_TYPE: return GGPlotPackage.GEOM_VIOLIN_LAYER__LINE_TYPE;
				default: return -1;
			}
		}
		if (baseClass == PropColorProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_COLOR_PROVIDER__COLOR: return GGPlotPackage.GEOM_VIOLIN_LAYER__COLOR;
				default: return -1;
			}
		}
		if (baseClass == PropFillProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_FILL_PROVIDER__FILL: return GGPlotPackage.GEOM_VIOLIN_LAYER__FILL;
				default: return -1;
			}
		}
		if (baseClass == PropAlphaProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_ALPHA_PROVIDER__ALPHA: return GGPlotPackage.GEOM_VIOLIN_LAYER__ALPHA;
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
		result.append(" (lineType: "); //$NON-NLS-1$
		result.append(lineType);
		result.append(", color: "); //$NON-NLS-1$
		result.append(color);
		result.append(", fill: "); //$NON-NLS-1$
		result.append(fill);
		result.append(", alpha: "); //$NON-NLS-1$
		result.append(alpha);
		result.append(')');
		return result.toString();
	}

} //GeomViolinLayerImpl
