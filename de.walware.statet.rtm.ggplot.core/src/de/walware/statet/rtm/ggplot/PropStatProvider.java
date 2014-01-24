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


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Prop Stat Provider</b></em>'.
 * <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.PropStatProvider#getStat <em>Stat</em>}</li>
 * </ul>
 * </p>
 * 
 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getPropStatProvider()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface PropStatProvider extends EObject {
	/**
	 * Returns the value of the '<em><b>Stat</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Stat</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Stat</em>' containment reference.
	 * @see #setStat(Stat)
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getPropStatProvider_Stat()
	 * @model containment="true"
	 * @generated
	 */
	Stat getStat();

	/**
	 * Sets the value of the '{@link de.walware.statet.rtm.ggplot.PropStatProvider#getStat <em>Stat</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Stat</em>' containment reference.
	 * @see #getStat()
	 * @generated
	 */
	void setStat(Stat value);

} // PropStatProvider
