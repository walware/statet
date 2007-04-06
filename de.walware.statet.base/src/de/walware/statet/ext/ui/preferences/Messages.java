/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
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
	
	private static final String BUNDLE_NAME = Messages.class.getName();

	public static String EditTemplateDialog_title_Edit;
	public static String EditTemplateDialog_title_New;
	public static String EditTemplateDialog_error_NoName;
	public static String EditTemplateDialog_Name_label;
	public static String EditTemplateDialog_Description_label;
	public static String EditTemplateDialog_Context_label;
	public static String EditTemplateDialog_AutoInsert_label;
	public static String EditTemplateDialog_Pattern_label;
	public static String EditTemplateDialog_InsertVariable;

	public static String EditTemplateDialog_Undo;
	public static String EditTemplateDialog_Cut;
	public static String EditTemplateDialog_Copy;
	public static String EditTemplateDialog_Paste;
	public static String EditTemplateDialog_SelectAll;
	public static String EditTemplateDialog_ContentAssist;

	public static String ProjectSelectionDialog_title;
	public static String ProjectSelectionDialog_desciption;
	public static String ProjectSelectionDialog_filter;

	public static String TemplateVariableProposal_error_title;
	

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
}
