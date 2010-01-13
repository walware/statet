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

package de.walware.ecommons.ui.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;


public class ImageRegistryUtil {
	
	/** Icons of Views */
	public static final String T_VIEW = "view_16";             //$NON-NLS-1$
	/** Icons for global tools */
	public static final String T_TOOL = "tool_16";             //$NON-NLS-1$
	/** Icons for deactivated global tools */
	public static final String T_TOOLD = "tool_16_d";          //$NON-NLS-1$
	/** Icons for object, e.g. files or model objects. */
	public static final String T_OBJ = "obj_16";               //$NON-NLS-1$
	/** Icon overlays */
	public static final String T_OVR = "ovr_16"; 	          //$NON-NLS-1$
	/** Icons in the banners of wizards */
	public static final String T_WIZBAN = "wizban";            //$NON-NLS-1$
	/** Icons in local tools */
	public static final String T_LOCTOOL = "loctool_16"; 	  //$NON-NLS-1$
	/** Icons of deactivated local tools */
	public static final String T_LOCTOOL_D = "loctool_16_d";   //$NON-NLS-1$
	
	
	private AbstractUIPlugin fPlugin;
	private URL fIconBaseURL;
	
	
	public ImageRegistryUtil(final AbstractUIPlugin plugin) {
		fIconBaseURL = plugin.getBundle().getEntry("/icons/"); //$NON-NLS-1$
		fPlugin = plugin;
	}
	
	
	public void register(final String key, final String prefix, final String name) {
		final ImageDescriptor descriptor = createDescriptor(prefix, name);
		fPlugin.getImageRegistry().put(key, descriptor);
	}
	
	protected ImageDescriptor createDescriptor(final String prefix, final String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		} catch (final MalformedURLException e) {
			fPlugin.getLog().log(new Status(IStatus.ERROR, fPlugin.getBundle().getSymbolicName(), 0,
					"Error occured while loading an image descriptor. (internal, unexpected)", e)); //$NON-NLS-1$
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
	
	protected URL makeIconFileURL(final String prefix, final String name) throws MalformedURLException {
		if (fIconBaseURL == null) {
			throw new MalformedURLException();
		}
		return new URL(fIconBaseURL, prefix+'/'+name);
	}
	
}
