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

package de.walware.statet.nico.core.runtime;

import java.util.Arrays;
import java.util.LinkedList;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;


/**
 * Queue with IToolRunnable waiting to be processed by the tool/controller.
 * 
 * Usage: You get your queue via accessor of the ToolProcess. 
 */
public class Queue {

	
	private ToolProcess fProcess;
	
	private LinkedList<IToolRunnable> fList;
	private IToolRunnable[] fSingleIOCache;

	
	Queue(ToolProcess process) {
		
		fProcess = process;
		fList = new LinkedList<IToolRunnable>();
	}
	
	
	public synchronized void sendElements() {
		
		internalCheckCache();
		IToolRunnable[] queueElements = fList.toArray(new IToolRunnable[fList.size()]);
		fireEvents(new DebugEvent[] {
				createDebugEvent(ToolProcess.QUEUE_COMPLETE_INFO, queueElements) });
	}
	
	public synchronized void removeElements(Object[] elements) {
		
		internalCheckCache();
		for (Object runnable : elements) {
			fList.remove(runnable);
		}
		IToolRunnable[] queueElements = fList.toArray(new IToolRunnable[fList.size()]);
		fireEvents(new DebugEvent[] {
				createDebugEvent(ToolProcess.QUEUE_COMPLETE_CHANGE, queueElements) });
	}
	
	
	void internalAdd(IToolRunnable[] runnables, boolean allowCache) {

		if (allowCache && internalIsEmpty() && runnables.length == 1) {
			fSingleIOCache = runnables;
			return;
		}
		
		internalCheckCache();
		fList.addAll(Arrays.asList(runnables));
		fireEvents(new DebugEvent[] { 
				createDebugEvent(ToolProcess.QUEUE_ENTRIES_ADDED, runnables) });
	}
	
	boolean internalIsEmpty() {
		
		return (fSingleIOCache == null && fList.isEmpty());
	}
	
	IToolRunnable internalPoll() {
		
		IToolRunnable runnable;
		DebugEvent[] events;
		
		if (fSingleIOCache != null) {
			runnable = fSingleIOCache[0];
			events = new DebugEvent[] {
					createDebugEvent(ToolProcess.QUEUE_ENTRIES_ADDED, fSingleIOCache),
					createDebugEvent(ToolProcess.QUEUE_ENTRY_STARTED_PROCESSING, runnable) };
			fSingleIOCache = null;
		}
		else {
			runnable = fList.poll();
			events = new DebugEvent[] {
					createDebugEvent(ToolProcess.QUEUE_ENTRY_STARTED_PROCESSING, runnable) };
		}
		
		fireEvents(events);
		return runnable;
	}

	void internalCheckCache() {
		
		if (fSingleIOCache != null) {
			DebugEvent[] events = new DebugEvent[] {
					createDebugEvent(ToolProcess.QUEUE_ENTRIES_ADDED, fSingleIOCache) };
			fList.add(fSingleIOCache[0]);
			fSingleIOCache = null;
			fireEvents(events);
		}
	}
	
	
	private DebugEvent createDebugEvent(int code, Object data) {
		
		DebugEvent event = new DebugEvent(fProcess, DebugEvent.MODEL_SPECIFIC, code);
		event.setData(data);
		return event;
	}

	private void fireEvents(DebugEvent[] events) {
		
		DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(events);
		}
	}
}
