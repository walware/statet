/**
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 * 
 */
package de.walware.statet.rtm.rtdata;

import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see de.walware.statet.rtm.rtdata.RtDataFactory
 * @model kind="package"
 * @generated
 */
public interface RtDataPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "rtdata"; //$NON-NLS-1$

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://walware.de/rtm/RtData/1.0"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "rtdata"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	RtDataPackage eINSTANCE = de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl.init();

	/**
	 * The meta object id for the '<em>RVar</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRVar()
	 * @generated
	 */
	int RVAR = 0;

	/**
	 * The meta object id for the '<em>RData Frame</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRDataFrame()
	 * @generated
	 */
	int RDATA_FRAME = 1;

	/**
	 * The meta object id for the '<em>RData Filter</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRDataFilter()
	 * @generated
	 */
	int RDATA_FILTER = 2;

	/**
	 * The meta object id for the '<em>RColor</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRColor()
	 * @generated
	 */
	int RCOLOR = 10;

	/**
	 * The meta object id for the '<em>RAlpha</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRAlpha()
	 * @generated
	 */
	int RALPHA = 11;

	/**
	 * The meta object id for the '<em>RPlot Point Symbol</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRPlotPointSymbol()
	 * @generated
	 */
	int RPLOT_POINT_SYMBOL = 12;

	/**
	 * The meta object id for the '<em>RPlot Line Type</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRPlotLineType()
	 * @generated
	 */
	int RPLOT_LINE_TYPE = 13;

	/**
	 * The meta object id for the '<em>RFont Family</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRFontFamily()
	 * @generated
	 */
	int RFONT_FAMILY = 14;

	/**
	 * The meta object id for the '<em>RText</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRText()
	 * @generated
	 */
	int RTEXT = 3;

	/**
	 * The meta object id for the '<em>RNum</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRNum()
	 * @generated
	 */
	int RNUM = 4;

	/**
	 * The meta object id for the '<em>RSize</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRSize()
	 * @generated
	 */
	int RSIZE = 9;


	/**
	 * The meta object id for the '<em>RInt</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRInt()
	 * @generated
	 */
	int RINT = 5;

	/**
	 * The meta object id for the '<em>RFunction</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRFunction()
	 * @generated
	 */
	int RFUNCTION = 6;

	/**
	 * The meta object id for the '<em>RLabel</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRLabel()
	 * @generated
	 */
	int RLABEL = 7;

	/**
	 * The meta object id for the '<em>RNum Range</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRNumRange()
	 * @generated
	 */
	int RNUM_RANGE = 8;


	/**
	 * Returns the meta object for data type '{@link de.walware.statet.rtm.rtdata.types.RTypedExpr <em>RVar</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>RVar</em>'.
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @model instanceClass="de.walware.statet.rtm.rtdata.types.RTypedExpr"
	 * @generated
	 */
	EDataType getRVar();

	/**
	 * Returns the meta object for data type '{@link de.walware.statet.rtm.rtdata.types.RTypedExpr <em>RData Frame</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>RData Frame</em>'.
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @model instanceClass="de.walware.statet.rtm.rtdata.types.RTypedExpr"
	 * @generated
	 */
	EDataType getRDataFrame();

	/**
	 * Returns the meta object for data type '{@link de.walware.statet.rtm.rtdata.types.RTypedExpr <em>RData Filter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>RData Filter</em>'.
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @model instanceClass="de.walware.statet.rtm.rtdata.types.RTypedExpr"
	 * @generated
	 */
	EDataType getRDataFilter();

	/**
	 * Returns the meta object for data type '{@link de.walware.statet.rtm.rtdata.types.RTypedExpr <em>RColor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>RColor</em>'.
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @model instanceClass="de.walware.statet.rtm.rtdata.types.RTypedExpr"
	 * @generated
	 */
	EDataType getRColor();

	/**
	 * Returns the meta object for data type '{@link de.walware.statet.rtm.rtdata.types.RTypedExpr <em>RAlpha</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>RAlpha</em>'.
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @model instanceClass="de.walware.statet.rtm.rtdata.types.RTypedExpr"
	 * @generated
	 */
	EDataType getRAlpha();

	/**
	 * Returns the meta object for data type '{@link de.walware.statet.rtm.rtdata.types.RTypedExpr <em>RPlot Point Symbol</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>RPlot Point Symbol</em>'.
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @model instanceClass="de.walware.statet.rtm.rtdata.types.RTypedExpr"
	 * @generated
	 */
	EDataType getRPlotPointSymbol();

	/**
	 * Returns the meta object for data type '{@link de.walware.statet.rtm.rtdata.types.RTypedExpr <em>RPlot Line Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>RPlot Line Type</em>'.
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @model instanceClass="de.walware.statet.rtm.rtdata.types.RTypedExpr"
	 * @generated
	 */
	EDataType getRPlotLineType();

	/**
	 * Returns the meta object for data type '{@link de.walware.statet.rtm.rtdata.types.RTypedExpr <em>RFont Family</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>RFont Family</em>'.
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @model instanceClass="de.walware.statet.rtm.rtdata.types.RTypedExpr"
	 * @generated
	 */
	EDataType getRFontFamily();

	/**
	 * Returns the meta object for data type '{@link de.walware.statet.rtm.rtdata.types.RTypedExpr <em>RText</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>RText</em>'.
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @model instanceClass="de.walware.statet.rtm.rtdata.types.RTypedExpr"
	 * @generated
	 */
	EDataType getRText();

	/**
	 * Returns the meta object for data type '{@link de.walware.statet.rtm.rtdata.types.RTypedExpr <em>RNum</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>RNum</em>'.
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @model instanceClass="de.walware.statet.rtm.rtdata.types.RTypedExpr"
	 * @generated
	 */
	EDataType getRNum();

	/**
	 * Returns the meta object for data type '{@link de.walware.statet.rtm.rtdata.types.RTypedExpr <em>RSize</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>RSize</em>'.
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @model instanceClass="de.walware.statet.rtm.rtdata.types.RTypedExpr"
	 * @generated
	 */
	EDataType getRSize();

	/**
	 * Returns the meta object for data type '{@link de.walware.statet.rtm.rtdata.types.RTypedExpr <em>RInt</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>RInt</em>'.
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @model instanceClass="de.walware.statet.rtm.rtdata.types.RTypedExpr"
	 * @generated
	 */
	EDataType getRInt();

	/**
	 * Returns the meta object for data type '{@link de.walware.statet.rtm.rtdata.types.RTypedExpr <em>RFunction</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>RFunction</em>'.
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @model instanceClass="de.walware.statet.rtm.rtdata.types.RTypedExpr"
	 * @generated
	 */
	EDataType getRFunction();

	/**
	 * Returns the meta object for data type '{@link de.walware.statet.rtm.rtdata.types.RTypedExpr <em>RLabel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>RLabel</em>'.
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @model instanceClass="de.walware.statet.rtm.rtdata.types.RTypedExpr"
	 * @generated
	 */
	EDataType getRLabel();

	/**
	 * Returns the meta object for data type '{@link de.walware.statet.rtm.rtdata.types.RTypedExpr <em>RNum Range</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>RNum Range</em>'.
	 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
	 * @model instanceClass="de.walware.statet.rtm.rtdata.types.RTypedExpr"
	 * @generated
	 */
	EDataType getRNumRange();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	RtDataFactory getRtDataFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '<em>RVar</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
		 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRVar()
		 * @generated
		 */
		EDataType RVAR = eINSTANCE.getRVar();

		/**
		 * The meta object literal for the '<em>RData Frame</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
		 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRDataFrame()
		 * @generated
		 */
		EDataType RDATA_FRAME = eINSTANCE.getRDataFrame();

		/**
		 * The meta object literal for the '<em>RData Filter</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
		 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRDataFilter()
		 * @generated
		 */
		EDataType RDATA_FILTER = eINSTANCE.getRDataFilter();

		/**
		 * The meta object literal for the '<em>RColor</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
		 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRColor()
		 * @generated
		 */
		EDataType RCOLOR = eINSTANCE.getRColor();

		/**
		 * The meta object literal for the '<em>RAlpha</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
		 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRAlpha()
		 * @generated
		 */
		EDataType RALPHA = eINSTANCE.getRAlpha();

		/**
		 * The meta object literal for the '<em>RPlot Point Symbol</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
		 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRPlotPointSymbol()
		 * @generated
		 */
		EDataType RPLOT_POINT_SYMBOL = eINSTANCE.getRPlotPointSymbol();

		/**
		 * The meta object literal for the '<em>RPlot Line Type</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
		 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRPlotLineType()
		 * @generated
		 */
		EDataType RPLOT_LINE_TYPE = eINSTANCE.getRPlotLineType();

		/**
		 * The meta object literal for the '<em>RFont Family</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
		 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRFontFamily()
		 * @generated
		 */
		EDataType RFONT_FAMILY = eINSTANCE.getRFontFamily();

		/**
		 * The meta object literal for the '<em>RText</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
		 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRText()
		 * @generated
		 */
		EDataType RTEXT = eINSTANCE.getRText();

		/**
		 * The meta object literal for the '<em>RNum</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
		 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRNum()
		 * @generated
		 */
		EDataType RNUM = eINSTANCE.getRNum();

		/**
		 * The meta object literal for the '<em>RSize</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
		 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRSize()
		 * @generated
		 */
		EDataType RSIZE = eINSTANCE.getRSize();

		/**
		 * The meta object literal for the '<em>RInt</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
		 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRInt()
		 * @generated
		 */
		EDataType RINT = eINSTANCE.getRInt();

		/**
		 * The meta object literal for the '<em>RFunction</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
		 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRFunction()
		 * @generated
		 */
		EDataType RFUNCTION = eINSTANCE.getRFunction();

		/**
		 * The meta object literal for the '<em>RLabel</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
		 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRLabel()
		 * @generated
		 */
		EDataType RLABEL = eINSTANCE.getRLabel();

		/**
		 * The meta object literal for the '<em>RNum Range</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.rtdata.types.RTypedExpr
		 * @see de.walware.statet.rtm.rtdata.impl.RtDataPackageImpl#getRNumRange()
		 * @generated
		 */
		EDataType RNUM_RANGE = eINSTANCE.getRNumRange();

	}

} //RtDataPackage
