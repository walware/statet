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
package de.walware.statet.rtm.ggplot;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see de.walware.statet.rtm.ggplot.GGPlotFactory
 * @model kind="package"
 * @generated
 */
public interface GGPlotPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "ggplot"; //$NON-NLS-1$

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://walware.de/rtm/Rt-ggplot/1.0"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "ggplot"; //$NON-NLS-1$

	/**
	 * The package content type ID.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eCONTENT_TYPE = "de.walware.statet.rtm.contentTypes.GGPlot"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	GGPlotPackage eINSTANCE = de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl.init();

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.PropDataProvider <em>Prop Data Provider</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.PropDataProvider
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropDataProvider()
	 * @generated
	 */
	int PROP_DATA_PROVIDER = 21;

	/**
	 * The feature id for the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_DATA_PROVIDER__DATA = 0;

	/**
	 * The number of structural features of the '<em>Prop Data Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_DATA_PROVIDER_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Prop Data Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_DATA_PROVIDER_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.GGPlotImpl <em>GG Plot</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGGPlot()
	 * @generated
	 */
	int GG_PLOT = 0;

	/**
	 * The feature id for the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GG_PLOT__DATA = PROP_DATA_PROVIDER__DATA;

	/**
	 * The feature id for the '<em><b>XVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GG_PLOT__XVAR = PROP_DATA_PROVIDER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>YVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GG_PLOT__YVAR = PROP_DATA_PROVIDER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Data Filter</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GG_PLOT__DATA_FILTER = PROP_DATA_PROVIDER_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Main Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GG_PLOT__MAIN_TITLE = PROP_DATA_PROVIDER_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Main Title Style</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GG_PLOT__MAIN_TITLE_STYLE = PROP_DATA_PROVIDER_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Facet</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GG_PLOT__FACET = PROP_DATA_PROVIDER_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Ax XLim</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GG_PLOT__AX_XLIM = PROP_DATA_PROVIDER_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Ax YLim</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GG_PLOT__AX_YLIM = PROP_DATA_PROVIDER_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Ax XLabel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GG_PLOT__AX_XLABEL = PROP_DATA_PROVIDER_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Ax YLabel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GG_PLOT__AX_YLABEL = PROP_DATA_PROVIDER_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Ax XLabel Style</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GG_PLOT__AX_XLABEL_STYLE = PROP_DATA_PROVIDER_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Ax YLabel Style</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GG_PLOT__AX_YLABEL_STYLE = PROP_DATA_PROVIDER_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Ax XText Style</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GG_PLOT__AX_XTEXT_STYLE = PROP_DATA_PROVIDER_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Ax YText Style</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GG_PLOT__AX_YTEXT_STYLE = PROP_DATA_PROVIDER_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Layers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GG_PLOT__LAYERS = PROP_DATA_PROVIDER_FEATURE_COUNT + 14;

	/**
	 * The number of structural features of the '<em>GG Plot</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GG_PLOT_FEATURE_COUNT = PROP_DATA_PROVIDER_FEATURE_COUNT + 15;

	/**
	 * The number of operations of the '<em>GG Plot</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GG_PLOT_OPERATION_COUNT = PROP_DATA_PROVIDER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.LayerImpl <em>Layer</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.LayerImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getLayer()
	 * @generated
	 */
	int LAYER = 1;

	/**
	 * The feature id for the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LAYER__DATA = PROP_DATA_PROVIDER__DATA;

	/**
	 * The number of structural features of the '<em>Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LAYER_FEATURE_COUNT = PROP_DATA_PROVIDER_FEATURE_COUNT + 0;

	/**
	 * The number of operations of the '<em>Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LAYER_OPERATION_COUNT = PROP_DATA_PROVIDER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.XVarLayerImpl <em>XVar Layer</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.XVarLayerImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getXVarLayer()
	 * @generated
	 */
	int XVAR_LAYER = 2;

	/**
	 * The feature id for the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int XVAR_LAYER__DATA = LAYER__DATA;

	/**
	 * The feature id for the '<em><b>XVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int XVAR_LAYER__XVAR = LAYER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Group Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int XVAR_LAYER__GROUP_VAR = LAYER_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>XVar Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int XVAR_LAYER_FEATURE_COUNT = LAYER_FEATURE_COUNT + 2;

	/**
	 * The number of operations of the '<em>XVar Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int XVAR_LAYER_OPERATION_COUNT = LAYER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.XYVarLayerImpl <em>XY Var Layer</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.XYVarLayerImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getXYVarLayer()
	 * @generated
	 */
	int XY_VAR_LAYER = 3;

	/**
	 * The feature id for the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int XY_VAR_LAYER__DATA = LAYER__DATA;

	/**
	 * The feature id for the '<em><b>XVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int XY_VAR_LAYER__XVAR = LAYER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>YVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int XY_VAR_LAYER__YVAR = LAYER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Group Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int XY_VAR_LAYER__GROUP_VAR = LAYER_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>XY Var Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int XY_VAR_LAYER_FEATURE_COUNT = LAYER_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>XY Var Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int XY_VAR_LAYER_OPERATION_COUNT = LAYER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.GeomPointLayerImpl <em>Geom Point Layer</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.GeomPointLayerImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomPointLayer()
	 * @generated
	 */
	int GEOM_POINT_LAYER = 9;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.GeomBarLayerImpl <em>Geom Bar Layer</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.GeomBarLayerImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomBarLayer()
	 * @generated
	 */
	int GEOM_BAR_LAYER = 5;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.GeomTextLayerImpl <em>Geom Text Layer</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.GeomTextLayerImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomTextLayer()
	 * @generated
	 */
	int GEOM_TEXT_LAYER = 10;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.GeomSmoothLayerImpl <em>Geom Smooth Layer</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.GeomSmoothLayerImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomSmoothLayer()
	 * @generated
	 */
	int GEOM_SMOOTH_LAYER = 11;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.GeomViolinLayerImpl <em>Geom Violin Layer</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.GeomViolinLayerImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomViolinLayer()
	 * @generated
	 */
	int GEOM_VIOLIN_LAYER = 13;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.GeomBoxplotLayerImpl <em>Geom Boxplot Layer</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.GeomBoxplotLayerImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomBoxplotLayer()
	 * @generated
	 */
	int GEOM_BOXPLOT_LAYER = 6;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.GeomLineLayerImpl <em>Geom Line Layer</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.GeomLineLayerImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomLineLayer()
	 * @generated
	 */
	int GEOM_LINE_LAYER = 8;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.GeomAblineLayerImpl <em>Geom Abline Layer</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.GeomAblineLayerImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomAblineLayer()
	 * @generated
	 */
	int GEOM_ABLINE_LAYER = 4;

	/**
	 * The feature id for the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_ABLINE_LAYER__DATA = LAYER__DATA;

	/**
	 * The feature id for the '<em><b>Line Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_ABLINE_LAYER__LINE_TYPE = LAYER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_ABLINE_LAYER__SIZE = LAYER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Color</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_ABLINE_LAYER__COLOR = LAYER_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Alpha</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_ABLINE_LAYER__ALPHA = LAYER_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Intercept Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_ABLINE_LAYER__INTERCEPT_VAR = LAYER_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Slope Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_ABLINE_LAYER__SLOPE_VAR = LAYER_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>Geom Abline Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_ABLINE_LAYER_FEATURE_COUNT = LAYER_FEATURE_COUNT + 6;

	/**
	 * The number of operations of the '<em>Geom Abline Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_ABLINE_LAYER_OPERATION_COUNT = LAYER_OPERATION_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_BAR_LAYER__DATA = XY_VAR_LAYER__DATA;

	/**
	 * The feature id for the '<em><b>XVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_BAR_LAYER__XVAR = XY_VAR_LAYER__XVAR;

	/**
	 * The feature id for the '<em><b>YVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_BAR_LAYER__YVAR = XY_VAR_LAYER__YVAR;

	/**
	 * The feature id for the '<em><b>Group Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_BAR_LAYER__GROUP_VAR = XY_VAR_LAYER__GROUP_VAR;

	/**
	 * The feature id for the '<em><b>Stat</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_BAR_LAYER__STAT = XY_VAR_LAYER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Color</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_BAR_LAYER__COLOR = XY_VAR_LAYER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Fill</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_BAR_LAYER__FILL = XY_VAR_LAYER_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Alpha</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_BAR_LAYER__ALPHA = XY_VAR_LAYER_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Geom Bar Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_BAR_LAYER_FEATURE_COUNT = XY_VAR_LAYER_FEATURE_COUNT + 4;

	/**
	 * The number of operations of the '<em>Geom Bar Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_BAR_LAYER_OPERATION_COUNT = XY_VAR_LAYER_OPERATION_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_BOXPLOT_LAYER__DATA = XVAR_LAYER__DATA;

	/**
	 * The feature id for the '<em><b>XVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_BOXPLOT_LAYER__XVAR = XVAR_LAYER__XVAR;

	/**
	 * The feature id for the '<em><b>Group Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_BOXPLOT_LAYER__GROUP_VAR = XVAR_LAYER__GROUP_VAR;

	/**
	 * The feature id for the '<em><b>Color</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_BOXPLOT_LAYER__COLOR = XVAR_LAYER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Fill</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_BOXPLOT_LAYER__FILL = XVAR_LAYER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Alpha</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_BOXPLOT_LAYER__ALPHA = XVAR_LAYER_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Geom Boxplot Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_BOXPLOT_LAYER_FEATURE_COUNT = XVAR_LAYER_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Geom Boxplot Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_BOXPLOT_LAYER_OPERATION_COUNT = XVAR_LAYER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.GeomHistogramLayerImpl <em>Geom Histogram Layer</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.GeomHistogramLayerImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomHistogramLayer()
	 * @generated
	 */
	int GEOM_HISTOGRAM_LAYER = 7;

	/**
	 * The feature id for the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_HISTOGRAM_LAYER__DATA = XVAR_LAYER__DATA;

	/**
	 * The feature id for the '<em><b>XVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_HISTOGRAM_LAYER__XVAR = XVAR_LAYER__XVAR;

	/**
	 * The feature id for the '<em><b>Group Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_HISTOGRAM_LAYER__GROUP_VAR = XVAR_LAYER__GROUP_VAR;

	/**
	 * The feature id for the '<em><b>Color</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_HISTOGRAM_LAYER__COLOR = XVAR_LAYER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Fill</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_HISTOGRAM_LAYER__FILL = XVAR_LAYER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Alpha</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_HISTOGRAM_LAYER__ALPHA = XVAR_LAYER_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Geom Histogram Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_HISTOGRAM_LAYER_FEATURE_COUNT = XVAR_LAYER_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Geom Histogram Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_HISTOGRAM_LAYER_OPERATION_COUNT = XVAR_LAYER_OPERATION_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_LINE_LAYER__DATA = XY_VAR_LAYER__DATA;

	/**
	 * The feature id for the '<em><b>XVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_LINE_LAYER__XVAR = XY_VAR_LAYER__XVAR;

	/**
	 * The feature id for the '<em><b>YVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_LINE_LAYER__YVAR = XY_VAR_LAYER__YVAR;

	/**
	 * The feature id for the '<em><b>Group Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_LINE_LAYER__GROUP_VAR = XY_VAR_LAYER__GROUP_VAR;

	/**
	 * The feature id for the '<em><b>Stat</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_LINE_LAYER__STAT = XY_VAR_LAYER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Line Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_LINE_LAYER__LINE_TYPE = XY_VAR_LAYER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_LINE_LAYER__SIZE = XY_VAR_LAYER_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Color</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_LINE_LAYER__COLOR = XY_VAR_LAYER_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Alpha</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_LINE_LAYER__ALPHA = XY_VAR_LAYER_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>Geom Line Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_LINE_LAYER_FEATURE_COUNT = XY_VAR_LAYER_FEATURE_COUNT + 5;

	/**
	 * The number of operations of the '<em>Geom Line Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_LINE_LAYER_OPERATION_COUNT = XY_VAR_LAYER_OPERATION_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_POINT_LAYER__DATA = XY_VAR_LAYER__DATA;

	/**
	 * The feature id for the '<em><b>XVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_POINT_LAYER__XVAR = XY_VAR_LAYER__XVAR;

	/**
	 * The feature id for the '<em><b>YVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_POINT_LAYER__YVAR = XY_VAR_LAYER__YVAR;

	/**
	 * The feature id for the '<em><b>Group Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_POINT_LAYER__GROUP_VAR = XY_VAR_LAYER__GROUP_VAR;

	/**
	 * The feature id for the '<em><b>Shape</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_POINT_LAYER__SHAPE = XY_VAR_LAYER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_POINT_LAYER__SIZE = XY_VAR_LAYER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Color</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_POINT_LAYER__COLOR = XY_VAR_LAYER_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Fill</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_POINT_LAYER__FILL = XY_VAR_LAYER_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Alpha</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_POINT_LAYER__ALPHA = XY_VAR_LAYER_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Position XJitter</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_POINT_LAYER__POSITION_XJITTER = XY_VAR_LAYER_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Position YJitter</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_POINT_LAYER__POSITION_YJITTER = XY_VAR_LAYER_FEATURE_COUNT + 6;

	/**
	 * The number of structural features of the '<em>Geom Point Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_POINT_LAYER_FEATURE_COUNT = XY_VAR_LAYER_FEATURE_COUNT + 7;

	/**
	 * The number of operations of the '<em>Geom Point Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_POINT_LAYER_OPERATION_COUNT = XY_VAR_LAYER_OPERATION_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TEXT_LAYER__DATA = XY_VAR_LAYER__DATA;

	/**
	 * The feature id for the '<em><b>XVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TEXT_LAYER__XVAR = XY_VAR_LAYER__XVAR;

	/**
	 * The feature id for the '<em><b>YVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TEXT_LAYER__YVAR = XY_VAR_LAYER__YVAR;

	/**
	 * The feature id for the '<em><b>Group Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TEXT_LAYER__GROUP_VAR = XY_VAR_LAYER__GROUP_VAR;

	/**
	 * The feature id for the '<em><b>Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TEXT_LAYER__SIZE = XY_VAR_LAYER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Color</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TEXT_LAYER__COLOR = XY_VAR_LAYER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Font Family</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TEXT_LAYER__FONT_FAMILY = XY_VAR_LAYER_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Font Face</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TEXT_LAYER__FONT_FACE = XY_VAR_LAYER_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>HJust</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TEXT_LAYER__HJUST = XY_VAR_LAYER_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>VJust</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TEXT_LAYER__VJUST = XY_VAR_LAYER_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Angle</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TEXT_LAYER__ANGLE = XY_VAR_LAYER_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Alpha</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TEXT_LAYER__ALPHA = XY_VAR_LAYER_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TEXT_LAYER__LABEL = XY_VAR_LAYER_FEATURE_COUNT + 8;

	/**
	 * The number of structural features of the '<em>Geom Text Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TEXT_LAYER_FEATURE_COUNT = XY_VAR_LAYER_FEATURE_COUNT + 9;

	/**
	 * The number of operations of the '<em>Geom Text Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TEXT_LAYER_OPERATION_COUNT = XY_VAR_LAYER_OPERATION_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_SMOOTH_LAYER__DATA = XY_VAR_LAYER__DATA;

	/**
	 * The feature id for the '<em><b>XVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_SMOOTH_LAYER__XVAR = XY_VAR_LAYER__XVAR;

	/**
	 * The feature id for the '<em><b>YVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_SMOOTH_LAYER__YVAR = XY_VAR_LAYER__YVAR;

	/**
	 * The feature id for the '<em><b>Group Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_SMOOTH_LAYER__GROUP_VAR = XY_VAR_LAYER__GROUP_VAR;

	/**
	 * The feature id for the '<em><b>Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_SMOOTH_LAYER__SIZE = XY_VAR_LAYER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Color</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_SMOOTH_LAYER__COLOR = XY_VAR_LAYER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Fill</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_SMOOTH_LAYER__FILL = XY_VAR_LAYER_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Alpha</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_SMOOTH_LAYER__ALPHA = XY_VAR_LAYER_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Geom Smooth Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_SMOOTH_LAYER_FEATURE_COUNT = XY_VAR_LAYER_FEATURE_COUNT + 4;

	/**
	 * The number of operations of the '<em>Geom Smooth Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_SMOOTH_LAYER_OPERATION_COUNT = XY_VAR_LAYER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.GeomTileLayerImpl <em>Geom Tile Layer</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.GeomTileLayerImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomTileLayer()
	 * @generated
	 */
	int GEOM_TILE_LAYER = 12;

	/**
	 * The feature id for the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TILE_LAYER__DATA = XY_VAR_LAYER__DATA;

	/**
	 * The feature id for the '<em><b>XVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TILE_LAYER__XVAR = XY_VAR_LAYER__XVAR;

	/**
	 * The feature id for the '<em><b>YVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TILE_LAYER__YVAR = XY_VAR_LAYER__YVAR;

	/**
	 * The feature id for the '<em><b>Group Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TILE_LAYER__GROUP_VAR = XY_VAR_LAYER__GROUP_VAR;

	/**
	 * The feature id for the '<em><b>Line Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TILE_LAYER__LINE_TYPE = XY_VAR_LAYER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Color</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TILE_LAYER__COLOR = XY_VAR_LAYER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Fill</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TILE_LAYER__FILL = XY_VAR_LAYER_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Alpha</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TILE_LAYER__ALPHA = XY_VAR_LAYER_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Geom Tile Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TILE_LAYER_FEATURE_COUNT = XY_VAR_LAYER_FEATURE_COUNT + 4;

	/**
	 * The number of operations of the '<em>Geom Tile Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_TILE_LAYER_OPERATION_COUNT = XY_VAR_LAYER_OPERATION_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_VIOLIN_LAYER__DATA = XY_VAR_LAYER__DATA;

	/**
	 * The feature id for the '<em><b>XVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_VIOLIN_LAYER__XVAR = XY_VAR_LAYER__XVAR;

	/**
	 * The feature id for the '<em><b>YVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_VIOLIN_LAYER__YVAR = XY_VAR_LAYER__YVAR;

	/**
	 * The feature id for the '<em><b>Group Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_VIOLIN_LAYER__GROUP_VAR = XY_VAR_LAYER__GROUP_VAR;

	/**
	 * The feature id for the '<em><b>Line Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_VIOLIN_LAYER__LINE_TYPE = XY_VAR_LAYER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Color</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_VIOLIN_LAYER__COLOR = XY_VAR_LAYER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Fill</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_VIOLIN_LAYER__FILL = XY_VAR_LAYER_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Alpha</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_VIOLIN_LAYER__ALPHA = XY_VAR_LAYER_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Geom Violin Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_VIOLIN_LAYER_FEATURE_COUNT = XY_VAR_LAYER_FEATURE_COUNT + 4;

	/**
	 * The number of operations of the '<em>Geom Violin Layer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEOM_VIOLIN_LAYER_OPERATION_COUNT = XY_VAR_LAYER_OPERATION_COUNT + 0;


	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.FacetLayout <em>Facet Layout</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.FacetLayout
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getFacetLayout()
	 * @generated
	 */
	int FACET_LAYOUT = 14;

	/**
	 * The number of structural features of the '<em>Facet Layout</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FACET_LAYOUT_FEATURE_COUNT = 0;

	/**
	 * The number of operations of the '<em>Facet Layout</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FACET_LAYOUT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.GridFacetLayoutImpl <em>Grid Facet Layout</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.GridFacetLayoutImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGridFacetLayout()
	 * @generated
	 */
	int GRID_FACET_LAYOUT = 15;

	/**
	 * The feature id for the '<em><b>Col Vars</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRID_FACET_LAYOUT__COL_VARS = FACET_LAYOUT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Row Vars</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRID_FACET_LAYOUT__ROW_VARS = FACET_LAYOUT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Grid Facet Layout</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRID_FACET_LAYOUT_FEATURE_COUNT = FACET_LAYOUT_FEATURE_COUNT + 2;

	/**
	 * The number of operations of the '<em>Grid Facet Layout</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GRID_FACET_LAYOUT_OPERATION_COUNT = FACET_LAYOUT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.WrapFacetLayoutImpl <em>Wrap Facet Layout</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.WrapFacetLayoutImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getWrapFacetLayout()
	 * @generated
	 */
	int WRAP_FACET_LAYOUT = 16;

	/**
	 * The feature id for the '<em><b>Col Vars</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRAP_FACET_LAYOUT__COL_VARS = FACET_LAYOUT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Col Num</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRAP_FACET_LAYOUT__COL_NUM = FACET_LAYOUT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Wrap Facet Layout</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRAP_FACET_LAYOUT_FEATURE_COUNT = FACET_LAYOUT_FEATURE_COUNT + 2;

	/**
	 * The number of operations of the '<em>Wrap Facet Layout</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRAP_FACET_LAYOUT_OPERATION_COUNT = FACET_LAYOUT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.PropColorProvider <em>Prop Color Provider</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.PropColorProvider
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropColorProvider()
	 * @generated
	 */
	int PROP_COLOR_PROVIDER = 29;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.TextStyleImpl <em>Text Style</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.TextStyleImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getTextStyle()
	 * @generated
	 */
	int TEXT_STYLE = 20;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.StatImpl <em>Stat</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.StatImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getStat()
	 * @generated
	 */
	int STAT = 17;

	/**
	 * The number of structural features of the '<em>Stat</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAT_FEATURE_COUNT = 0;

	/**
	 * The number of operations of the '<em>Stat</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.IdentityStatImpl <em>Identity Stat</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.IdentityStatImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getIdentityStat()
	 * @generated
	 */
	int IDENTITY_STAT = 18;

	/**
	 * The number of structural features of the '<em>Identity Stat</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTITY_STAT_FEATURE_COUNT = STAT_FEATURE_COUNT + 0;

	/**
	 * The number of operations of the '<em>Identity Stat</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTITY_STAT_OPERATION_COUNT = STAT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.impl.SummaryStatImpl <em>Summary Stat</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.impl.SummaryStatImpl
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getSummaryStat()
	 * @generated
	 */
	int SUMMARY_STAT = 19;

	/**
	 * The feature id for the '<em><b>YFun</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SUMMARY_STAT__YFUN = STAT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Summary Stat</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SUMMARY_STAT_FEATURE_COUNT = STAT_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Summary Stat</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SUMMARY_STAT_OPERATION_COUNT = STAT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.PropXVarProvider <em>Prop XVar Provider</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.PropXVarProvider
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropXVarProvider()
	 * @generated
	 */
	int PROP_XVAR_PROVIDER = 22;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.PropYVarProvider <em>Prop YVar Provider</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.PropYVarProvider
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropYVarProvider()
	 * @generated
	 */
	int PROP_YVAR_PROVIDER = 23;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.PropStatProvider <em>Prop Stat Provider</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.PropStatProvider
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropStatProvider()
	 * @generated
	 */
	int PROP_STAT_PROVIDER = 24;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.PropGroupVarProvider <em>Prop Group Var Provider</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.PropGroupVarProvider
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropGroupVarProvider()
	 * @generated
	 */
	int PROP_GROUP_VAR_PROVIDER = 25;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.PropFillProvider <em>Prop Fill Provider</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.PropFillProvider
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropFillProvider()
	 * @generated
	 */
	int PROP_FILL_PROVIDER = 30;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.PropShapeProvider <em>Prop Shape Provider</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.PropShapeProvider
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropShapeProvider()
	 * @generated
	 */
	int PROP_SHAPE_PROVIDER = 26;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.PropLineTypeProvider <em>Prop Line Type Provider</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.PropLineTypeProvider
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropLineTypeProvider()
	 * @generated
	 */
	int PROP_LINE_TYPE_PROVIDER = 27;

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.PropSizeProvider <em>Prop Size Provider</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.PropSizeProvider
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropSizeProvider()
	 * @generated
	 */
	int PROP_SIZE_PROVIDER = 28;

	/**
	 * The feature id for the '<em><b>Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_SIZE_PROVIDER__SIZE = 0;

	/**
	 * The number of structural features of the '<em>Prop Size Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_SIZE_PROVIDER_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Prop Size Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_SIZE_PROVIDER_OPERATION_COUNT = 0;

	/**
	 * The feature id for the '<em><b>Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_STYLE__SIZE = PROP_SIZE_PROVIDER__SIZE;

	/**
	 * The feature id for the '<em><b>Color</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_STYLE__COLOR = PROP_SIZE_PROVIDER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Font Family</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_STYLE__FONT_FAMILY = PROP_SIZE_PROVIDER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Font Face</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_STYLE__FONT_FACE = PROP_SIZE_PROVIDER_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>HJust</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_STYLE__HJUST = PROP_SIZE_PROVIDER_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>VJust</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_STYLE__VJUST = PROP_SIZE_PROVIDER_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Angle</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_STYLE__ANGLE = PROP_SIZE_PROVIDER_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>Text Style</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_STYLE_FEATURE_COUNT = PROP_SIZE_PROVIDER_FEATURE_COUNT + 6;

	/**
	 * The number of operations of the '<em>Text Style</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_STYLE_OPERATION_COUNT = PROP_SIZE_PROVIDER_OPERATION_COUNT + 0;

	/**
	 * The feature id for the '<em><b>XVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_XVAR_PROVIDER__XVAR = 0;

	/**
	 * The number of structural features of the '<em>Prop XVar Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_XVAR_PROVIDER_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Prop XVar Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_XVAR_PROVIDER_OPERATION_COUNT = 0;

	/**
	 * The feature id for the '<em><b>YVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_YVAR_PROVIDER__YVAR = 0;

	/**
	 * The number of structural features of the '<em>Prop YVar Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_YVAR_PROVIDER_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Prop YVar Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_YVAR_PROVIDER_OPERATION_COUNT = 0;

	/**
	 * The feature id for the '<em><b>Stat</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_STAT_PROVIDER__STAT = 0;

	/**
	 * The number of structural features of the '<em>Prop Stat Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_STAT_PROVIDER_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Prop Stat Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_STAT_PROVIDER_OPERATION_COUNT = 0;

	/**
	 * The feature id for the '<em><b>Group Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_GROUP_VAR_PROVIDER__GROUP_VAR = 0;

	/**
	 * The number of structural features of the '<em>Prop Group Var Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_GROUP_VAR_PROVIDER_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Prop Group Var Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_GROUP_VAR_PROVIDER_OPERATION_COUNT = 0;

	/**
	 * The feature id for the '<em><b>Shape</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_SHAPE_PROVIDER__SHAPE = 0;

	/**
	 * The number of structural features of the '<em>Prop Shape Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_SHAPE_PROVIDER_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Prop Shape Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_SHAPE_PROVIDER_OPERATION_COUNT = 0;

	/**
	 * The feature id for the '<em><b>Line Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_LINE_TYPE_PROVIDER__LINE_TYPE = 0;

	/**
	 * The number of structural features of the '<em>Prop Line Type Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_LINE_TYPE_PROVIDER_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Prop Line Type Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_LINE_TYPE_PROVIDER_OPERATION_COUNT = 0;

	/**
	 * The feature id for the '<em><b>Color</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_COLOR_PROVIDER__COLOR = 0;

	/**
	 * The number of structural features of the '<em>Prop Color Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_COLOR_PROVIDER_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Prop Color Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_COLOR_PROVIDER_OPERATION_COUNT = 0;

	/**
	 * The feature id for the '<em><b>Fill</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_FILL_PROVIDER__FILL = 0;

	/**
	 * The number of structural features of the '<em>Prop Fill Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_FILL_PROVIDER_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Prop Fill Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_FILL_PROVIDER_OPERATION_COUNT = 0;


	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ggplot.PropAlphaProvider <em>Prop Alpha Provider</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ggplot.PropAlphaProvider
	 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropAlphaProvider()
	 * @generated
	 */
	int PROP_ALPHA_PROVIDER = 31;

	/**
	 * The feature id for the '<em><b>Alpha</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_ALPHA_PROVIDER__ALPHA = 0;

	/**
	 * The number of structural features of the '<em>Prop Alpha Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_ALPHA_PROVIDER_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Prop Alpha Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROP_ALPHA_PROVIDER_OPERATION_COUNT = 0;


	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.GGPlot <em>GG Plot</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>GG Plot</em>'.
	 * @see de.walware.statet.rtm.ggplot.GGPlot
	 * @generated
	 */
	EClass getGGPlot();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.GGPlot#getDataFilter <em>Data Filter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Data Filter</em>'.
	 * @see de.walware.statet.rtm.ggplot.GGPlot#getDataFilter()
	 * @see #getGGPlot()
	 * @generated
	 */
	EAttribute getGGPlot_DataFilter();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.GGPlot#getMainTitle <em>Main Title</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Main Title</em>'.
	 * @see de.walware.statet.rtm.ggplot.GGPlot#getMainTitle()
	 * @see #getGGPlot()
	 * @generated
	 */
	EAttribute getGGPlot_MainTitle();

	/**
	 * Returns the meta object for the containment reference '{@link de.walware.statet.rtm.ggplot.GGPlot#getMainTitleStyle <em>Main Title Style</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Main Title Style</em>'.
	 * @see de.walware.statet.rtm.ggplot.GGPlot#getMainTitleStyle()
	 * @see #getGGPlot()
	 * @generated
	 */
	EReference getGGPlot_MainTitleStyle();

	/**
	 * Returns the meta object for the containment reference '{@link de.walware.statet.rtm.ggplot.GGPlot#getFacet <em>Facet</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Facet</em>'.
	 * @see de.walware.statet.rtm.ggplot.GGPlot#getFacet()
	 * @see #getGGPlot()
	 * @generated
	 */
	EReference getGGPlot_Facet();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.GGPlot#getAxXLim <em>Ax XLim</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ax XLim</em>'.
	 * @see de.walware.statet.rtm.ggplot.GGPlot#getAxXLim()
	 * @see #getGGPlot()
	 * @generated
	 */
	EAttribute getGGPlot_AxXLim();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.GGPlot#getAxYLim <em>Ax YLim</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ax YLim</em>'.
	 * @see de.walware.statet.rtm.ggplot.GGPlot#getAxYLim()
	 * @see #getGGPlot()
	 * @generated
	 */
	EAttribute getGGPlot_AxYLim();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.GGPlot#getAxXLabel <em>Ax XLabel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ax XLabel</em>'.
	 * @see de.walware.statet.rtm.ggplot.GGPlot#getAxXLabel()
	 * @see #getGGPlot()
	 * @generated
	 */
	EAttribute getGGPlot_AxXLabel();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.GGPlot#getAxYLabel <em>Ax YLabel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ax YLabel</em>'.
	 * @see de.walware.statet.rtm.ggplot.GGPlot#getAxYLabel()
	 * @see #getGGPlot()
	 * @generated
	 */
	EAttribute getGGPlot_AxYLabel();

	/**
	 * Returns the meta object for the containment reference '{@link de.walware.statet.rtm.ggplot.GGPlot#getAxXLabelStyle <em>Ax XLabel Style</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Ax XLabel Style</em>'.
	 * @see de.walware.statet.rtm.ggplot.GGPlot#getAxXLabelStyle()
	 * @see #getGGPlot()
	 * @generated
	 */
	EReference getGGPlot_AxXLabelStyle();

	/**
	 * Returns the meta object for the containment reference '{@link de.walware.statet.rtm.ggplot.GGPlot#getAxYLabelStyle <em>Ax YLabel Style</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Ax YLabel Style</em>'.
	 * @see de.walware.statet.rtm.ggplot.GGPlot#getAxYLabelStyle()
	 * @see #getGGPlot()
	 * @generated
	 */
	EReference getGGPlot_AxYLabelStyle();

	/**
	 * Returns the meta object for the containment reference '{@link de.walware.statet.rtm.ggplot.GGPlot#getAxXTextStyle <em>Ax XText Style</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Ax XText Style</em>'.
	 * @see de.walware.statet.rtm.ggplot.GGPlot#getAxXTextStyle()
	 * @see #getGGPlot()
	 * @generated
	 */
	EReference getGGPlot_AxXTextStyle();

	/**
	 * Returns the meta object for the containment reference '{@link de.walware.statet.rtm.ggplot.GGPlot#getAxYTextStyle <em>Ax YText Style</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Ax YText Style</em>'.
	 * @see de.walware.statet.rtm.ggplot.GGPlot#getAxYTextStyle()
	 * @see #getGGPlot()
	 * @generated
	 */
	EReference getGGPlot_AxYTextStyle();

	/**
	 * Returns the meta object for the containment reference list '{@link de.walware.statet.rtm.ggplot.GGPlot#getLayers <em>Layers</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Layers</em>'.
	 * @see de.walware.statet.rtm.ggplot.GGPlot#getLayers()
	 * @see #getGGPlot()
	 * @generated
	 */
	EReference getGGPlot_Layers();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.Layer <em>Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Layer</em>'.
	 * @see de.walware.statet.rtm.ggplot.Layer
	 * @generated
	 */
	EClass getLayer();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.XVarLayer <em>XVar Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>XVar Layer</em>'.
	 * @see de.walware.statet.rtm.ggplot.XVarLayer
	 * @generated
	 */
	EClass getXVarLayer();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.XYVarLayer <em>XY Var Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>XY Var Layer</em>'.
	 * @see de.walware.statet.rtm.ggplot.XYVarLayer
	 * @generated
	 */
	EClass getXYVarLayer();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.GeomPointLayer <em>Geom Point Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Geom Point Layer</em>'.
	 * @see de.walware.statet.rtm.ggplot.GeomPointLayer
	 * @generated
	 */
	EClass getGeomPointLayer();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.GeomPointLayer#getPositionXJitter <em>Position XJitter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Position XJitter</em>'.
	 * @see de.walware.statet.rtm.ggplot.GeomPointLayer#getPositionXJitter()
	 * @see #getGeomPointLayer()
	 * @generated
	 */
	EAttribute getGeomPointLayer_PositionXJitter();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.GeomPointLayer#getPositionYJitter <em>Position YJitter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Position YJitter</em>'.
	 * @see de.walware.statet.rtm.ggplot.GeomPointLayer#getPositionYJitter()
	 * @see #getGeomPointLayer()
	 * @generated
	 */
	EAttribute getGeomPointLayer_PositionYJitter();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.GeomBarLayer <em>Geom Bar Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Geom Bar Layer</em>'.
	 * @see de.walware.statet.rtm.ggplot.GeomBarLayer
	 * @generated
	 */
	EClass getGeomBarLayer();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.GeomTextLayer <em>Geom Text Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Geom Text Layer</em>'.
	 * @see de.walware.statet.rtm.ggplot.GeomTextLayer
	 * @generated
	 */
	EClass getGeomTextLayer();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.GeomTextLayer#getLabel <em>Label</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Label</em>'.
	 * @see de.walware.statet.rtm.ggplot.GeomTextLayer#getLabel()
	 * @see #getGeomTextLayer()
	 * @generated
	 */
	EAttribute getGeomTextLayer_Label();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.GeomSmoothLayer <em>Geom Smooth Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Geom Smooth Layer</em>'.
	 * @see de.walware.statet.rtm.ggplot.GeomSmoothLayer
	 * @generated
	 */
	EClass getGeomSmoothLayer();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.GeomViolinLayer <em>Geom Violin Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Geom Violin Layer</em>'.
	 * @see de.walware.statet.rtm.ggplot.GeomViolinLayer
	 * @generated
	 */
	EClass getGeomViolinLayer();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.FacetLayout <em>Facet Layout</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Facet Layout</em>'.
	 * @see de.walware.statet.rtm.ggplot.FacetLayout
	 * @generated
	 */
	EClass getFacetLayout();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.GridFacetLayout <em>Grid Facet Layout</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Grid Facet Layout</em>'.
	 * @see de.walware.statet.rtm.ggplot.GridFacetLayout
	 * @generated
	 */
	EClass getGridFacetLayout();

	/**
	 * Returns the meta object for the attribute list '{@link de.walware.statet.rtm.ggplot.GridFacetLayout#getColVars <em>Col Vars</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Col Vars</em>'.
	 * @see de.walware.statet.rtm.ggplot.GridFacetLayout#getColVars()
	 * @see #getGridFacetLayout()
	 * @generated
	 */
	EAttribute getGridFacetLayout_ColVars();

	/**
	 * Returns the meta object for the attribute list '{@link de.walware.statet.rtm.ggplot.GridFacetLayout#getRowVars <em>Row Vars</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Row Vars</em>'.
	 * @see de.walware.statet.rtm.ggplot.GridFacetLayout#getRowVars()
	 * @see #getGridFacetLayout()
	 * @generated
	 */
	EAttribute getGridFacetLayout_RowVars();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.WrapFacetLayout <em>Wrap Facet Layout</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Wrap Facet Layout</em>'.
	 * @see de.walware.statet.rtm.ggplot.WrapFacetLayout
	 * @generated
	 */
	EClass getWrapFacetLayout();

	/**
	 * Returns the meta object for the attribute list '{@link de.walware.statet.rtm.ggplot.WrapFacetLayout#getColVars <em>Col Vars</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Col Vars</em>'.
	 * @see de.walware.statet.rtm.ggplot.WrapFacetLayout#getColVars()
	 * @see #getWrapFacetLayout()
	 * @generated
	 */
	EAttribute getWrapFacetLayout_ColVars();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.WrapFacetLayout#getColNum <em>Col Num</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Col Num</em>'.
	 * @see de.walware.statet.rtm.ggplot.WrapFacetLayout#getColNum()
	 * @see #getWrapFacetLayout()
	 * @generated
	 */
	EAttribute getWrapFacetLayout_ColNum();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.TextStyle <em>Text Style</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Text Style</em>'.
	 * @see de.walware.statet.rtm.ggplot.TextStyle
	 * @generated
	 */
	EClass getTextStyle();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.TextStyle#getFontFamily <em>Font Family</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Font Family</em>'.
	 * @see de.walware.statet.rtm.ggplot.TextStyle#getFontFamily()
	 * @see #getTextStyle()
	 * @generated
	 */
	EAttribute getTextStyle_FontFamily();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.TextStyle#getFontFace <em>Font Face</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Font Face</em>'.
	 * @see de.walware.statet.rtm.ggplot.TextStyle#getFontFace()
	 * @see #getTextStyle()
	 * @generated
	 */
	EAttribute getTextStyle_FontFace();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.TextStyle#getHJust <em>HJust</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>HJust</em>'.
	 * @see de.walware.statet.rtm.ggplot.TextStyle#getHJust()
	 * @see #getTextStyle()
	 * @generated
	 */
	EAttribute getTextStyle_HJust();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.TextStyle#getVJust <em>VJust</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>VJust</em>'.
	 * @see de.walware.statet.rtm.ggplot.TextStyle#getVJust()
	 * @see #getTextStyle()
	 * @generated
	 */
	EAttribute getTextStyle_VJust();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.TextStyle#getAngle <em>Angle</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Angle</em>'.
	 * @see de.walware.statet.rtm.ggplot.TextStyle#getAngle()
	 * @see #getTextStyle()
	 * @generated
	 */
	EAttribute getTextStyle_Angle();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.Stat <em>Stat</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Stat</em>'.
	 * @see de.walware.statet.rtm.ggplot.Stat
	 * @generated
	 */
	EClass getStat();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.IdentityStat <em>Identity Stat</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Identity Stat</em>'.
	 * @see de.walware.statet.rtm.ggplot.IdentityStat
	 * @generated
	 */
	EClass getIdentityStat();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.SummaryStat <em>Summary Stat</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Summary Stat</em>'.
	 * @see de.walware.statet.rtm.ggplot.SummaryStat
	 * @generated
	 */
	EClass getSummaryStat();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.SummaryStat#getYFun <em>YFun</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>YFun</em>'.
	 * @see de.walware.statet.rtm.ggplot.SummaryStat#getYFun()
	 * @see #getSummaryStat()
	 * @generated
	 */
	EAttribute getSummaryStat_YFun();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.PropDataProvider <em>Prop Data Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Prop Data Provider</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropDataProvider
	 * @generated
	 */
	EClass getPropDataProvider();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.PropDataProvider#getData <em>Data</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Data</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropDataProvider#getData()
	 * @see #getPropDataProvider()
	 * @generated
	 */
	EAttribute getPropDataProvider_Data();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.PropXVarProvider <em>Prop XVar Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Prop XVar Provider</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropXVarProvider
	 * @generated
	 */
	EClass getPropXVarProvider();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.PropXVarProvider#getXVar <em>XVar</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>XVar</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropXVarProvider#getXVar()
	 * @see #getPropXVarProvider()
	 * @generated
	 */
	EAttribute getPropXVarProvider_XVar();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.PropYVarProvider <em>Prop YVar Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Prop YVar Provider</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropYVarProvider
	 * @generated
	 */
	EClass getPropYVarProvider();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.PropYVarProvider#getYVar <em>YVar</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>YVar</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropYVarProvider#getYVar()
	 * @see #getPropYVarProvider()
	 * @generated
	 */
	EAttribute getPropYVarProvider_YVar();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.PropStatProvider <em>Prop Stat Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Prop Stat Provider</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropStatProvider
	 * @generated
	 */
	EClass getPropStatProvider();

	/**
	 * Returns the meta object for the containment reference '{@link de.walware.statet.rtm.ggplot.PropStatProvider#getStat <em>Stat</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Stat</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropStatProvider#getStat()
	 * @see #getPropStatProvider()
	 * @generated
	 */
	EReference getPropStatProvider_Stat();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.PropGroupVarProvider <em>Prop Group Var Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Prop Group Var Provider</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropGroupVarProvider
	 * @generated
	 */
	EClass getPropGroupVarProvider();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.PropGroupVarProvider#getGroupVar <em>Group Var</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Group Var</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropGroupVarProvider#getGroupVar()
	 * @see #getPropGroupVarProvider()
	 * @generated
	 */
	EAttribute getPropGroupVarProvider_GroupVar();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.PropColorProvider <em>Prop Color Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Prop Color Provider</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropColorProvider
	 * @generated
	 */
	EClass getPropColorProvider();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.PropColorProvider#getColor <em>Color</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Color</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropColorProvider#getColor()
	 * @see #getPropColorProvider()
	 * @generated
	 */
	EAttribute getPropColorProvider_Color();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.PropFillProvider <em>Prop Fill Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Prop Fill Provider</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropFillProvider
	 * @generated
	 */
	EClass getPropFillProvider();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.PropFillProvider#getFill <em>Fill</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Fill</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropFillProvider#getFill()
	 * @see #getPropFillProvider()
	 * @generated
	 */
	EAttribute getPropFillProvider_Fill();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.PropAlphaProvider <em>Prop Alpha Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Prop Alpha Provider</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropAlphaProvider
	 * @generated
	 */
	EClass getPropAlphaProvider();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.PropAlphaProvider#getAlpha <em>Alpha</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Alpha</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropAlphaProvider#getAlpha()
	 * @see #getPropAlphaProvider()
	 * @generated
	 */
	EAttribute getPropAlphaProvider_Alpha();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.PropShapeProvider <em>Prop Shape Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Prop Shape Provider</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropShapeProvider
	 * @generated
	 */
	EClass getPropShapeProvider();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.PropShapeProvider#getShape <em>Shape</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Shape</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropShapeProvider#getShape()
	 * @see #getPropShapeProvider()
	 * @generated
	 */
	EAttribute getPropShapeProvider_Shape();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.PropLineTypeProvider <em>Prop Line Type Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Prop Line Type Provider</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropLineTypeProvider
	 * @generated
	 */
	EClass getPropLineTypeProvider();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.PropLineTypeProvider#getLineType <em>Line Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Line Type</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropLineTypeProvider#getLineType()
	 * @see #getPropLineTypeProvider()
	 * @generated
	 */
	EAttribute getPropLineTypeProvider_LineType();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.PropSizeProvider <em>Prop Size Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Prop Size Provider</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropSizeProvider
	 * @generated
	 */
	EClass getPropSizeProvider();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.PropSizeProvider#getSize <em>Size</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Size</em>'.
	 * @see de.walware.statet.rtm.ggplot.PropSizeProvider#getSize()
	 * @see #getPropSizeProvider()
	 * @generated
	 */
	EAttribute getPropSizeProvider_Size();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.GeomBoxplotLayer <em>Geom Boxplot Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Geom Boxplot Layer</em>'.
	 * @see de.walware.statet.rtm.ggplot.GeomBoxplotLayer
	 * @generated
	 */
	EClass getGeomBoxplotLayer();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.GeomHistogramLayer <em>Geom Histogram Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Geom Histogram Layer</em>'.
	 * @see de.walware.statet.rtm.ggplot.GeomHistogramLayer
	 * @generated
	 */
	EClass getGeomHistogramLayer();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.GeomLineLayer <em>Geom Line Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Geom Line Layer</em>'.
	 * @see de.walware.statet.rtm.ggplot.GeomLineLayer
	 * @generated
	 */
	EClass getGeomLineLayer();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.GeomAblineLayer <em>Geom Abline Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Geom Abline Layer</em>'.
	 * @see de.walware.statet.rtm.ggplot.GeomAblineLayer
	 * @generated
	 */
	EClass getGeomAblineLayer();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.GeomAblineLayer#getInterceptVar <em>Intercept Var</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Intercept Var</em>'.
	 * @see de.walware.statet.rtm.ggplot.GeomAblineLayer#getInterceptVar()
	 * @see #getGeomAblineLayer()
	 * @generated
	 */
	EAttribute getGeomAblineLayer_InterceptVar();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ggplot.GeomAblineLayer#getSlopeVar <em>Slope Var</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Slope Var</em>'.
	 * @see de.walware.statet.rtm.ggplot.GeomAblineLayer#getSlopeVar()
	 * @see #getGeomAblineLayer()
	 * @generated
	 */
	EAttribute getGeomAblineLayer_SlopeVar();

	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ggplot.GeomTileLayer <em>Geom Tile Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Geom Tile Layer</em>'.
	 * @see de.walware.statet.rtm.ggplot.GeomTileLayer
	 * @generated
	 */
	EClass getGeomTileLayer();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	GGPlotFactory getGGPlotFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.GGPlotImpl <em>GG Plot</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGGPlot()
		 * @generated
		 */
		EClass GG_PLOT = eINSTANCE.getGGPlot();

		/**
		 * The meta object literal for the '<em><b>Data Filter</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GG_PLOT__DATA_FILTER = eINSTANCE.getGGPlot_DataFilter();

		/**
		 * The meta object literal for the '<em><b>Main Title</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GG_PLOT__MAIN_TITLE = eINSTANCE.getGGPlot_MainTitle();

		/**
		 * The meta object literal for the '<em><b>Main Title Style</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GG_PLOT__MAIN_TITLE_STYLE = eINSTANCE.getGGPlot_MainTitleStyle();

		/**
		 * The meta object literal for the '<em><b>Facet</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GG_PLOT__FACET = eINSTANCE.getGGPlot_Facet();

		/**
		 * The meta object literal for the '<em><b>Ax XLim</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GG_PLOT__AX_XLIM = eINSTANCE.getGGPlot_AxXLim();

		/**
		 * The meta object literal for the '<em><b>Ax YLim</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GG_PLOT__AX_YLIM = eINSTANCE.getGGPlot_AxYLim();

		/**
		 * The meta object literal for the '<em><b>Ax XLabel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GG_PLOT__AX_XLABEL = eINSTANCE.getGGPlot_AxXLabel();

		/**
		 * The meta object literal for the '<em><b>Ax YLabel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GG_PLOT__AX_YLABEL = eINSTANCE.getGGPlot_AxYLabel();

		/**
		 * The meta object literal for the '<em><b>Ax XLabel Style</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GG_PLOT__AX_XLABEL_STYLE = eINSTANCE.getGGPlot_AxXLabelStyle();

		/**
		 * The meta object literal for the '<em><b>Ax YLabel Style</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GG_PLOT__AX_YLABEL_STYLE = eINSTANCE.getGGPlot_AxYLabelStyle();

		/**
		 * The meta object literal for the '<em><b>Ax XText Style</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GG_PLOT__AX_XTEXT_STYLE = eINSTANCE.getGGPlot_AxXTextStyle();

		/**
		 * The meta object literal for the '<em><b>Ax YText Style</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GG_PLOT__AX_YTEXT_STYLE = eINSTANCE.getGGPlot_AxYTextStyle();

		/**
		 * The meta object literal for the '<em><b>Layers</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GG_PLOT__LAYERS = eINSTANCE.getGGPlot_Layers();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.LayerImpl <em>Layer</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.LayerImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getLayer()
		 * @generated
		 */
		EClass LAYER = eINSTANCE.getLayer();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.XVarLayerImpl <em>XVar Layer</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.XVarLayerImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getXVarLayer()
		 * @generated
		 */
		EClass XVAR_LAYER = eINSTANCE.getXVarLayer();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.XYVarLayerImpl <em>XY Var Layer</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.XYVarLayerImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getXYVarLayer()
		 * @generated
		 */
		EClass XY_VAR_LAYER = eINSTANCE.getXYVarLayer();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.GeomPointLayerImpl <em>Geom Point Layer</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.GeomPointLayerImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomPointLayer()
		 * @generated
		 */
		EClass GEOM_POINT_LAYER = eINSTANCE.getGeomPointLayer();

		/**
		 * The meta object literal for the '<em><b>Position XJitter</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GEOM_POINT_LAYER__POSITION_XJITTER = eINSTANCE.getGeomPointLayer_PositionXJitter();

		/**
		 * The meta object literal for the '<em><b>Position YJitter</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GEOM_POINT_LAYER__POSITION_YJITTER = eINSTANCE.getGeomPointLayer_PositionYJitter();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.GeomBarLayerImpl <em>Geom Bar Layer</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.GeomBarLayerImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomBarLayer()
		 * @generated
		 */
		EClass GEOM_BAR_LAYER = eINSTANCE.getGeomBarLayer();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.GeomTextLayerImpl <em>Geom Text Layer</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.GeomTextLayerImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomTextLayer()
		 * @generated
		 */
		EClass GEOM_TEXT_LAYER = eINSTANCE.getGeomTextLayer();

		/**
		 * The meta object literal for the '<em><b>Label</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GEOM_TEXT_LAYER__LABEL = eINSTANCE.getGeomTextLayer_Label();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.GeomSmoothLayerImpl <em>Geom Smooth Layer</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.GeomSmoothLayerImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomSmoothLayer()
		 * @generated
		 */
		EClass GEOM_SMOOTH_LAYER = eINSTANCE.getGeomSmoothLayer();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.GeomViolinLayerImpl <em>Geom Violin Layer</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.GeomViolinLayerImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomViolinLayer()
		 * @generated
		 */
		EClass GEOM_VIOLIN_LAYER = eINSTANCE.getGeomViolinLayer();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.FacetLayout <em>Facet Layout</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.FacetLayout
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getFacetLayout()
		 * @generated
		 */
		EClass FACET_LAYOUT = eINSTANCE.getFacetLayout();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.GridFacetLayoutImpl <em>Grid Facet Layout</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.GridFacetLayoutImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGridFacetLayout()
		 * @generated
		 */
		EClass GRID_FACET_LAYOUT = eINSTANCE.getGridFacetLayout();

		/**
		 * The meta object literal for the '<em><b>Col Vars</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GRID_FACET_LAYOUT__COL_VARS = eINSTANCE.getGridFacetLayout_ColVars();

		/**
		 * The meta object literal for the '<em><b>Row Vars</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GRID_FACET_LAYOUT__ROW_VARS = eINSTANCE.getGridFacetLayout_RowVars();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.WrapFacetLayoutImpl <em>Wrap Facet Layout</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.WrapFacetLayoutImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getWrapFacetLayout()
		 * @generated
		 */
		EClass WRAP_FACET_LAYOUT = eINSTANCE.getWrapFacetLayout();

		/**
		 * The meta object literal for the '<em><b>Col Vars</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute WRAP_FACET_LAYOUT__COL_VARS = eINSTANCE.getWrapFacetLayout_ColVars();

		/**
		 * The meta object literal for the '<em><b>Col Num</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute WRAP_FACET_LAYOUT__COL_NUM = eINSTANCE.getWrapFacetLayout_ColNum();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.TextStyleImpl <em>Text Style</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.TextStyleImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getTextStyle()
		 * @generated
		 */
		EClass TEXT_STYLE = eINSTANCE.getTextStyle();

		/**
		 * The meta object literal for the '<em><b>Font Family</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TEXT_STYLE__FONT_FAMILY = eINSTANCE.getTextStyle_FontFamily();

		/**
		 * The meta object literal for the '<em><b>Font Face</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TEXT_STYLE__FONT_FACE = eINSTANCE.getTextStyle_FontFace();

		/**
		 * The meta object literal for the '<em><b>HJust</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TEXT_STYLE__HJUST = eINSTANCE.getTextStyle_HJust();

		/**
		 * The meta object literal for the '<em><b>VJust</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TEXT_STYLE__VJUST = eINSTANCE.getTextStyle_VJust();

		/**
		 * The meta object literal for the '<em><b>Angle</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TEXT_STYLE__ANGLE = eINSTANCE.getTextStyle_Angle();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.StatImpl <em>Stat</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.StatImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getStat()
		 * @generated
		 */
		EClass STAT = eINSTANCE.getStat();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.IdentityStatImpl <em>Identity Stat</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.IdentityStatImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getIdentityStat()
		 * @generated
		 */
		EClass IDENTITY_STAT = eINSTANCE.getIdentityStat();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.SummaryStatImpl <em>Summary Stat</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.SummaryStatImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getSummaryStat()
		 * @generated
		 */
		EClass SUMMARY_STAT = eINSTANCE.getSummaryStat();

		/**
		 * The meta object literal for the '<em><b>YFun</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SUMMARY_STAT__YFUN = eINSTANCE.getSummaryStat_YFun();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.PropDataProvider <em>Prop Data Provider</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.PropDataProvider
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropDataProvider()
		 * @generated
		 */
		EClass PROP_DATA_PROVIDER = eINSTANCE.getPropDataProvider();

		/**
		 * The meta object literal for the '<em><b>Data</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROP_DATA_PROVIDER__DATA = eINSTANCE.getPropDataProvider_Data();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.PropXVarProvider <em>Prop XVar Provider</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.PropXVarProvider
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropXVarProvider()
		 * @generated
		 */
		EClass PROP_XVAR_PROVIDER = eINSTANCE.getPropXVarProvider();

		/**
		 * The meta object literal for the '<em><b>XVar</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROP_XVAR_PROVIDER__XVAR = eINSTANCE.getPropXVarProvider_XVar();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.PropYVarProvider <em>Prop YVar Provider</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.PropYVarProvider
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropYVarProvider()
		 * @generated
		 */
		EClass PROP_YVAR_PROVIDER = eINSTANCE.getPropYVarProvider();

		/**
		 * The meta object literal for the '<em><b>YVar</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROP_YVAR_PROVIDER__YVAR = eINSTANCE.getPropYVarProvider_YVar();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.PropStatProvider <em>Prop Stat Provider</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.PropStatProvider
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropStatProvider()
		 * @generated
		 */
		EClass PROP_STAT_PROVIDER = eINSTANCE.getPropStatProvider();

		/**
		 * The meta object literal for the '<em><b>Stat</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PROP_STAT_PROVIDER__STAT = eINSTANCE.getPropStatProvider_Stat();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.PropGroupVarProvider <em>Prop Group Var Provider</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.PropGroupVarProvider
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropGroupVarProvider()
		 * @generated
		 */
		EClass PROP_GROUP_VAR_PROVIDER = eINSTANCE.getPropGroupVarProvider();

		/**
		 * The meta object literal for the '<em><b>Group Var</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROP_GROUP_VAR_PROVIDER__GROUP_VAR = eINSTANCE.getPropGroupVarProvider_GroupVar();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.PropColorProvider <em>Prop Color Provider</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.PropColorProvider
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropColorProvider()
		 * @generated
		 */
		EClass PROP_COLOR_PROVIDER = eINSTANCE.getPropColorProvider();

		/**
		 * The meta object literal for the '<em><b>Color</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROP_COLOR_PROVIDER__COLOR = eINSTANCE.getPropColorProvider_Color();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.PropFillProvider <em>Prop Fill Provider</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.PropFillProvider
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropFillProvider()
		 * @generated
		 */
		EClass PROP_FILL_PROVIDER = eINSTANCE.getPropFillProvider();

		/**
		 * The meta object literal for the '<em><b>Fill</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROP_FILL_PROVIDER__FILL = eINSTANCE.getPropFillProvider_Fill();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.PropAlphaProvider <em>Prop Alpha Provider</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.PropAlphaProvider
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropAlphaProvider()
		 * @generated
		 */
		EClass PROP_ALPHA_PROVIDER = eINSTANCE.getPropAlphaProvider();

		/**
		 * The meta object literal for the '<em><b>Alpha</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROP_ALPHA_PROVIDER__ALPHA = eINSTANCE.getPropAlphaProvider_Alpha();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.PropShapeProvider <em>Prop Shape Provider</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.PropShapeProvider
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropShapeProvider()
		 * @generated
		 */
		EClass PROP_SHAPE_PROVIDER = eINSTANCE.getPropShapeProvider();

		/**
		 * The meta object literal for the '<em><b>Shape</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROP_SHAPE_PROVIDER__SHAPE = eINSTANCE.getPropShapeProvider_Shape();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.PropLineTypeProvider <em>Prop Line Type Provider</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.PropLineTypeProvider
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropLineTypeProvider()
		 * @generated
		 */
		EClass PROP_LINE_TYPE_PROVIDER = eINSTANCE.getPropLineTypeProvider();

		/**
		 * The meta object literal for the '<em><b>Line Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROP_LINE_TYPE_PROVIDER__LINE_TYPE = eINSTANCE.getPropLineTypeProvider_LineType();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.PropSizeProvider <em>Prop Size Provider</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.PropSizeProvider
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getPropSizeProvider()
		 * @generated
		 */
		EClass PROP_SIZE_PROVIDER = eINSTANCE.getPropSizeProvider();

		/**
		 * The meta object literal for the '<em><b>Size</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROP_SIZE_PROVIDER__SIZE = eINSTANCE.getPropSizeProvider_Size();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.GeomBoxplotLayerImpl <em>Geom Boxplot Layer</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.GeomBoxplotLayerImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomBoxplotLayer()
		 * @generated
		 */
		EClass GEOM_BOXPLOT_LAYER = eINSTANCE.getGeomBoxplotLayer();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.GeomHistogramLayerImpl <em>Geom Histogram Layer</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.GeomHistogramLayerImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomHistogramLayer()
		 * @generated
		 */
		EClass GEOM_HISTOGRAM_LAYER = eINSTANCE.getGeomHistogramLayer();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.GeomLineLayerImpl <em>Geom Line Layer</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.GeomLineLayerImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomLineLayer()
		 * @generated
		 */
		EClass GEOM_LINE_LAYER = eINSTANCE.getGeomLineLayer();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.GeomAblineLayerImpl <em>Geom Abline Layer</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.GeomAblineLayerImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomAblineLayer()
		 * @generated
		 */
		EClass GEOM_ABLINE_LAYER = eINSTANCE.getGeomAblineLayer();

		/**
		 * The meta object literal for the '<em><b>Intercept Var</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GEOM_ABLINE_LAYER__INTERCEPT_VAR = eINSTANCE.getGeomAblineLayer_InterceptVar();

		/**
		 * The meta object literal for the '<em><b>Slope Var</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GEOM_ABLINE_LAYER__SLOPE_VAR = eINSTANCE.getGeomAblineLayer_SlopeVar();

		/**
		 * The meta object literal for the '{@link de.walware.statet.rtm.ggplot.impl.GeomTileLayerImpl <em>Geom Tile Layer</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ggplot.impl.GeomTileLayerImpl
		 * @see de.walware.statet.rtm.ggplot.impl.GGPlotPackageImpl#getGeomTileLayer()
		 * @generated
		 */
		EClass GEOM_TILE_LAYER = eINSTANCE.getGeomTileLayer();

	}

} //GGPlotPackage
