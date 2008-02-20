/*******************************************************************************
 * Copyright (c) 2006-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
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
	
	public static final String IMG_OBJ_R_ENVIRONMENT = RUI.PLUGIN_ID+"/image/obj/r_environment"; //$NON-NLS-1$
	
	
	public static ImageDescriptor getImageDescriptor(final String key) {
		return RUIPlugin.getDefault().getImageRegistry().getDescriptor(key);
	}
	
	public static Image getImage(final String key) {
		return RUIPlugin.getDefault().getImageRegistry().get(key);
	}
	
}
