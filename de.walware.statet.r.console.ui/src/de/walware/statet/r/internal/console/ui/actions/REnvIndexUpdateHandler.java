/*=============================================================================#
 # Copyright (c) 2010-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.console.ui.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.walware.ecommons.ts.ITool;

import de.walware.statet.nico.ui.actions.AbstractToolHandler;

import de.walware.statet.r.console.core.RConsoleTool;
import de.walware.statet.r.console.ui.tools.REnvIndexAutoUpdater;


/**
 * Command handler scheduling the update of an R environment index.
 */
public class REnvIndexUpdateHandler extends AbstractToolHandler {
	
	
	public static final int INDEX_COMPLETELY=               0x0000_0001;
	
	
	public static class Completely extends REnvIndexUpdateHandler {
		
		
		public Completely() {
			super(INDEX_COMPLETELY);
		}
		
	}
	
	
	private final int mode;
	
	
	public REnvIndexUpdateHandler() {
		this(0);
	}
	
	public REnvIndexUpdateHandler(final int mode) {
		super(RConsoleTool.TYPE, RConsoleTool.R_DATA_FEATURESET_ID);
		
		this.mode= mode;
	}
	
	
	@Override
	protected Object execute(final ITool tool, final ExecutionEvent event) throws ExecutionException {
		tool.getQueue().add(new REnvIndexAutoUpdater.UpdateRunnable(
				((this.mode & 0xf) == INDEX_COMPLETELY) ));
		return null;
	}
	
}
