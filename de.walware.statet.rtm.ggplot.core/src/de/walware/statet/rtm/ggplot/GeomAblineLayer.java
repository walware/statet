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

import de.walware.statet.rtm.rtdata.types.RTypedExpr;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Geom Abline Layer</b></em>'.
 * <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.GeomAblineLayer#getInterceptVar <em>Intercept Var</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.GeomAblineLayer#getSlopeVar <em>Slope Var</em>}</li>
 * </ul>
 * </p>
 * 
 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGeomAblineLayer()
 * @model
 * @generated
 */
public interface GeomAblineLayer extends Layer, PropLineTypeProvider, PropSizeProvider, PropColorProvider, PropAlphaProvider {
	/**
	 * Returns the value of the '<em><b>Intercept Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Intercept Var</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Intercept Var</em>' attribute.
	 * @see #setInterceptVar(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGeomAblineLayer_InterceptVar()
	 * @model dataType="de.walware.statet.rtm.rtdata.RNum"
	 * @generated
	 */
	RTypedExpr getInterceptVar();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.GeomAblineLayer#getInterceptVar <em>Intercept Var</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Intercept Var</em>' attribute.
	 * @see #getInterceptVar()
	 * @generated
	 */
	void setInterceptVar(RTypedExpr value);

	/**
	 * Returns the value of the '<em><b>Slope Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Slope Var</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Slope Var</em>' attribute.
	 * @see #setSlopeVar(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGeomAblineLayer_SlopeVar()
	 * @model dataType="de.walware.statet.rtm.rtdata.RNum"
	 * @generated
	 */
	RTypedExpr getSlopeVar();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.GeomAblineLayer#getSlopeVar <em>Slope Var</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Slope Var</em>' attribute.
	 * @see #getSlopeVar()
	 * @generated
	 */
	void setSlopeVar(RTypedExpr value);

} // GeomAblineLayer
