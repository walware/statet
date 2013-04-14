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

import org.eclipse.emf.common.util.EList;

import de.walware.statet.rtm.rtdata.types.RTypedExpr;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Wrap Facet Layout</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.WrapFacetLayout#getColVars <em>Col Vars</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.WrapFacetLayout#getColNum <em>Col Num</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getWrapFacetLayout()
 * @model
 * @generated
 */
public interface WrapFacetLayout extends FacetLayout {
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
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getWrapFacetLayout_ColVars()
	 * @model dataType="de.walware.statet.rtm.rtdata.RVar"
	 * @generated
	 */
	EList<RTypedExpr> getColVars();

	/**
	 * Returns the value of the '<em><b>Col Num</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Col Num</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Col Num</em>' attribute.
	 * @see #setColNum(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getWrapFacetLayout_ColNum()
	 * @model dataType="de.walware.statet.rtm.rtdata.RInt"
	 * @generated
	 */
	RTypedExpr getColNum();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.WrapFacetLayout#getColNum <em>Col Num</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Col Num</em>' attribute.
	 * @see #getColNum()
	 * @generated
	 */
	void setColNum(RTypedExpr value);

} // WrapFacetLayout
