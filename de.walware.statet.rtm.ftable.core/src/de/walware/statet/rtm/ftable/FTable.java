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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import de.walware.statet.rtm.rtdata.types.RTypedExpr;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>FTable</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ftable.FTable#getData <em>Data</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ftable.FTable#getDataFilter <em>Data Filter</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ftable.FTable#getColVars <em>Col Vars</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ftable.FTable#getRowVars <em>Row Vars</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.walware.statet.rtm.ftable.FTablePackage#getFTable()
 * @model
 * @generated
 */
public interface FTable extends EObject {

	/**
	 * Returns the value of the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data</em>' attribute.
	 * @see #setData(RTypedExpr)
	 * @see de.walware.statet.rtm.ftable.FTablePackage#getFTable_Data()
	 * @model dataType="de.walware.statet.rtm.rtdata.RDataFrame"
	 * @generated
	 */
	RTypedExpr getData();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ftable.FTable#getData <em>Data</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Data</em>' attribute.
	 * @see #getData()
	 * @generated
	 */
	void setData(RTypedExpr value);

	/**
	 * Returns the value of the '<em><b>Data Filter</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data Filter</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data Filter</em>' attribute.
	 * @see #setDataFilter(RTypedExpr)
	 * @see de.walware.statet.rtm.ftable.FTablePackage#getFTable_DataFilter()
	 * @model dataType="de.walware.statet.rtm.rtdata.RDataFilter"
	 * @generated
	 */
	RTypedExpr getDataFilter();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ftable.FTable#getDataFilter <em>Data Filter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Data Filter</em>' attribute.
	 * @see #getDataFilter()
	 * @generated
	 */
	void setDataFilter(RTypedExpr value);

	/**
	 * Returns the value of the '<em><b>Col Vars</b></em>' attribute list.
	 * The list contents are of type {@link de.walware.statet.rtm.rtdata.types.RTypedExpr}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Col Vars</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Col Vars</em>' attribute list.
	 * @see de.walware.statet.rtm.ftable.FTablePackage#getFTable_ColVars()
	 * @model dataType="de.walware.statet.rtm.rtdata.RVar"
	 * @generated
	 */
	EList<RTypedExpr> getColVars();

	/**
	 * Returns the value of the '<em><b>Row Vars</b></em>' attribute list.
	 * The list contents are of type {@link de.walware.statet.rtm.rtdata.types.RTypedExpr}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Row Vars</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Row Vars</em>' attribute list.
	 * @see de.walware.statet.rtm.ftable.FTablePackage#getFTable_RowVars()
	 * @model dataType="de.walware.statet.rtm.rtdata.RVar"
	 * @generated
	 */
	EList<RTypedExpr> getRowVars();
} // FTable
