/*=============================================================================#
 # Copyright (c) 2006-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import de.walware.statet.r.internal.ui.RUIPlugin;


/**
 * 
 */
public class RUI {
	
	public static final String PLUGIN_ID = "de.walware.statet.r.ui"; //$NON-NLS-1$
	
	public static final String R_EDITOR_ID = "de.walware.statet.r.editors.R"; //$NON-NLS-1$
	public static final String RD_EDITOR_ID = "de.walware.statet.r.editors.Rd"; //$NON-NLS-1$
	
	public static final String R_HELP_VIEW_ID = "de.walware.statet.r.views.RHelp"; //$NON-NLS-1$
	public static final String R_HELP_SEARCH_PAGE_ID = "de.walware.statet.r.searchPages.RHelpPage"; //$NON-NLS-1$
	
	public static final String IMG_OBJ_R_SCRIPT = RUI.PLUGIN_ID + "/image/obj/r_script"; //$NON-NLS-1$
	public static final String IMG_OBJ_R_RUNTIME_ENV = RUI.PLUGIN_ID + "/image/obj/r_environment"; //$NON-NLS-1$
	public static final String IMG_OBJ_R_REMOTE_ENV = RUI.PLUGIN_ID + "/image/obj/r_environment.remote"; //$NON-NLS-1$
	public static final String IMG_OBJ_R_PACKAGE = RUI.PLUGIN_ID + "/image/obj/r_package"; //$NON-NLS-1$
	public static final String IMG_OBJ_R_PACKAGE_NOTA = RUI.PLUGIN_ID + "/image/obj/r_package-notavail"; //$NON-NLS-1$
	public static final String IMG_OBJ_R_HELP_PAGE = RUI.PLUGIN_ID + "/image/obj/r_help/page"; //$NON-NLS-1$
	public static final String IMG_OBJ_R_HELP_SEARCH = RUI.PLUGIN_ID + "/image/obj/r_help/search"; //$NON-NLS-1$
	
	public static final String IMG_OBJ_METHOD = RUI.PLUGIN_ID + "/image/obj/method"; //$NON-NLS-1$
	public static final String IMG_OBJ_COMMON_FUNCTION = RUI.PLUGIN_ID + "/image/obj/function.common"; //$NON-NLS-1$
	public static final String IMG_OBJ_COMMON_LOCAL_FUNCTION = RUI.PLUGIN_ID + "/image/obj/function.common.local"; //$NON-NLS-1$
	public static final String IMG_OBJ_GENERIC_FUNCTION = RUI.PLUGIN_ID + "/image/obj/function.generic"; //$NON-NLS-1$
	public static final String IMG_OBJ_GENERAL_VARIABLE = RUI.PLUGIN_ID + "/image/obj/variable.common"; //$NON-NLS-1$
	public static final String IMG_OBJ_GENERAL_LOCAL_VARIABLE = RUI.PLUGIN_ID + "/image/obj/variable.common.local"; //$NON-NLS-1$
	public static final String IMG_OBJ_SLOT = RUI.PLUGIN_ID + "/image/obj/variable.slot"; //$NON-NLS-1$
	public static final String IMG_OBJ_PACKAGEENV = RUI.PLUGIN_ID + "/image/obj/packageenv"; //$NON-NLS-1$
	public static final String IMG_OBJ_GLOBALENV = RUI.PLUGIN_ID + "/image/obj/globalenv"; //$NON-NLS-1$
	public static final String IMG_OBJ_EMPTYENV = RUI.PLUGIN_ID + "/image/obj/emptyenv"; //$NON-NLS-1$
	public static final String IMG_OBJ_OTHERENV = RUI.PLUGIN_ID + "/image/obj/otherenv"; //$NON-NLS-1$
	public static final String IMG_OBJ_LIST = RUI.PLUGIN_ID + "/image/obj/list"; //$NON-NLS-1$
	public static final String IMG_OBJ_DATAFRAME = RUI.PLUGIN_ID + "/image/obj/dataframe"; //$NON-NLS-1$
	public static final String IMG_OBJ_DATAFRAME_COLUMN = RUI.PLUGIN_ID + "/image/obj/datastore"; //$NON-NLS-1$
	public static final String IMG_OBJ_VECTOR = RUI.PLUGIN_ID + "/image/obj/vector"; //$NON-NLS-1$
	public static final String IMG_OBJ_ARRAY = RUI.PLUGIN_ID + "/image/obj/array"; //$NON-NLS-1$
	public static final String IMG_OBJ_S4OBJ = RUI.PLUGIN_ID + "/image/obj/s4obj"; //$NON-NLS-1$
	public static final String IMG_OBJ_S4OBJ_VECTOR = RUI.PLUGIN_ID + "/image/obj/s4obj.vector"; //$NON-NLS-1$
	public static final String IMG_OBJ_S4OBJ_DATAFRAME_COLUMN = RUI.PLUGIN_ID + "/image/obj/s4obj.dataframe_col"; //$NON-NLS-1$
	public static final String IMG_OBJ_NULL = RUI.PLUGIN_ID + "/image/obj/null"; //$NON-NLS-1$
	public static final String IMG_OBJ_MISSING = RUI.PLUGIN_ID + "/image/obj/missing"; //$NON-NLS-1$
	public static final String IMG_OBJ_PROMISE = RUI.PLUGIN_ID + "/image/obj/promise"; //$NON-NLS-1$
	public static final String IMG_OBJ_ARGUMENT_ASSIGN = RUI.PLUGIN_ID + "/image/obj/argument.assign"; //$NON-NLS-1$
	
	public static final String IMG_OBJ_COL_LOGI = RUI.PLUGIN_ID + "/image/obj/col.logi"; //$NON-NLS-1$
	public static final String IMG_OBJ_COL_INT = RUI.PLUGIN_ID + "/image/obj/col.int"; //$NON-NLS-1$
	public static final String IMG_OBJ_COL_NUM = RUI.PLUGIN_ID + "/image/obj/col.num"; //$NON-NLS-1$
	public static final String IMG_OBJ_COL_CPLX = RUI.PLUGIN_ID + "/image/obj/col.cplx"; //$NON-NLS-1$
	public static final String IMG_OBJ_COL_CHAR = RUI.PLUGIN_ID + "/image/obj/col.char"; //$NON-NLS-1$
	public static final String IMG_OBJ_COL_RAW = RUI.PLUGIN_ID + "/image/obj/col.raw"; //$NON-NLS-1$
	public static final String IMG_OBJ_COL_FACTOR = RUI.PLUGIN_ID + "/image/obj/col.factor"; //$NON-NLS-1$
	public static final String IMG_OBJ_COL_DATE = RUI.PLUGIN_ID + "/image/obj/col.date"; //$NON-NLS-1$
	public static final String IMG_OBJ_COL_DATETIME = RUI.PLUGIN_ID + "/image/obj/col.datetime"; //$NON-NLS-1$
	
	public static final String IMG_OBJ_LIBRARY_GROUP = PLUGIN_ID + "/image/obj/library.group"; //$NON-NLS-1$
	public static final String IMG_OBJ_LIBRARY_LOCATION = PLUGIN_ID + "/image/obj/library.location"; //$NON-NLS-1$
	
	public static final String IMG_LOCTOOL_SORT_PACKAGE = PLUGIN_ID + "/image/obj/sort.package"; //$NON-NLS-1$
	
	
	public static ImageDescriptor getImageDescriptor(final String key) {
		return RUIPlugin.getDefault().getImageRegistry().getDescriptor(key);
	}
	
	public static Image getImage(final String key) {
		return RUIPlugin.getDefault().getImageRegistry().get(key);
	}
	
}
