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
 * A representation of the model object '<em><b>Prop Group Var Provider</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.PropGroupVarProvider#getGroupVar <em>Group Var</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getPropGroupVarProvider()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface PropGroupVarProvider extends EObject {
	/**
	 * Returns the value of the '<em><b>Group Var</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Group Var</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Group Var</em>' attribute.
	 * @see #setGroupVar(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getPropGroupVarProvider_GroupVar()
	 * @model dataType="de.walware.statet.rtm.rtdata.RVar"
	 * @generated
	 */
	RTypedExpr getGroupVar();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.PropGroupVarProvider#getGroupVar <em>Group Var</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Group Var</em>' attribute.
	 * @see #getGroupVar()
	 * @generated
	 */
	void setGroupVar(RTypedExpr value);

} // PropGroupVarProvider
