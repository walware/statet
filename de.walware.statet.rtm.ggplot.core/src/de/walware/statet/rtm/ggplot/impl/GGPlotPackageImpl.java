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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import de.walware.statet.rtm.ggplot.FacetLayout;
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
import de.walware.statet.rtm.rtdata.RtDataPackage;


/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class GGPlotPackageImpl extends EPackageImpl implements GGPlotPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass ggPlotEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass layerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass xVarLayerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass xyVarLayerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass geomPointLayerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass geomBarLayerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass geomTextLayerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass geomSmoothLayerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass geomViolinLayerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass facetLayoutEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass gridFacetLayoutEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass wrapFacetLayoutEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass textStyleEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass statEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass identityStatEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass summaryStatEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass propDataProviderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass propXVarProviderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass propYVarProviderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass propStatProviderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass propGroupVarProviderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass propColorProviderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass propFillProviderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass propAlphaProviderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass propShapeProviderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass propLineTypeProviderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass propSizeProviderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass geomBoxplotLayerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass geomHistogramLayerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass geomLineLayerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass geomAblineLayerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass geomTileLayerEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private GGPlotPackageImpl() {
		super(eNS_URI, GGPlotFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * 
	 * <p>This method is used to initialize {@link GGPlotPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static GGPlotPackage init() {
		if (isInited) return (GGPlotPackage)EPackage.Registry.INSTANCE.getEPackage(GGPlotPackage.eNS_URI);

		// Obtain or create and register package
		GGPlotPackageImpl theGGPlotPackage = (GGPlotPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof GGPlotPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new GGPlotPackageImpl());

		isInited = true;

		// Initialize simple dependencies
		RtDataPackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theGGPlotPackage.createPackageContents();

		// Initialize created meta-data
		theGGPlotPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theGGPlotPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(GGPlotPackage.eNS_URI, theGGPlotPackage);
		return theGGPlotPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGGPlot() {
		return ggPlotEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGGPlot_DataFilter() {
		return (EAttribute)ggPlotEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGGPlot_MainTitle() {
		return (EAttribute)ggPlotEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGGPlot_MainTitleStyle() {
		return (EReference)ggPlotEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGGPlot_Facet() {
		return (EReference)ggPlotEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGGPlot_AxXLim() {
		return (EAttribute)ggPlotEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGGPlot_AxYLim() {
		return (EAttribute)ggPlotEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGGPlot_AxXLabel() {
		return (EAttribute)ggPlotEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGGPlot_AxYLabel() {
		return (EAttribute)ggPlotEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGGPlot_AxXLabelStyle() {
		return (EReference)ggPlotEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGGPlot_AxYLabelStyle() {
		return (EReference)ggPlotEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getGGPlot_AxXTextStyle() {
		return (EReference)ggPlotEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getGGPlot_AxYTextStyle() {
		return (EReference)ggPlotEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGGPlot_Layers() {
		return (EReference)ggPlotEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getLayer() {
		return layerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getXVarLayer() {
		return xVarLayerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getXYVarLayer() {
		return xyVarLayerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGeomPointLayer() {
		return geomPointLayerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGeomPointLayer_PositionXJitter() {
		return (EAttribute)geomPointLayerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGeomPointLayer_PositionYJitter() {
		return (EAttribute)geomPointLayerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGeomBarLayer() {
		return geomBarLayerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGeomTextLayer() {
		return geomTextLayerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGeomTextLayer_Label() {
		return (EAttribute)geomTextLayerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGeomSmoothLayer() {
		return geomSmoothLayerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGeomViolinLayer() {
		return geomViolinLayerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getFacetLayout() {
		return facetLayoutEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getGridFacetLayout() {
		return gridFacetLayoutEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGridFacetLayout_ColVars() {
		return (EAttribute)gridFacetLayoutEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGridFacetLayout_RowVars() {
		return (EAttribute)gridFacetLayoutEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getWrapFacetLayout() {
		return wrapFacetLayoutEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getWrapFacetLayout_ColVars() {
		return (EAttribute)wrapFacetLayoutEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getWrapFacetLayout_ColNum() {
		return (EAttribute)wrapFacetLayoutEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getTextStyle() {
		return textStyleEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTextStyle_FontFamily() {
		return (EAttribute)textStyleEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTextStyle_FontFace() {
		return (EAttribute)textStyleEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTextStyle_HJust() {
		return (EAttribute)textStyleEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTextStyle_VJust() {
		return (EAttribute)textStyleEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTextStyle_Angle() {
		return (EAttribute)textStyleEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getStat() {
		return statEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIdentityStat() {
		return identityStatEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getSummaryStat() {
		return summaryStatEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getSummaryStat_YFun() {
		return (EAttribute)summaryStatEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPropDataProvider() {
		return propDataProviderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPropDataProvider_Data() {
		return (EAttribute)propDataProviderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPropXVarProvider() {
		return propXVarProviderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPropXVarProvider_XVar() {
		return (EAttribute)propXVarProviderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPropYVarProvider() {
		return propYVarProviderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPropYVarProvider_YVar() {
		return (EAttribute)propYVarProviderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPropStatProvider() {
		return propStatProviderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getPropStatProvider_Stat() {
		return (EReference)propStatProviderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPropGroupVarProvider() {
		return propGroupVarProviderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPropGroupVarProvider_GroupVar() {
		return (EAttribute)propGroupVarProviderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPropColorProvider() {
		return propColorProviderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPropColorProvider_Color() {
		return (EAttribute)propColorProviderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPropFillProvider() {
		return propFillProviderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPropFillProvider_Fill() {
		return (EAttribute)propFillProviderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPropAlphaProvider() {
		return propAlphaProviderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPropAlphaProvider_Alpha() {
		return (EAttribute)propAlphaProviderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPropShapeProvider() {
		return propShapeProviderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPropShapeProvider_Shape() {
		return (EAttribute)propShapeProviderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPropLineTypeProvider() {
		return propLineTypeProviderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPropLineTypeProvider_LineType() {
		return (EAttribute)propLineTypeProviderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPropSizeProvider() {
		return propSizeProviderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPropSizeProvider_Size() {
		return (EAttribute)propSizeProviderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGeomBoxplotLayer() {
		return geomBoxplotLayerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGeomHistogramLayer() {
		return geomHistogramLayerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGeomLineLayer() {
		return geomLineLayerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGeomAblineLayer() {
		return geomAblineLayerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGeomAblineLayer_InterceptVar() {
		return (EAttribute)geomAblineLayerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGeomAblineLayer_SlopeVar() {
		return (EAttribute)geomAblineLayerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGeomTileLayer() {
		return geomTileLayerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GGPlotFactory getGGPlotFactory() {
		return (GGPlotFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		ggPlotEClass = createEClass(GG_PLOT);
		createEAttribute(ggPlotEClass, GG_PLOT__DATA_FILTER);
		createEAttribute(ggPlotEClass, GG_PLOT__MAIN_TITLE);
		createEReference(ggPlotEClass, GG_PLOT__MAIN_TITLE_STYLE);
		createEReference(ggPlotEClass, GG_PLOT__FACET);
		createEAttribute(ggPlotEClass, GG_PLOT__AX_XLIM);
		createEAttribute(ggPlotEClass, GG_PLOT__AX_YLIM);
		createEAttribute(ggPlotEClass, GG_PLOT__AX_XLABEL);
		createEAttribute(ggPlotEClass, GG_PLOT__AX_YLABEL);
		createEReference(ggPlotEClass, GG_PLOT__AX_XLABEL_STYLE);
		createEReference(ggPlotEClass, GG_PLOT__AX_YLABEL_STYLE);
		createEReference(ggPlotEClass, GG_PLOT__AX_XTEXT_STYLE);
		createEReference(ggPlotEClass, GG_PLOT__AX_YTEXT_STYLE);
		createEReference(ggPlotEClass, GG_PLOT__LAYERS);

		layerEClass = createEClass(LAYER);

		xVarLayerEClass = createEClass(XVAR_LAYER);

		xyVarLayerEClass = createEClass(XY_VAR_LAYER);

		geomAblineLayerEClass = createEClass(GEOM_ABLINE_LAYER);
		createEAttribute(geomAblineLayerEClass, GEOM_ABLINE_LAYER__INTERCEPT_VAR);
		createEAttribute(geomAblineLayerEClass, GEOM_ABLINE_LAYER__SLOPE_VAR);

		geomBarLayerEClass = createEClass(GEOM_BAR_LAYER);

		geomBoxplotLayerEClass = createEClass(GEOM_BOXPLOT_LAYER);

		geomHistogramLayerEClass = createEClass(GEOM_HISTOGRAM_LAYER);

		geomLineLayerEClass = createEClass(GEOM_LINE_LAYER);

		geomPointLayerEClass = createEClass(GEOM_POINT_LAYER);
		createEAttribute(geomPointLayerEClass, GEOM_POINT_LAYER__POSITION_XJITTER);
		createEAttribute(geomPointLayerEClass, GEOM_POINT_LAYER__POSITION_YJITTER);

		geomTextLayerEClass = createEClass(GEOM_TEXT_LAYER);
		createEAttribute(geomTextLayerEClass, GEOM_TEXT_LAYER__LABEL);

		geomSmoothLayerEClass = createEClass(GEOM_SMOOTH_LAYER);

		geomTileLayerEClass = createEClass(GEOM_TILE_LAYER);

		geomViolinLayerEClass = createEClass(GEOM_VIOLIN_LAYER);

		facetLayoutEClass = createEClass(FACET_LAYOUT);

		gridFacetLayoutEClass = createEClass(GRID_FACET_LAYOUT);
		createEAttribute(gridFacetLayoutEClass, GRID_FACET_LAYOUT__COL_VARS);
		createEAttribute(gridFacetLayoutEClass, GRID_FACET_LAYOUT__ROW_VARS);

		wrapFacetLayoutEClass = createEClass(WRAP_FACET_LAYOUT);
		createEAttribute(wrapFacetLayoutEClass, WRAP_FACET_LAYOUT__COL_VARS);
		createEAttribute(wrapFacetLayoutEClass, WRAP_FACET_LAYOUT__COL_NUM);

		statEClass = createEClass(STAT);

		identityStatEClass = createEClass(IDENTITY_STAT);

		summaryStatEClass = createEClass(SUMMARY_STAT);
		createEAttribute(summaryStatEClass, SUMMARY_STAT__YFUN);

		textStyleEClass = createEClass(TEXT_STYLE);
		createEAttribute(textStyleEClass, TEXT_STYLE__FONT_FAMILY);
		createEAttribute(textStyleEClass, TEXT_STYLE__FONT_FACE);
		createEAttribute(textStyleEClass, TEXT_STYLE__HJUST);
		createEAttribute(textStyleEClass, TEXT_STYLE__VJUST);
		createEAttribute(textStyleEClass, TEXT_STYLE__ANGLE);

		propDataProviderEClass = createEClass(PROP_DATA_PROVIDER);
		createEAttribute(propDataProviderEClass, PROP_DATA_PROVIDER__DATA);

		propXVarProviderEClass = createEClass(PROP_XVAR_PROVIDER);
		createEAttribute(propXVarProviderEClass, PROP_XVAR_PROVIDER__XVAR);

		propYVarProviderEClass = createEClass(PROP_YVAR_PROVIDER);
		createEAttribute(propYVarProviderEClass, PROP_YVAR_PROVIDER__YVAR);

		propStatProviderEClass = createEClass(PROP_STAT_PROVIDER);
		createEReference(propStatProviderEClass, PROP_STAT_PROVIDER__STAT);

		propGroupVarProviderEClass = createEClass(PROP_GROUP_VAR_PROVIDER);
		createEAttribute(propGroupVarProviderEClass, PROP_GROUP_VAR_PROVIDER__GROUP_VAR);

		propShapeProviderEClass = createEClass(PROP_SHAPE_PROVIDER);
		createEAttribute(propShapeProviderEClass, PROP_SHAPE_PROVIDER__SHAPE);

		propLineTypeProviderEClass = createEClass(PROP_LINE_TYPE_PROVIDER);
		createEAttribute(propLineTypeProviderEClass, PROP_LINE_TYPE_PROVIDER__LINE_TYPE);

		propSizeProviderEClass = createEClass(PROP_SIZE_PROVIDER);
		createEAttribute(propSizeProviderEClass, PROP_SIZE_PROVIDER__SIZE);

		propColorProviderEClass = createEClass(PROP_COLOR_PROVIDER);
		createEAttribute(propColorProviderEClass, PROP_COLOR_PROVIDER__COLOR);

		propFillProviderEClass = createEClass(PROP_FILL_PROVIDER);
		createEAttribute(propFillProviderEClass, PROP_FILL_PROVIDER__FILL);

		propAlphaProviderEClass = createEClass(PROP_ALPHA_PROVIDER);
		createEAttribute(propAlphaProviderEClass, PROP_ALPHA_PROVIDER__ALPHA);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		RtDataPackage theRtDataPackage = (RtDataPackage)EPackage.Registry.INSTANCE.getEPackage(RtDataPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		ggPlotEClass.getESuperTypes().add(this.getPropDataProvider());
		ggPlotEClass.getESuperTypes().add(this.getPropXVarProvider());
		ggPlotEClass.getESuperTypes().add(this.getPropYVarProvider());
		layerEClass.getESuperTypes().add(this.getPropDataProvider());
		xVarLayerEClass.getESuperTypes().add(this.getLayer());
		xVarLayerEClass.getESuperTypes().add(this.getPropXVarProvider());
		xVarLayerEClass.getESuperTypes().add(this.getPropGroupVarProvider());
		xyVarLayerEClass.getESuperTypes().add(this.getLayer());
		xyVarLayerEClass.getESuperTypes().add(this.getPropXVarProvider());
		xyVarLayerEClass.getESuperTypes().add(this.getPropYVarProvider());
		xyVarLayerEClass.getESuperTypes().add(this.getPropGroupVarProvider());
		geomAblineLayerEClass.getESuperTypes().add(this.getLayer());
		geomAblineLayerEClass.getESuperTypes().add(this.getPropLineTypeProvider());
		geomAblineLayerEClass.getESuperTypes().add(this.getPropSizeProvider());
		geomAblineLayerEClass.getESuperTypes().add(this.getPropColorProvider());
		geomAblineLayerEClass.getESuperTypes().add(this.getPropAlphaProvider());
		geomBarLayerEClass.getESuperTypes().add(this.getXYVarLayer());
		geomBarLayerEClass.getESuperTypes().add(this.getPropStatProvider());
		geomBarLayerEClass.getESuperTypes().add(this.getPropColorProvider());
		geomBarLayerEClass.getESuperTypes().add(this.getPropFillProvider());
		geomBarLayerEClass.getESuperTypes().add(this.getPropAlphaProvider());
		geomBoxplotLayerEClass.getESuperTypes().add(this.getXVarLayer());
		geomBoxplotLayerEClass.getESuperTypes().add(this.getPropColorProvider());
		geomBoxplotLayerEClass.getESuperTypes().add(this.getPropFillProvider());
		geomBoxplotLayerEClass.getESuperTypes().add(this.getPropAlphaProvider());
		geomHistogramLayerEClass.getESuperTypes().add(this.getXVarLayer());
		geomHistogramLayerEClass.getESuperTypes().add(this.getPropColorProvider());
		geomHistogramLayerEClass.getESuperTypes().add(this.getPropFillProvider());
		geomHistogramLayerEClass.getESuperTypes().add(this.getPropAlphaProvider());
		geomLineLayerEClass.getESuperTypes().add(this.getXYVarLayer());
		geomLineLayerEClass.getESuperTypes().add(this.getPropStatProvider());
		geomLineLayerEClass.getESuperTypes().add(this.getPropLineTypeProvider());
		geomLineLayerEClass.getESuperTypes().add(this.getPropSizeProvider());
		geomLineLayerEClass.getESuperTypes().add(this.getPropColorProvider());
		geomLineLayerEClass.getESuperTypes().add(this.getPropAlphaProvider());
		geomPointLayerEClass.getESuperTypes().add(this.getXYVarLayer());
		geomPointLayerEClass.getESuperTypes().add(this.getPropShapeProvider());
		geomPointLayerEClass.getESuperTypes().add(this.getPropSizeProvider());
		geomPointLayerEClass.getESuperTypes().add(this.getPropColorProvider());
		geomPointLayerEClass.getESuperTypes().add(this.getPropFillProvider());
		geomPointLayerEClass.getESuperTypes().add(this.getPropAlphaProvider());
		geomTextLayerEClass.getESuperTypes().add(this.getXYVarLayer());
		geomTextLayerEClass.getESuperTypes().add(this.getTextStyle());
		geomTextLayerEClass.getESuperTypes().add(this.getPropAlphaProvider());
		geomSmoothLayerEClass.getESuperTypes().add(this.getXYVarLayer());
		geomSmoothLayerEClass.getESuperTypes().add(this.getPropSizeProvider());
		geomSmoothLayerEClass.getESuperTypes().add(this.getPropColorProvider());
		geomSmoothLayerEClass.getESuperTypes().add(this.getPropFillProvider());
		geomSmoothLayerEClass.getESuperTypes().add(this.getPropAlphaProvider());
		geomTileLayerEClass.getESuperTypes().add(this.getXYVarLayer());
		geomTileLayerEClass.getESuperTypes().add(this.getPropLineTypeProvider());
		geomTileLayerEClass.getESuperTypes().add(this.getPropColorProvider());
		geomTileLayerEClass.getESuperTypes().add(this.getPropFillProvider());
		geomTileLayerEClass.getESuperTypes().add(this.getPropAlphaProvider());
		geomViolinLayerEClass.getESuperTypes().add(this.getXYVarLayer());
		geomViolinLayerEClass.getESuperTypes().add(this.getPropLineTypeProvider());
		geomViolinLayerEClass.getESuperTypes().add(this.getPropColorProvider());
		geomViolinLayerEClass.getESuperTypes().add(this.getPropFillProvider());
		geomViolinLayerEClass.getESuperTypes().add(this.getPropAlphaProvider());
		gridFacetLayoutEClass.getESuperTypes().add(this.getFacetLayout());
		wrapFacetLayoutEClass.getESuperTypes().add(this.getFacetLayout());
		identityStatEClass.getESuperTypes().add(this.getStat());
		summaryStatEClass.getESuperTypes().add(this.getStat());
		textStyleEClass.getESuperTypes().add(this.getPropSizeProvider());
		textStyleEClass.getESuperTypes().add(this.getPropColorProvider());

		// Initialize classes, features, and operations; add parameters
		initEClass(ggPlotEClass, GGPlot.class, "GGPlot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getGGPlot_DataFilter(), theRtDataPackage.getRDataFilter(), "dataFilter", null, 0, 1, GGPlot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGGPlot_MainTitle(), theRtDataPackage.getRLabel(), "mainTitle", null, 0, 1, GGPlot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getGGPlot_MainTitleStyle(), this.getTextStyle(), null, "mainTitleStyle", null, 1, 1, GGPlot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getGGPlot_Facet(), this.getFacetLayout(), null, "facet", null, 0, 1, GGPlot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGGPlot_AxXLim(), theRtDataPackage.getRNumRange(), "axXLim", null, 0, 1, GGPlot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGGPlot_AxYLim(), theRtDataPackage.getRNumRange(), "axYLim", null, 0, 1, GGPlot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGGPlot_AxXLabel(), theRtDataPackage.getRLabel(), "axXLabel", null, 0, 1, GGPlot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGGPlot_AxYLabel(), theRtDataPackage.getRLabel(), "axYLabel", null, 0, 1, GGPlot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED); //$NON-NLS-1$
		initEReference(getGGPlot_AxXLabelStyle(), this.getTextStyle(), null, "axXLabelStyle", null, 1, 1, GGPlot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getGGPlot_AxYLabelStyle(), this.getTextStyle(), null, "axYLabelStyle", null, 1, 1, GGPlot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getGGPlot_AxXTextStyle(), this.getTextStyle(), null, "axXTextStyle", null, 1, 1, GGPlot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getGGPlot_AxYTextStyle(), this.getTextStyle(), null, "axYTextStyle", null, 1, 1, GGPlot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getGGPlot_Layers(), this.getLayer(), null, "layers", null, 0, -1, GGPlot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(layerEClass, Layer.class, "Layer", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(xVarLayerEClass, XVarLayer.class, "XVarLayer", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(xyVarLayerEClass, XYVarLayer.class, "XYVarLayer", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(geomAblineLayerEClass, GeomAblineLayer.class, "GeomAblineLayer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getGeomAblineLayer_InterceptVar(), theRtDataPackage.getRNum(), "interceptVar", null, 0, 1, GeomAblineLayer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGeomAblineLayer_SlopeVar(), theRtDataPackage.getRNum(), "slopeVar", null, 0, 1, GeomAblineLayer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(geomBarLayerEClass, GeomBarLayer.class, "GeomBarLayer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(geomBoxplotLayerEClass, GeomBoxplotLayer.class, "GeomBoxplotLayer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(geomHistogramLayerEClass, GeomHistogramLayer.class, "GeomHistogramLayer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(geomLineLayerEClass, GeomLineLayer.class, "GeomLineLayer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(geomPointLayerEClass, GeomPointLayer.class, "GeomPointLayer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getGeomPointLayer_PositionXJitter(), theRtDataPackage.getRNum(), "positionXJitter", null, 0, 1, GeomPointLayer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGeomPointLayer_PositionYJitter(), theRtDataPackage.getRNum(), "positionYJitter", null, 0, 1, GeomPointLayer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(geomTextLayerEClass, GeomTextLayer.class, "GeomTextLayer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getGeomTextLayer_Label(), theRtDataPackage.getRVar(), "label", null, 0, 1, GeomTextLayer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(geomSmoothLayerEClass, GeomSmoothLayer.class, "GeomSmoothLayer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(geomTileLayerEClass, GeomTileLayer.class, "GeomTileLayer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(geomViolinLayerEClass, GeomViolinLayer.class, "GeomViolinLayer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(facetLayoutEClass, FacetLayout.class, "FacetLayout", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(gridFacetLayoutEClass, GridFacetLayout.class, "GridFacetLayout", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getGridFacetLayout_ColVars(), theRtDataPackage.getRVar(), "colVars", null, 0, -1, GridFacetLayout.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGridFacetLayout_RowVars(), theRtDataPackage.getRVar(), "rowVars", null, 0, -1, GridFacetLayout.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(wrapFacetLayoutEClass, WrapFacetLayout.class, "WrapFacetLayout", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getWrapFacetLayout_ColVars(), theRtDataPackage.getRVar(), "colVars", null, 0, -1, WrapFacetLayout.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getWrapFacetLayout_ColNum(), theRtDataPackage.getRInt(), "colNum", null, 0, 1, WrapFacetLayout.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(statEClass, Stat.class, "Stat", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(identityStatEClass, IdentityStat.class, "IdentityStat", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(summaryStatEClass, SummaryStat.class, "SummaryStat", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getSummaryStat_YFun(), theRtDataPackage.getRFunction(), "yFun", null, 0, 1, SummaryStat.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(textStyleEClass, TextStyle.class, "TextStyle", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getTextStyle_FontFamily(), theRtDataPackage.getRFontFamily(), "fontFamily", null, 0, 1, TextStyle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getTextStyle_FontFace(), theRtDataPackage.getRText(), "fontFace", null, 0, 1, TextStyle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getTextStyle_HJust(), theRtDataPackage.getRNum(), "hJust", null, 0, 1, TextStyle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getTextStyle_VJust(), theRtDataPackage.getRNum(), "vJust", null, 0, 1, TextStyle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getTextStyle_Angle(), theRtDataPackage.getRNum(), "angle", null, 0, 1, TextStyle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(propDataProviderEClass, PropDataProvider.class, "PropDataProvider", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getPropDataProvider_Data(), theRtDataPackage.getRDataFrame(), "data", null, 0, 1, PropDataProvider.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(propXVarProviderEClass, PropXVarProvider.class, "PropXVarProvider", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getPropXVarProvider_XVar(), theRtDataPackage.getRVar(), "xVar", null, 0, 1, PropXVarProvider.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(propYVarProviderEClass, PropYVarProvider.class, "PropYVarProvider", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getPropYVarProvider_YVar(), theRtDataPackage.getRVar(), "yVar", null, 0, 1, PropYVarProvider.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(propStatProviderEClass, PropStatProvider.class, "PropStatProvider", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getPropStatProvider_Stat(), this.getStat(), null, "stat", null, 0, 1, PropStatProvider.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(propGroupVarProviderEClass, PropGroupVarProvider.class, "PropGroupVarProvider", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getPropGroupVarProvider_GroupVar(), theRtDataPackage.getRVar(), "groupVar", null, 0, 1, PropGroupVarProvider.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(propShapeProviderEClass, PropShapeProvider.class, "PropShapeProvider", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getPropShapeProvider_Shape(), theRtDataPackage.getRPlotPointSymbol(), "shape", null, 0, 1, PropShapeProvider.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(propLineTypeProviderEClass, PropLineTypeProvider.class, "PropLineTypeProvider", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getPropLineTypeProvider_LineType(), theRtDataPackage.getRPlotLineType(), "lineType", null, 0, 1, PropLineTypeProvider.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(propSizeProviderEClass, PropSizeProvider.class, "PropSizeProvider", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getPropSizeProvider_Size(), theRtDataPackage.getRSize(), "size", null, 0, 1, PropSizeProvider.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(propColorProviderEClass, PropColorProvider.class, "PropColorProvider", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getPropColorProvider_Color(), theRtDataPackage.getRColor(), "color", "", 0, 1, PropColorProvider.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$

		initEClass(propFillProviderEClass, PropFillProvider.class, "PropFillProvider", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getPropFillProvider_Fill(), theRtDataPackage.getRColor(), "fill", "", 0, 1, PropFillProvider.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$

		initEClass(propAlphaProviderEClass, PropAlphaProvider.class, "PropAlphaProvider", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getPropAlphaProvider_Alpha(), theRtDataPackage.getRAlpha(), "alpha", "", 0, 1, PropAlphaProvider.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$

		// Create resource
		createResource(eNS_URI);
	}

} //GGPlotPackageImpl
