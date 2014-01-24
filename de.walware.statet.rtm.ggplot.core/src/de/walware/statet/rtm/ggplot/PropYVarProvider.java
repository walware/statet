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
 * A representation of the model object '<em><b>Prop YVar Provider</b></em>'.
 * <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.PropYVarProvider#getYVar <em>YVar</em>}</li>
 * </ul>
 * </p>
 * 
 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getPropYVarProvider()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface PropYVarProvider extends EObject {
	/**
	 * Returns the value of the '<em><b>YVar</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>YVar</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>YVar</em>' attribute.
	 * @see #setYVar(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getPropYVarProvider_YVar()
	 * @model dataType="de.walware.statet.rtm.rtdata.RVar"
	 * @generated
	 */
	RTypedExpr getYVar();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.PropYVarProvider#getYVar <em>YVar</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>YVar</em>' attribute.
	 * @see #getYVar()
	 * @generated
	 */
	void setYVar(RTypedExpr value);

} // PropYVarProvider
