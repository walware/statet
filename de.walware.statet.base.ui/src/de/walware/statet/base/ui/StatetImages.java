/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


public class StatetImages {
	// Image files must be registered in StatetUIPlugin
	
	public static final String LOCTOOL_FILTER = StatetUIPlugin.PLUGIN_ID + "/image/loctool/filter_view"; //$NON-NLS-1$
	public static final String LOCTOOLD_FILTER = StatetUIPlugin.PLUGIN_ID + "/image/loctoold/filter_view"; //$NON-NLS-1$
	public static final String LOCTOOL_SORT_ALPHA = StatetUIPlugin.PLUGIN_ID + "/image/loctool/sort.alpha"; //$NON-NLS-1$
	
	public static final String LOCTOOL_EXPANDALL = StatetUIPlugin.PLUGIN_ID + "/image/loctool/expandall"; //$NON-NLS-1$
	public static final String LOCTOOL_COLLAPSEALL = StatetUIPlugin.PLUGIN_ID + "/image/loctool/collapseall"; //$NON-NLS-1$
	
	public static final String LOCTOOL_SCROLLLOCK = StatetUIPlugin.PLUGIN_ID + "/image/loctool/scrolllock"; //$NON-NLS-1$
	
	public static final String LOCTOOL_CLEARSEARCH = StatetUIPlugin.PLUGIN_ID + "/image/loctool/clearsearch"; //$NON-NLS-1$
	public static final String LOCTOOLD_CLEARSEARCH = StatetUIPlugin.PLUGIN_ID + "/image/loctoold/clearsearch"; //$NON-NLS-1$
	public static final String lOCTOOL_SYNCHRONIZED = StatetUIPlugin.PLUGIN_ID + "/image/loctool/synchronized"; //$NON-NLS-1$
	
	public static final String LOCTOOL_PAUSE = StatetUIPlugin.PLUGIN_ID + "/image/loctool/pause"; //$NON-NLS-1$
	public static final String LOCTOOLD_PAUSE = StatetUIPlugin.PLUGIN_ID + "/image/loctoold/pause"; //$NON-NLS-1$
	
	public static final String OBJ_TEXT_TEMPLATE = StatetUIPlugin.PLUGIN_ID + "/image/obj/text.template"; //$NON-NLS-1$
	public static final String OBJ_TEXT_AT_TAG = StatetUIPlugin.PLUGIN_ID + "/image/obj/text.at_tag"; //$NON-NLS-1$
	public static final String CONTENTASSIST_CORRECTION_LINKEDRENAME = StatetUIPlugin.PLUGIN_ID + "/image/obj/assist.linked_rename"; //$NON-NLS-1$
	
	public static final String LAUNCHCONFIG_MAIN = StatetUIPlugin.PLUGIN_ID + "/image/obj/launchconfig.main"; //$NON-NLS-1$
	
	public static final String OBJ_TASK_CONSOLECOMMAND = StatetUIPlugin.PLUGIN_ID + "/image/obj/task.consolecommand"; //$NON-NLS-1$
	public static final String OBJ_TASK_DUMMY = StatetUIPlugin.PLUGIN_ID + "/image/obj/task.commanddummy"; //$NON-NLS-1$
	public static final String OBJ_CONSOLECOMMAND = StatetUIPlugin.PLUGIN_ID + "/image/obj/consolecommand"; //$NON-NLS-1$
	
	public static final String OBJ_IMPORT = StatetUIPlugin.PLUGIN_ID + "/image/obj/import"; //$NON-NLS-1$
	public static final String OBJ_CLASS = StatetUIPlugin.PLUGIN_ID + "/image/obj/class"; //$NON-NLS-1$
	public static final String OBJ_CLASS_EXT = StatetUIPlugin.PLUGIN_ID + "/image/obj/class_ext"; //$NON-NLS-1$
	
	public static final String OBJ_USER = StatetUIPlugin.PLUGIN_ID + "/image/obj/user"; //$NON-NLS-1$
	
	public static final String OVR_DEFAULT_MARKER = StatetUIPlugin.PLUGIN_ID + "/image/ovr/default_marker"; //$NON-NLS-1$
	
	
	public static final String LOCTOOL_CLOSETRAY = StatetUIPlugin.PLUGIN_ID + "/image/loctool/close"; //$NON-NLS-1$
	public static final String LOCTOOL_CLOSETRAY_H = StatetUIPlugin.PLUGIN_ID + "/image/loctoolh/close"; //$NON-NLS-1$
	public static final String LOCTOOL_LEFT = StatetUIPlugin.PLUGIN_ID + "/image/loctool/left"; //$NON-NLS-1$
	public static final String LOCTOOL_LEFT_H = StatetUIPlugin.PLUGIN_ID + "/image/loctoolh/left"; //$NON-NLS-1$
	public static final String LOCTOOL_RIGHT = StatetUIPlugin.PLUGIN_ID + "/image/loctool/right"; //$NON-NLS-1$
	public static final String LOCTOOL_RIGHT_H = StatetUIPlugin.PLUGIN_ID + "/image/loctoolh/right"; //$NON-NLS-1$
	public static final String LOCTOOL_UP = StatetUIPlugin.PLUGIN_ID + "/image/loctool/up"; //$NON-NLS-1$
	public static final String LOCTOOL_UP_H = StatetUIPlugin.PLUGIN_ID + "/image/loctoolh/up"; //$NON-NLS-1$
	public static final String LOCTOOL_DOWN = StatetUIPlugin.PLUGIN_ID + "/image/loctool/down"; //$NON-NLS-1$
	public static final String LOCTOOL_DOWN_H = StatetUIPlugin.PLUGIN_ID + "/image/loctoolh/down"; //$NON-NLS-1$
	
	
	public static ImageDescriptor getDescriptor(final String key) {
		return StatetUIPlugin.getDefault().getImageRegistry().getDescriptor(key);
	}
	
	public static Image getImage(final String key) {
		return StatetUIPlugin.getDefault().getImageRegistry().get(key);
	}
	
	public static Image getCachedImage(final ImageDescriptor descriptor) {
		final String key = descriptor.toString();
		final ImageRegistry registry = StatetUIPlugin.getDefault().getImageRegistry();
		Image image = registry.get(key);
		if (image == null) {
			registry.put(key, descriptor);
			image = registry.get(key);
		}
		return image;
	}
	
	
	private StatetImages() {}
	
}
