/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.core;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;


@NonNullByDefault
@SuppressWarnings("null")
public class Messages extends NLS {
	
	
	public static String DebugContext_label;
	public static String DebugContext_UpdateStackFrame_task;
	public static String DebugContext_UpdateVariables_task;
	
	public static String Expression_Validate_Invalid_message;
	public static String Expression_Validate_Detail_SingleExpression_message;
	public static String Expression_Validate_Detail_DetailMissing_message;
	public static String Expression_Evaluate_task;
	public static String Expression_Evaluate_Cancelled_message;
	public static String Expression_Clean_task;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
