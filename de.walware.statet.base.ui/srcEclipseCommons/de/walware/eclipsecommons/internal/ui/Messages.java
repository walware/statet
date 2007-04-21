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
	
	
	public static String BrowseFilesystem_button_name;
	public static String BrowseWorkspace_button_name;
	public static String Location_error_UnknownFormat_messageLocation_error_UnknownFormat_message;
	public static String ResourceSelectionDialog_title;
	public static String ResourceSelectionDialog_message;
	
	public static String File_error_DoesNotExists_message;
	public static String File_error_AlreadyExists_message;
	public static String File_error_NoValidFile_message;
	

	private static final String BUNDLE_NAME = Messages.class.getName();
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String CopyToClipboard_error_title;
	public static String CopyToClipboard_error_message;

}
