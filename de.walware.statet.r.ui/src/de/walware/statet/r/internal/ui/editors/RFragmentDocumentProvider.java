/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;

import de.walware.ecommons.ltk.ui.sourceediting.FragmentDocumentProvider;

import de.walware.statet.nico.core.runtime.ToolProcess;

import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.ui.editors.RDocumentSetupParticipant;


public class RFragmentDocumentProvider extends FragmentDocumentProvider {
	
	
	private final Map<ToolProcess, ProcessListener> fProcesses = new HashMap<ToolProcess, ProcessListener>();
	
	
	private class ProcessListener implements IDebugEventSetListener {
		
		private final ToolProcess fProcess;
		
		private final List<Object> fElements = new ArrayList<Object>(4);
		
		public ProcessListener(final ToolProcess process) {
			fProcess = process;
			
			init();
		}
		
		private void init() {
			DebugPlugin.getDefault().addDebugEventListener(this);
			fProcesses.put(fProcess, this);
		}
		
		private void dispose() {
			DebugPlugin.getDefault().removeDebugEventListener(this);
			fProcesses.remove(fProcess);
		}
		
		@Override
		public void handleDebugEvents(final DebugEvent[] events) {
			for (int i = 0; i < events.length; i++) {
				if (events[i].getSource() == fProcess) {
					if (events[i].getKind() == DebugEvent.TERMINATE) {
						terminated();
					}
				}
			}
		}
		
		protected void terminated() {
			Object[] elements;
			synchronized (fProcesses) {
				dispose();
				elements = fElements.toArray();
				fElements.clear();
			}
			
			for (final Object element : elements) {
				fireElementDeleted(element);
			}
		}
		
		public void add(final Object element) {
			fElements.add(element);
		}
		
		public void remove(final Object element) {
			if (fElements.remove(element) && fElements.isEmpty()) {
				dispose();
			}
		}
		
	}
	
	
	public RFragmentDocumentProvider() {
		super(RModel.TYPE_ID, new RDocumentSetupParticipant());
	}
	
	
	@Override
	protected ElementInfo createElementInfo(final Object element) throws CoreException {
		final ElementInfo info = super.createElementInfo(element);
		
		if (info != null && element instanceof IAdaptable) {
			final ToolProcess process = (ToolProcess) ((IAdaptable) element).getAdapter(ToolProcess.class);
			if (process != null) {
				synchronized (fProcesses) {
					ProcessListener listener = fProcesses.get(process);
					if (listener == null) {
						listener = new ProcessListener(process);
					}
					listener.add(element);
				}
			}
		}
		
		return info;
	}
	
	@Override
	protected void disposeElementInfo(final Object element, final ElementInfo info) {
		super.disposeElementInfo(element, info);
		
		if (element instanceof IAdaptable) {
			final ToolProcess process = (ToolProcess) ((IAdaptable) element).getAdapter(ToolProcess.class);
			if (process != null) {
				synchronized (fProcesses) {
					final ProcessListener listener = fProcesses.get(process);
					if (listener != null) {
						listener.remove(element);
					}
				}
			}
		}
	}
	
}
