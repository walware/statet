/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.text.r;


/**
 * Keys of TextTokens recognized by text-parser (syntax-highlighting)
 *
 * @author Stephan Wahlbrink
 */
public interface IRTextTokens {
	
	public static final String ROOT = "text_R_";
	
	public static final String DEFAULT = ROOT+"rDefault";
	public static final String UNDEFINED = ROOT+"rUndefined";
	public static final String COMMENT = ROOT+"rComment";
	public static final String STRING = ROOT+"rString";
	
	public static final String NUMBERS = ROOT+"rNumbers";

	/** @see de.walware.statet.r.core.rlang.RTokens */
	public static final String SPECIAL_CONSTANTS = ROOT+"rSpecialConstants";
	/** @see de.walware.statet.r.core.rlang.RTokens */
	public static final String LOGICAL_CONSTANTS = ROOT+"rLogicalConstants";
	/** @see de.walware.statet.r.core.rlang.RTokens */
	public static final String FLOWCONTROL = ROOT+"rFlowcontrol";
	
	/** @see de.walware.statet.r.core.rlang.RTokens */
	public static final String SEPARATORS = ROOT+"rSeparators";
	/** @see de.walware.statet.r.core.rlang.RTokens */
	public static final String ASSIGNMENT = ROOT+"rAssignment";
	/** @see de.walware.statet.r.core.rlang.RTokens */
	public static final String OTHER_OPERATORS = ROOT+"rOtherOperators";
	/** @see de.walware.statet.r.core.rlang.RTokens */
	public static final String GROUPING = ROOT+"rGrouping";
	/** @see de.walware.statet.r.core.rlang.RTokens */
	public static final String INDEXING = ROOT+"rIndexing";	
	
	public static final String TASK_TAG = ROOT+"taskTag";
	
}
