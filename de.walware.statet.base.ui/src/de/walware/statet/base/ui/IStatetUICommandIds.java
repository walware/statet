/*******************************************************************************
 * Copyright (c) 2005-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui;


/**
 * Defines the commandIds (= action definition IDs).
 * 
 * <p>
 * This interface is not intended to be implemented or extended.
 * </p>.
 */
public interface IStatetUICommandIds {
	
	
	/**
	 * Action definition ID of the 'insert assignment' action
	 * 
	 * Value: @value
	 */
	public static final String INSERT_ASSIGNMENT =      "de.walware.ecommons.ltk.commands.InsertAssignment"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the 'add doc comment' action
	 * 
	 * Value: @value
	 */
	public static final String ADD_DOC_COMMENT =        "de.walware.ecommons.ltk.commands.AddDocComment"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the 'generate element comment' action
	 * 
	 * Value: @value
	 */
	public static final String GENERATE_ELEMENT_COMMENT = "de.walware.ecommons.ltk.commands.GenerateElementComment"; //$NON-NLS-1$
	
}
