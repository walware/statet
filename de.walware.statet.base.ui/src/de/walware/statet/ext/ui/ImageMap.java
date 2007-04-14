/*******************************************************************************
 * Copyright (c) 2000-2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import de.walware.eclipsecommons.ui.util.UIAccess;



/**
 * A registry that maps <code>ImageDescriptors</code> to <code>Image</code>.
 */
public class ImageMap {


	private Map<ImageDescriptor, Image> fRegistry = new HashMap<ImageDescriptor, Image>(10);
	private ResourceManager fManager;
	
	
	/**
	 * Creates a new image descriptor registry for the current or the workbench display,
	 * respectively.
	 */
	public ImageMap() {
		
		this(UIAccess.getDisplay(null));
	}
	
	/**
	 * Creates a new image descriptor registry for the given display. All images
	 * managed by this registry will be disposed when the display gets disposed.
	 * 
	 * @param display the display the images managed by this registry are allocated for 
	 */
	private ImageMap(Display display) {

		assert (display != null);
		fManager = JFaceResources.getResources(display);
		
		fManager.disposeExec(new Runnable() {
			public void run() {
				dispose();
			}
		});
	}
	
	/**
	 * Returns the image associated with the given image descriptor.
	 * 
	 * @param descriptor the image descriptor for which the registry manages an image
	 * @return the image associated with the image descriptor or <code>null</code>
	 *  if the image descriptor can't create the requested image.
	 */
	public Image get(ImageDescriptor descriptor) {
		
		Image image = (Image) fRegistry.get(descriptor);
		if (image == null) {
			image = fManager.createImageWithDefault(descriptor);
			fRegistry.put(descriptor, image);
		}
		return image;
	}

	/**
	 * Disposes all images managed by this registry.
	 */	
	private void dispose() {
		
		// add fManager.cancelDisposeExec(r), if public.
		for (Object obj : fRegistry.keySet()) {
			fManager.destroyImage((ImageDescriptor) obj);
		}
		fRegistry.clear();
	}
	
}
