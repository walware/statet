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
import org.eclipse.emf.ecore.impl.EObjectImpl;

import de.walware.statet.rtm.ggplot.GGPlotPackage;
import de.walware.statet.rtm.ggplot.PropColorProvider;
import de.walware.statet.rtm.ggplot.TextStyle;
import de.walware.statet.rtm.rtdata.RtDataFactory;
import de.walware.statet.rtm.rtdata.RtDataPackage;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;


/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Text Style</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.TextStyleImpl#getSize <em>Size</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.TextStyleImpl#getColor <em>Color</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.TextStyleImpl#getFontFamily <em>Font Family</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.TextStyleImpl#getFontFace <em>Font Face</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.TextStyleImpl#getHJust <em>HJust</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.TextStyleImpl#getVJust <em>VJust</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.TextStyleImpl#getAngle <em>Angle</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class TextStyleImpl extends EObjectImpl implements TextStyle {
	/**
	 * The default value of the '{@link #getSize() <em>Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSize()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr SIZE_EDEFAULT = null; //$NON-NLS-1$

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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TextStyleImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GGPlotPackage.Literals.TEXT_STYLE;
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
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.TEXT_STYLE__SIZE, oldSize, size));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.TEXT_STYLE__COLOR, oldColor, color));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.TEXT_STYLE__FONT_FAMILY, oldFontFamily, fontFamily));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.TEXT_STYLE__FONT_FACE, oldFontFace, fontFace));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.TEXT_STYLE__HJUST, oldHJust, hJust));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.TEXT_STYLE__VJUST, oldVJust, vJust));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.TEXT_STYLE__ANGLE, oldAngle, angle));
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
			case GGPlotPackage.TEXT_STYLE__SIZE:
				return getSize();
			case GGPlotPackage.TEXT_STYLE__COLOR:
				return getColor();
			case GGPlotPackage.TEXT_STYLE__FONT_FAMILY:
				return getFontFamily();
			case GGPlotPackage.TEXT_STYLE__FONT_FACE:
				return getFontFace();
			case GGPlotPackage.TEXT_STYLE__HJUST:
				return getHJust();
			case GGPlotPackage.TEXT_STYLE__VJUST:
				return getVJust();
			case GGPlotPackage.TEXT_STYLE__ANGLE:
				return getAngle();
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
			case GGPlotPackage.TEXT_STYLE__SIZE:
				setSize((RTypedExpr)newValue);
				return;
			case GGPlotPackage.TEXT_STYLE__COLOR:
				setColor((RTypedExpr)newValue);
				return;
			case GGPlotPackage.TEXT_STYLE__FONT_FAMILY:
				setFontFamily((RTypedExpr)newValue);
				return;
			case GGPlotPackage.TEXT_STYLE__FONT_FACE:
				setFontFace((RTypedExpr)newValue);
				return;
			case GGPlotPackage.TEXT_STYLE__HJUST:
				setHJust((RTypedExpr)newValue);
				return;
			case GGPlotPackage.TEXT_STYLE__VJUST:
				setVJust((RTypedExpr)newValue);
				return;
			case GGPlotPackage.TEXT_STYLE__ANGLE:
				setAngle((RTypedExpr)newValue);
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
			case GGPlotPackage.TEXT_STYLE__SIZE:
				setSize(SIZE_EDEFAULT);
				return;
			case GGPlotPackage.TEXT_STYLE__COLOR:
				setColor(COLOR_EDEFAULT);
				return;
			case GGPlotPackage.TEXT_STYLE__FONT_FAMILY:
				setFontFamily(FONT_FAMILY_EDEFAULT);
				return;
			case GGPlotPackage.TEXT_STYLE__FONT_FACE:
				setFontFace(FONT_FACE_EDEFAULT);
				return;
			case GGPlotPackage.TEXT_STYLE__HJUST:
				setHJust(HJUST_EDEFAULT);
				return;
			case GGPlotPackage.TEXT_STYLE__VJUST:
				setVJust(VJUST_EDEFAULT);
				return;
			case GGPlotPackage.TEXT_STYLE__ANGLE:
				setAngle(ANGLE_EDEFAULT);
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
			case GGPlotPackage.TEXT_STYLE__SIZE:
				return SIZE_EDEFAULT == null ? size != null : !SIZE_EDEFAULT.equals(size);
			case GGPlotPackage.TEXT_STYLE__COLOR:
				return COLOR_EDEFAULT == null ? color != null : !COLOR_EDEFAULT.equals(color);
			case GGPlotPackage.TEXT_STYLE__FONT_FAMILY:
				return FONT_FAMILY_EDEFAULT == null ? fontFamily != null : !FONT_FAMILY_EDEFAULT.equals(fontFamily);
			case GGPlotPackage.TEXT_STYLE__FONT_FACE:
				return FONT_FACE_EDEFAULT == null ? fontFace != null : !FONT_FACE_EDEFAULT.equals(fontFace);
			case GGPlotPackage.TEXT_STYLE__HJUST:
				return HJUST_EDEFAULT == null ? hJust != null : !HJUST_EDEFAULT.equals(hJust);
			case GGPlotPackage.TEXT_STYLE__VJUST:
				return VJUST_EDEFAULT == null ? vJust != null : !VJUST_EDEFAULT.equals(vJust);
			case GGPlotPackage.TEXT_STYLE__ANGLE:
				return ANGLE_EDEFAULT == null ? angle != null : !ANGLE_EDEFAULT.equals(angle);
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
		if (baseClass == PropColorProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.TEXT_STYLE__COLOR: return GGPlotPackage.PROP_COLOR_PROVIDER__COLOR;
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
		if (baseClass == PropColorProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_COLOR_PROVIDER__COLOR: return GGPlotPackage.TEXT_STYLE__COLOR;
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
		result.append(')');
		return result.toString();
	}

} //TextStyleImpl
