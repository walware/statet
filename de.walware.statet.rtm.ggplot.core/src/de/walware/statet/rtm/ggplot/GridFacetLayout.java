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

package de.walware.statet.rtm.ggplot;

import org.eclipse.emf.common.util.EList;

import de.walware.statet.rtm.rtdata.types.RTypedExpr;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Grid Facet Layout</b></em>'.
 * <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.GridFacetLayout#getColVars <em>Col Vars</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.GridFacetLayout#getRowVars <em>Row Vars</em>}</li>
 * </ul>
 * </p>
 * 
 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGridFacetLayout()
 * @model
 * @generated
 */
public interface GridFacetLayout extends FacetLayout {
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
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGridFacetLayout_ColVars()
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
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGridFacetLayout_RowVars()
	 * @model dataType="de.walware.statet.rtm.rtdata.RVar"
	 * @generated
	 */
	EList<RTypedExpr> getRowVars();

} // GridFacetLayout
