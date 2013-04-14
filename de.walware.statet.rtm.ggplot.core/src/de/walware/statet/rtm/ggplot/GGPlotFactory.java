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

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see de.walware.statet.rtm.ggplot.GGPlotPackage
 * @generated
 */
public interface GGPlotFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	GGPlotFactory eINSTANCE = de.walware.statet.rtm.ggplot.impl.GGPlotFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>GG Plot</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>GG Plot</em>'.
	 * @generated
	 */
	GGPlot createGGPlot();

	/**
	 * Returns a new object of class '<em>Geom Point Layer</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Geom Point Layer</em>'.
	 * @generated
	 */
	GeomPointLayer createGeomPointLayer();

	/**
	 * Returns a new object of class '<em>Geom Bar Layer</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Geom Bar Layer</em>'.
	 * @generated
	 */
	GeomBarLayer createGeomBarLayer();

	/**
	 * Returns a new object of class '<em>Geom Text Layer</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Geom Text Layer</em>'.
	 * @generated
	 */
	GeomTextLayer createGeomTextLayer();

	/**
	 * Returns a new object of class '<em>Geom Smooth Layer</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Geom Smooth Layer</em>'.
	 * @generated
	 */
	GeomSmoothLayer createGeomSmoothLayer();

	/**
	 * Returns a new object of class '<em>Geom Violin Layer</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Geom Violin Layer</em>'.
	 * @generated
	 */
	GeomViolinLayer createGeomViolinLayer();

	/**
	 * Returns a new object of class '<em>Grid Facet Layout</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Grid Facet Layout</em>'.
	 * @generated
	 */
	GridFacetLayout createGridFacetLayout();

	/**
	 * Returns a new object of class '<em>Wrap Facet Layout</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Wrap Facet Layout</em>'.
	 * @generated
	 */
	WrapFacetLayout createWrapFacetLayout();

	/**
	 * Returns a new object of class '<em>Text Style</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Text Style</em>'.
	 * @generated
	 */
	TextStyle createTextStyle();

	/**
	 * Returns a new object of class '<em>Identity Stat</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Identity Stat</em>'.
	 * @generated
	 */
	IdentityStat createIdentityStat();

	/**
	 * Returns a new object of class '<em>Summary Stat</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Summary Stat</em>'.
	 * @generated
	 */
	SummaryStat createSummaryStat();

	/**
	 * Returns a new object of class '<em>Geom Boxplot Layer</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Geom Boxplot Layer</em>'.
	 * @generated
	 */
	GeomBoxplotLayer createGeomBoxplotLayer();

	/**
	 * Returns a new object of class '<em>Geom Histogram Layer</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Geom Histogram Layer</em>'.
	 * @generated
	 */
	GeomHistogramLayer createGeomHistogramLayer();

	/**
	 * Returns a new object of class '<em>Geom Line Layer</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Geom Line Layer</em>'.
	 * @generated
	 */
	GeomLineLayer createGeomLineLayer();

	/**
	 * Returns a new object of class '<em>Geom Abline Layer</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Geom Abline Layer</em>'.
	 * @generated
	 */
	GeomAblineLayer createGeomAblineLayer();

	/**
	 * Returns a new object of class '<em>Geom Tile Layer</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Geom Tile Layer</em>'.
	 * @generated
	 */
	GeomTileLayer createGeomTileLayer();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	GGPlotPackage getGGPlotPackage();

} //GGPlotFactory
