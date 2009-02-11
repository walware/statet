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

package de.walware.statet.r.internal.core.sourcemodel;

import org.eclipse.osgi.util.NLS;


/**
 * {@link SyntaxProblemReporter}
 */
public class ProblemMessages extends NLS {
	
	
	public static String Syntax_StringNotClosed_message;
	public static String Syntax_QuotedSymbolNotClosed_message;
	public static String Syntax_SpecialNotClosed_message;
	
	public static String Syntax_FloatWithLLiteral_message;
	public static String Syntax_FloatExpInvalid_message;
	
	public static String Syntax_TokenUnknown_message;
	public static String Syntax_TokenUnexpected_message;
	
	public static String Syntax_ExprInGroupMissing_message;
	public static String Syntax_ExprBeforeOpMissing_message;
	public static String Syntax_ExprAfterOpMissing_message;
	public static String Syntax_ExprAsConditionMissing_message;
	public static String Syntax_ExprAsForSequenceMissing_message;
	public static String Syntax_ExprAsThenBodyMissing_message;
	public static String Syntax_ExprAsElseBodyMissing_message;
	public static String Syntax_ExprAsLoopBodyMissing_message;
	public static String Syntax_ExprAsFdefBodyMissing_message;
	public static String Syntax_ExprAsFdefArgDefaultMissing_message;
	public static String Syntax_ElementnameMissing_message;
	public static String Syntax_SymbolMissing_message;
	
	public static String Syntax_FcallArgsNotClosed_message;
	public static String Syntax_OperatorMissing_message;
	public static String Syntax_SubindexedNotClosed_message;
	
	public static String Syntax_GroupNotClosed_message;
	public static String Syntax_BlockNotClosed_message;
	public static String Syntax_ConditionMissing_message;
	public static String Syntax_IfOfElseMissing_message;
	public static String Syntax_InOfForConditionMissing_message;
	public static String Syntax_ConditionNotClosed_message;
	
	public static String Syntax_FdefArgsMissing_message;
	public static String Syntax_FdefArgsNotClosed_message;
	
	// automatically generated
	public static String Syntax_SubindexedNotClosed_S_message;
	public static String Syntax_SubindexedNotClosed_Done_message;
	public static String Syntax_SubindexedNotClosed_Dboth_message;
	public static String Syntax_ConditionMissing_If_message;
	public static String Syntax_ConditionMissing_For_message;
	public static String Syntax_ConditionMissing_While_message;
	public static String Syntax_ConditionNotClosed_If_message;
	public static String Syntax_ConditionNotClosed_For_message;
	public static String Syntax_ConditionNotClosed_While_message;
	
	
	static {
		NLS.initializeMessages(ProblemMessages.class.getName(), ProblemMessages.class);
		
		// Combined messages
		Syntax_SubindexedNotClosed_S_message = NLS.bind(Syntax_SubindexedNotClosed_message, "'[…]'", "]"); //$NON-NLS-1$ //$NON-NLS-2$
		Syntax_SubindexedNotClosed_Done_message = NLS.bind(Syntax_SubindexedNotClosed_message, "'[[…]]'", "]"); //$NON-NLS-1$ //$NON-NLS-2$
		Syntax_SubindexedNotClosed_Dboth_message = NLS.bind(Syntax_SubindexedNotClosed_message, "'[[…]]'", "]]"); //$NON-NLS-1$ //$NON-NLS-2$
		Syntax_ConditionMissing_If_message = NLS.bind(Syntax_ConditionMissing_message, "if"); //$NON-NLS-1$
		Syntax_ConditionMissing_For_message = NLS.bind(Syntax_ConditionMissing_message, "for"); //$NON-NLS-1$
		Syntax_ConditionMissing_While_message = NLS.bind(Syntax_ConditionMissing_message, "while"); //$NON-NLS-1$
		Syntax_ConditionNotClosed_If_message = NLS.bind(Syntax_ConditionNotClosed_message, "if"); //$NON-NLS-1$
		Syntax_ConditionNotClosed_For_message = NLS.bind(Syntax_ConditionNotClosed_message, "for"); //$NON-NLS-1$
		Syntax_ConditionNotClosed_While_message = NLS.bind(Syntax_ConditionNotClosed_message, "while"); //$NON-NLS-1$
	}
	
	private ProblemMessages() {}
	
}
