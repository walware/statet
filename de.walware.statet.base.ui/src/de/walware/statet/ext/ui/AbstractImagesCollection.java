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

package de.walware.statet.ext.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;


public abstract class AbstractImagesCollection {

	/** Icons of Views */
	protected static final String T_VIEW = "view_16";             //$NON-NLS-1$
	/** Icons for global tools */
	protected static final String T_TOOL = "tool_16";             //$NON-NLS-1$
	/** Icons for object, e.g. files or model objects. */
	protected static final String T_OBJ = "obj_16";               //$NON-NLS-1$
	/** Icon overlays */
	protected static final String T_OVR = "ovr_16"; 	          //$NON-NLS-1$
	/** Icons in the banners of wizards */
	protected static final String T_WIZBAN = "wizban";            //$NON-NLS-1$
	/** Icons in local tools */
	protected static final String T_LOCTOOL = "loctool_16"; 	  //$NON-NLS-1$
	/** Icons of deactivated local tools */
	protected static final String T_LOCTOOL_D = "loctool_16_d";   //$NON-NLS-1$
	
	
/* Methods ********************************************************************/
	
	protected URL fIconBaseURL;
	private ImageRegistry fImageRegistry;
	

	public AbstractImagesCollection(AbstractUIPlugin plugin) {
		
		fIconBaseURL = plugin.getBundle().getEntry("/icons/");
		fImageRegistry = plugin.getImageRegistry();
		
		declareImages();
	}

	/**
	 * Declare all images in registry.
	 * Should be implemented by subclasses.
	 */
	protected void declareImages() {
		
	}
	
	protected ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	
	protected URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
		
		if (fIconBaseURL == null)
			throw new MalformedURLException();
			
		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append('/');
		buffer.append(name);
		return new URL(fIconBaseURL, buffer.toString());
	}	

	/**
	 * Returns the ImageRegistry.
	 */
	protected final ImageRegistry getImageRegistry() {
		
//		if (fImageRegistry == null) {
//			initImageRegistry();
//		}
		return fImageRegistry;
	}

	/**
	 * Declare an Image in the registry table.
	 * @param key 	The key to use when registering the image
	 * @param path	The path where the image can be found. This path is relative to where
	 *				this plugin class is found (i.e. typically the packages directory)
	 */
	protected final void declareRegistryImage(String key, String prefix, String path) {
		
		ImageDescriptor desc = ImageDescriptor.getMissingImageDescriptor();
		try {
			desc = ImageDescriptor.createFromURL(makeIconFileURL(prefix, path));
		} catch (MalformedURLException e) {
		}
		fImageRegistry.put(key, desc);
	}
	
	
	/**
	 * Returns the shared <code>Image<code> identified by the given key,
	 * or <code>null</code> if it does not exist.
	 */
	public Image getImage(String key) {
		
		return getImageRegistry().get(key);
	}
	
	/**
	 * Returns the <code>ImageDescriptor<code> of shared images, identified 
	 * by the given key, or <code>null</code> if it does not exist.
	 */
	public ImageDescriptor getDescriptor(String key) {
		
		return getImageRegistry().getDescriptor(key);
	}
}
