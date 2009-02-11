/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import de.walware.statet.nico.internal.ui.NicoUIPlugin;


/**
 * Public Nico-UI services.
 * <p>
 * Access via static methods.
 */
public class NicoUI {
	
	/**
	 * Plugin-ID
	 * Value: @value
	 */
	public static final String PLUGIN_ID = "de.walware.statet.nico.ui"; //$NON-NLS-1$
	
	
	public static final String IMG_LOCTOOL_CANCEL = PLUGIN_ID+"/img/loctool/cancel";  //$NON-NLS-1$
	public static final String IMG_LOCTOOLD_CANCEL = PLUGIN_ID+"/img.d/loctool/cancel";  //$NON-NLS-1$
	
	
	public static ImageDescriptor getImageDescriptor(final String key) {
		return NicoUIPlugin.getDefault().getImageRegistry().getDescriptor(key);
	}
	
	public static Image getImage(final String key) {
		return NicoUIPlugin.getDefault().getImageRegistry().get(key);
	}
	
	public static IToolRegistry getToolRegistry() {
		return NicoUIPlugin.getDefault().getToolRegistry();
	}
	
	
	private NicoUI() {}
	
}
