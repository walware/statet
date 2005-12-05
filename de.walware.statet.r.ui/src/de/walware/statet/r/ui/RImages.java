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

package de.walware.statet.r.ui;

import org.eclipse.jface.resource.ImageDescriptor;

import de.walware.statet.ext.ui.AbstractImagesCollection;


public class RImages extends AbstractImagesCollection {


	private static final RImages fgInstance = new RImages();

	public static RImages getDefault() {
		
		return fgInstance;
	}
	
/* Set of Image Descriptions **************************************************/

	public static final ImageDescriptor DESC_WIZBAN_NEWRFILE = fgInstance.create(T_WIZBAN, "new_r-file.png"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_WIZBAN_NEWRDFILE = fgInstance.create(T_WIZBAN, "new_rd-file.png"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_WIZBAN_NEWRPROJECT = fgInstance.create(T_WIZBAN, "new_r-project.png"); //$NON-NLS-1$
	
	public static final String IMG_LAUNCHCONFIGTAB_MAIN = RUiPlugin.ID + ".image.launchconfigtab.main";	 //$NON-NLS-1$
	
	private RImages() {
		super(RUiPlugin.getDefault());
	}
	
	@Override
	protected void declareImages() {

		declareRegistryImage(IMG_LAUNCHCONFIGTAB_MAIN, T_OBJ, "main_tab.gif"); //$NON-NLS-1$
	}
}
