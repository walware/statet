/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui.snippets;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String Variable_ResourceEncoding_description;
	public static String Variable_Echo_description;
	
	public static String SnippetTemplates_title;
	public static String SnippetTemplates_RSnippet_label;
	public static String SnippetTemplates_KeysNote_label;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
