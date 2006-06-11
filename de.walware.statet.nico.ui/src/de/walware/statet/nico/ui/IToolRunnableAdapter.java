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

package de.walware.statet.nico.ui;

import org.eclipse.jface.resource.ImageDescriptor;


/**
 * UI Adapter for IToolRunnable
 */
public interface IToolRunnableAdapter {

	
    /**
     * Returns an image descriptor to be used for displaying a runnable.
     *
     * @return the image descriptor or <code>null</code>.
     */
	public ImageDescriptor getImageDescriptor();
	
}
