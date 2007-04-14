/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.templates;

import org.eclipse.osgi.util.NLS;


public class TemplatesMessages extends NLS {
	
	private static final String BUNDLE_NAME = TemplatesMessages.class.getName();

	
	public static String Templates_Variable_ToDo_description;

	public static String Templates_Variable_EnclosingProject_description;
	public static String Templates_Variable_File_description;
	
	public static String Templates_Variable_SelectedLines_description;
	

	static {
		NLS.initializeMessages(BUNDLE_NAME, TemplatesMessages.class);
	}
	
}
