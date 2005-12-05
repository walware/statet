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

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.ext.ui.AbstractImagesCollection;


public class StatetImages extends AbstractImagesCollection {


	private static final StatetImages fgInstance = new StatetImages();
	
	public static StatetImages getDefault() {
		
		return fgInstance;
	}

	
/* Set of Image Descriptions **************************************************/

	public static final ImageDescriptor DESC_LOCTOOL_FILTER = fgInstance.create(T_LOCTOOL, "filter_view.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_LOCTOOLD_FILTER = fgInstance.create(T_LOCTOOL_D, "filter_view.gif"); //$NON-NLS-1$

	public static final ImageDescriptor DESC_LOCTOOL_EXPANDALL = fgInstance.create(T_LOCTOOL, "expandall.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_LOCTOOL_COLLAPSEALL = fgInstance.create(T_LOCTOOL, "collapseall.gif"); //$NON-NLS-1$

/* Set of Image Keys **********************************************************/
	public static final String IMG_CONTENTASSIST_TEMPLATE = StatetPlugin.ID + ".image.contentassist.template";	 //$NON-NLS-1$
	
	
	private StatetImages() {
		super(StatetPlugin.getDefault());
	}
	
	@Override
	protected void declareImages() {

		declareRegistryImage(StatetImages.IMG_CONTENTASSIST_TEMPLATE, T_OBJ, "template_proposal.gif"); //$NON-NLS-1$
	}
}
