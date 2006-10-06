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

package de.walware.statet.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.ext.ui.AbstractImagesCollection;
import de.walware.statet.ext.ui.ImageMap;


public class StatetImages extends AbstractImagesCollection {


	private static final StatetImages fgInstance = new StatetImages();
	
	public static StatetImages getDefault() {
		
		return fgInstance;
	}
	
	public static Image getCachedImage(ImageDescriptor descriptor) {
		
		return getDefault().fImageCache.get(descriptor);
	}
	
	
/* Set of Image Descriptions **************************************************/

	public static final ImageDescriptor DESC_LOCTOOL_FILTER = fgInstance.create(T_LOCTOOL, "filter_view.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_LOCTOOLD_FILTER = fgInstance.create(T_LOCTOOL_D, "filter_view.gif"); //$NON-NLS-1$

	public static final ImageDescriptor DESC_LOCTOOL_EXPANDALL = fgInstance.create(T_LOCTOOL, "expandall.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_LOCTOOL_COLLAPSEALL = fgInstance.create(T_LOCTOOL, "collapseall.gif"); //$NON-NLS-1$
	
	public static final ImageDescriptor DESC_LOCTOOL_SCROLLLOCK = fgInstance.create(T_LOCTOOL, "scrolllock.gif"); //$NON-NLS-1$
	
	public static final ImageDescriptor DESC_LOCTOOL_PAUSE = fgInstance.create(T_LOCTOOL, "pause.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_LOCTOOLD_PAUSE = fgInstance.create(T_LOCTOOL_D, "pause.gif"); //$NON-NLS-1$

	
/* Set of Image Keys **********************************************************/
	public static final String IMG_CONTENTASSIST_TEMPLATE = StatetPlugin.ID + ".image.contentassist.template";  //$NON-NLS-1$
	
	public static final String IMG_LAUNCHCONFIG_MAIN = StatetPlugin.ID + ".image.launchconfig.main";  //$NON-NLS-1$
	
	public static final String IMG_OBJ_COMMAND = StatetPlugin.ID + ".image.obj.command";  //$NON-NLS-1$
	public static final String IMG_OBJ_COMMAND_DUMMY = StatetPlugin.ID + ".image.obj.commanddummy";  //$NON-NLS-1$
	
	public static final String IMG_LOCTOOL_SORT_ALPHA = StatetPlugin.ID + ".tool.sort.alpha";  //$NON-NLS-1$
	
	
	
	private ImageMap fImageCache;
	
	private StatetImages() {
		super(StatetPlugin.getDefault());
		fImageCache = new ImageMap();
	}
	
	@Override
	protected void declareImages() {

		declareRegistryImage(IMG_CONTENTASSIST_TEMPLATE, T_OBJ, "template_proposal.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_LAUNCHCONFIG_MAIN, T_OBJ, "main_tab.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_OBJ_COMMAND, T_OBJ, "command.png"); //$NON-NLS-1$
		declareRegistryImage(IMG_OBJ_COMMAND_DUMMY, T_OBJ, "command_dummy.png"); //$NON-NLS-1$
		declareRegistryImage(IMG_LOCTOOL_SORT_ALPHA, T_LOCTOOL, "sort_alpha.gif"); //$NON-NLS-1$
	}
		
}
