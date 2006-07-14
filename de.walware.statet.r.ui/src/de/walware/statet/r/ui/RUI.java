/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import de.walware.statet.r.ui.internal.RUIPlugin;


/**
 *
 */
public class RUI {

	public static final String PLUGIN_ID = "de.walware.statet.r.ui"; //$NON-NLS-1$
	
	
	public static final String IMG_RCONSOLE = RUI.PLUGIN_ID+"/img/tool/rconsole"; //$NON-NLS-1$

	
	public static ImageDescriptor getImageDescriptor(String key) {
		
		return RUIPlugin.getDefault().getImageRegistry().getDescriptor(key);
	}
	
	/**
	 * Image registry, if you want to reuse this registry.
	 * 
	 * @return image registry of this plugin.
	 */
	public static ImageRegistry getImageRegistry() {
		
		return RUIPlugin.getDefault().getImageRegistry();
	}
	
}
