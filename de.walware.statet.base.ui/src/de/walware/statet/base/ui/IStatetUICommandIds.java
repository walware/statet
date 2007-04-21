/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui;


/**
 * Defines the action definition IDs for the editor actions.
 * 
 * <p>
 * This interface is not intended to be implemented or extended.
 * </p>.
 */
public interface IStatetUICommandIds {

	
	/**
	 * Action definition ID of the navigate -> go to matching bracket action
	 * 
	 * Value: @value
	 */
	public static final String GOTO_MATCHING_BRACKET = 	"de.walware.statet.ui.textediting.navigate.GotoMatchingBracket"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the source -> toggle comment action
	 * 
	 * Value: @value
	 */
	public static final String TOGGLE_COMMENT = 		"de.walware.statet.ui.textediting.source.ToggleComment";
	
}
