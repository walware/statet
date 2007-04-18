/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;

import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.ui.IToolRunnableAdapter;
import de.walware.statet.ui.StatetImages;


/**
 * Factory for IToolRunnableAdapter for {@link ToolController.SimpleRunnable }.
 */
public class CommandRunnableAdapterFactory implements IAdapterFactory {

	
	private static final Class[] ADAPTERS = new Class[] { IToolRunnableAdapter.class };
	
	
	public class Adapter implements IToolRunnableAdapter {
		
		private Adapter() {
		}
		
		public ImageDescriptor getImageDescriptor() {
			
			return StatetImages.getDefault().getDescriptor(StatetImages.IMG_OBJ_COMMAND);
		}
	}
	
	
	private Adapter fAdapter = new Adapter(); // adapter can be reused

	
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		
		if (IToolRunnableAdapter.class.equals(adapterType)) {
			return fAdapter;
		}
		return null;
	}

	public Class[] getAdapterList() {
		
		return ADAPTERS;
	}

}
