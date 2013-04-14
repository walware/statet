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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;

import de.walware.statet.rtm.ggplot.GGPlot;
import de.walware.statet.rtm.ggplot.GGPlotFactory;
import de.walware.statet.rtm.ggplot.GGPlotPackage;
import de.walware.statet.rtm.ggplot.GeomAblineLayer;
import de.walware.statet.rtm.ggplot.GeomBarLayer;
import de.walware.statet.rtm.ggplot.GeomBoxplotLayer;
import de.walware.statet.rtm.ggplot.GeomHistogramLayer;
import de.walware.statet.rtm.ggplot.GeomLineLayer;
import de.walware.statet.rtm.ggplot.GeomPointLayer;
import de.walware.statet.rtm.ggplot.GeomSmoothLayer;
import de.walware.statet.rtm.ggplot.GeomTextLayer;
import de.walware.statet.rtm.ggplot.GeomTileLayer;
import de.walware.statet.rtm.ggplot.GeomViolinLayer;
import de.walware.statet.rtm.ggplot.GridFacetLayout;
import de.walware.statet.rtm.ggplot.IdentityStat;
import de.walware.statet.rtm.ggplot.SummaryStat;
import de.walware.statet.rtm.ggplot.TextStyle;
import de.walware.statet.rtm.ggplot.WrapFacetLayout;


/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class GGPlotFactoryImpl extends EFactoryImpl implements GGPlotFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static GGPlotFactory init() {
		try {
			GGPlotFactory theGGPlotFactory = (GGPlotFactory)EPackage.Registry.INSTANCE.getEFactory("http://walware.de/rtm/Rt-ggplot/1.0"); //$NON-NLS-1$ 
			if (theGGPlotFactory != null) {
				return theGGPlotFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new GGPlotFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GGPlotFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case GGPlotPackage.GG_PLOT: return createGGPlot();
			case GGPlotPackage.GEOM_ABLINE_LAYER: return createGeomAblineLayer();
			case GGPlotPackage.GEOM_BAR_LAYER: return createGeomBarLayer();
			case GGPlotPackage.GEOM_BOXPLOT_LAYER: return createGeomBoxplotLayer();
			case GGPlotPackage.GEOM_HISTOGRAM_LAYER: return createGeomHistogramLayer();
			case GGPlotPackage.GEOM_LINE_LAYER: return createGeomLineLayer();
			case GGPlotPackage.GEOM_POINT_LAYER: return createGeomPointLayer();
			case GGPlotPackage.GEOM_TEXT_LAYER: return createGeomTextLayer();
			case GGPlotPackage.GEOM_SMOOTH_LAYER: return createGeomSmoothLayer();
			case GGPlotPackage.GEOM_TILE_LAYER: return createGeomTileLayer();
			case GGPlotPackage.GEOM_VIOLIN_LAYER: return createGeomViolinLayer();
			case GGPlotPackage.GRID_FACET_LAYOUT: return createGridFacetLayout();
			case GGPlotPackage.WRAP_FACET_LAYOUT: return createWrapFacetLayout();
			case GGPlotPackage.IDENTITY_STAT: return createIdentityStat();
			case GGPlotPackage.SUMMARY_STAT: return createSummaryStat();
			case GGPlotPackage.TEXT_STYLE: return createTextStyle();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GGPlot createGGPlot() {
		GGPlotImpl ggPlot = new GGPlotImpl();
		return ggPlot;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GeomPointLayer createGeomPointLayer() {
		GeomPointLayerImpl geomPointLayer = new GeomPointLayerImpl();
		return geomPointLayer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GeomBarLayer createGeomBarLayer() {
		GeomBarLayerImpl geomBarLayer = new GeomBarLayerImpl();
		return geomBarLayer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GeomTextLayer createGeomTextLayer() {
		GeomTextLayerImpl geomTextLayer = new GeomTextLayerImpl();
		return geomTextLayer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GeomSmoothLayer createGeomSmoothLayer() {
		GeomSmoothLayerImpl geomSmoothLayer = new GeomSmoothLayerImpl();
		return geomSmoothLayer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GeomViolinLayer createGeomViolinLayer() {
		GeomViolinLayerImpl geomViolinLayer = new GeomViolinLayerImpl();
		return geomViolinLayer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GridFacetLayout createGridFacetLayout() {
		GridFacetLayoutImpl gridFacetLayout = new GridFacetLayoutImpl();
		return gridFacetLayout;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public WrapFacetLayout createWrapFacetLayout() {
		WrapFacetLayoutImpl wrapFacetLayout = new WrapFacetLayoutImpl();
		return wrapFacetLayout;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TextStyle createTextStyle() {
		TextStyleImpl textStyle = new TextStyleImpl();
		return textStyle;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public IdentityStat createIdentityStat() {
		IdentityStatImpl identityStat = new IdentityStatImpl();
		return identityStat;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SummaryStat createSummaryStat() {
		SummaryStatImpl summaryStat = new SummaryStatImpl();
		return summaryStat;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GeomBoxplotLayer createGeomBoxplotLayer() {
		GeomBoxplotLayerImpl geomBoxplotLayer = new GeomBoxplotLayerImpl();
		return geomBoxplotLayer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GeomHistogramLayer createGeomHistogramLayer() {
		GeomHistogramLayerImpl geomHistogramLayer = new GeomHistogramLayerImpl();
		return geomHistogramLayer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GeomLineLayer createGeomLineLayer() {
		GeomLineLayerImpl geomLineLayer = new GeomLineLayerImpl();
		return geomLineLayer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GeomAblineLayer createGeomAblineLayer() {
		GeomAblineLayerImpl geomAblineLayer = new GeomAblineLayerImpl();
		return geomAblineLayer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GeomTileLayer createGeomTileLayer() {
		GeomTileLayerImpl geomTileLayer = new GeomTileLayerImpl();
		return geomTileLayer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GGPlotPackage getGGPlotPackage() {
		return (GGPlotPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static GGPlotPackage getPackage() {
		return GGPlotPackage.eINSTANCE;
	}

} //GGPlotFactoryImpl
