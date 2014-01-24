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
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

import de.walware.statet.rtm.ggplot.GGPlotPackage;
import de.walware.statet.rtm.ggplot.GeomAblineLayer;
import de.walware.statet.rtm.ggplot.PropAlphaProvider;
import de.walware.statet.rtm.ggplot.PropColorProvider;
import de.walware.statet.rtm.ggplot.PropLineTypeProvider;
import de.walware.statet.rtm.ggplot.PropSizeProvider;
import de.walware.statet.rtm.rtdata.RtDataFactory;
import de.walware.statet.rtm.rtdata.RtDataPackage;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;


/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Geom Abline Layer</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomAblineLayerImpl#getLineType <em>Line Type</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomAblineLayerImpl#getSize <em>Size</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomAblineLayerImpl#getColor <em>Color</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomAblineLayerImpl#getAlpha <em>Alpha</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomAblineLayerImpl#getInterceptVar <em>Intercept Var</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomAblineLayerImpl#getSlopeVar <em>Slope Var</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class GeomAblineLayerImpl extends LayerImpl implements GeomAblineLayer {
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
	 * The default value of the '{@link #getSize() <em>Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSize()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr SIZE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSize() <em>Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSize()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr size = SIZE_EDEFAULT;

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
	 * The default value of the '{@link #getInterceptVar() <em>Intercept Var</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInterceptVar()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr INTERCEPT_VAR_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getInterceptVar() <em>Intercept Var</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInterceptVar()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr interceptVar = INTERCEPT_VAR_EDEFAULT;

	/**
	 * The default value of the '{@link #getSlopeVar() <em>Slope Var</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSlopeVar()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr SLOPE_VAR_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSlopeVar() <em>Slope Var</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSlopeVar()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr slopeVar = SLOPE_VAR_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GeomAblineLayerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GGPlotPackage.Literals.GEOM_ABLINE_LAYER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getLineType() {
		return lineType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLineType(RTypedExpr newLineType) {
		RTypedExpr oldLineType = lineType;
		lineType = newLineType;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_ABLINE_LAYER__LINE_TYPE, oldLineType, lineType));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getSize() {
		return size;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSize(RTypedExpr newSize) {
		RTypedExpr oldSize = size;
		size = newSize;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_ABLINE_LAYER__SIZE, oldSize, size));
		}
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
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_ABLINE_LAYER__COLOR, oldColor, color));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_ABLINE_LAYER__ALPHA, oldAlpha, alpha));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getInterceptVar() {
		return interceptVar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setInterceptVar(RTypedExpr newInterceptVar) {
		RTypedExpr oldInterceptVar = interceptVar;
		interceptVar = newInterceptVar;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_ABLINE_LAYER__INTERCEPT_VAR, oldInterceptVar, interceptVar));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getSlopeVar() {
		return slopeVar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSlopeVar(RTypedExpr newSlopeVar) {
		RTypedExpr oldSlopeVar = slopeVar;
		slopeVar = newSlopeVar;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_ABLINE_LAYER__SLOPE_VAR, oldSlopeVar, slopeVar));
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
			case GGPlotPackage.GEOM_ABLINE_LAYER__LINE_TYPE:
				return getLineType();
			case GGPlotPackage.GEOM_ABLINE_LAYER__SIZE:
				return getSize();
			case GGPlotPackage.GEOM_ABLINE_LAYER__COLOR:
				return getColor();
			case GGPlotPackage.GEOM_ABLINE_LAYER__ALPHA:
				return getAlpha();
			case GGPlotPackage.GEOM_ABLINE_LAYER__INTERCEPT_VAR:
				return getInterceptVar();
			case GGPlotPackage.GEOM_ABLINE_LAYER__SLOPE_VAR:
				return getSlopeVar();
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
			case GGPlotPackage.GEOM_ABLINE_LAYER__LINE_TYPE:
				setLineType((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_ABLINE_LAYER__SIZE:
				setSize((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_ABLINE_LAYER__COLOR:
				setColor((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_ABLINE_LAYER__ALPHA:
				setAlpha((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_ABLINE_LAYER__INTERCEPT_VAR:
				setInterceptVar((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_ABLINE_LAYER__SLOPE_VAR:
				setSlopeVar((RTypedExpr)newValue);
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
			case GGPlotPackage.GEOM_ABLINE_LAYER__LINE_TYPE:
				setLineType(LINE_TYPE_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_ABLINE_LAYER__SIZE:
				setSize(SIZE_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_ABLINE_LAYER__COLOR:
				setColor(COLOR_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_ABLINE_LAYER__ALPHA:
				setAlpha(ALPHA_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_ABLINE_LAYER__INTERCEPT_VAR:
				setInterceptVar(INTERCEPT_VAR_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_ABLINE_LAYER__SLOPE_VAR:
				setSlopeVar(SLOPE_VAR_EDEFAULT);
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
			case GGPlotPackage.GEOM_ABLINE_LAYER__LINE_TYPE:
				return LINE_TYPE_EDEFAULT == null ? lineType != null : !LINE_TYPE_EDEFAULT.equals(lineType);
			case GGPlotPackage.GEOM_ABLINE_LAYER__SIZE:
				return SIZE_EDEFAULT == null ? size != null : !SIZE_EDEFAULT.equals(size);
			case GGPlotPackage.GEOM_ABLINE_LAYER__COLOR:
				return COLOR_EDEFAULT == null ? color != null : !COLOR_EDEFAULT.equals(color);
			case GGPlotPackage.GEOM_ABLINE_LAYER__ALPHA:
				return ALPHA_EDEFAULT == null ? alpha != null : !ALPHA_EDEFAULT.equals(alpha);
			case GGPlotPackage.GEOM_ABLINE_LAYER__INTERCEPT_VAR:
				return INTERCEPT_VAR_EDEFAULT == null ? interceptVar != null : !INTERCEPT_VAR_EDEFAULT.equals(interceptVar);
			case GGPlotPackage.GEOM_ABLINE_LAYER__SLOPE_VAR:
				return SLOPE_VAR_EDEFAULT == null ? slopeVar != null : !SLOPE_VAR_EDEFAULT.equals(slopeVar);
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
				case GGPlotPackage.GEOM_ABLINE_LAYER__LINE_TYPE: return GGPlotPackage.PROP_LINE_TYPE_PROVIDER__LINE_TYPE;
				default: return -1;
			}
		}
		if (baseClass == PropSizeProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_ABLINE_LAYER__SIZE: return GGPlotPackage.PROP_SIZE_PROVIDER__SIZE;
				default: return -1;
			}
		}
		if (baseClass == PropColorProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_ABLINE_LAYER__COLOR: return GGPlotPackage.PROP_COLOR_PROVIDER__COLOR;
				default: return -1;
			}
		}
		if (baseClass == PropAlphaProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_ABLINE_LAYER__ALPHA: return GGPlotPackage.PROP_ALPHA_PROVIDER__ALPHA;
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
				case GGPlotPackage.PROP_LINE_TYPE_PROVIDER__LINE_TYPE: return GGPlotPackage.GEOM_ABLINE_LAYER__LINE_TYPE;
				default: return -1;
			}
		}
		if (baseClass == PropSizeProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_SIZE_PROVIDER__SIZE: return GGPlotPackage.GEOM_ABLINE_LAYER__SIZE;
				default: return -1;
			}
		}
		if (baseClass == PropColorProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_COLOR_PROVIDER__COLOR: return GGPlotPackage.GEOM_ABLINE_LAYER__COLOR;
				default: return -1;
			}
		}
		if (baseClass == PropAlphaProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_ALPHA_PROVIDER__ALPHA: return GGPlotPackage.GEOM_ABLINE_LAYER__ALPHA;
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
		result.append(" (lineType: "); //$NON-NLS-1$
		result.append(lineType);
		result.append(", size: "); //$NON-NLS-1$
		result.append(size);
		result.append(", color: "); //$NON-NLS-1$
		result.append(color);
		result.append(", alpha: "); //$NON-NLS-1$
		result.append(alpha);
		result.append(", interceptVar: "); //$NON-NLS-1$
		result.append(interceptVar);
		result.append(", slopeVar: "); //$NON-NLS-1$
		result.append(slopeVar);
		result.append(')');
		return result.toString();
	}

} //GeomAblineLayerImpl
