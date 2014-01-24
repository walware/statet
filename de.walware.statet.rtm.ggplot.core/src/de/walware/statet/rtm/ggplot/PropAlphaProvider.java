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
 * A representation of the model object '<em><b>Prop Alpha Provider</b></em>'.
 * <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.PropAlphaProvider#getAlpha <em>Alpha</em>}</li>
 * </ul>
 * </p>
 * 
 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getPropAlphaProvider()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface PropAlphaProvider extends EObject {
	/**
	 * Returns the value of the '<em><b>Alpha</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Alpha</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Alpha</em>' attribute.
	 * @see #setAlpha(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getPropAlphaProvider_Alpha()
	 * @model default="" dataType="de.walware.statet.rtm.rtdata.RAlpha"
	 * @generated
	 */
	RTypedExpr getAlpha();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.PropAlphaProvider#getAlpha <em>Alpha</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Alpha</em>' attribute.
	 * @see #getAlpha()
	 * @generated
	 */
	void setAlpha(RTypedExpr value);

} // PropAlphaProvider
