/*******************************************************************************
 * Copyright (c) 2011-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.nolazy;

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;


public class RResourceDecorator extends BaseLabelProvider implements ILightweightLabelDecorator {
	
	
	private static final Path OVR_PATH = new Path("icons/ovr_16"); //$NON-NLS-1$
	
	
	private static ImageDescriptor fPackageProjectOverlay;
	
	
	/**
	 * Created by extension point
	 */
	public RResourceDecorator() {
	}
	
	
	@Override
	public void decorate(final Object element, final IDecoration decoration) {
		IProject project = null;
		if (element instanceof IProject) {
			project = (IProject) element;
		}
		if (project != null) {
			try {
				if (project.hasNature(de.walware.statet.r.core.RPkgProject.NATURE_ID)) {
					decoration.addOverlay(getPackageProjectOverlay(), IDecoration.TOP_LEFT);
				}
			}
			catch (final CoreException e) {
			}
		}
	}
	
	private ImageDescriptor getPackageProjectOverlay() {
		if (fPackageProjectOverlay == null) {
			fPackageProjectOverlay = createOverlayDescriptor("rpkg_project"); //$NON-NLS-1$
		}
		return fPackageProjectOverlay;
	}
	
	private ImageDescriptor createOverlayDescriptor(final String id) {
		final URL url = FileLocator.find(Platform.getBundle(de.walware.statet.r.ui.RUI.PLUGIN_ID),
				OVR_PATH.append(id + ".png"), null );
		return ImageDescriptor.createFromURL(url);
	}
	
}
