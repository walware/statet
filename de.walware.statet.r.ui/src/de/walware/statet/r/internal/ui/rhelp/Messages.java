/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.rhelp;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String Search_Query_label;
	
	public static String Search_PatternInTopics_label;
	public static String Search_PatternInFields_label;
	public static String Search_Pattern_label;
	public static String Search_SingleMatch_label;
	public static String Search_MultipleMatches_label;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
