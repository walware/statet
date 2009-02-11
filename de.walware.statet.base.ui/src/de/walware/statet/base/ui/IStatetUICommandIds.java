/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
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
	 * Action definition ID of the 'edit' &gt; 'copy (qualified) (element) name' action
	 * 
	 * Value: @value
	 */
	public static final String COPY_ELEMENT_NAME = 		"de.walware.statet.base.commands.CopyElementName"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the edit -> select enclosing action
	 * 
	 * Value: @value
	 */
	public static final String SELECT_ENCLOSING = 		"de.walware.statet.base.commands.SelectExpandEnclosing"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the edit -> select next action
	 * 
	 * Value: @value
	 */
	public static final String SELECT_NEXT = 			"de.walware.statet.base.commands.SelectExpandNext"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the edit -> select previous action
	 * 
	 * Value: @value
	 */
	public static final String SELECT_PREVIOUS = 		"de.walware.statet.base.commands.SelectExpandPrevious"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the edit -> select restore last action
	 * 
	 * Value: @value
	 */
	public static final String SELECT_LAST = 			"de.walware.statet.base.commands.SelectLast"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the 'navigate' &gt; 'go to matching bracket' action
	 * 
	 * Value: @value
	 */
	public static final String GOTO_MATCHING_BRACKET = 	"de.walware.statet.base.commands.GotoMatchingBracket"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the 'source' &gt; 'toggle comment' action
	 * 
	 * Value: @value
	 */
	public static final String TOGGLE_COMMENT = 		"de.walware.statet.base.commands.ToggleComment"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the 'insert assignment' action
	 * 
	 * Value: @value
	 */
	public static final String INSERT_ASSIGNMENT =      "de.walware.statet.base.commands.InsertAssignment"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the 'add doc comment' action
	 * 
	 * Value: @value
	 */
	public static final String ADD_DOC_COMMENT =        "de.walware.statet.base.commands.AddDocComment"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the 'generate element comment' action
	 * 
	 * Value: @value
	 */
	public static final String GENERATE_ELEMENT_COMMENT = "de.walware.statet.base.commands.GenerateElementComment"; //$NON-NLS-1$
	
	/**
	 * Action definition ID of the 'correct indentation' action
	 * 
	 * Value: @value
	 */
	public static final String CORRECT_INDENT =        "de.walware.statet.base.commands.CorrectIndent"; //$NON-NLS-1$
	
}
