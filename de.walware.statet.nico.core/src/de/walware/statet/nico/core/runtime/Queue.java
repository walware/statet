/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
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
public final class Queue {
	
	public static class Delta {
		
		public final int type;
		public final IToolRunnable[] data;
		
		private Delta(final int pType, final IToolRunnable[] pData) {
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
	
	public static final int ENTRIES_MOVE_ADD = 0x0111;
	
	
	public static final int MASK_UNFINISHED = 0x0120;
	
	/**
	 * Constant for type of a Delta, signalising that
	 * one or multiple entries (IToolRunnable) was moved
	 * (normally by a user request) to another queue.
	 */
	public static final int ENTRIES_MOVE_DELETE = 0x0121;
	
	/**
	 * Constant for type of a Delta, signalising that
	 * one or multiple entries (IToolRunnable) was deleted
	 * (normally by a user request) from the queue.
	 */
	public static final int ENTRIES_DELETE = 0x0122;
	
	/**
	 * Constant for type of a Delta, signalising that
	 * entries (IToolRunnable) are now abandoned, because the
	 * queue is terminated.
	 * In IDE: When launch/process removed (not when terminated).
	 */
	public static final int ENTRIES_ABANDONED = 0x0123;
	
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
	
	
	private final LinkedList<IToolRunnable> fList = new LinkedList<IToolRunnable>();
	private IToolRunnable[] fSingleIOCache = null;
	private IToolRunnable[] fFinishedExpected = null;
	private IToolRunnable[] fFinishedCache = null;
	private int fFinishedCacheDetail = -1;
	private List<DebugEvent> fEventList = new ArrayList<DebugEvent>(5);
	
	private final ToolProcess fProcess;
	
	private final LinkedList<IToolRunnable> fIdleList = new LinkedList<IToolRunnable>();
	
	
	Queue(final ToolProcess process) {
		fProcess = process;
	}
	
	
	public synchronized void sendElements() {
		checkFinishedCache();
		checkIOCache();
		final IToolRunnable[] queueElements = fList.toArray(new IToolRunnable[fList.size()]);
		final DebugEvent event = new DebugEvent(this, DebugEvent.MODEL_SPECIFIC, QUEUE_INFO);
		event.setData(queueElements);
		fEventList.add(event);
		fireEvents();
	}
	
	public synchronized void removeElements(final Object[] elements) {
		checkFinishedCache();
		checkIOCache();
		final List<IToolRunnable> removed = new ArrayList<IToolRunnable>(elements.length);
		for (final Object runnable : elements) {
			if (fList.remove(runnable)) {
				removed.add((IToolRunnable) runnable);
			}
		}
//		IToolRunnable[] queueElements = fList.toArray(new IToolRunnable[fList.size()]);
//		addDebugEvent(COMPLETE_CHANGE, queueElements);
		final IToolRunnable[] array = removed.toArray(new IToolRunnable[removed.size()]);
		for (int i = 0; i < array.length; i++) {
			array[i].changed(ENTRIES_DELETE, fProcess);
		}
		addChangeEvent(ENTRIES_DELETE, array);
		fireEvents();
	}
	
	public void moveElements(final Object[] elements, final Queue to) {
		final IToolRunnable[] array;
		synchronized (this) {
			checkFinishedCache();
			checkIOCache();
			final List<IToolRunnable> removed = new ArrayList<IToolRunnable>(elements.length);
			for (final Object runnable : elements) {
				if (fList.remove(runnable)) {
					removed.add((IToolRunnable) runnable);
				}
			}
			array = removed.toArray(new IToolRunnable[removed.size()]);
			for (int i = 0; i < array.length; i++) {
				array[i].changed(ENTRIES_MOVE_DELETE, fProcess);
			}
			addChangeEvent(ENTRIES_MOVE_DELETE, array);
			fireEvents();
		}
		
		synchronized (to) {
			to.checkFinishedCache();
			to.checkIOCache();
			to.fList.addAll(Arrays.asList(array));
			for (int i = 0; i < array.length; i++) {
				array[i].changed(ENTRIES_MOVE_ADD, to.fProcess);
			}
			to.addChangeEvent(ENTRIES_MOVE_ADD, array);
			to.fireEvents();
			to.notifyAll();
		}
	}
	
	public void moveAllElements(final Queue to) {
		final IToolRunnable[] array;
		synchronized (this) {
			checkFinishedCache();
			checkIOCache();
			array = fList.toArray(new IToolRunnable[fList.size()]);
			fList.clear();
			for (int i = 0; i < array.length; i++) {
				array[i].changed(ENTRIES_MOVE_DELETE, fProcess);
			}
			addChangeEvent(ENTRIES_MOVE_DELETE, array);
			fireEvents();
		}
		
		synchronized (to) {
			to.checkFinishedCache();
			to.checkIOCache();
			to.fList.addAll(Arrays.asList(array));
			for (int i = 0; i < array.length; i++) {
				array[i].changed(ENTRIES_MOVE_ADD, to.fProcess);
			}
			to.addChangeEvent(ENTRIES_MOVE_ADD, array);
			to.fireEvents();
			to.notifyAll();
		}
	}
	
	
	void internalAdd(final IToolRunnable[] runnables, final boolean allowCache) {
		if (runnables == null) {
			throw new NullPointerException();
		}
		for (int i = 0; i < runnables.length; i++) {
			if (runnables[i] == null) {
				throw new NullPointerException();
			}
		}
		if (allowCache && internalIsEmpty() && runnables.length == 1) {
			fSingleIOCache = runnables;
			return;
		}
		
		checkFinishedCache();
		checkIOCache();
		if (runnables.length == 1) {
			fList.add(runnables[0]);
		}
		else {
			fList.addAll(Arrays.asList(runnables));
		}
		addChangeEvent(ENTRIES_ADD, runnables);
		fireEvents();
	}
	
	void internalScheduleIdle(final IToolRunnable runnable) {
		if (runnable == null) {
			throw new NullPointerException();
		}
		fIdleList.add(runnable);
	}
	
	boolean internalIsEmpty() {
		return (fSingleIOCache == null && fList.isEmpty() && fIdleList.isEmpty());
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
		else if (!fList.isEmpty()) {
			runnable = new IToolRunnable[] { fList.poll() };
		}
		else {
			runnable = new IToolRunnable[] { fIdleList.poll() };
		}
		addChangeEvent(ENTRY_START_PROCESSING, runnable);
		
		fireEvents();
		fFinishedExpected = runnable;
		return runnable[0];
	}
	
	/**
	 * Not necessary in synchronized block
	 */
	void internalFinished(final IToolRunnable runnable, final int detail) {
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
			final IToolRunnable[] array = fList.toArray(new IToolRunnable[fList.size()]);
			for (int i = 0; i < array.length; i++) {
				array[i].changed(ENTRIES_ABANDONED, fProcess);
			}
			addDebugEvent(DebugEvent.TERMINATE, DebugEvent.UNSPECIFIED,
					ENTRIES_ABANDONED, array);
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
	
	private void addChangeEvent(final int deltaType, final IToolRunnable[] deltaData) {
		addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT, deltaType, deltaData);
	}
	
	private void addDebugEvent(final int code, final int detail, final int deltaType, final IToolRunnable[] deltaData) {
		final DebugEvent event = new DebugEvent(this, code, detail);
		event.setData(new Delta(deltaType, deltaData));
		fEventList.add(event);
	}
	
	private void fireEvents() {
		if (fEventList.isEmpty()) {
			return;
		}
		final DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(fEventList.toArray(new DebugEvent[fEventList.size()]));
		}
		fEventList.clear();
	}
	
}
