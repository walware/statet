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
package de.walware.statet.rtm.ftable;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

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
 * @see de.walware.statet.rtm.ftable.FTableFactory
 * @model kind="package"
 * @generated
 */
public interface FTablePackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "ftable"; //$NON-NLS-1$

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://walware.de/rtm/Rt-ftable/1.0"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "ftable"; //$NON-NLS-1$

	/**
	 * The package content type ID.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eCONTENT_TYPE = "de.walware.statet.rtm.contentTypes.FTable"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	FTablePackage eINSTANCE = de.walware.statet.rtm.ftable.impl.FTablePackageImpl.init();

	/**
	 * The meta object id for the '{@link de.walware.statet.rtm.ftable.impl.FTableImpl <em>FTable</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.walware.statet.rtm.ftable.impl.FTableImpl
	 * @see de.walware.statet.rtm.ftable.impl.FTablePackageImpl#getFTable()
	 * @generated
	 */
	int FTABLE = 0;

	/**
	 * The feature id for the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FTABLE__DATA = 0;

	/**
	 * The feature id for the '<em><b>Data Filter</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FTABLE__DATA_FILTER = 1;

	/**
	 * The feature id for the '<em><b>Col Vars</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FTABLE__COL_VARS = 2;

	/**
	 * The feature id for the '<em><b>Row Vars</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FTABLE__ROW_VARS = 3;

	/**
	 * The number of structural features of the '<em>FTable</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FTABLE_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>FTable</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FTABLE_OPERATION_COUNT = 0;


	/**
	 * Returns the meta object for class '{@link de.walware.statet.rtm.ftable.FTable <em>FTable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>FTable</em>'.
	 * @see de.walware.statet.rtm.ftable.FTable
	 * @generated
	 */
	EClass getFTable();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ftable.FTable#getData <em>Data</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Data</em>'.
	 * @see de.walware.statet.rtm.ftable.FTable#getData()
	 * @see #getFTable()
	 * @generated
	 */
	EAttribute getFTable_Data();

	/**
	 * Returns the meta object for the attribute '{@link de.walware.statet.rtm.ftable.FTable#getDataFilter <em>Data Filter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Data Filter</em>'.
	 * @see de.walware.statet.rtm.ftable.FTable#getDataFilter()
	 * @see #getFTable()
	 * @generated
	 */
	EAttribute getFTable_DataFilter();

	/**
	 * Returns the meta object for the attribute list '{@link de.walware.statet.rtm.ftable.FTable#getColVars <em>Col Vars</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Col Vars</em>'.
	 * @see de.walware.statet.rtm.ftable.FTable#getColVars()
	 * @see #getFTable()
	 * @generated
	 */
	EAttribute getFTable_ColVars();

	/**
	 * Returns the meta object for the attribute list '{@link de.walware.statet.rtm.ftable.FTable#getRowVars <em>Row Vars</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Row Vars</em>'.
	 * @see de.walware.statet.rtm.ftable.FTable#getRowVars()
	 * @see #getFTable()
	 * @generated
	 */
	EAttribute getFTable_RowVars();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	FTableFactory getFTableFactory();

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
		 * The meta object literal for the '{@link de.walware.statet.rtm.ftable.impl.FTableImpl <em>FTable</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.walware.statet.rtm.ftable.impl.FTableImpl
		 * @see de.walware.statet.rtm.ftable.impl.FTablePackageImpl#getFTable()
		 * @generated
		 */
		EClass FTABLE = eINSTANCE.getFTable();
		/**
		 * The meta object literal for the '<em><b>Data</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FTABLE__DATA = eINSTANCE.getFTable_Data();
		/**
		 * The meta object literal for the '<em><b>Data Filter</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FTABLE__DATA_FILTER = eINSTANCE.getFTable_DataFilter();
		/**
		 * The meta object literal for the '<em><b>Col Vars</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FTABLE__COL_VARS = eINSTANCE.getFTable_ColVars();
		/**
		 * The meta object literal for the '<em><b>Row Vars</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FTABLE__ROW_VARS = eINSTANCE.getFTable_RowVars();

	}

} //FTablePackage
