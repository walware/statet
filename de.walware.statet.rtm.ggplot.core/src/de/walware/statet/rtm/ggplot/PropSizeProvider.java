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
 * A representation of the model object '<em><b>Prop Size Provider</b></em>'.
 * <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.PropSizeProvider#getSize <em>Size</em>}</li>
 * </ul>
 * </p>
 * 
 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getPropSizeProvider()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface PropSizeProvider extends EObject {
	/**
	 * Returns the value of the '<em><b>Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Size</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Size</em>' attribute.
	 * @see #setSize(RTypedExpr)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getPropSizeProvider_Size()
	 * @model dataType="de.walware.statet.rtm.rtdata.RSize"
	 * @generated
	 */
	RTypedExpr getSize();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.PropSizeProvider#getSize <em>Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Size</em>' attribute.
	 * @see #getSize()
	 * @generated
	 */
	void setSize(RTypedExpr value);

} // PropSizeProvider
