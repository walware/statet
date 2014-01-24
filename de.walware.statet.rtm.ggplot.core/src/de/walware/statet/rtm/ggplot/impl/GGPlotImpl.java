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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import de.walware.statet.rtm.ggplot.FacetLayout;
import de.walware.statet.rtm.ggplot.GGPlot;
import de.walware.statet.rtm.ggplot.GGPlotPackage;
import de.walware.statet.rtm.ggplot.Layer;
import de.walware.statet.rtm.ggplot.PropXVarProvider;
import de.walware.statet.rtm.ggplot.PropYVarProvider;
import de.walware.statet.rtm.ggplot.TextStyle;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;


/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>GG Plot</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GGPlotImpl#getData <em>Data</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GGPlotImpl#getXVar <em>XVar</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GGPlotImpl#getYVar <em>YVar</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GGPlotImpl#getDataFilter <em>Data Filter</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GGPlotImpl#getMainTitle <em>Main Title</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GGPlotImpl#getMainTitleStyle <em>Main Title Style</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GGPlotImpl#getFacet <em>Facet</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GGPlotImpl#getAxXLim <em>Ax XLim</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GGPlotImpl#getAxYLim <em>Ax YLim</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GGPlotImpl#getAxXLabel <em>Ax XLabel</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GGPlotImpl#getAxYLabel <em>Ax YLabel</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GGPlotImpl#getAxXLabelStyle <em>Ax XLabel Style</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GGPlotImpl#getAxYLabelStyle <em>Ax YLabel Style</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GGPlotImpl#getAxXTextStyle <em>Ax XText Style</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GGPlotImpl#getAxYTextStyle <em>Ax YText Style</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.impl.GGPlotImpl#getLayers <em>Layers</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class GGPlotImpl extends EObjectImpl implements GGPlot {
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
	protected static final RTypedExpr YVAR_EDEFAULT = null; //$NON-NLS-1$

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
	 * The default value of the '{@link #getMainTitle() <em>Main Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMainTitle()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr MAIN_TITLE_EDEFAULT = null; //$NON-NLS-1$

	/**
	 * The cached value of the '{@link #getMainTitle() <em>Main Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMainTitle()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr mainTitle = MAIN_TITLE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getMainTitleStyle() <em>Main Title Style</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMainTitleStyle()
	 * @generated
	 * @ordered
	 */
	protected TextStyle mainTitleStyle;

	/**
	 * The cached value of the '{@link #getFacet() <em>Facet</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFacet()
	 * @generated
	 * @ordered
	 */
	protected FacetLayout facet;

	/**
	 * The default value of the '{@link #getAxXLim() <em>Ax XLim</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAxXLim()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr AX_XLIM_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getAxXLim() <em>Ax XLim</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAxXLim()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr axXLim = AX_XLIM_EDEFAULT;

	/**
	 * The default value of the '{@link #getAxYLim() <em>Ax YLim</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAxYLim()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr AX_YLIM_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getAxYLim() <em>Ax YLim</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAxYLim()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr axYLim = AX_YLIM_EDEFAULT;

	/**
	 * The default value of the '{@link #getAxXLabel() <em>Ax XLabel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAxXLabel()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr AX_XLABEL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getAxXLabel() <em>Ax XLabel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAxXLabel()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr axXLabel = AX_XLABEL_EDEFAULT;

	/**
	 * The default value of the '{@link #getAxYLabel() <em>Ax YLabel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAxYLabel()
	 * @generated
	 * @ordered
	 */
	protected static final RTypedExpr AX_YLABEL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getAxYLabel() <em>Ax YLabel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAxYLabel()
	 * @generated
	 * @ordered
	 */
	protected RTypedExpr axYLabel = AX_YLABEL_EDEFAULT;

	/**
	 * The cached value of the '{@link #getAxXLabelStyle() <em>Ax XLabel Style</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAxXLabelStyle()
	 * @generated
	 * @ordered
	 */
	protected TextStyle axXLabelStyle;

	/**
	 * The cached value of the '{@link #getAxYLabelStyle() <em>Ax YLabel Style</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAxYLabelStyle()
	 * @generated
	 * @ordered
	 */
	protected TextStyle axYLabelStyle;

	/**
	 * The cached value of the '{@link #getAxXTextStyle() <em>Ax XText Style</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAxXTextStyle()
	 * @generated
	 * @ordered
	 */
	protected TextStyle axXTextStyle;

	/**
	 * The cached value of the '{@link #getAxYTextStyle() <em>Ax YText Style</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAxYTextStyle()
	 * @generated
	 * @ordered
	 */
	protected TextStyle axYTextStyle;

	/**
	 * The cached value of the '{@link #getLayers() <em>Layers</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLayers()
	 * @generated
	 * @ordered
	 */
	protected EList<Layer> layers;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	protected GGPlotImpl() {
		super();
		
		NotificationChain msgs = null;
		{	TextStyle style = GGPlotPackage.eINSTANCE.getGGPlotFactory().createTextStyle();
			msgs = ((InternalEObject)style).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GG_PLOT__MAIN_TITLE_STYLE, null, msgs);
			msgs = basicSetMainTitleStyle(style, msgs); 
		}
		{	TextStyle style = GGPlotPackage.eINSTANCE.getGGPlotFactory().createTextStyle();
			msgs = ((InternalEObject)style).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GG_PLOT__AX_XLABEL_STYLE, null, msgs);
			msgs = basicSetAxXLabelStyle(style, msgs); 
		}
		{	TextStyle style = GGPlotPackage.eINSTANCE.getGGPlotFactory().createTextStyle();
			msgs = ((InternalEObject)style).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GG_PLOT__AX_YLABEL_STYLE, null, msgs);
			msgs = basicSetAxYLabelStyle(style, msgs); 
		}
		{	TextStyle style = GGPlotPackage.eINSTANCE.getGGPlotFactory().createTextStyle();
			msgs = ((InternalEObject)style).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GG_PLOT__AX_XTEXT_STYLE, null, msgs);
			msgs = basicSetAxXTextStyle(style, msgs); 
		}
		{	TextStyle style = GGPlotPackage.eINSTANCE.getGGPlotFactory().createTextStyle();
			msgs = ((InternalEObject)style).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GG_PLOT__AX_YTEXT_STYLE, null, msgs);
			msgs = basicSetAxYTextStyle(style, msgs); 
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GGPlotPackage.Literals.GG_PLOT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getData() {
		return data;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setData(RTypedExpr newData) {
		RTypedExpr oldData = data;
		data = newData;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__DATA, oldData, data));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getXVar() {
		return xVar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setXVar(RTypedExpr newXVar) {
		RTypedExpr oldXVar = xVar;
		xVar = newXVar;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__XVAR, oldXVar, xVar));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getYVar() {
		return yVar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setYVar(RTypedExpr newYVar) {
		RTypedExpr oldYVar = yVar;
		yVar = newYVar;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__YVAR, oldYVar, yVar));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getDataFilter() {
		return dataFilter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDataFilter(RTypedExpr newDataFilter) {
		RTypedExpr oldDataFilter = dataFilter;
		dataFilter = newDataFilter;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__DATA_FILTER, oldDataFilter, dataFilter));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getMainTitle() {
		return mainTitle;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMainTitle(RTypedExpr newMainTitle) {
		RTypedExpr oldMainTitle = mainTitle;
		mainTitle = newMainTitle;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__MAIN_TITLE, oldMainTitle, mainTitle));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TextStyle getMainTitleStyle() {
		return mainTitleStyle;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetMainTitleStyle(TextStyle newMainTitleStyle, NotificationChain msgs) {
		TextStyle oldMainTitleStyle = mainTitleStyle;
		mainTitleStyle = newMainTitleStyle;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__MAIN_TITLE_STYLE, oldMainTitleStyle, newMainTitleStyle);
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
	public void setMainTitleStyle(TextStyle newMainTitleStyle) {
		if (newMainTitleStyle != mainTitleStyle) {
			NotificationChain msgs = null;
			if (mainTitleStyle != null) {
				msgs = ((InternalEObject)mainTitleStyle).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GG_PLOT__MAIN_TITLE_STYLE, null, msgs);
			}
			if (newMainTitleStyle != null) {
				msgs = ((InternalEObject)newMainTitleStyle).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GG_PLOT__MAIN_TITLE_STYLE, null, msgs);
			}
			msgs = basicSetMainTitleStyle(newMainTitleStyle, msgs);
			if (msgs != null) {
				msgs.dispatch();
			}
		}
		else if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__MAIN_TITLE_STYLE, newMainTitleStyle, newMainTitleStyle));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FacetLayout getFacet() {
		return facet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetFacet(FacetLayout newFacet, NotificationChain msgs) {
		FacetLayout oldFacet = facet;
		facet = newFacet;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__FACET, oldFacet, newFacet);
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
	public void setFacet(FacetLayout newFacet) {
		if (newFacet != facet) {
			NotificationChain msgs = null;
			if (facet != null) {
				msgs = ((InternalEObject)facet).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GG_PLOT__FACET, null, msgs);
			}
			if (newFacet != null) {
				msgs = ((InternalEObject)newFacet).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GG_PLOT__FACET, null, msgs);
			}
			msgs = basicSetFacet(newFacet, msgs);
			if (msgs != null) {
				msgs.dispatch();
			}
		}
		else if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__FACET, newFacet, newFacet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getAxXLim() {
		return axXLim;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAxXLim(RTypedExpr newAxXLim) {
		RTypedExpr oldAxXLim = axXLim;
		axXLim = newAxXLim;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__AX_XLIM, oldAxXLim, axXLim));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getAxYLim() {
		return axYLim;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAxYLim(RTypedExpr newAxYLim) {
		RTypedExpr oldAxYLim = axYLim;
		axYLim = newAxYLim;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__AX_YLIM, oldAxYLim, axYLim));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getAxXLabel() {
		return axXLabel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAxXLabel(RTypedExpr newAxXLabel) {
		RTypedExpr oldAxXLabel = axXLabel;
		axXLabel = newAxXLabel;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__AX_XLABEL, oldAxXLabel, axXLabel));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RTypedExpr getAxYLabel() {
		return axYLabel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAxYLabel(RTypedExpr newAxYLabel) {
		RTypedExpr oldAxYLabel = axYLabel;
		axYLabel = newAxYLabel;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__AX_YLABEL, oldAxYLabel, axYLabel));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TextStyle getAxXLabelStyle() {
		return axXLabelStyle;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetAxXLabelStyle(TextStyle newAxXLabelStyle, NotificationChain msgs) {
		TextStyle oldAxXLabelStyle = axXLabelStyle;
		axXLabelStyle = newAxXLabelStyle;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__AX_XLABEL_STYLE, oldAxXLabelStyle, newAxXLabelStyle);
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
	public void setAxXLabelStyle(TextStyle newAxXLabelStyle) {
		if (newAxXLabelStyle != axXLabelStyle) {
			NotificationChain msgs = null;
			if (axXLabelStyle != null) {
				msgs = ((InternalEObject)axXLabelStyle).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GG_PLOT__AX_XLABEL_STYLE, null, msgs);
			}
			if (newAxXLabelStyle != null) {
				msgs = ((InternalEObject)newAxXLabelStyle).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GG_PLOT__AX_XLABEL_STYLE, null, msgs);
			}
			msgs = basicSetAxXLabelStyle(newAxXLabelStyle, msgs);
			if (msgs != null) {
				msgs.dispatch();
			}
		}
		else if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__AX_XLABEL_STYLE, newAxXLabelStyle, newAxXLabelStyle));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TextStyle getAxYLabelStyle() {
		return axYLabelStyle;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetAxYLabelStyle(TextStyle newAxYLabelStyle, NotificationChain msgs) {
		TextStyle oldAxYLabelStyle = axYLabelStyle;
		axYLabelStyle = newAxYLabelStyle;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__AX_YLABEL_STYLE, oldAxYLabelStyle, newAxYLabelStyle);
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
	public void setAxYLabelStyle(TextStyle newAxYLabelStyle) {
		if (newAxYLabelStyle != axYLabelStyle) {
			NotificationChain msgs = null;
			if (axYLabelStyle != null) {
				msgs = ((InternalEObject)axYLabelStyle).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GG_PLOT__AX_YLABEL_STYLE, null, msgs);
			}
			if (newAxYLabelStyle != null) {
				msgs = ((InternalEObject)newAxYLabelStyle).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GG_PLOT__AX_YLABEL_STYLE, null, msgs);
			}
			msgs = basicSetAxYLabelStyle(newAxYLabelStyle, msgs);
			if (msgs != null) {
				msgs.dispatch();
			}
		}
		else if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__AX_YLABEL_STYLE, newAxYLabelStyle, newAxYLabelStyle));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TextStyle getAxXTextStyle() {
		return axXTextStyle;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetAxXTextStyle(TextStyle newAxXTextStyle, NotificationChain msgs) {
		TextStyle oldAxXTextStyle = axXTextStyle;
		axXTextStyle = newAxXTextStyle;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__AX_XTEXT_STYLE, oldAxXTextStyle, newAxXTextStyle);
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
	public void setAxXTextStyle(TextStyle newAxXTextStyle) {
		if (newAxXTextStyle != axXTextStyle) {
			NotificationChain msgs = null;
			if (axXTextStyle != null) {
				msgs = ((InternalEObject)axXTextStyle).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GG_PLOT__AX_XTEXT_STYLE, null, msgs);
			}
			if (newAxXTextStyle != null) {
				msgs = ((InternalEObject)newAxXTextStyle).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GG_PLOT__AX_XTEXT_STYLE, null, msgs);
			}
			msgs = basicSetAxXTextStyle(newAxXTextStyle, msgs);
			if (msgs != null) {
				msgs.dispatch();
			}
		}
		else if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__AX_XTEXT_STYLE, newAxXTextStyle, newAxXTextStyle));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TextStyle getAxYTextStyle() {
		return axYTextStyle;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetAxYTextStyle(TextStyle newAxYTextStyle, NotificationChain msgs) {
		TextStyle oldAxYTextStyle = axYTextStyle;
		axYTextStyle = newAxYTextStyle;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__AX_YTEXT_STYLE, oldAxYTextStyle, newAxYTextStyle);
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
	public void setAxYTextStyle(TextStyle newAxYTextStyle) {
		if (newAxYTextStyle != axYTextStyle) {
			NotificationChain msgs = null;
			if (axYTextStyle != null) {
				msgs = ((InternalEObject)axYTextStyle).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GG_PLOT__AX_YTEXT_STYLE, null, msgs);
			}
			if (newAxYTextStyle != null) {
				msgs = ((InternalEObject)newAxYTextStyle).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GGPlotPackage.GG_PLOT__AX_YTEXT_STYLE, null, msgs);
			}
			msgs = basicSetAxYTextStyle(newAxYTextStyle, msgs);
			if (msgs != null) {
				msgs.dispatch();
			}
		}
		else if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, GGPlotPackage.GG_PLOT__AX_YTEXT_STYLE, newAxYTextStyle, newAxYTextStyle));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Layer> getLayers() {
		if (layers == null) {
			layers = new EObjectContainmentEList<Layer>(Layer.class, this, GGPlotPackage.GG_PLOT__LAYERS);
		}
		return layers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case GGPlotPackage.GG_PLOT__MAIN_TITLE_STYLE:
				return basicSetMainTitleStyle(null, msgs);
			case GGPlotPackage.GG_PLOT__FACET:
				return basicSetFacet(null, msgs);
			case GGPlotPackage.GG_PLOT__AX_XLABEL_STYLE:
				return basicSetAxXLabelStyle(null, msgs);
			case GGPlotPackage.GG_PLOT__AX_YLABEL_STYLE:
				return basicSetAxYLabelStyle(null, msgs);
			case GGPlotPackage.GG_PLOT__AX_XTEXT_STYLE:
				return basicSetAxXTextStyle(null, msgs);
			case GGPlotPackage.GG_PLOT__AX_YTEXT_STYLE:
				return basicSetAxYTextStyle(null, msgs);
			case GGPlotPackage.GG_PLOT__LAYERS:
				return ((InternalEList<?>)getLayers()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case GGPlotPackage.GG_PLOT__DATA:
				return getData();
			case GGPlotPackage.GG_PLOT__XVAR:
				return getXVar();
			case GGPlotPackage.GG_PLOT__YVAR:
				return getYVar();
			case GGPlotPackage.GG_PLOT__DATA_FILTER:
				return getDataFilter();
			case GGPlotPackage.GG_PLOT__MAIN_TITLE:
				return getMainTitle();
			case GGPlotPackage.GG_PLOT__MAIN_TITLE_STYLE:
				return getMainTitleStyle();
			case GGPlotPackage.GG_PLOT__FACET:
				return getFacet();
			case GGPlotPackage.GG_PLOT__AX_XLIM:
				return getAxXLim();
			case GGPlotPackage.GG_PLOT__AX_YLIM:
				return getAxYLim();
			case GGPlotPackage.GG_PLOT__AX_XLABEL:
				return getAxXLabel();
			case GGPlotPackage.GG_PLOT__AX_YLABEL:
				return getAxYLabel();
			case GGPlotPackage.GG_PLOT__AX_XLABEL_STYLE:
				return getAxXLabelStyle();
			case GGPlotPackage.GG_PLOT__AX_YLABEL_STYLE:
				return getAxYLabelStyle();
			case GGPlotPackage.GG_PLOT__AX_XTEXT_STYLE:
				return getAxXTextStyle();
			case GGPlotPackage.GG_PLOT__AX_YTEXT_STYLE:
				return getAxYTextStyle();
			case GGPlotPackage.GG_PLOT__LAYERS:
				return getLayers();
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
			case GGPlotPackage.GG_PLOT__DATA:
				setData((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GG_PLOT__XVAR:
				setXVar((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GG_PLOT__YVAR:
				setYVar((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GG_PLOT__DATA_FILTER:
				setDataFilter((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GG_PLOT__MAIN_TITLE:
				setMainTitle((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GG_PLOT__MAIN_TITLE_STYLE:
				setMainTitleStyle((TextStyle)newValue);
				return;
			case GGPlotPackage.GG_PLOT__FACET:
				setFacet((FacetLayout)newValue);
				return;
			case GGPlotPackage.GG_PLOT__AX_XLIM:
				setAxXLim((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GG_PLOT__AX_YLIM:
				setAxYLim((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GG_PLOT__AX_XLABEL:
				setAxXLabel((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GG_PLOT__AX_YLABEL:
				setAxYLabel((RTypedExpr)newValue);
				return;
			case GGPlotPackage.GG_PLOT__AX_XLABEL_STYLE:
				setAxXLabelStyle((TextStyle)newValue);
				return;
			case GGPlotPackage.GG_PLOT__AX_YLABEL_STYLE:
				setAxYLabelStyle((TextStyle)newValue);
				return;
			case GGPlotPackage.GG_PLOT__AX_XTEXT_STYLE:
				setAxXTextStyle((TextStyle)newValue);
				return;
			case GGPlotPackage.GG_PLOT__AX_YTEXT_STYLE:
				setAxYTextStyle((TextStyle)newValue);
				return;
			case GGPlotPackage.GG_PLOT__LAYERS:
				getLayers().clear();
				getLayers().addAll((Collection<? extends Layer>)newValue);
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
			case GGPlotPackage.GG_PLOT__DATA:
				setData(DATA_EDEFAULT);
				return;
			case GGPlotPackage.GG_PLOT__XVAR:
				setXVar(XVAR_EDEFAULT);
				return;
			case GGPlotPackage.GG_PLOT__YVAR:
				setYVar(YVAR_EDEFAULT);
				return;
			case GGPlotPackage.GG_PLOT__DATA_FILTER:
				setDataFilter(DATA_FILTER_EDEFAULT);
				return;
			case GGPlotPackage.GG_PLOT__MAIN_TITLE:
				setMainTitle(MAIN_TITLE_EDEFAULT);
				return;
			case GGPlotPackage.GG_PLOT__MAIN_TITLE_STYLE:
				setMainTitleStyle((TextStyle)null);
				return;
			case GGPlotPackage.GG_PLOT__FACET:
				setFacet((FacetLayout)null);
				return;
			case GGPlotPackage.GG_PLOT__AX_XLIM:
				setAxXLim(AX_XLIM_EDEFAULT);
				return;
			case GGPlotPackage.GG_PLOT__AX_YLIM:
				setAxYLim(AX_YLIM_EDEFAULT);
				return;
			case GGPlotPackage.GG_PLOT__AX_XLABEL:
				setAxXLabel(AX_XLABEL_EDEFAULT);
				return;
			case GGPlotPackage.GG_PLOT__AX_YLABEL:
				setAxYLabel(AX_YLABEL_EDEFAULT);
				return;
			case GGPlotPackage.GG_PLOT__AX_XLABEL_STYLE:
				setAxXLabelStyle((TextStyle)null);
				return;
			case GGPlotPackage.GG_PLOT__AX_YLABEL_STYLE:
				setAxYLabelStyle((TextStyle)null);
				return;
			case GGPlotPackage.GG_PLOT__AX_XTEXT_STYLE:
				setAxXTextStyle((TextStyle)null);
				return;
			case GGPlotPackage.GG_PLOT__AX_YTEXT_STYLE:
				setAxYTextStyle((TextStyle)null);
				return;
			case GGPlotPackage.GG_PLOT__LAYERS:
				getLayers().clear();
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
			case GGPlotPackage.GG_PLOT__DATA:
				return DATA_EDEFAULT == null ? data != null : !DATA_EDEFAULT.equals(data);
			case GGPlotPackage.GG_PLOT__XVAR:
				return XVAR_EDEFAULT == null ? xVar != null : !XVAR_EDEFAULT.equals(xVar);
			case GGPlotPackage.GG_PLOT__YVAR:
				return YVAR_EDEFAULT == null ? yVar != null : !YVAR_EDEFAULT.equals(yVar);
			case GGPlotPackage.GG_PLOT__DATA_FILTER:
				return DATA_FILTER_EDEFAULT == null ? dataFilter != null : !DATA_FILTER_EDEFAULT.equals(dataFilter);
			case GGPlotPackage.GG_PLOT__MAIN_TITLE:
				return MAIN_TITLE_EDEFAULT == null ? mainTitle != null : !MAIN_TITLE_EDEFAULT.equals(mainTitle);
			case GGPlotPackage.GG_PLOT__MAIN_TITLE_STYLE:
				return mainTitleStyle != null;
			case GGPlotPackage.GG_PLOT__FACET:
				return facet != null;
			case GGPlotPackage.GG_PLOT__AX_XLIM:
				return AX_XLIM_EDEFAULT == null ? axXLim != null : !AX_XLIM_EDEFAULT.equals(axXLim);
			case GGPlotPackage.GG_PLOT__AX_YLIM:
				return AX_YLIM_EDEFAULT == null ? axYLim != null : !AX_YLIM_EDEFAULT.equals(axYLim);
			case GGPlotPackage.GG_PLOT__AX_XLABEL:
				return AX_XLABEL_EDEFAULT == null ? axXLabel != null : !AX_XLABEL_EDEFAULT.equals(axXLabel);
			case GGPlotPackage.GG_PLOT__AX_YLABEL:
				return AX_YLABEL_EDEFAULT == null ? axYLabel != null : !AX_YLABEL_EDEFAULT.equals(axYLabel);
			case GGPlotPackage.GG_PLOT__AX_XLABEL_STYLE:
				return axXLabelStyle != null;
			case GGPlotPackage.GG_PLOT__AX_YLABEL_STYLE:
				return axYLabelStyle != null;
			case GGPlotPackage.GG_PLOT__AX_XTEXT_STYLE:
				return axXTextStyle != null;
			case GGPlotPackage.GG_PLOT__AX_YTEXT_STYLE:
				return axYTextStyle != null;
			case GGPlotPackage.GG_PLOT__LAYERS:
				return layers != null && !layers.isEmpty();
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
				case GGPlotPackage.GG_PLOT__XVAR: return GGPlotPackage.PROP_XVAR_PROVIDER__XVAR;
				default: return -1;
			}
		}
		if (baseClass == PropYVarProvider.class) {
			switch (derivedFeatureID) {
				case GGPlotPackage.GG_PLOT__YVAR: return GGPlotPackage.PROP_YVAR_PROVIDER__YVAR;
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
				case GGPlotPackage.PROP_XVAR_PROVIDER__XVAR: return GGPlotPackage.GG_PLOT__XVAR;
				default: return -1;
			}
		}
		if (baseClass == PropYVarProvider.class) {
			switch (baseFeatureID) {
				case GGPlotPackage.PROP_YVAR_PROVIDER__YVAR: return GGPlotPackage.GG_PLOT__YVAR;
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
		result.append(" (data: "); //$NON-NLS-1$
		result.append(data);
		result.append(", xVar: "); //$NON-NLS-1$
		result.append(xVar);
		result.append(", yVar: "); //$NON-NLS-1$
		result.append(yVar);
		result.append(", dataFilter: "); //$NON-NLS-1$
		result.append(dataFilter);
		result.append(", mainTitle: "); //$NON-NLS-1$
		result.append(mainTitle);
		result.append(", axXLim: "); //$NON-NLS-1$
		result.append(axXLim);
		result.append(", axYLim: "); //$NON-NLS-1$
		result.append(axYLim);
		result.append(", axXLabel: "); //$NON-NLS-1$
		result.append(axXLabel);
		result.append(", axYLabel: "); //$NON-NLS-1$
		result.append(axYLabel);
		result.append(')');
		return result.toString();
	}

} //GGPlotImpl
