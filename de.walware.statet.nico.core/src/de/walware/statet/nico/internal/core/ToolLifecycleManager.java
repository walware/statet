/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.internal.core;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;

import de.walware.ecommons.FastList;

import de.walware.statet.nico.core.IToolLifeListener;
import de.walware.statet.nico.core.runtime.ToolProcess;


public class ToolLifecycleManager implements IDebugEventSetListener {
	
	
	private class StartupListener implements IDebugEventSetListener {
		
		private final ToolProcess fTool;
		
		public StartupListener(final ToolProcess tool) {
			fTool = tool;
		}
		
		@Override
		public void handleDebugEvents(final DebugEvent[] events) {
			for (final DebugEvent event : events) {
				if (event.getSource() == fTool) {
					switch (event.getKind()) {
						case DebugEvent.MODEL_SPECIFIC:
							DebugPlugin.getDefault().removeDebugEventListener(this);
							if ((event.getDetail() & ToolProcess.TYPE_MASK) == ToolProcess.STATUS) {
								for (final IToolLifeListener listener : fLifeListeners.toArray()) {
									listener.toolStarted(fTool);
								}
							}
							return;
						case DebugEvent.TERMINATE:
							DebugPlugin.getDefault().removeDebugEventListener(this);
							return;
						}
					}
				}
			}
	}
	
	
	private final FastList<IToolLifeListener> fLifeListeners= new FastList<>(IToolLifeListener.class, FastList.IDENTITY);
	
	
	public ToolLifecycleManager() {
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
		fLifeListeners.clear();
	}
	
	
	public void addToolLifeListener(final IToolLifeListener listener) {
		fLifeListeners.add(listener);
	}
	
	public void removeToolLifeListener(final IToolLifeListener listener) {
		fLifeListeners.remove(listener);
	}
	
	@Override
	public void handleDebugEvents(final DebugEvent[] events) {
		for (final DebugEvent event : events) {
			switch (event.getKind()) {
			case DebugEvent.CREATE:
				if (event.getSource() instanceof ToolProcess) {
					DebugPlugin.getDefault().addDebugEventListener(new StartupListener((ToolProcess) event.getSource()));
				}
				break;
			case DebugEvent.TERMINATE:
				if (event.getSource() instanceof ToolProcess) {
					final ToolProcess tool = (ToolProcess) event.getSource();
					for (final IToolLifeListener listener : fLifeListeners.toArray()) {
						listener.toolTerminated(tool);
					}
				}
				break;
			}
		}
	}
	
}
