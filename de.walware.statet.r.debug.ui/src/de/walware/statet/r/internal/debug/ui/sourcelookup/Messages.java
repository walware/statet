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

package de.walware.statet.r.internal.debug.ui.sourcelookup;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String RLibrarySourceContainerBrowser_Add_title;
	public static String RLibrarySourceContainerBrowser_Edit_title;
	public static String RLibrarySourceContainerBrowser_Directory_label;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}
