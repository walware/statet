/*******************************************************************************
 * Copyright (c) 2006-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.swt.graphics.Image;

import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.ui.IToolRunnableDecorator;
import de.walware.statet.nico.ui.NicoUI;


/**
 * Factory for IToolRunnableAdapter for {@link ToolController.SimpleRunnable }.
 */
public class CommandRunnableAdapterFactory implements IAdapterFactory {
	
	
	private static final Class[] ADAPTERS = new Class[] { IToolRunnableDecorator.class };
	
	
	private static class Adapter implements IToolRunnableDecorator {
		
		private Adapter() {
		}
		
		public Image getImage() {
			return NicoUIPlugin.getDefault().getImageRegistry().get(NicoUI.OBJ_TASK_CONSOLECOMMAND_IMAGE_ID);
		}
		
	}
	
	
	private Adapter fAdapter = new Adapter(); // adapter can be reused
	
	
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (IToolRunnableDecorator.class.equals(adapterType)) {
			return fAdapter;
		}
		return null;
	}
	
	public Class[] getAdapterList() {
		return ADAPTERS;
	}
	
}
