/*******************************************************************************
 * Copyright (c) 2006-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;


/**
 * Queue with IToolRunnable waiting to be processed by the tool/controller.
 * 
 * Usage: You get your queue via accessor of the ToolProcess.
 * 
 * DebugEvents for a lifecycle of an entry:<pre>
 *                       CHANGE (CONTENT)
 *                       [ENTRIES_ADDED]
 *                             |
 *        +--------------------+---------------------+
 *        |                    |                     |
 *        |                    |              CHANGE (CONTENT)
 *        |                    |          [ENTRY_START_PROCESSING]
 *  CHANGE (CONTENT)           |                     |
 *  [ENTRIES_DELETE]           |              CHANGE (CONTENT)
 *                             |          [ENTRY_FINISH_PROCESSING]
 *                         TERMINATE
 *                    [ENTRIES_ABANDONED]
 * </pre>
 * The events of this type are sended by the queue (source element).
 * 
 */
public class Queue {

	public static class Delta {
		
		public final int type;
		public final IToolRunnable[] data;
		
		private Delta(int pType, IToolRunnable[] pData) {
			
			type = pType;
			data = pData;
		}
	}
	
	public static final int OK = 1;
	public static final int CANCEL = 2;
	public static final int ERROR = 3;
	
	/**
	 * Constant for detail of a DebugEvent, sending the complete queue.
	 * This does not signalising, that the queue has changed.
	 * <p>
	 * The queue entries (<code>IToolRunnable[]</code>) are attached as
	 * data to this event. The source of the event is the ToolProcess.
	 * <p>
	 * Usage: Events of this type are sended by the ToolProcess/its queue.
	 * The constant is applicable for DebugEvents of kind
	 * <code>MODEL_SPECIFIC</code>.</p>
	 */
	public static final int QUEUE_INFO = 1;
	
//	/**
//	 * Constant for detail of a DebugEvent, signalising that
//	 * queue has changed e.g. reordered, cleared,... .
//	 * <p>
//	 * The queue entries (<code>IToolRunnable[]</code>) are attached as
//	 * data to this event. The source of the event is the ToolProcess.
//	 * <p>
//	 * Usage: Events of this type are sended by the ToolProcess/its queue.
//	 * The constant is applicable for DebugEvents of kind
//	 * <code>MODEL_SPECIFIC</code>.</p>
//	 */
//	public static final int QUEUE_MAJOR_CHANGE = 2;

	/**
	 * Constant for type of a Delta, signalising that
	 * one or multiple entries (IToolRunnable) was added to the queue.
	 */
	public static final int ENTRIES_ADD = 0x0110;
	
	public static final int MASK_UNFINISHED = 0x0120;
	
	/**
	 * Constant for type of a Delta, signalising that
	 * one or multiple entries (IToolRunnable) was deleted
	 * (normally by a user request) from the queue.
	 */
	public static final int ENTRIES_DELETE = 0x0121;
	
	/**
	 * Constant for type of a Delta, signalising that
	 * entries (IToolRunnable) are now abandoned, because the
	 * queue is terminated.
	 */
	public static final int ENTRIES_ABANDONED = 0x0122;
	
	/**
	 * Constant for type of a Delta, signalising that
	 * a entry (IToolRunnable) was removed from the queue and that
	 * the process/controller started processing the entry.
	 * <p>
	 * The entry is not longer listed in queue.
	 */
	public static final int ENTRY_START_PROCESSING = 0x0210;
	
	public static final int MASK_FINISHED = 0x0220;

	/**
	 * Constant for type of a Delta, signalising that
	 * a entry (IToolRunnable) has finished normally.
	 */
	public static final int ENTRY_FINISH_PROCESSING_OK = MASK_FINISHED | OK;
	
	/**
	 * Constant for type of a Delta, signalising that
	 * a entry (IToolRunnable) has finished with handeled cancelation.
	 */
	public static final int ENTRY_FINISH_PROCESSING_CANCEL = MASK_FINISHED | CANCEL;

	/**
	 * Constant for type of a Delta, signalising that
	 * a entry (IToolRunnable) has finished with an error (exception).
	 */
	public static final int ENTRY_FINISH_PROCESSING_ERROR = MASK_FINISHED | ERROR;

	
	private LinkedList<IToolRunnable> fList = new LinkedList<IToolRunnable>();
	private IToolRunnable[] fSingleIOCache = null;
	private IToolRunnable[] fFinishedExpected = null;
	private IToolRunnable[] fFinishedCache = null;
	private int fFinishedCacheDetail = -1;
	private List<DebugEvent> fEventList = new ArrayList<DebugEvent>(5);

	
	Queue() {
	}
	
	
	public synchronized void sendElements() {
		
		checkFinishedCache();
		checkIOCache();
		IToolRunnable[] queueElements = fList.toArray(new IToolRunnable[fList.size()]);
		DebugEvent event = new DebugEvent(this, DebugEvent.MODEL_SPECIFIC, QUEUE_INFO);
		event.setData(queueElements);
		fEventList.add(event);
		fireEvents();
	}
	
	public synchronized void removeElements(Object[] elements) {
		
		checkFinishedCache();
		checkIOCache();
		LinkedList<IToolRunnable> removed = new LinkedList<IToolRunnable>();
		for (Object runnable : elements) {
			if (fList.remove(runnable)) {
				removed.add((IToolRunnable) runnable);
			}
		}
//		IToolRunnable[] queueElements = fList.toArray(new IToolRunnable[fList.size()]);
//		addDebugEvent(COMPLETE_CHANGE, queueElements);
		addChangeEvent(ENTRIES_DELETE, removed.toArray(new IToolRunnable[removed.size()]));
		fireEvents();
	}
	
	
	void internalAdd(IToolRunnable[] runnables, boolean allowCache) {

		if (allowCache && internalIsEmpty() && runnables.length == 1) {
			fSingleIOCache = runnables;
			return;
		}
		
		checkFinishedCache();
		checkIOCache();
		fList.addAll(Arrays.asList(runnables));
		addChangeEvent(ENTRIES_ADD, runnables);
		fireEvents();
	}
	
	boolean internalIsEmpty() {
		
		return (fSingleIOCache == null && fList.isEmpty());
	}
	
	void internalCheck() {
		
		checkFinishedCache();
		checkIOCache();
		fireEvents();
	}
	
	IToolRunnable internalPoll() {
		
		checkFinishedCache();

		IToolRunnable[] runnable;
		if (fSingleIOCache != null) {
			runnable = fSingleIOCache;
			addChangeEvent(ENTRIES_ADD, fSingleIOCache);
			fSingleIOCache = null;
		}
		else {
			runnable = new IToolRunnable[] { fList.poll() };
		}
		addChangeEvent(ENTRY_START_PROCESSING, runnable);
		
		fireEvents();
		fFinishedExpected = runnable;
		return runnable[0];
	}
	
	/**
	 * Not necessary in synchronized block
	 */
	void internalFinished(IToolRunnable runnable, int detail) {
		
		assert (runnable == fFinishedExpected[0]);
		assert (fFinishedCache == null);
		
		// this order is important
		fFinishedCacheDetail = MASK_FINISHED | detail;
		fFinishedCache = fFinishedExpected;
	}
	
	List<IToolRunnable> internalGetList() {
		internalCheck();
		return fList;
	}
	
	void dispose() {
		
		checkFinishedCache();
		checkIOCache();
		if (!fList.isEmpty()) {
			addDebugEvent(DebugEvent.TERMINATE, DebugEvent.UNSPECIFIED,
					ENTRIES_ABANDONED, fList.toArray(new IToolRunnable[fList.size()]));
			fList.clear();
		}
		fireEvents();
	}

	
	private void checkFinishedCache() {
		
		if (fFinishedCache != null) {
			addChangeEvent(fFinishedCacheDetail, fFinishedCache);
			fFinishedCache = null;
		}
	}
	
	private void checkIOCache() {
		
		if (fSingleIOCache != null) {
			addChangeEvent(ENTRIES_ADD, fSingleIOCache);
			fList.add(fSingleIOCache[0]);
			fSingleIOCache = null;
		}
	}
	
	private void addChangeEvent(int deltaType, IToolRunnable[] deltaData) {
		
		addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT, deltaType, deltaData);
	}
	
	private void addDebugEvent(int code, int detail, int deltaType, IToolRunnable[] deltaData) {
		
		DebugEvent event = new DebugEvent(this, code, detail);
		event.setData(new Delta(deltaType, deltaData));
		fEventList.add(event);
	}

	private void fireEvents() {
		
		if (fEventList.isEmpty()) {
			return;
		}
		DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(fEventList.toArray(new DebugEvent[fEventList.size()]));
		}
		fEventList.clear();
	}
}
