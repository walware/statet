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

package de.walware.statet.r.internal.ui.datafilter;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String IntervalFilter_label;
	public static String LevelFilter_label;
	public static String LevelFilter_TooMuch_message;
	public static String TextFilter_label;
	public static String TextFilter_TooMuch_message;
	
	public static String TextSearch_Eclipse_label;
	public static String TextSearch_Regex_label;
	public static String TextSearch_Exact_label;
	
	public static String UpdateJob_label;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
