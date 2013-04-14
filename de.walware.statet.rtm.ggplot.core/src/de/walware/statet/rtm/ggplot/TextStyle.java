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
 * A representation of the model object '<em><b>Text Style</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.TextStyle#getFontFamily <em>Font Family</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.TextStyle#getFontFace <em>Font Face</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.TextStyle#getHJust <em>HJust</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.TextStyle#getVJust <em>VJust</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.TextStyle#getAngle <em>Angle</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getTextStyle()
 * @model
 * @generated
 */
public interface TextStyle extends PropSizeProvider, PropColorProvider {
	/**
	 * Returns the value of the '<em><b>Font Family</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Font Family</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Font Family</em>' attribute.
	 * @see #setFontFamily(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getTextStyle_FontFamily()
	 * @model dataType="de.walware.statet.rtm.rtdata.RFontFamily"
	 * @generated
	 */
	RTypedExpr getFontFamily();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.TextStyle#getFontFamily <em>Font Family</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Font Family</em>' attribute.
	 * @see #getFontFamily()
	 * @generated
	 */
	void setFontFamily(RTypedExpr value);

	/**
	 * Returns the value of the '<em><b>Font Face</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Font Face</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Font Face</em>' attribute.
	 * @see #setFontFace(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getTextStyle_FontFace()
	 * @model dataType="de.walware.statet.rtm.rtdata.RText"
	 * @generated
	 */
	RTypedExpr getFontFace();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.TextStyle#getFontFace <em>Font Face</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Font Face</em>' attribute.
	 * @see #getFontFace()
	 * @generated
	 */
	void setFontFace(RTypedExpr value);

	/**
	 * Returns the value of the '<em><b>HJust</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>HJust</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>HJust</em>' attribute.
	 * @see #setHJust(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getTextStyle_HJust()
	 * @model dataType="de.walware.statet.rtm.rtdata.RNum"
	 * @generated
	 */
	RTypedExpr getHJust();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.TextStyle#getHJust <em>HJust</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>HJust</em>' attribute.
	 * @see #getHJust()
	 * @generated
	 */
	void setHJust(RTypedExpr value);

	/**
	 * Returns the value of the '<em><b>VJust</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>VJust</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>VJust</em>' attribute.
	 * @see #setVJust(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getTextStyle_VJust()
	 * @model dataType="de.walware.statet.rtm.rtdata.RNum"
	 * @generated
	 */
	RTypedExpr getVJust();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.TextStyle#getVJust <em>VJust</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>VJust</em>' attribute.
	 * @see #getVJust()
	 * @generated
	 */
	void setVJust(RTypedExpr value);

	/**
	 * Returns the value of the '<em><b>Angle</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Angle</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Angle</em>' attribute.
	 * @see #setAngle(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getTextStyle_Angle()
	 * @model dataType="de.walware.statet.rtm.rtdata.RNum"
	 * @generated
	 */
	RTypedExpr getAngle();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.TextStyle#getAngle <em>Angle</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Angle</em>' attribute.
	 * @see #getAngle()
	 * @generated
	 */
	void setAngle(RTypedExpr value);

} // TextStyle
