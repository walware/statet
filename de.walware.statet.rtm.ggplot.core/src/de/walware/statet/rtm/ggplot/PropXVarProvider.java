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

import org.eclipse.emf.ecore.EObject;

import de.walware.statet.rtm.rtdata.types.RTypedExpr;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Prop XVar Provider</b></em>'.
 * <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.PropXVarProvider#getXVar <em>XVar</em>}</li>
 * </ul>
 * </p>
 * 
 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getPropXVarProvider()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface PropXVarProvider extends EObject {
	/**
	 * Returns the value of the '<em><b>XVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>XVar</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>XVar</em>' attribute.
	 * @see #setXVar(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getPropXVarProvider_XVar()
	 * @model dataType="de.walware.statet.rtm.rtdata.RVar"
	 * @generated
	 */
	RTypedExpr getXVar();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.PropXVarProvider#getXVar <em>XVar</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>XVar</em>' attribute.
	 * @see #getXVar()
	 * @generated
	 */
	void setXVar(RTypedExpr value);

} // PropXVarProvider
