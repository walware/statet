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

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.EObject;

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
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see de.walware.statet.rtm.ggplot.GGPlotPackage
 * @generated
 */
public class GGPlotAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static GGPlotPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GGPlotAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = GGPlotPackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GGPlotSwitch<Adapter> modelSwitch =
		new GGPlotSwitch<Adapter>() {
			@Override
			public Adapter caseGGPlot(GGPlot object) {
				return createGGPlotAdapter();
			}
			@Override
			public Adapter caseLayer(Layer object) {
				return createLayerAdapter();
			}
			@Override
			public Adapter caseXVarLayer(XVarLayer object) {
				return createXVarLayerAdapter();
			}
			@Override
			public Adapter caseXYVarLayer(XYVarLayer object) {
				return createXYVarLayerAdapter();
			}
			@Override
			public Adapter caseGeomAblineLayer(GeomAblineLayer object) {
				return createGeomAblineLayerAdapter();
			}
			@Override
			public Adapter caseGeomBarLayer(GeomBarLayer object) {
				return createGeomBarLayerAdapter();
			}
			@Override
			public Adapter caseGeomBoxplotLayer(GeomBoxplotLayer object) {
				return createGeomBoxplotLayerAdapter();
			}
			@Override
			public Adapter caseGeomHistogramLayer(GeomHistogramLayer object) {
				return createGeomHistogramLayerAdapter();
			}
			@Override
			public Adapter caseGeomLineLayer(GeomLineLayer object) {
				return createGeomLineLayerAdapter();
			}
			@Override
			public Adapter caseGeomPointLayer(GeomPointLayer object) {
				return createGeomPointLayerAdapter();
			}
			@Override
			public Adapter caseGeomTextLayer(GeomTextLayer object) {
				return createGeomTextLayerAdapter();
			}
			@Override
			public Adapter caseGeomSmoothLayer(GeomSmoothLayer object) {
				return createGeomSmoothLayerAdapter();
			}
			@Override
			public Adapter caseGeomTileLayer(GeomTileLayer object) {
				return createGeomTileLayerAdapter();
			}
			@Override
			public Adapter caseGeomViolinLayer(GeomViolinLayer object) {
				return createGeomViolinLayerAdapter();
			}
			@Override
			public Adapter caseFacetLayout(FacetLayout object) {
				return createFacetLayoutAdapter();
			}
			@Override
			public Adapter caseGridFacetLayout(GridFacetLayout object) {
				return createGridFacetLayoutAdapter();
			}
			@Override
			public Adapter caseWrapFacetLayout(WrapFacetLayout object) {
				return createWrapFacetLayoutAdapter();
			}
			@Override
			public Adapter caseStat(Stat object) {
				return createStatAdapter();
			}
			@Override
			public Adapter caseIdentityStat(IdentityStat object) {
				return createIdentityStatAdapter();
			}
			@Override
			public Adapter caseSummaryStat(SummaryStat object) {
				return createSummaryStatAdapter();
			}
			@Override
			public Adapter caseTextStyle(TextStyle object) {
				return createTextStyleAdapter();
			}
			@Override
			public Adapter casePropDataProvider(PropDataProvider object) {
				return createPropDataProviderAdapter();
			}
			@Override
			public Adapter casePropXVarProvider(PropXVarProvider object) {
				return createPropXVarProviderAdapter();
			}
			@Override
			public Adapter casePropYVarProvider(PropYVarProvider object) {
				return createPropYVarProviderAdapter();
			}
			@Override
			public Adapter casePropStatProvider(PropStatProvider object) {
				return createPropStatProviderAdapter();
			}
			@Override
			public Adapter casePropGroupVarProvider(PropGroupVarProvider object) {
				return createPropGroupVarProviderAdapter();
			}
			@Override
			public Adapter casePropShapeProvider(PropShapeProvider object) {
				return createPropShapeProviderAdapter();
			}
			@Override
			public Adapter casePropLineTypeProvider(PropLineTypeProvider object) {
				return createPropLineTypeProviderAdapter();
			}
			@Override
			public Adapter casePropSizeProvider(PropSizeProvider object) {
				return createPropSizeProviderAdapter();
			}
			@Override
			public Adapter casePropColorProvider(PropColorProvider object) {
				return createPropColorProviderAdapter();
			}
			@Override
			public Adapter casePropFillProvider(PropFillProvider object) {
				return createPropFillProviderAdapter();
			}
			@Override
			public Adapter casePropAlphaProvider(PropAlphaProvider object) {
				return createPropAlphaProviderAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.GGPlot <em>GG Plot</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.GGPlot
	 * @generated
	 */
	public Adapter createGGPlotAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.Layer <em>Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.Layer
	 * @generated
	 */
	public Adapter createLayerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.XVarLayer <em>XVar Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.XVarLayer
	 * @generated
	 */
	public Adapter createXVarLayerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.XYVarLayer <em>XY Var Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.XYVarLayer
	 * @generated
	 */
	public Adapter createXYVarLayerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.GeomPointLayer <em>Geom Point Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.GeomPointLayer
	 * @generated
	 */
	public Adapter createGeomPointLayerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.GeomBarLayer <em>Geom Bar Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.GeomBarLayer
	 * @generated
	 */
	public Adapter createGeomBarLayerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.GeomTextLayer <em>Geom Text Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.GeomTextLayer
	 * @generated
	 */
	public Adapter createGeomTextLayerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.GeomSmoothLayer <em>Geom Smooth Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.GeomSmoothLayer
	 * @generated
	 */
	public Adapter createGeomSmoothLayerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.GeomViolinLayer <em>Geom Violin Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.GeomViolinLayer
	 * @generated
	 */
	public Adapter createGeomViolinLayerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.FacetLayout <em>Facet Layout</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.FacetLayout
	 * @generated
	 */
	public Adapter createFacetLayoutAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.GridFacetLayout <em>Grid Facet Layout</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.GridFacetLayout
	 * @generated
	 */
	public Adapter createGridFacetLayoutAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.WrapFacetLayout <em>Wrap Facet Layout</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.WrapFacetLayout
	 * @generated
	 */
	public Adapter createWrapFacetLayoutAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.TextStyle <em>Text Style</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.TextStyle
	 * @generated
	 */
	public Adapter createTextStyleAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.Stat <em>Stat</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.Stat
	 * @generated
	 */
	public Adapter createStatAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.IdentityStat <em>Identity Stat</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.IdentityStat
	 * @generated
	 */
	public Adapter createIdentityStatAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.SummaryStat <em>Summary Stat</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.SummaryStat
	 * @generated
	 */
	public Adapter createSummaryStatAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.PropDataProvider <em>Prop Data Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.PropDataProvider
	 * @generated
	 */
	public Adapter createPropDataProviderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.PropXVarProvider <em>Prop XVar Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.PropXVarProvider
	 * @generated
	 */
	public Adapter createPropXVarProviderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.PropYVarProvider <em>Prop YVar Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.PropYVarProvider
	 * @generated
	 */
	public Adapter createPropYVarProviderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.PropStatProvider <em>Prop Stat Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.PropStatProvider
	 * @generated
	 */
	public Adapter createPropStatProviderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.PropGroupVarProvider <em>Prop Group Var Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.PropGroupVarProvider
	 * @generated
	 */
	public Adapter createPropGroupVarProviderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.PropColorProvider <em>Prop Color Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.PropColorProvider
	 * @generated
	 */
	public Adapter createPropColorProviderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.PropFillProvider <em>Prop Fill Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.PropFillProvider
	 * @generated
	 */
	public Adapter createPropFillProviderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.PropAlphaProvider <em>Prop Alpha Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.PropAlphaProvider
	 * @generated
	 */
	public Adapter createPropAlphaProviderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.PropShapeProvider <em>Prop Shape Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.PropShapeProvider
	 * @generated
	 */
	public Adapter createPropShapeProviderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.PropLineTypeProvider <em>Prop Line Type Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.PropLineTypeProvider
	 * @generated
	 */
	public Adapter createPropLineTypeProviderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.PropSizeProvider <em>Prop Size Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.PropSizeProvider
	 * @generated
	 */
	public Adapter createPropSizeProviderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.GeomBoxplotLayer <em>Geom Boxplot Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.GeomBoxplotLayer
	 * @generated
	 */
	public Adapter createGeomBoxplotLayerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.GeomHistogramLayer <em>Geom Histogram Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.GeomHistogramLayer
	 * @generated
	 */
	public Adapter createGeomHistogramLayerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.GeomLineLayer <em>Geom Line Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.GeomLineLayer
	 * @generated
	 */
	public Adapter createGeomLineLayerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.GeomAblineLayer <em>Geom Abline Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.GeomAblineLayer
	 * @generated
	 */
	public Adapter createGeomAblineLayerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.walware.statet.rtm.ggplot.GeomTileLayer <em>Geom Tile Layer</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.walware.statet.rtm.ggplot.GeomTileLayer
	 * @generated
	 */
	public Adapter createGeomTileLayerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //GGPlotAdapterFactory
