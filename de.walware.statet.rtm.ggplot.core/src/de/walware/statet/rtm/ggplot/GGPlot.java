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
 * A representation of the model object '<em><b>GG Plot</b></em>'.
 * <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.GGPlot#getDataFilter <em>Data Filter</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.GGPlot#getMainTitle <em>Main Title</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.GGPlot#getMainTitleStyle <em>Main Title Style</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.GGPlot#getFacet <em>Facet</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.GGPlot#getAxXLim <em>Ax XLim</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.GGPlot#getAxYLim <em>Ax YLim</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.GGPlot#getAxXLabel <em>Ax XLabel</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.GGPlot#getAxYLabel <em>Ax YLabel</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.GGPlot#getAxXLabelStyle <em>Ax XLabel Style</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.GGPlot#getAxYLabelStyle <em>Ax YLabel Style</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.GGPlot#getAxXTextStyle <em>Ax XText Style</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.GGPlot#getAxYTextStyle <em>Ax YText Style</em>}</li>
 *   <li>{@link de.walware.statet.rtm.ggplot.GGPlot#getLayers <em>Layers</em>}</li>
 * </ul>
 * </p>
 * 
 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGGPlot()
 * @model
 * @generated
 */
public interface GGPlot extends PropDataProvider, PropXVarProvider, PropYVarProvider {
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
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGGPlot_DataFilter()
	 * @model dataType="de.walware.statet.rtm.rtdata.RDataFilter"
	 * @generated
	 */
	RTypedExpr getDataFilter();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.GGPlot#getDataFilter <em>Data Filter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Data Filter</em>' attribute.
	 * @see #getDataFilter()
	 * @generated
	 */
	void setDataFilter(RTypedExpr value);

	/**
	 * Returns the value of the '<em><b>Main Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Main Title</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Main Title</em>' attribute.
	 * @see #setMainTitle(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGGPlot_MainTitle()
	 * @model dataType="de.walware.statet.rtm.rtdata.RLabel"
	 * @generated
	 */
	RTypedExpr getMainTitle();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.GGPlot#getMainTitle <em>Main Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Main Title</em>' attribute.
	 * @see #getMainTitle()
	 * @generated
	 */
	void setMainTitle(RTypedExpr value);

	/**
	 * Returns the value of the '<em><b>Main Title Style</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Main Title Style</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Main Title Style</em>' containment reference.
	 * @see #setMainTitleStyle(TextStyle)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGGPlot_MainTitleStyle()
	 * @model containment="true" required="true"
	 * @generated
	 */
	TextStyle getMainTitleStyle();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.GGPlot#getMainTitleStyle <em>Main Title Style</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Main Title Style</em>' containment reference.
	 * @see #getMainTitleStyle()
	 * @generated
	 */
	void setMainTitleStyle(TextStyle value);

	/**
	 * Returns the value of the '<em><b>Facet</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Facet</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Facet</em>' containment reference.
	 * @see #setFacet(FacetLayout)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGGPlot_Facet()
	 * @model containment="true"
	 * @generated
	 */
	FacetLayout getFacet();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.GGPlot#getFacet <em>Facet</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Facet</em>' containment reference.
	 * @see #getFacet()
	 * @generated
	 */
	void setFacet(FacetLayout value);

	/**
	 * Returns the value of the '<em><b>Ax XLim</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ax XLim</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ax XLim</em>' attribute.
	 * @see #setAxXLim(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGGPlot_AxXLim()
	 * @model dataType="de.walware.statet.rtm.rtdata.RNumRange"
	 * @generated
	 */
	RTypedExpr getAxXLim();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.GGPlot#getAxXLim <em>Ax XLim</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ax XLim</em>' attribute.
	 * @see #getAxXLim()
	 * @generated
	 */
	void setAxXLim(RTypedExpr value);

	/**
	 * Returns the value of the '<em><b>Ax YLim</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ax YLim</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ax YLim</em>' attribute.
	 * @see #setAxYLim(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGGPlot_AxYLim()
	 * @model dataType="de.walware.statet.rtm.rtdata.RNumRange"
	 * @generated
	 */
	RTypedExpr getAxYLim();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.GGPlot#getAxYLim <em>Ax YLim</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ax YLim</em>' attribute.
	 * @see #getAxYLim()
	 * @generated
	 */
	void setAxYLim(RTypedExpr value);

	/**
	 * Returns the value of the '<em><b>Ax XLabel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ax XLabel</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ax XLabel</em>' attribute.
	 * @see #setAxXLabel(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGGPlot_AxXLabel()
	 * @model dataType="de.walware.statet.rtm.rtdata.RLabel" ordered="false"
	 * @generated
	 */
	RTypedExpr getAxXLabel();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.GGPlot#getAxXLabel <em>Ax XLabel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ax XLabel</em>' attribute.
	 * @see #getAxXLabel()
	 * @generated
	 */
	void setAxXLabel(RTypedExpr value);

	/**
	 * Returns the value of the '<em><b>Ax YLabel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ax YLabel</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ax YLabel</em>' attribute.
	 * @see #setAxYLabel(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGGPlot_AxYLabel()
	 * @model dataType="de.walware.statet.rtm.rtdata.RLabel" ordered="false"
	 * @generated
	 */
	RTypedExpr getAxYLabel();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.GGPlot#getAxYLabel <em>Ax YLabel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ax YLabel</em>' attribute.
	 * @see #getAxYLabel()
	 * @generated
	 */
	void setAxYLabel(RTypedExpr value);

	/**
	 * Returns the value of the '<em><b>Ax XLabel Style</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ax XLabel Style</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ax XLabel Style</em>' containment reference.
	 * @see #setAxXLabelStyle(TextStyle)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGGPlot_AxXLabelStyle()
	 * @model containment="true" required="true"
	 * @generated
	 */
	TextStyle getAxXLabelStyle();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.GGPlot#getAxXLabelStyle <em>Ax XLabel Style</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ax XLabel Style</em>' containment reference.
	 * @see #getAxXLabelStyle()
	 * @generated
	 */
	void setAxXLabelStyle(TextStyle value);

	/**
	 * Returns the value of the '<em><b>Ax YLabel Style</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ax YLabel Style</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ax YLabel Style</em>' containment reference.
	 * @see #setAxYLabelStyle(TextStyle)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGGPlot_AxYLabelStyle()
	 * @model containment="true" required="true"
	 * @generated
	 */
	TextStyle getAxYLabelStyle();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.GGPlot#getAxYLabelStyle <em>Ax YLabel Style</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ax YLabel Style</em>' containment reference.
	 * @see #getAxYLabelStyle()
	 * @generated
	 */
	void setAxYLabelStyle(TextStyle value);

	/**
	 * Returns the value of the '<em><b>Ax XText Style</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ax XText Style</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ax XText Style</em>' containment reference.
	 * @see #setAxXTextStyle(TextStyle)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGGPlot_AxXTextStyle()
	 * @model containment="true" required="true"
	 * @generated
	 */
	TextStyle getAxXTextStyle();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.GGPlot#getAxXTextStyle <em>Ax XText Style</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ax XText Style</em>' containment reference.
	 * @see #getAxXTextStyle()
	 * @generated
	 */
	void setAxXTextStyle(TextStyle value);

	/**
	 * Returns the value of the '<em><b>Ax YText Style</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ax YText Style</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ax YText Style</em>' containment reference.
	 * @see #setAxYTextStyle(TextStyle)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGGPlot_AxYTextStyle()
	 * @model containment="true" required="true"
	 * @generated
	 */
	TextStyle getAxYTextStyle();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.GGPlot#getAxYTextStyle <em>Ax YText Style</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ax YText Style</em>' containment reference.
	 * @see #getAxYTextStyle()
	 * @generated
	 */
	void setAxYTextStyle(TextStyle value);

	/**
	 * Returns the value of the '<em><b>Layers</b></em>' containment reference list.
	 * The list contents are of type {@link de.walware.statet.rtm.ggplot.Layer}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Layers</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Layers</em>' containment reference list.
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getGGPlot_Layers()
	 * @model containment="true"
	 * @generated
	 */
	EList<Layer> getLayers();

} // GGPlot
