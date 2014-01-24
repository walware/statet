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
import de.walware.statet.rtm.ggplot.GeomPointLayer;
import de.walware.statet.rtm.ggplot.PropAlphaProvider;
import de.walware.statet.rtm.ggplot.PropColorProvider;
import de.walware.statet.rtm.ggplot.PropFillProvider;
import de.walware.statet.rtm.ggplot.PropShapeProvider;
import de.walware.statet.rtm.ggplot.PropSizeProvider;
import de.walware.statet.rtm.rtdata.RtDataFactory;
import de.walware.statet.rtm.rtdata.RtDataPackage;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;


/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Geom Point Layer</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomPointLayerImpl#getShape <em>Shape</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomPointLayerImpl#getSize <em>Size</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomPointLayerImpl#getColor <em>Color</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomPointLayerImpl#getFill <em>Fill</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomPointLayerImpl#getAlpha <em>Alpha</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomPointLayerImpl#getPositionXJitter <em>Position XJitter</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GeomPointLayerImpl#getPositionYJitter <em>Position YJitter</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class GeomPointLayerImpl extends XYVarLayerImpl implements GeomPointLayer {
	/**
	 * The default value of the '{@link #getShape() <em>Shape</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShape()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr SHAPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getShape() <em>Shape</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShape()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr shape = SHAPE_EDEFAULT;

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
	 * The default value of the '{@link #getPositionXJitter() <em>Position XJitter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPositionXJitter()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr POSITION_XJITTER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPositionXJitter() <em>Position XJitter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPositionXJitter()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr positionXJitter = POSITION_XJITTER_EDEFAULT;

	/**
	 * The default value of the '{@link #getPositionYJitter() <em>Position YJitter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPositionYJitter()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr POSITION_YJITTER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPositionYJitter() <em>Position YJitter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPositionYJitter()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr positionYJitter = POSITION_YJITTER_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GeomPointLayerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GGPlotPackage.Literals.GEOM_POINT_LAYER;
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
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_POINT_LAYER__COLOR, oldColor, color));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_POINT_LAYER__FILL, oldFill, fill));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_POINT_LAYER__ALPHA, oldAlpha, alpha));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getShape() {
		return shape;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setShape(RTypedExpr newShape) {
		RTypedExpr oldShape = shape;
		shape = newShape;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_POINT_LAYER__SHAPE, oldShape, shape));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_POINT_LAYER__SIZE, oldSize, size));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getPositionXJitter() {
		return positionXJitter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPositionXJitter(RTypedExpr newPositionXJitter) {
		RTypedExpr oldPositionXJitter = positionXJitter;
		positionXJitter = newPositionXJitter;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_POINT_LAYER__POSITION_XJITTER, oldPositionXJitter, positionXJitter));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getPositionYJitter() {
		return positionYJitter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPositionYJitter(RTypedExpr newPositionYJitter) {
		RTypedExpr oldPositionYJitter = positionYJitter;
		positionYJitter = newPositionYJitter;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GEOM_POINT_LAYER__POSITION_YJITTER, oldPositionYJitter, positionYJitter));
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
			case GGPlotPackage.GEOM_POINT_LAYER__SHAPE:
				return getShape();
			case GGPlotPackage.GEOM_POINT_LAYER__SIZE:
				return getSize();
			case GGPlotPackage.GEOM_POINT_LAYER__COLOR:
				return getColor();
			case GGPlotPackage.GEOM_POINT_LAYER__FILL:
				return getFill();
			case GGPlotPackage.GEOM_POINT_LAYER__ALPHA:
				return getAlpha();
			case GGPlotPackage.GEOM_POINT_LAYER__POSITION_XJITTER:
				return getPositionXJitter();
			case GGPlotPackage.GEOM_POINT_LAYER__POSITION_YJITTER:
				return getPositionYJitter();
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
			case GGPlotPackage.GEOM_POINT_LAYER__SHAPE:
				setShape((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_POINT_LAYER__SIZE:
				setSize((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_POINT_LAYER__COLOR:
				setColor((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_POINT_LAYER__FILL:
				setFill((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_POINT_LAYER__ALPHA:
				setAlpha((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_POINT_LAYER__POSITION_XJITTER:
				setPositionXJitter((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GEOM_POINT_LAYER__POSITION_YJITTER:
				setPositionYJitter((RTypedExpr)newValue);
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
			case GGPlotPackage.GEOM_POINT_LAYER__SHAPE:
				setShape(SHAPE_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_POINT_LAYER__SIZE:
				setSize(SIZE_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_POINT_LAYER__COLOR:
				setColor(COLOR_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_POINT_LAYER__FILL:
				setFill(FILL_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_POINT_LAYER__ALPHA:
				setAlpha(ALPHA_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_POINT_LAYER__POSITION_XJITTER:
				setPositionXJitter(POSITION_XJITTER_EDEFAULT);
				return;
			case GGPlotPackage.GEOM_POINT_LAYER__POSITION_YJITTER:
				setPositionYJitter(POSITION_YJITTER_EDEFAULT);
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
			case GGPlotPackage.GEOM_POINT_LAYER__SHAPE:
				return SHAPE_EDEFAULT == null ? shape != null : !SHAPE_EDEFAULT.equals(shape);
			case GGPlotPackage.GEOM_POINT_LAYER__SIZE:
				return SIZE_EDEFAULT == null ? size != null : !SIZE_EDEFAULT.equals(size);
			case GGPlotPackage.GEOM_POINT_LAYER__COLOR:
				return COLOR_EDEFAULT == null ? color != null : !COLOR_EDEFAULT.equals(color);
			case GGPlotPackage.GEOM_POINT_LAYER__FILL:
				return FILL_EDEFAULT == null ? fill != null : !FILL_EDEFAULT.equals(fill);
			case GGPlotPackage.GEOM_POINT_LAYER__ALPHA:
				return ALPHA_EDEFAULT == null ? alpha != null : !ALPHA_EDEFAULT.equals(alpha);
			case GGPlotPackage.GEOM_POINT_LAYER__POSITION_XJITTER:
				return POSITION_XJITTER_EDEFAULT == null ? positionXJitter != null : !POSITION_XJITTER_EDEFAULT.equals(positionXJitter);
			case GGPlotPackage.GEOM_POINT_LAYER__POSITION_YJITTER:
				return POSITION_YJITTER_EDEFAULT == null ? positionYJitter != null : !POSITION_YJITTER_EDEFAULT.equals(positionYJitter);
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
		if (baseClass == PropShapeProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_POINT_LAYER__SHAPE: return GGPlotPackage.PROP_SHAPE_PROVIDER__SHAPE;
				default: return -1;
			}
		}
		if (baseClass == PropSizeProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_POINT_LAYER__SIZE: return GGPlotPackage.PROP_SIZE_PROVIDER__SIZE;
				default: return -1;
			}
		}
		if (baseClass == PropColorProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_POINT_LAYER__COLOR: return GGPlotPackage.PROP_COLOR_PROVIDER__COLOR;
				default: return -1;
			}
		}
		if (baseClass == PropFillProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_POINT_LAYER__FILL: return GGPlotPackage.PROP_FILL_PROVIDER__FILL;
				default: return -1;
			}
		}
		if (baseClass == PropAlphaProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GEOM_POINT_LAYER__ALPHA: return GGPlotPackage.PROP_ALPHA_PROVIDER__ALPHA;
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
		if (baseClass == PropShapeProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_SHAPE_PROVIDER__SHAPE: return GGPlotPackage.GEOM_POINT_LAYER__SHAPE;
				default: return -1;
			}
		}
		if (baseClass == PropSizeProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_SIZE_PROVIDER__SIZE: return GGPlotPackage.GEOM_POINT_LAYER__SIZE;
				default: return -1;
			}
		}
		if (baseClass == PropColorProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_COLOR_PROVIDER__COLOR: return GGPlotPackage.GEOM_POINT_LAYER__COLOR;
				default: return -1;
			}
		}
		if (baseClass == PropFillProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_FILL_PROVIDER__FILL: return GGPlotPackage.GEOM_POINT_LAYER__FILL;
				default: return -1;
			}
		}
		if (baseClass == PropAlphaProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_ALPHA_PROVIDER__ALPHA: return GGPlotPackage.GEOM_POINT_LAYER__ALPHA;
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
		result.append(" (shape: "); //$NON-NLS-1$
		result.append(shape);
		result.append(", size: "); //$NON-NLS-1$
		result.append(size);
		result.append(", color: "); //$NON-NLS-1$
		result.append(color);
		result.append(", fill: "); //$NON-NLS-1$
		result.append(fill);
		result.append(", alpha: "); //$NON-NLS-1$
		result.append(alpha);
		result.append(", positionXJitter: "); //$NON-NLS-1$
		result.append(positionXJitter);
		result.append(", positionYJitter: "); //$NON-NLS-1$
		result.append(positionYJitter);
		result.append(')');
		return result.toString();
	}

} //GeomPointLayerImpl
