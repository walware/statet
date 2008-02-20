/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.preferences;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	public static String EditTemplateDialog_title_Edit;
	public static String EditTemplateDialog_title_New;
	public static String EditTemplateDialog_error_NoName;
	public static String EditTemplateDialog_Name_label;
	public static String EditTemplateDialog_Description_label;
	public static String EditTemplateDialog_Context_label;
	public static String EditTemplateDialog_AutoInsert_label;
	public static String EditTemplateDialog_Pattern_label;
	public static String EditTemplateDialog_InsertVariable;
	
	public static String EditTemplateDialog_ContentAssist;
	
	public static String ProjectSelectionDialog_title;
	public static String ProjectSelectionDialog_desciption;
	public static String ProjectSelectionDialog_filter;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	
}
