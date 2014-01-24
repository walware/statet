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

package de.walware.statet.rtm.rtdata.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;

import de.walware.statet.rtm.rtdata.RtDataFactory;
import de.walware.statet.rtm.rtdata.RtDataPackage;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;


/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class RtDataFactoryImpl extends EFactoryImpl implements RtDataFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static RtDataFactory init() {
		try {
			RtDataFactory theRtDataFactory = (RtDataFactory)EPackage.Registry.INSTANCE.getEFactory("http://walware.de/rtm/RtData/1.0"); //$NON-NLS-1$ 
			if (theRtDataFactory != null) {
				return theRtDataFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new RtDataFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RtDataFactoryImpl() {
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
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case RtDataPackage.RVAR:
				return createRVarFromString(eDataType, initialValue);
			case RtDataPackage.RDATA_FRAME:
				return createRDataFrameFromString(eDataType, initialValue);
			case RtDataPackage.RDATA_FILTER:
				return createRDataFilterFromString(eDataType, initialValue);
			case RtDataPackage.RTEXT:
				return createRTextFromString(eDataType, initialValue);
			case RtDataPackage.RNUM:
				return createRNumFromString(eDataType, initialValue);
			case RtDataPackage.RINT:
				return createRIntFromString(eDataType, initialValue);
			case RtDataPackage.RFUNCTION:
				return createRFunctionFromString(eDataType, initialValue);
			case RtDataPackage.RLABEL:
				return createRLabelFromString(eDataType, initialValue);
			case RtDataPackage.RNUM_RANGE:
				return createRNumRangeFromString(eDataType, initialValue);
			case RtDataPackage.RSIZE:
				return createRSizeFromString(eDataType, initialValue);
			case RtDataPackage.RCOLOR:
				return createRColorFromString(eDataType, initialValue);
			case RtDataPackage.RALPHA:
				return createRAlphaFromString(eDataType, initialValue);
			case RtDataPackage.RPLOT_POINT_SYMBOL:
				return createRPlotPointSymbolFromString(eDataType, initialValue);
			case RtDataPackage.RPLOT_LINE_TYPE:
				return createRPlotLineTypeFromString(eDataType, initialValue);
			case RtDataPackage.RFONT_FAMILY:
				return createRFontFamilyFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case RtDataPackage.RVAR:
				return convertRVarToString(eDataType, instanceValue);
			case RtDataPackage.RDATA_FRAME:
				return convertRDataFrameToString(eDataType, instanceValue);
			case RtDataPackage.RDATA_FILTER:
				return convertRDataFilterToString(eDataType, instanceValue);
			case RtDataPackage.RTEXT:
				return convertRTextToString(eDataType, instanceValue);
			case RtDataPackage.RNUM:
				return convertRNumToString(eDataType, instanceValue);
			case RtDataPackage.RINT:
				return convertRIntToString(eDataType, instanceValue);
			case RtDataPackage.RFUNCTION:
				return convertRFunctionToString(eDataType, instanceValue);
			case RtDataPackage.RLABEL:
				return convertRLabelToString(eDataType, instanceValue);
			case RtDataPackage.RNUM_RANGE:
				return convertRNumRangeToString(eDataType, instanceValue);
			case RtDataPackage.RSIZE:
				return convertRSizeToString(eDataType, instanceValue);
			case RtDataPackage.RCOLOR:
				return convertRColorToString(eDataType, instanceValue);
			case RtDataPackage.RALPHA:
				return convertRAlphaToString(eDataType, instanceValue);
			case RtDataPackage.RPLOT_POINT_SYMBOL:
				return convertRPlotPointSymbolToString(eDataType, instanceValue);
			case RtDataPackage.RPLOT_LINE_TYPE:
				return convertRPlotLineTypeToString(eDataType, instanceValue);
			case RtDataPackage.RFONT_FAMILY:
				return convertRFontFamilyToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	
	private int getTypedExprIdxFromString(final EDataType eDataType, final String s) {
		if (s == null) {
			return -1;
		}
		if (getEPackage() != eDataType.getEPackage()) {
			throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
		return s.indexOf(':');
	}
	
	private RTypedExpr doTypedExprFromString(final EDataType eDataType, final String s) {
		final int idx = getTypedExprIdxFromString(eDataType, s);
		if (idx < 0) {
			return null;
		}
		return new RTypedExpr(s.substring(0, idx).intern(), s.substring(idx+1));
	}
	
	private String doTypedExprToString(final EDataType eDataType, final Object expr) {
		if (expr == null) {
			return null;
		}
		if (getEPackage() != eDataType.getEPackage()) {
			throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
		return ((RTypedExpr) expr).toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public RTypedExpr createRVarFromString(final EDataType eDataType, final String initialValue) {
		return doTypedExprFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String convertRVarToString(final EDataType eDataType, final Object instanceValue) {
		return doTypedExprToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public RTypedExpr createRDataFrameFromString(EDataType eDataType, String initialValue) {
		return doTypedExprFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String convertRDataFrameToString(EDataType eDataType, Object instanceValue) {
		return doTypedExprToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public RTypedExpr createRDataFilterFromString(EDataType eDataType, String initialValue) {
		return doTypedExprFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String convertRDataFilterToString(EDataType eDataType, Object instanceValue) {
		return doTypedExprToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public RTypedExpr createRColorFromString(final EDataType eDataType, final String initialValue) {
		return doTypedExprFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String convertRColorToString(final EDataType eDataType, final Object instanceValue) {
		return doTypedExprToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public RTypedExpr createRAlphaFromString(EDataType eDataType, String initialValue) {
		return doTypedExprFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String convertRAlphaToString(EDataType eDataType, Object instanceValue) {
		return doTypedExprToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public RTypedExpr createRPlotPointSymbolFromString(final EDataType eDataType, final String initialValue) {
		return doTypedExprFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String convertRPlotPointSymbolToString(final EDataType eDataType, final Object instanceValue) {
		return doTypedExprToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public RTypedExpr createRPlotLineTypeFromString(EDataType eDataType, String initialValue) {
		return doTypedExprFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String convertRPlotLineTypeToString(EDataType eDataType, Object instanceValue) {
		return doTypedExprToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public RTypedExpr createRFontFamilyFromString(EDataType eDataType, String initialValue) {
		return doTypedExprFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String convertRFontFamilyToString(EDataType eDataType, Object instanceValue) {
		return doTypedExprToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public RTypedExpr createRTextFromString(final EDataType eDataType, final String initialValue) {
		return doTypedExprFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String convertRTextToString(final EDataType eDataType, final Object instanceValue) {
		return doTypedExprToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public RTypedExpr createRNumFromString(final EDataType eDataType, final String initialValue) {
		return doTypedExprFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String convertRNumToString(final EDataType eDataType, final Object instanceValue) {
		return doTypedExprToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public RTypedExpr createRSizeFromString(final EDataType eDataType, final String initialValue) {
		return doTypedExprFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String convertRSizeToString(final EDataType eDataType, final Object instanceValue) {
		return doTypedExprToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public RTypedExpr createRIntFromString(final EDataType eDataType, final String initialValue) {
		return doTypedExprFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String convertRIntToString(final EDataType eDataType, final Object instanceValue) {
		return doTypedExprToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public RTypedExpr createRFunctionFromString(EDataType eDataType, String initialValue) {
		return doTypedExprFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String convertRFunctionToString(EDataType eDataType, Object instanceValue) {
		return doTypedExprToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public RTypedExpr createRLabelFromString(EDataType eDataType, String initialValue) {
		return doTypedExprFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String convertRLabelToString(EDataType eDataType, Object instanceValue) {
		return doTypedExprToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public RTypedExpr createRNumRangeFromString(final EDataType eDataType, final String initialValue) {
		return doTypedExprFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public String convertRNumRangeToString(final EDataType eDataType, final Object instanceValue) {
		return doTypedExprToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RtDataPackage getRtDataPackage() {
		return (RtDataPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static RtDataPackage getPackage() {
		return RtDataPackage.eINSTANCE;
	}

} //RtDataFactoryImpl
