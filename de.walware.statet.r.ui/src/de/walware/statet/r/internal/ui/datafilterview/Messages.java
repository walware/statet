/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.datafilterview;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String Variables_DisableFilters_label;
	public static String Variables_CopyExpr_label;
	public static String Variable_Clear_label;
	
	public static String Interval_LowerBound_tooltip;
	public static String Interval_UpperBound_tooltip;
	
	public static String Items_Search_label;
	public static String Items_Remove_label;
	public static String Items_RemoveAll_label;
	public static String Items_RemoveSelected_label;
	public static String Items_RemoveUnchecked_label;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}

}
