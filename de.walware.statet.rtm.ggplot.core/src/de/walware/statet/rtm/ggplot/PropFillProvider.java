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
 * A representation of the model object '<em><b>Prop Fill Provider</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.PropFillProvider#getFill <em>Fill</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getPropFillProvider()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface PropFillProvider extends EObject {
	/**
	 * Returns the value of the '<em><b>Fill</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Fill</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Fill</em>' attribute.
	 * @see #setFill(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getPropFillProvider_Fill()
	 * @model default="" dataType="de.walware.statet.rtm.rtdata.RColor"
	 * @generated
	 */
	RTypedExpr getFill();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.PropFillProvider#getFill <em>Fill</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Fill</em>' attribute.
	 * @see #getFill()
	 * @generated
	 */
	void setFill(RTypedExpr value);

} // PropFillProvider
