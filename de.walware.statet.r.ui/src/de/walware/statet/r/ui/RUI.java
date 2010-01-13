/*******************************************************************************
 * Copyright (c) 2006-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

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
	
	public static final String IMG_OBJ_R_SCRIPT = RUI.PLUGIN_ID + "/image/obj/r_script"; //$NON-NLS-1$
	public static final String IMG_OBJ_R_RUNTIME_ENV = RUI.PLUGIN_ID + "/image/obj/r_environment"; //$NON-NLS-1$
	
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
	public static final String IMG_OBJ_NULL = RUI.PLUGIN_ID + "/image/obj/null"; //$NON-NLS-1$
	public static final String IMG_OBJ_S4OBJ = RUI.PLUGIN_ID + "/image/obj/s4obj"; //$NON-NLS-1$
	public static final String IMG_OBJ_S4OBJ_VECTOR = RUI.PLUGIN_ID + "/image/obj/s4obj.vector"; //$NON-NLS-1$
	public static final String IMG_OBJ_S4OBJ_DATAFRAME_COLUMN = RUI.PLUGIN_ID + "/image/obj/s4obj.dataframe_col"; //$NON-NLS-1$
	public static final String IMG_OBJ_ARGUMENT_ASSIGN = RUI.PLUGIN_ID + "/image/obj/argument.assign"; //$NON-NLS-1$
	
	
	public static ImageDescriptor getImageDescriptor(final String key) {
		return RUIPlugin.getDefault().getImageRegistry().getDescriptor(key);
	}
	
	public static Image getImage(final String key) {
		return RUIPlugin.getDefault().getImageRegistry().get(key);
	}
	
}
