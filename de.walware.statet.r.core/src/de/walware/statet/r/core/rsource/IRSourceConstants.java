/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource;


public interface IRSourceConstants {
	
	public static final int STATUS_MASK_1 =                    0x00ff00;
	public static final int STATUS_MASK_12 =                   0x0ffff0;
	public static final int STATUS_MASK_3 =                    0x00000f;
	public static final int STATUS_MASK_123 =                  0x0fffff;
	
	public static final int STATUS_OK =                          0x0000;
	public static final int STATUS_RUNTIME_ERROR =               0xf000;
	public static final int STATUSFLAG_REAL_ERROR =            0x010000;
	public static final int STATUSFLAG_SUBSEQUENT =            0x100000;
	
	/**
	 * An existing token is not OK.
	 */
	public static final int STATUS1_SYNTAX_INCORRECT_TOKEN =     0x1100;
	public static final int STATUS2_SYNTAX_TOKEN_NOT_CLOSED =                  0x1110 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_FLOAT_WITH_L =                      0x1130;
	public static final int STATUS2_SYNTAX_FLOAT_EXP_INVALID =                 0x1140 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_TOKEN_UNKNOWN =                     0x1150 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_TOKEN_UNEXPECTED =                  0x1160 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS_SYNTAX_SEQREL_UNEXPECTED =                  0x1161 | STATUSFLAG_REAL_ERROR;
	
	/**
	 * A token (represented by an node) is missing.
	 */
	public static final int STATUS1_SYNTAX_MISSING_TOKEN =       0x1300;
	public static final int STATUS2_SYNTAX_EXPR_AS_REF_MISSING =               0x1310 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_EXPR_BEFORE_OP_MISSING =            0x1320 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_EXPR_AFTER_OP_MISSING =             0x1330 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_EXPR_AS_CONDITION_MISSING =         0x1340 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_EXPR_AS_FORSEQ_MISSING =            0x1350 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_EXPR_AS_BODY_MISSING =              0x1360 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_EXPR_IN_GROUP_MISSING =             0x1370 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_EXPR_AS_ARGVALUE_MISSING =          0x1380 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_ELEMENTNAME_MISSING =               0x1390 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_SYMBOL_MISSING =                    0x13f0 | STATUSFLAG_REAL_ERROR;
//	public static final SyntaxValidity P_MISSING_EXPR_STATUS = new SyntaxValidity(SyntaxValidity.ERROR,
//	P_CAT_SYNTAX_FLAG | 0x210,
//	"Syntax Error/Missing Expression: a valid expression is expected.");
	
	public static final int STATUS2_SYNTAX_OPERATOR_MISSING =                  0x1410 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_FCALL_NOT_CLOSED =                  0x1420 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_SUBINDEXED_NOT_CLOSED =             0x1430 | STATUSFLAG_REAL_ERROR;
	
	/**
	 * A control statement (part of an existing node) is incomplete.
	 */
	public static final int STATUS1_SYNTAX_INCOMPLETE_CC =       0x1500;
	public static final int STATUS2_SYNTAX_CC_NOT_CLOSED =                     0x1510 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_IF_MISSING =                        0x1530 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_CONDITION_MISSING =                 0x1540 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_IN_MISSING =                        0x1550 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_CONDITION_NOT_CLOSED =              0x1560 | STATUSFLAG_REAL_ERROR;
	
	/**
	 * A function definition is incomplete.
	 */
	public static final int STATUS1_SYNTAX_INCOMPLETE_FDEF =     0x1600;
	public static final int STATUS2_SYNTAX_FDEF_ARGS_MISSING =                 0x1610 | STATUSFLAG_REAL_ERROR;
	public static final int STATUS2_SYNTAX_FDEF_ARGS_NOT_CLOSED =              0x1620 | STATUSFLAG_REAL_ERROR;
	
	public static final int STATUS3_IF = 1;
	public static final int STATUS3_ELSE = 2;
	public static final int STATUS3_FOR = 3;
	public static final int STATUS3_WHILE = 4;
	public static final int STATUS3_REPEAT = 5;
	public static final int STATUS3_FDEF = 6;
	public static final int STATUS3_FCALL = 7;
	
}
