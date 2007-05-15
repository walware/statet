/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.internal.ui;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	public static String CopyToClipboard_error_title;
	public static String CopyToClipboard_error_message;

	public static String StatusMessage_Warning_prefix;
	public static String StatusMessage_Info_prefix;

	public static String BrowseFilesystem_label;
	public static String BrowseWorkspace_label;

	public static String ChooseResource_Task_description;
	public static String ResourceSelectionDialog_title;
	public static String ResourceSelectionDialog_message;
	

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
