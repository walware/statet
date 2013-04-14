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

import org.eclipse.emf.ecore.EObject;

import de.walware.statet.rtm.rtdata.types.RTypedExpr;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Prop Line Type Provider</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.PropLineTypeProvider#getLineType <em>Line Type</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getPropLineTypeProvider()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface PropLineTypeProvider extends EObject {
	/**
	 * Returns the value of the '<em><b>Line Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Line Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Line Type</em>' attribute.
	 * @see #setLineType(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getPropLineTypeProvider_LineType()
	 * @model dataType="de.walware.statet.rtm.rtdata.RPlotLineType"
	 * @generated
	 */
	RTypedExpr getLineType();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.PropLineTypeProvider#getLineType <em>Line Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Line Type</em>' attribute.
	 * @see #getLineType()
	 * @generated
	 */
	void setLineType(RTypedExpr value);

} // PropLineTypeProvider
