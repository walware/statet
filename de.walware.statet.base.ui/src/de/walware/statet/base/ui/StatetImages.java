/*=============================================================================#
 # Copyright (c) 2005-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.base.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


public class StatetImages {
	// Image files must be registered in StatetUIPlugin
	
	public static final String OBJ_IMPORT = StatetUIPlugin.PLUGIN_ID + "/image/obj/import"; //$NON-NLS-1$
	public static final String OBJ_CLASS = StatetUIPlugin.PLUGIN_ID + "/image/obj/class"; //$NON-NLS-1$
	public static final String OBJ_CLASS_EXT = StatetUIPlugin.PLUGIN_ID + "/image/obj/class_ext"; //$NON-NLS-1$
	
	public static final String TOOL_REFRESH = StatetUIPlugin.PLUGIN_ID + "/image/tool/refresh"; //$NON-NLS-1$
	public static final String TOOLD_REFRESH = StatetUIPlugin.PLUGIN_ID + "/image/toold/refresh"; //$NON-NLS-1$
	
	
	public static ImageDescriptor getDescriptor(final String key) {
		return StatetUIPlugin.getDefault().getImageRegistry().getDescriptor(key);
	}
	
	public static Image getImage(final String key) {
		return StatetUIPlugin.getDefault().getImageRegistry().get(key);
	}
	
	
	private StatetImages() {}
	
}
