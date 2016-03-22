/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.core.util;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;

import de.walware.ecommons.ts.ITool;


public abstract class ToolTerminateListener implements IDebugEventSetListener {
	
	
	private final ITool tool;
	
	
	public ToolTerminateListener(final ITool tool) {
		this.tool= tool;
	}
	
	
	public void install() {
		final DebugPlugin debugPlugin= DebugPlugin.getDefault();
		if (debugPlugin != null) {
			debugPlugin.addDebugEventListener(this);
			if (this.tool.isTerminated()) {
				debugPlugin.removeDebugEventListener(this);
				toolTerminated();
			}
		}
	}
	
	public void dispose() {
		final DebugPlugin debugPlugin= DebugPlugin.getDefault();
		if (debugPlugin != null) {
			debugPlugin.removeDebugEventListener(this);
		}
	}
	
	@Override
	public void handleDebugEvents(final DebugEvent[] events) {
		for (int i= 0; i < events.length; i++) {
			final DebugEvent event= events[i];
			if (event.getSource() == this.tool
					&& event.getKind() == DebugEvent.TERMINATE) {
				toolTerminated();
			}
		}
	}
	
	public abstract void toolTerminated();
	
}
