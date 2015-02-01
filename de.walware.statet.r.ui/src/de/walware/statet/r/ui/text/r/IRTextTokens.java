/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.text.r;

import de.walware.statet.r.core.rlang.RTerminal;


/**
 * Groups of terminals and keys (suffix KEY) for text tokens recognized by text-parser
 * (syntax-highlighting)
 */
public interface IRTextTokens {
	
	public static final RTerminal[] SYMBOL = new RTerminal[] {
		RTerminal.SYMBOL,
//		RTerminal.SYMBOL_G, // colored like a string because of partitioning
	};
	public static final RTerminal[] STRING = new RTerminal[] {
		RTerminal.STRING_D,
		RTerminal.STRING_S,
	};
	public static final RTerminal[] NUM = new RTerminal[] {
		RTerminal.NUM_NUM,
	};
	public static final RTerminal[] NUM_SUB_INT = new RTerminal[] {
		RTerminal.NUM_INT,
	};
	public static final RTerminal[] NUM_SUB_CPLX = new RTerminal[] {
		RTerminal.NUM_COMPLEX,
	};
	public static final RTerminal[] SPECIALCONST = new RTerminal[] {
		RTerminal.NULL,
		RTerminal.NA,
		RTerminal.NA_REAL,
		RTerminal.NA_INT,
		RTerminal.NA_CPLX,
		RTerminal.NA_CHAR,
		RTerminal.INF,
		RTerminal.NAN,
	};
	public static final RTerminal[] LOGICALCONST = new RTerminal[] {
		RTerminal.TRUE,
		RTerminal.FALSE,
	};
	
	public static final RTerminal[] FLOWCONTROL = new RTerminal[] {
		RTerminal.IF,
		RTerminal.ELSE,
		RTerminal.FOR,
		RTerminal.IN,
		RTerminal.WHILE,
		RTerminal.REPEAT,
		RTerminal.NEXT,
		RTerminal.BREAK,
		RTerminal.FUNCTION,
	};
	public static final RTerminal[] GROUPING = new RTerminal[] {
		RTerminal.BLOCK_OPEN,
		RTerminal.BLOCK_CLOSE,
		RTerminal.GROUP_OPEN,
		RTerminal.GROUP_CLOSE,
	};
	public static final RTerminal[] SEPARATOR = new RTerminal[] {
		RTerminal.COMMA,
		RTerminal.SEMI,
	};
	public static final RTerminal[] NSGET = new RTerminal[] {
		RTerminal.NS_GET,
		RTerminal.NS_GET_INT,
	};
	public static final RTerminal[] SUBACCESS = new RTerminal[] {
		RTerminal.SUB_INDEXED_S_OPEN,
		RTerminal.SUB_INDEXED_D_OPEN,
		RTerminal.SUB_INDEXED_CLOSE,
		RTerminal.SUB_NAMED_PART,
		RTerminal.SUB_NAMED_SLOT,
	};
	public static final RTerminal[] ASSIGN = new RTerminal[] {
		RTerminal.ARROW_LEFT_S,
		RTerminal.ARROW_LEFT_D,
		RTerminal.ARROW_RIGHT_S,
		RTerminal.ARROW_RIGHT_D,
	};
	public static final RTerminal[] ASSIGN_SUB_EQUAL = new RTerminal[] {
		RTerminal.EQUAL,
	};
	public static final RTerminal[] OP = new RTerminal[] {
		RTerminal.PLUS,
		RTerminal.MINUS,
		RTerminal.MULT,
		RTerminal.DIV,
		RTerminal.POWER,
		RTerminal.SEQ,
		RTerminal.TILDE,
		RTerminal.QUESTIONMARK,
	};
	public static final RTerminal[] OP_SUB_LOGICAL = new RTerminal[] {
		RTerminal.NOT,
		RTerminal.AND,
		RTerminal.AND_D,
		RTerminal.OR,
		RTerminal.OR_D,
	};
	public static final RTerminal[] OP_SUB_RELATIONAL = new RTerminal[] {
		RTerminal.REL_EQ,
		RTerminal.REL_GT,
		RTerminal.REL_GE,
		RTerminal.REL_LE,
		RTerminal.REL_LT,
		RTerminal.REL_NE,
	};
	
	public static final RTerminal[] UNDEFINED = new RTerminal[] {
		RTerminal.UNKNOWN,
	};
	public static final RTerminal[] COMMENT = new RTerminal[] {
		RTerminal.COMMENT, RTerminal.ROXYGEN_COMMENT,
	};
	
	
	public static final String ROOT = "text_R_"; //$NON-NLS-1$
	
	public static final String SYMBOL_KEY = ROOT+"rDefault"; //$NON-NLS-1$
	public static final String SYMBOL_SUB_ASSIGN_KEY = SYMBOL_KEY + ".Assignment"; //$NON-NLS-1$
	public static final String SYMBOL_SUB_LOGICAL_KEY = SYMBOL_KEY + ".Logical"; //$NON-NLS-1$
	public static final String SYMBOL_SUB_FLOWCONTROL_KEY = SYMBOL_KEY + ".Flowcontrol"; //$NON-NLS-1$
	public static final String SYMBOL_SUB_CUSTOM1_KEY = SYMBOL_KEY + ".Custom2"; //$NON-NLS-1$
	public static final String SYMBOL_SUB_CUSTOM2_KEY = SYMBOL_KEY + ".Custom1"; //$NON-NLS-1$
	
	public static final String STRING_KEY = ROOT+"rString"; //$NON-NLS-1$
	public static final String NUM_KEY = ROOT+"rNumbers"; //$NON-NLS-1$
	public static final String NUM_SUB_INT_KEY = NUM_KEY+".Integer"; //$NON-NLS-1$
	public static final String NUM_SUB_CPLX_KEY = NUM_KEY+".Complex"; //$NON-NLS-1$
	
	public static final String SPECIALCONST_KEY = ROOT+"rSpecialConstants"; //$NON-NLS-1$
	public static final String LOGICALCONST_KEY = ROOT+"rLogicalConstants"; //$NON-NLS-1$
	public static final String GROUPING_KEY = ROOT+"rGrouping"; //$NON-NLS-1$
	public static final String SEPARATOR_KEY = ROOT+"rSeparators"; //$NON-NLS-1$
	public static final String SUBACCESS_KEY = ROOT+"rIndexing";	 //$NON-NLS-1$
	public static final String ASSIGN_KEY = ROOT+"rAssignment"; //$NON-NLS-1$
	public static final String ASSIGN_SUB_EQUAL_KEY = ASSIGN_KEY + ".Equalsign"; //$NON-NLS-1$
	public static final String FLOWCONTROL_KEY = ROOT+"rFlowcontrol"; //$NON-NLS-1$
	public static final String OP_KEY = ROOT+"rOtherOperators"; //$NON-NLS-1$
	public static final String OP_SUB_LOGICAL_KEY = OP_KEY + ".Logical"; //$NON-NLS-1$
	public static final String OP_SUB_RELATIONAL_KEY = OP_KEY + ".Relational"; //$NON-NLS-1$
	public static final String OP_SUB_USERDEFINED_KEY = OP_KEY + ".Userdefined"; //$NON-NLS-1$
	
	public static final String COMMENT_KEY = ROOT+"rComment"; //$NON-NLS-1$
	public static final String TASK_TAG_KEY = ROOT+"taskTag"; //$NON-NLS-1$
	public static final String ROXYGEN_KEY = ROOT + "rRoxygen"; //$NON-NLS-1$
	public static final String ROXYGEN_TAG_KEY = ROOT + "rRoxygenTag"; //$NON-NLS-1$
	public static final String UNDEFINED_KEY = ROOT+"rUndefined"; //$NON-NLS-1$
	
}
