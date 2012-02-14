/*******************************************************************************
 * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource.ast;

import de.walware.statet.r.core.rsource.ast.RAstNode.Assoc;


/**
 * Definitions for RAst nodes.
 */
public enum NodeType {
	
	SOURCELINES ("R", 100000000, Assoc.CONTAINER), //$NON-NLS-1$
	COMMENT ("#", -1, -1), //$NON-NLS-1$
	DOCU_AGGREGATION ("#'", -1, -1), //$NON-NLS-1$
	DOCU_TAG ("@", -1, -1), //$NON-NLS-1$
	DOCU_TEXT ("abc", -1, -1), //$NON-NLS-1$
	ERROR ("‼", 99, Assoc.LEFTSTD), //$NON-NLS-1$
	ERROR_TERM ("‼", 1, Assoc.TERM), //$NON-NLS-1$
	DUMMY ("", 1, Assoc.CONTAINER), //$NON-NLS-1$
	
	STRING_CONST ("str", 1, Assoc.TERM), //$NON-NLS-1$
	NUM_CONST ("num", 1, Assoc.TERM), //$NON-NLS-1$
	NULL_CONST ("null", 1, Assoc.TERM), //$NON-NLS-1$
	SYMBOL ("sym", 1, Assoc.TERM), //$NON-NLS-1$
	
	BLOCK ("{ }", 11, Assoc.CONTAINER), //$NON-NLS-1$
	GROUP ("( )", 11, Assoc.CONTAINER), //$NON-NLS-1$
	
	SUB_INDEXED_S ("[", 12, Assoc.CONTAINER), //$NON-NLS-1$
	SUB_INDEXED_D ("[[", 12, Assoc.CONTAINER), //$NON-NLS-1$
	SUB_INDEXED_ARGS ("○", 12, Assoc.CONTAINER), //$NON-NLS-1$
	SUB_INDEXED_ARG ("•", 12, Assoc.CONTAINER), //$NON-NLS-1$
	
	NS_GET ("::", 13, Assoc.TERM), //$NON-NLS-1$
	NS_GET_INT (":::", 13, Assoc.TERM), //$NON-NLS-1$
	
	SUB_NAMED_PART ("$", 14, Assoc.LEFTSTD), //$NON-NLS-1$
	SUB_NAMED_SLOT ("@", 14, Assoc.LEFTSTD), //$NON-NLS-1$
	
	POWER ("^", 101, Assoc.RIGHTSTD), //$NON-NLS-1$
	SIGN ("±", 102, Assoc.LEFTSTD), //$NON-NLS-1$
	SEQ (":", 103, Assoc.LEFTSTD), //$NON-NLS-1$
	SPECIAL ("%", 104, Assoc.LEFTSTD), //$NON-NLS-1$
	MULT ("*/", 105, Assoc.LEFTSTD), //$NON-NLS-1$
	ADD ("+-", 106, Assoc.LEFTSTD), //$NON-NLS-1$
	RELATIONAL ("<>", 111, Assoc.NOSTD), //$NON-NLS-1$
	NOT ("!", 112, Assoc.LEFTSTD), //$NON-NLS-1$
	AND ("&", 113, Assoc.LEFTSTD), //$NON-NLS-1$
	OR ("|", 114, Assoc.LEFTSTD), //$NON-NLS-1$
	
	MODEL ("~", 1001, Assoc.LEFTSTD), //$NON-NLS-1$
	
	A_RIGHT ("->", 10001, Assoc.LEFTSTD), //$NON-NLS-1$
	A_EQUALS ("=", 10002, Assoc.RIGHTSTD), //$NON-NLS-1$
	A_LEFT ("<-", 10003, Assoc.RIGHTSTD), //$NON-NLS-1$
	
	C_IF ("if", 100001, Assoc.RIGHTSTD), //$NON-NLS-1$
	C_FOR ("for", 100002, Assoc.LEFTSTD), //$NON-NLS-1$
	C_IN ("in", 100002, Assoc.LEFTSTD), //$NON-NLS-1$
	C_WHILE ("while", 100002, Assoc.LEFTSTD), //$NON-NLS-1$
	C_REPEAT ("repeat", 100002, Assoc.LEFTSTD), //$NON-NLS-1$
	C_NEXT ("next", 1, Assoc.TERM), //$NON-NLS-1$
	C_BREAK ("break", 1, Assoc.TERM), //$NON-NLS-1$
	F_DEF ("def", 100002, Assoc.RIGHTSTD), //$NON-NLS-1$
	F_DEF_ARGS ("○", 12, Assoc.CONTAINER), //$NON-NLS-1$
	F_DEF_ARG ("•", 12, Assoc.CONTAINER), //$NON-NLS-1$
	F_CALL ("call", 11, Assoc.LEFTSTD), //$NON-NLS-1$
	F_CALL_ARGS ("○", 12, Assoc.CONTAINER), //$NON-NLS-1$
	F_CALL_ARG ("•", 12, Assoc.CONTAINER), //$NON-NLS-1$
	
	HELP ("?", 999, Assoc.LEFTSTD), //$NON-NLS-1$
	;
	
	
	public final String label;
	public final int opPrec;
	final int opAssoc;
	
	
	NodeType(final String label, final int precedence, final int assoc) {
		this.label = label;
		this.opPrec = precedence;
		this.opAssoc = assoc;
	}
	
}
