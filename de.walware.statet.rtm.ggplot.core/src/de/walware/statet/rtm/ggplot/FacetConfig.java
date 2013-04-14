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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import de.walware.statet.rtm.rtdata.types.RTypedExpr;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Facet Config</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.walware.statet.rtm.ggplot.FacetConfig#getRowVars <em>Row Vars</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getFacetConfig()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface FacetConfig extends EObject {
	/**
	 * Returns the value of the '<em><b>Row Vars</b></em>' attribute list.
	 * The list contents are of type {@link de.walware.statet.rtm.rtdata.types.RTypedExpr}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Row Vars</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Row Vars</em>' attribute list.
	 * @see de.walware.statet.rtm.ggplot.GGPlotPackage#getFacetConfig_RowVars()
	 * @model dataType="de.walware.statet.rtm.rtdata.RVar"
	 * @generated
	 */
	EList<RTypedExpr> getRowVars();

} // FacetConfig
