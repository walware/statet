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

import de.walware.statet.rtm.rtdata.types.RTypedExpr;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Geom Point Layer</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.GeomPointLayer#getPositionXJitter <em>Position XJitter</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.GeomPointLayer#getPositionYJitter <em>Position YJitter</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGeomPointLayer()
 * @model
 * @generated
 */
public interface GeomPointLayer extends XYVarLayer, PropShapeProvider, PropSizeProvider, PropColorProvider, PropFillProvider, PropAlphaProvider {
	/**
	 * Returns the value of the '<em><b>Position XJitter</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Position XJitter</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Position XJitter</em>' attribute.
	 * @see #setPositionXJitter(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGeomPointLayer_PositionXJitter()
	 * @model dataType="de.walware.statet.rtm.rtdata.RNum"
	 * @generated
	 */
	RTypedExpr getPositionXJitter();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.GeomPointLayer#getPositionXJitter <em>Position XJitter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Position XJitter</em>' attribute.
	 * @see #getPositionXJitter()
	 * @generated
	 */
	void setPositionXJitter(RTypedExpr value);

	/**
	 * Returns the value of the '<em><b>Position YJitter</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Position YJitter</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Position YJitter</em>' attribute.
	 * @see #setPositionYJitter(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGeomPointLayer_PositionYJitter()
	 * @model dataType="de.walware.statet.rtm.rtdata.RNum"
	 * @generated
	 */
	RTypedExpr getPositionYJitter();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.GeomPointLayer#getPositionYJitter <em>Position YJitter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Position YJitter</em>' attribute.
	 * @see #getPositionYJitter()
	 * @generated
	 */
	void setPositionYJitter(RTypedExpr value);

} // GeomPointLayer
