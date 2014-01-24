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
import de.walware.statet.rtm.ggplot.GeomTextLayer;
import de.walware.statet.rtm.ggplot.PropAlphaProvider;
import de.walware.statet.rtm.ggplot.PropColorProvider;
import de.walware.statet.rtm.ggplot.PropSizeProvider;
import de.walware.statet.rtm.ggplot.TextStyle;
import de.walware.statet.rtm.rtdata.RtDataFactory;
import de.walware.statet.rtm.rtdata.RtDataPackage;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;


/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Geom Text Layer</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomTextLayerImpl#getSize <em>Size</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomTextLayerImpl#getColor <em>Color</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomTextLayerImpl#getFontFamily <em>Font Family</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomTextLayerImpl#getFontFace <em>Font Face</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomTextLayerImpl#getHJust <em>HJust</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomTextLayerImpl#getVJust <em>VJust</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomTextLayerImpl#getAngle <em>Angle</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomTextLayerImpl#getAlpha <em>Alpha</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomTextLayerImpl#getLabel <em>Label</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class GeomTextLayerImpl extends XYVarLayerImpl implements GeomTextLayer {
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
	protected static final RTypedExpr COLOR_EDEFAULT = (RTypedExpr)RtDataFactory.eINSTANCE.createFromString(RtDataPackage.eINSTANCE.getRColor(), "");

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
	 * The default value of the '{@link #getFontFamily() <em>Font Family</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFontFamily()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr FONT_FAMILY_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFontFamily() <em>Font Family</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFontFamily()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr fontFamily = FONT_FAMILY_EDEFAULT;

	/**
	 * The default value of the '{@link #getFontFace() <em>Font Face</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFontFace()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr FONT_FACE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFontFace() <em>Font Face</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFontFace()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr fontFace = FONT_FACE_EDEFAULT;

	/**
	 * The default value of the '{@link #getHJust() <em>HJust</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHJust()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr HJUST_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getHJust() <em>HJust</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHJust()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr hJust = HJUST_EDEFAULT;

	/**
	 * The default value of the '{@link #getVJust() <em>VJust</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVJust()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr VJUST_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getVJust() <em>VJust</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVJust()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr vJust = VJUST_EDEFAULT;

	/**
	 * The default value of the '{@link #getAngle() <em>Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAngle()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr ANGLE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getAngle() <em>Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAngle()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr angle = ANGLE_EDEFAULT;

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
	 * The default value of the '{@link #getLabel() <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabel()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr LABEL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLabel() <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabel()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr label = LABEL_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GeomTextLayerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GGPlotPackage.Literals.GEOM_TEXT_LAYER;
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
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_TEXT_LAYER__COLOR, oldColor, color));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getFontFamily() {
		return fontFamily;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFontFamily(RTypedExpr newFontFamily) {
		RTypedExpr oldFontFamily = fontFamily;
		fontFamily = newFontFamily;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_TEXT_LAYER__FONT_FAMILY, oldFontFamily, fontFamily));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getFontFace() {
		return fontFace;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFontFace(RTypedExpr newFontFace) {
		RTypedExpr oldFontFace = fontFace;
		fontFace = newFontFace;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_TEXT_LAYER__FONT_FACE, oldFontFace, fontFace));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getHJust() {
		return hJust;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setHJust(RTypedExpr newHJust) {
		RTypedExpr oldHJust = hJust;
		hJust = newHJust;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_TEXT_LAYER__HJUST, oldHJust, hJust));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getVJust() {
		return vJust;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setVJust(RTypedExpr newVJust) {
		RTypedExpr oldVJust = vJust;
		vJust = newVJust;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_TEXT_LAYER__VJUST, oldVJust, vJust));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getAngle() {
		return angle;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAngle(RTypedExpr newAngle) {
		RTypedExpr oldAngle = angle;
		angle = newAngle;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_TEXT_LAYER__ANGLE, oldAngle, angle));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_TEXT_LAYER__ALPHA, oldAlpha, alpha));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getLabel() {
		return label;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLabel(RTypedExpr newLabel) {
		RTypedExpr oldLabel = label;
		label = newLabel;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_TEXT_LAYER__LABEL, oldLabel, label));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_TEXT_LAYER__SIZE, oldSize, size));
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
			case GGPlotPackage.GEOM_TEXT_LAYER__SIZE:
				return getSize();
			case GGPlotPackage.GEOM_TEXT_LAYER__COLOR:
				return getColor();
			case GGPlotPackage.GEOM_TEXT_LAYER__FONT_FAMILY:
				return getFontFamily();
			case GGPlotPackage.GEOM_TEXT_LAYER__FONT_FACE:
				return getFontFace();
			case GGPlotPackage.GEOM_TEXT_LAYER__HJUST:
				return getHJust();
			case GGPlotPackage.GEOM_TEXT_LAYER__VJUST:
				return getVJust();
			case GGPlotPackage.GEOM_TEXT_LAYER__ANGLE:
				return getAngle();
			case GGPlotPackage.GEOM_TEXT_LAYER__ALPHA:
				return getAlpha();
			case GGPlotPackage.GEOM_TEXT_LAYER__LABEL:
				return getLabel();
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
			case GGPlotPackage.GEOM_TEXT_LAYER__SIZE:
				setSize((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_TEXT_LAYER__COLOR:
				setColor((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_TEXT_LAYER__FONT_FAMILY:
				setFontFamily((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_TEXT_LAYER__FONT_FACE:
				setFontFace((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_TEXT_LAYER__HJUST:
				setHJust((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_TEXT_LAYER__VJUST:
				setVJust((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_TEXT_LAYER__ANGLE:
				setAngle((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_TEXT_LAYER__ALPHA:
				setAlpha((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_TEXT_LAYER__LABEL:
				setLabel((RTypedExpr)newValue);
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
			case GGPlotPackage.GEOM_TEXT_LAYER__SIZE:
				setSize(SIZE_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_TEXT_LAYER__COLOR:
				setColor(COLOR_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_TEXT_LAYER__FONT_FAMILY:
				setFontFamily(FONT_FAMILY_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_TEXT_LAYER__FONT_FACE:
				setFontFace(FONT_FACE_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_TEXT_LAYER__HJUST:
				setHJust(HJUST_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_TEXT_LAYER__VJUST:
				setVJust(VJUST_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_TEXT_LAYER__ANGLE:
				setAngle(ANGLE_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_TEXT_LAYER__ALPHA:
				setAlpha(ALPHA_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_TEXT_LAYER__LABEL:
				setLabel(LABEL_EDEFAULT);
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
			case GGPlotPackage.GEOM_TEXT_LAYER__SIZE:
				return SIZE_EDEFAULT == null ? size != null : !SIZE_EDEFAULT.equals(size);
			case GGPlotPackage.GEOM_TEXT_LAYER__COLOR:
				return COLOR_EDEFAULT == null ? color != null : !COLOR_EDEFAULT.equals(color);
			case GGPlotPackage.GEOM_TEXT_LAYER__FONT_FAMILY:
				return FONT_FAMILY_EDEFAULT == null ? fontFamily != null : !FONT_FAMILY_EDEFAULT.equals(fontFamily);
			case GGPlotPackage.GEOM_TEXT_LAYER__FONT_FACE:
				return FONT_FACE_EDEFAULT == null ? fontFace != null : !FONT_FACE_EDEFAULT.equals(fontFace);
			case GGPlotPackage.GEOM_TEXT_LAYER__HJUST:
				return HJUST_EDEFAULT == null ? hJust != null : !HJUST_EDEFAULT.equals(hJust);
			case GGPlotPackage.GEOM_TEXT_LAYER__VJUST:
				return VJUST_EDEFAULT == null ? vJust != null : !VJUST_EDEFAULT.equals(vJust);
			case GGPlotPackage.GEOM_TEXT_LAYER__ANGLE:
				return ANGLE_EDEFAULT == null ? angle != null : !ANGLE_EDEFAULT.equals(angle);
			case GGPlotPackage.GEOM_TEXT_LAYER__ALPHA:
				return ALPHA_EDEFAULT == null ? alpha != null : !ALPHA_EDEFAULT.equals(alpha);
			case GGPlotPackage.GEOM_TEXT_LAYER__LABEL:
				return LABEL_EDEFAULT == null ? label != null : !LABEL_EDEFAULT.equals(label);
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
		if (baseClass == PropSizeProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_TEXT_LAYER__SIZE: return GGPlotPackage.PROP_SIZE_PROVIDER__SIZE;
				default: return -1;
			}
		}
		if (baseClass == PropColorProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_TEXT_LAYER__COLOR: return GGPlotPackage.PROP_COLOR_PROVIDER__COLOR;
				default: return -1;
			}
		}
		if (baseClass == TextStyle.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_TEXT_LAYER__FONT_FAMILY: return GGPlotPackage.TEXT_STYLE__FONT_FAMILY;
				case GGPlotPackage.GEOM_TEXT_LAYER__FONT_FACE: return GGPlotPackage.TEXT_STYLE__FONT_FACE;
				case GGPlotPackage.GEOM_TEXT_LAYER__HJUST: return GGPlotPackage.TEXT_STYLE__HJUST;
				case GGPlotPackage.GEOM_TEXT_LAYER__VJUST: return GGPlotPackage.TEXT_STYLE__VJUST;
				case GGPlotPackage.GEOM_TEXT_LAYER__ANGLE: return GGPlotPackage.TEXT_STYLE__ANGLE;
				default: return -1;
			}
		}
		if (baseClass == PropAlphaProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_TEXT_LAYER__ALPHA: return GGPlotPackage.PROP_ALPHA_PROVIDER__ALPHA;
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
		if (baseClass == PropSizeProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_SIZE_PROVIDER__SIZE: return GGPlotPackage.GEOM_TEXT_LAYER__SIZE;
				default: return -1;
			}
		}
		if (baseClass == PropColorProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_COLOR_PROVIDER__COLOR: return GGPlotPackage.GEOM_TEXT_LAYER__COLOR;
				default: return -1;
			}
		}
		if (baseClass == TextStyle.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.TEXT_STYLE__FONT_FAMILY: return GGPlotPackage.GEOM_TEXT_LAYER__FONT_FAMILY;
				case GGPlotPackage.TEXT_STYLE__FONT_FACE: return GGPlotPackage.GEOM_TEXT_LAYER__FONT_FACE;
				case GGPlotPackage.TEXT_STYLE__HJUST: return GGPlotPackage.GEOM_TEXT_LAYER__HJUST;
				case GGPlotPackage.TEXT_STYLE__VJUST: return GGPlotPackage.GEOM_TEXT_LAYER__VJUST;
				case GGPlotPackage.TEXT_STYLE__ANGLE: return GGPlotPackage.GEOM_TEXT_LAYER__ANGLE;
				default: return -1;
			}
		}
		if (baseClass == PropAlphaProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_ALPHA_PROVIDER__ALPHA: return GGPlotPackage.GEOM_TEXT_LAYER__ALPHA;
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
		result.append(" (size: "); //$NON-NLS-1$
		result.append(size);
		result.append(", color: "); //$NON-NLS-1$
		result.append(color);
		result.append(", fontFamily: "); //$NON-NLS-1$
		result.append(fontFamily);
		result.append(", fontFace: "); //$NON-NLS-1$
		result.append(fontFace);
		result.append(", hJust: "); //$NON-NLS-1$
		result.append(hJust);
		result.append(", vJust: "); //$NON-NLS-1$
		result.append(vJust);
		result.append(", angle: "); //$NON-NLS-1$
		result.append(angle);
		result.append(", alpha: "); //$NON-NLS-1$
		result.append(alpha);
		result.append(", label: "); //$NON-NLS-1$
		result.append(label);
		result.append(')');
		return result.toString();
	}

} //GeomTextLayerImpl
