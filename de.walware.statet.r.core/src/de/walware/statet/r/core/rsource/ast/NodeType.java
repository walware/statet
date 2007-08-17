/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource.ast;

import de.walware.statet.r.core.rsource.ast.RAstNode.Assoc;


/**
 *
 */
public enum NodeType {
	
	SOURCELINES ("R", 100000000, Assoc.CONTAINER),
	COMMENT ("#", -1, -1),
	ERROR ("‼", 99, Assoc.LEFTSTD),
	ERROR_TERM ("‼", 1, Assoc.TERM),
	
	STRING_CONST ("str", 1, Assoc.TERM),
	NUM_CONST ("num", 1, Assoc.TERM),
	NULL_CONST ("null", 1, Assoc.TERM),
	SYMBOL ("sym", 1, Assoc.TERM),

	BLOCK ("{ }", 11, Assoc.CONTAINER),
	GROUP ("( )", 11, Assoc.CONTAINER),

	SUB_INDEXED_S ("[", 12, Assoc.CONTAINER),
	SUB_INDEXED_D ("[[", 12, Assoc.CONTAINER),
	SUB_INDEXED_ARGS ("○", 12, Assoc.CONTAINER),
	SUB_INDEXED_ARG ("•", 12, Assoc.CONTAINER),
	
	NS_GET ("::", 13, Assoc.TERM),
	NS_GET_INT (":::", 13, Assoc.TERM),

	SUB_NAMED ("$", 14, Assoc.LEFTSTD),
	SUB_SLOT ("@", 14, Assoc.LEFTSTD),
	
	POWER ("^", 101, Assoc.RIGHTSTD),
	SIGN ("±", 102, Assoc.LEFTSTD),
	SEQ (":", 103, Assoc.LEFTSTD),
	SPECIAL ("%", 104, Assoc.LEFTSTD),
	MULT ("*/", 105, Assoc.LEFTMULTI),
	ADD ("+-", 106, Assoc.LEFTMULTI),
	RELATIONAL ("<>", 111, Assoc.NOSTD),
	NOT ("!", 112, Assoc.LEFTSTD),
	AND ("&", 113, Assoc.LEFTMULTI),
	OR ("|", 114, Assoc.LEFTMULTI),

	MODEL ("~", 1001, Assoc.LEFTSTD),

	A_RIGHT_S ("->", 10001, Assoc.LEFTSTD),
	A_RIGHT_D ("->>", 10001, Assoc.LEFTSTD),
	A_LEFT_E ("=", 10002, Assoc.RIGHTSTD),
	ITEM_EQUAL ("=", 10002, Assoc.RIGHTSTD),
	A_LEFT_S ("<-", 10003, Assoc.RIGHTSTD),
	A_LEFT_D ("<<-", 10003, Assoc.RIGHTSTD),

	C_IF ("if", 100001, Assoc.RIGHTSTD),
	C_FOR ("for", 100002, Assoc.LEFTSTD),
	C_IN ("in", 100002, Assoc.LEFTSTD),
	C_WHILE ("while", 100002, Assoc.LEFTSTD),
	C_REPEAT ("repeat", 100002, Assoc.LEFTSTD),
	C_NEXT ("next", 1, Assoc.TERM),
	C_BREAK ("break", 1, Assoc.TERM),
	F_DEF ("def", 100002, Assoc.RIGHTSTD),
	F_DEF_ARGS ("○", 12, Assoc.CONTAINER),
	F_DEF_ARG ("•", 12, Assoc.CONTAINER),
	F_CALL ("call", 11, Assoc.LEFTSTD),
	F_CALL_ARGS ("○", 12, Assoc.CONTAINER),
	F_CALL_ARG ("•", 12, Assoc.CONTAINER),

	HELP ("?", 999, Assoc.LEFTSTD),
	;

	
	public final String label;
	final int opPrec;
	final int opAssoc;
	
	
	NodeType(String label, int precedence, int assoc) {
		this.label = label;
		this.opPrec = precedence;
		this.opAssoc = assoc;
	}

}