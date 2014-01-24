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

package de.walware.statet.rtm.ggplot.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.Switch;

import de.walware.statet.rtm.ggplot.FacetLayout;
import de.walware.statet.rtm.ggplot.GGPlot;
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
import de.walware.statet.rtm.ggplot.Layer;
import de.walware.statet.rtm.ggplot.PropAlphaProvider;
import de.walware.statet.rtm.ggplot.PropColorProvider;
import de.walware.statet.rtm.ggplot.PropDataProvider;
import de.walware.statet.rtm.ggplot.PropFillProvider;
import de.walware.statet.rtm.ggplot.PropGroupVarProvider;
import de.walware.statet.rtm.ggplot.PropLineTypeProvider;
import de.walware.statet.rtm.ggplot.PropShapeProvider;
import de.walware.statet.rtm.ggplot.PropSizeProvider;
import de.walware.statet.rtm.ggplot.PropStatProvider;
import de.walware.statet.rtm.ggplot.PropXVarProvider;
import de.walware.statet.rtm.ggplot.PropYVarProvider;
import de.walware.statet.rtm.ggplot.Stat;
import de.walware.statet.rtm.ggplot.SummaryStat;
import de.walware.statet.rtm.ggplot.TextStyle;
import de.walware.statet.rtm.ggplot.WrapFacetLayout;
import de.walware.statet.rtm.ggplot.XVarLayer;
import de.walware.statet.rtm.ggplot.XYVarLayer;


/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see de.walware.statet.rtm.ggplot.GGPlotPackage
 * @generated
 */
public class GGPlotSwitch<T> extends Switch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static GGPlotPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GGPlotSwitch() {
		if (modelPackage == null) {
			modelPackage = GGPlotPackage.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @parameter ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage) {
		return ePackage == modelPackage;
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case GGPlotPackage.GG_PLOT: {
				GGPlot ggPlot = (GGPlot)theEObject;
				T result = caseGGPlot(ggPlot);
				if (result == null) {
					result = casePropDataProvider(ggPlot);
				}
				if (result == null) {
					result = casePropXVarProvider(ggPlot);
				}
				if (result == null) {
					result = casePropYVarProvider(ggPlot);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.LAYER: {
				Layer layer = (Layer)theEObject;
				T result = caseLayer(layer);
				if (result == null) {
					result = casePropDataProvider(layer);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.XVAR_LAYER: {
				XVarLayer xVarLayer = (XVarLayer)theEObject;
				T result = caseXVarLayer(xVarLayer);
				if (result == null) {
					result = caseLayer(xVarLayer);
				}
				if (result == null) {
					result = casePropXVarProvider(xVarLayer);
				}
				if (result == null) {
					result = casePropGroupVarProvider(xVarLayer);
				}
				if (result == null) {
					result = casePropDataProvider(xVarLayer);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.XY_VAR_LAYER: {
				XYVarLayer xyVarLayer = (XYVarLayer)theEObject;
				T result = caseXYVarLayer(xyVarLayer);
				if (result == null) {
					result = caseLayer(xyVarLayer);
				}
				if (result == null) {
					result = casePropXVarProvider(xyVarLayer);
				}
				if (result == null) {
					result = casePropYVarProvider(xyVarLayer);
				}
				if (result == null) {
					result = casePropGroupVarProvider(xyVarLayer);
				}
				if (result == null) {
					result = casePropDataProvider(xyVarLayer);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.GEOM_ABLINE_LAYER: {
				GeomAblineLayer geomAblineLayer = (GeomAblineLayer)theEObject;
				T result = caseGeomAblineLayer(geomAblineLayer);
				if (result == null) {
					result = caseLayer(geomAblineLayer);
				}
				if (result == null) {
					result = casePropLineTypeProvider(geomAblineLayer);
				}
				if (result == null) {
					result = casePropSizeProvider(geomAblineLayer);
				}
				if (result == null) {
					result = casePropColorProvider(geomAblineLayer);
				}
				if (result == null) {
					result = casePropAlphaProvider(geomAblineLayer);
				}
				if (result == null) {
					result = casePropDataProvider(geomAblineLayer);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.GEOM_BAR_LAYER: {
				GeomBarLayer geomBarLayer = (GeomBarLayer)theEObject;
				T result = caseGeomBarLayer(geomBarLayer);
				if (result == null) {
					result = caseXYVarLayer(geomBarLayer);
				}
				if (result == null) {
					result = casePropStatProvider(geomBarLayer);
				}
				if (result == null) {
					result = casePropColorProvider(geomBarLayer);
				}
				if (result == null) {
					result = casePropFillProvider(geomBarLayer);
				}
				if (result == null) {
					result = casePropAlphaProvider(geomBarLayer);
				}
				if (result == null) {
					result = caseLayer(geomBarLayer);
				}
				if (result == null) {
					result = casePropXVarProvider(geomBarLayer);
				}
				if (result == null) {
					result = casePropYVarProvider(geomBarLayer);
				}
				if (result == null) {
					result = casePropGroupVarProvider(geomBarLayer);
				}
				if (result == null) {
					result = casePropDataProvider(geomBarLayer);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.GEOM_BOXPLOT_LAYER: {
				GeomBoxplotLayer geomBoxplotLayer = (GeomBoxplotLayer)theEObject;
				T result = caseGeomBoxplotLayer(geomBoxplotLayer);
				if (result == null) {
					result = caseXVarLayer(geomBoxplotLayer);
				}
				if (result == null) {
					result = casePropColorProvider(geomBoxplotLayer);
				}
				if (result == null) {
					result = casePropFillProvider(geomBoxplotLayer);
				}
				if (result == null) {
					result = casePropAlphaProvider(geomBoxplotLayer);
				}
				if (result == null) {
					result = caseLayer(geomBoxplotLayer);
				}
				if (result == null) {
					result = casePropXVarProvider(geomBoxplotLayer);
				}
				if (result == null) {
					result = casePropGroupVarProvider(geomBoxplotLayer);
				}
				if (result == null) {
					result = casePropDataProvider(geomBoxplotLayer);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.GEOM_HISTOGRAM_LAYER: {
				GeomHistogramLayer geomHistogramLayer = (GeomHistogramLayer)theEObject;
				T result = caseGeomHistogramLayer(geomHistogramLayer);
				if (result == null) {
					result = caseXVarLayer(geomHistogramLayer);
				}
				if (result == null) {
					result = casePropColorProvider(geomHistogramLayer);
				}
				if (result == null) {
					result = casePropFillProvider(geomHistogramLayer);
				}
				if (result == null) {
					result = casePropAlphaProvider(geomHistogramLayer);
				}
				if (result == null) {
					result = caseLayer(geomHistogramLayer);
				}
				if (result == null) {
					result = casePropXVarProvider(geomHistogramLayer);
				}
				if (result == null) {
					result = casePropGroupVarProvider(geomHistogramLayer);
				}
				if (result == null) {
					result = casePropDataProvider(geomHistogramLayer);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.GEOM_LINE_LAYER: {
				GeomLineLayer geomLineLayer = (GeomLineLayer)theEObject;
				T result = caseGeomLineLayer(geomLineLayer);
				if (result == null) {
					result = caseXYVarLayer(geomLineLayer);
				}
				if (result == null) {
					result = casePropStatProvider(geomLineLayer);
				}
				if (result == null) {
					result = casePropLineTypeProvider(geomLineLayer);
				}
				if (result == null) {
					result = casePropSizeProvider(geomLineLayer);
				}
				if (result == null) {
					result = casePropColorProvider(geomLineLayer);
				}
				if (result == null) {
					result = casePropAlphaProvider(geomLineLayer);
				}
				if (result == null) {
					result = caseLayer(geomLineLayer);
				}
				if (result == null) {
					result = casePropXVarProvider(geomLineLayer);
				}
				if (result == null) {
					result = casePropYVarProvider(geomLineLayer);
				}
				if (result == null) {
					result = casePropGroupVarProvider(geomLineLayer);
				}
				if (result == null) {
					result = casePropDataProvider(geomLineLayer);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.GEOM_POINT_LAYER: {
				GeomPointLayer geomPointLayer = (GeomPointLayer)theEObject;
				T result = caseGeomPointLayer(geomPointLayer);
				if (result == null) {
					result = caseXYVarLayer(geomPointLayer);
				}
				if (result == null) {
					result = casePropShapeProvider(geomPointLayer);
				}
				if (result == null) {
					result = casePropSizeProvider(geomPointLayer);
				}
				if (result == null) {
					result = casePropColorProvider(geomPointLayer);
				}
				if (result == null) {
					result = casePropFillProvider(geomPointLayer);
				}
				if (result == null) {
					result = casePropAlphaProvider(geomPointLayer);
				}
				if (result == null) {
					result = caseLayer(geomPointLayer);
				}
				if (result == null) {
					result = casePropXVarProvider(geomPointLayer);
				}
				if (result == null) {
					result = casePropYVarProvider(geomPointLayer);
				}
				if (result == null) {
					result = casePropGroupVarProvider(geomPointLayer);
				}
				if (result == null) {
					result = casePropDataProvider(geomPointLayer);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.GEOM_TEXT_LAYER: {
				GeomTextLayer geomTextLayer = (GeomTextLayer)theEObject;
				T result = caseGeomTextLayer(geomTextLayer);
				if (result == null) {
					result = caseXYVarLayer(geomTextLayer);
				}
				if (result == null) {
					result = caseTextStyle(geomTextLayer);
				}
				if (result == null) {
					result = casePropAlphaProvider(geomTextLayer);
				}
				if (result == null) {
					result = caseLayer(geomTextLayer);
				}
				if (result == null) {
					result = casePropXVarProvider(geomTextLayer);
				}
				if (result == null) {
					result = casePropYVarProvider(geomTextLayer);
				}
				if (result == null) {
					result = casePropGroupVarProvider(geomTextLayer);
				}
				if (result == null) {
					result = casePropSizeProvider(geomTextLayer);
				}
				if (result == null) {
					result = casePropColorProvider(geomTextLayer);
				}
				if (result == null) {
					result = casePropDataProvider(geomTextLayer);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.GEOM_SMOOTH_LAYER: {
				GeomSmoothLayer geomSmoothLayer = (GeomSmoothLayer)theEObject;
				T result = caseGeomSmoothLayer(geomSmoothLayer);
				if (result == null) {
					result = caseXYVarLayer(geomSmoothLayer);
				}
				if (result == null) {
					result = casePropSizeProvider(geomSmoothLayer);
				}
				if (result == null) {
					result = casePropColorProvider(geomSmoothLayer);
				}
				if (result == null) {
					result = casePropFillProvider(geomSmoothLayer);
				}
				if (result == null) {
					result = casePropAlphaProvider(geomSmoothLayer);
				}
				if (result == null) {
					result = caseLayer(geomSmoothLayer);
				}
				if (result == null) {
					result = casePropXVarProvider(geomSmoothLayer);
				}
				if (result == null) {
					result = casePropYVarProvider(geomSmoothLayer);
				}
				if (result == null) {
					result = casePropGroupVarProvider(geomSmoothLayer);
				}
				if (result == null) {
					result = casePropDataProvider(geomSmoothLayer);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.GEOM_TILE_LAYER: {
				GeomTileLayer geomTileLayer = (GeomTileLayer)theEObject;
				T result = caseGeomTileLayer(geomTileLayer);
				if (result == null) {
					result = caseXYVarLayer(geomTileLayer);
				}
				if (result == null) {
					result = casePropLineTypeProvider(geomTileLayer);
				}
				if (result == null) {
					result = casePropColorProvider(geomTileLayer);
				}
				if (result == null) {
					result = casePropFillProvider(geomTileLayer);
				}
				if (result == null) {
					result = casePropAlphaProvider(geomTileLayer);
				}
				if (result == null) {
					result = caseLayer(geomTileLayer);
				}
				if (result == null) {
					result = casePropXVarProvider(geomTileLayer);
				}
				if (result == null) {
					result = casePropYVarProvider(geomTileLayer);
				}
				if (result == null) {
					result = casePropGroupVarProvider(geomTileLayer);
				}
				if (result == null) {
					result = casePropDataProvider(geomTileLayer);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.GEOM_VIOLIN_LAYER: {
				GeomViolinLayer geomViolinLayer = (GeomViolinLayer)theEObject;
				T result = caseGeomViolinLayer(geomViolinLayer);
				if (result == null) {
					result = caseXYVarLayer(geomViolinLayer);
				}
				if (result == null) {
					result = casePropLineTypeProvider(geomViolinLayer);
				}
				if (result == null) {
					result = casePropColorProvider(geomViolinLayer);
				}
				if (result == null) {
					result = casePropFillProvider(geomViolinLayer);
				}
				if (result == null) {
					result = casePropAlphaProvider(geomViolinLayer);
				}
				if (result == null) {
					result = caseLayer(geomViolinLayer);
				}
				if (result == null) {
					result = casePropXVarProvider(geomViolinLayer);
				}
				if (result == null) {
					result = casePropYVarProvider(geomViolinLayer);
				}
				if (result == null) {
					result = casePropGroupVarProvider(geomViolinLayer);
				}
				if (result == null) {
					result = casePropDataProvider(geomViolinLayer);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.FACET_LAYOUT: {
				FacetLayout facetLayout = (FacetLayout)theEObject;
				T result = caseFacetLayout(facetLayout);
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.GRID_FACET_LAYOUT: {
				GridFacetLayout gridFacetLayout = (GridFacetLayout)theEObject;
				T result = caseGridFacetLayout(gridFacetLayout);
				if (result == null) {
					result = caseFacetLayout(gridFacetLayout);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.WRAP_FACET_LAYOUT: {
				WrapFacetLayout wrapFacetLayout = (WrapFacetLayout)theEObject;
				T result = caseWrapFacetLayout(wrapFacetLayout);
				if (result == null) {
					result = caseFacetLayout(wrapFacetLayout);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.STAT: {
				Stat stat = (Stat)theEObject;
				T result = caseStat(stat);
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.IDENTITY_STAT: {
				IdentityStat identityStat = (IdentityStat)theEObject;
				T result = caseIdentityStat(identityStat);
				if (result == null) {
					result = caseStat(identityStat);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.SUMMARY_STAT: {
				SummaryStat summaryStat = (SummaryStat)theEObject;
				T result = caseSummaryStat(summaryStat);
				if (result == null) {
					result = caseStat(summaryStat);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.TEXT_STYLE: {
				TextStyle textStyle = (TextStyle)theEObject;
				T result = caseTextStyle(textStyle);
				if (result == null) {
					result = casePropSizeProvider(textStyle);
				}
				if (result == null) {
					result = casePropColorProvider(textStyle);
				}
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.PROP_DATA_PROVIDER: {
				PropDataProvider propDataProvider = (PropDataProvider)theEObject;
				T result = casePropDataProvider(propDataProvider);
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.PROP_XVAR_PROVIDER: {
				PropXVarProvider propXVarProvider = (PropXVarProvider)theEObject;
				T result = casePropXVarProvider(propXVarProvider);
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.PROP_YVAR_PROVIDER: {
				PropYVarProvider propYVarProvider = (PropYVarProvider)theEObject;
				T result = casePropYVarProvider(propYVarProvider);
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.PROP_STAT_PROVIDER: {
				PropStatProvider propStatProvider = (PropStatProvider)theEObject;
				T result = casePropStatProvider(propStatProvider);
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.PROP_GROUP_VAR_PROVIDER: {
				PropGroupVarProvider propGroupVarProvider = (PropGroupVarProvider)theEObject;
				T result = casePropGroupVarProvider(propGroupVarProvider);
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.PROP_SHAPE_PROVIDER: {
				PropShapeProvider propShapeProvider = (PropShapeProvider)theEObject;
				T result = casePropShapeProvider(propShapeProvider);
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.PROP_LINE_TYPE_PROVIDER: {
				PropLineTypeProvider propLineTypeProvider = (PropLineTypeProvider)theEObject;
				T result = casePropLineTypeProvider(propLineTypeProvider);
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.PROP_SIZE_PROVIDER: {
				PropSizeProvider propSizeProvider = (PropSizeProvider)theEObject;
				T result = casePropSizeProvider(propSizeProvider);
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.PROP_COLOR_PROVIDER: {
				PropColorProvider propColorProvider = (PropColorProvider)theEObject;
				T result = casePropColorProvider(propColorProvider);
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.PROP_FILL_PROVIDER: {
				PropFillProvider propFillProvider = (PropFillProvider)theEObject;
				T result = casePropFillProvider(propFillProvider);
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			case GGPlotPackage.PROP_ALPHA_PROVIDER: {
				PropAlphaProvider propAlphaProvider = (PropAlphaProvider)theEObject;
				T result = casePropAlphaProvider(propAlphaProvider);
				if (result == null) {
					result = defaultCase(theEObject);
				}
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>GG Plot</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>GG Plot</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGGPlot(GGPlot object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Layer</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Layer</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseLayer(Layer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>XVar Layer</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>XVar Layer</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseXVarLayer(XVarLayer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>XY Var Layer</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>XY Var Layer</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseXYVarLayer(XYVarLayer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Geom Point Layer</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Geom Point Layer</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGeomPointLayer(GeomPointLayer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Geom Bar Layer</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Geom Bar Layer</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGeomBarLayer(GeomBarLayer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Geom Text Layer</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Geom Text Layer</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGeomTextLayer(GeomTextLayer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Geom Smooth Layer</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Geom Smooth Layer</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGeomSmoothLayer(GeomSmoothLayer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Geom Violin Layer</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Geom Violin Layer</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGeomViolinLayer(GeomViolinLayer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Facet Layout</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Facet Layout</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFacetLayout(FacetLayout object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Grid Facet Layout</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Grid Facet Layout</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGridFacetLayout(GridFacetLayout object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Wrap Facet Layout</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Wrap Facet Layout</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseWrapFacetLayout(WrapFacetLayout object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Text Style</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Text Style</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTextStyle(TextStyle object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Stat</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Stat</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseStat(Stat object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Identity Stat</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Identity Stat</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseIdentityStat(IdentityStat object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Summary Stat</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Summary Stat</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSummaryStat(SummaryStat object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Prop Data Provider</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Prop Data Provider</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePropDataProvider(PropDataProvider object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Prop XVar Provider</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Prop XVar Provider</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePropXVarProvider(PropXVarProvider object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Prop YVar Provider</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Prop YVar Provider</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePropYVarProvider(PropYVarProvider object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Prop Stat Provider</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Prop Stat Provider</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePropStatProvider(PropStatProvider object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Prop Group Var Provider</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Prop Group Var Provider</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePropGroupVarProvider(PropGroupVarProvider object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Prop Color Provider</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Prop Color Provider</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePropColorProvider(PropColorProvider object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Prop Fill Provider</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Prop Fill Provider</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePropFillProvider(PropFillProvider object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Prop Alpha Provider</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Prop Alpha Provider</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePropAlphaProvider(PropAlphaProvider object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Prop Shape Provider</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Prop Shape Provider</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePropShapeProvider(PropShapeProvider object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Prop Line Type Provider</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Prop Line Type Provider</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePropLineTypeProvider(PropLineTypeProvider object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Prop Size Provider</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Prop Size Provider</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePropSizeProvider(PropSizeProvider object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Geom Boxplot Layer</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Geom Boxplot Layer</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGeomBoxplotLayer(GeomBoxplotLayer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Geom Histogram Layer</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Geom Histogram Layer</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGeomHistogramLayer(GeomHistogramLayer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Geom Line Layer</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Geom Line Layer</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGeomLineLayer(GeomLineLayer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Geom Abline Layer</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Geom Abline Layer</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGeomAblineLayer(GeomAblineLayer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Geom Tile Layer</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Geom Tile Layer</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGeomTileLayer(GeomTileLayer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	@Override
	public T defaultCase(EObject object) {
		return null;
	}

} //GGPlotSwitch
