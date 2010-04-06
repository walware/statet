/*******************************************************************************
 * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui.actions;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.IHandlerService;

import de.walware.ecommons.ui.actions.WindowContributionsProvider;

import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.actions.CancelHandler;
import de.walware.statet.nico.ui.actions.WindowToolProvider;


public class NicoWindowContributions extends WindowContributionsProvider {
	
	
	private static class Contributions extends WindowContributions {
		
		public Contributions(final IWorkbenchWindow window) {
			super(window);
		}
		
		@Override
		protected void init() {
			final IWorkbenchWindow window = getWindow();
			final IHandlerService handlerService = (IHandlerService) window.getService(IHandlerService.class);
			
			add(handlerService.activateHandler(NicoUI.PAUSE_COMMAND_ID, 
					new PauseHandler(new WindowToolProvider(window), window)));
			add(handlerService.activateHandler(NicoUI.DISCONNECT_COMMAND_ID,
					new DisconnectEngineHandler(new WindowToolProvider(window), window)));
			add(handlerService.activateHandler(NicoUI.RECONNECT_COMMAND_ID,
					new ReconnectEngineHandler(new WindowToolProvider(window), window)));
			
			add(handlerService.activateHandler(NicoUI.CANCEL_ALL_COMMAND_ID,
					new CancelHandler(new WindowToolProvider(window), ToolController.CANCEL_ALL)));
			add(handlerService.activateHandler(NicoUI.CANCEL_CURRENT_COMMAND_ID,
					new CancelHandler(new WindowToolProvider(window), ToolController.CANCEL_CURRENT)));
			add(handlerService.activateHandler(NicoUI.CANCEL_PAUSE_COMMAND_ID,
					new CancelHandler(new WindowToolProvider(window), ToolController.CANCEL_CURRENT | ToolController.CANCEL_PAUSE)));
		}
		
	}
	
	
	public NicoWindowContributions() {
	}
	
	
	@Override
	protected String getPluginId() {
		return NicoUI.PLUGIN_ID;
	}
	
	@Override
	protected WindowContributions createWindowContributions(final IWorkbenchWindow window) {
		return new Contributions(window);
	}
	
}
