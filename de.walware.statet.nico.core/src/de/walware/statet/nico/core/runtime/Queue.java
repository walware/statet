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

package de.walware.statet.nico.core.runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.collections.ConstList;
import de.walware.ecommons.ts.IQueue;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.IToolRunnable;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.internal.core.Messages;


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
public final class Queue implements IQueue {
	
	/**
	 * Delta for events of the queue.
	 * 
	 * Type is a event type of {@link IToolRunnable} events
	 */
	public static class Delta {
		
		public final int type;
		public final int position;
		/**
		 * One or multiple runnable effected by this event.
		 * STARTING and FINISHING events have always only a single item.
		 */
		public final IToolRunnable[] data;
		
		private Delta(final int type, final int position, final IToolRunnable[] data) {
			this.type = type;
			this.position = position;
			this.data = data;
		}
	}
	
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
	
	
	static final int RUN_NONE = -2;
	static final int RUN_SUSPEND = -1;
	static final int RUN_RESERVED = 1;
	static final int RUN_HOT = 2;
	static final int RUN_OTHER = 3;
	static final int RUN_DEFAULT = 4;
	
	private static class RankedItem {
		
		final IToolRunnable runnable;
		final int rank;
		
		public RankedItem(final IToolRunnable runnable, final int rank) {
			this.runnable = runnable;
			this.rank = rank;
		}
		
		
		@Override
		public int hashCode() {
			return runnable.hashCode();
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			final RankedItem other = (RankedItem) obj;
			return (runnable.equals(other.runnable) );
		}
		
	}
	
	private final LinkedList<IToolRunnable> fList = new LinkedList<IToolRunnable>();
	private IToolRunnable[] fSingleIOCache = null;
	private IToolRunnable fInsertRunnable = null;
	private final List<IToolRunnable> fInsertRunnableStack = new ArrayList<IToolRunnable>();
	private int fInsertIndex = -1;
	private IToolRunnable[] fFinishedExpected = null;
	private IToolRunnable[] fFinishedCache = null;
	private int fFinishedCacheDetail = -1;
	private final List<DebugEvent> fEventList = new ArrayList<DebugEvent>(5);
	
	private final ToolProcess<?> fProcess;
	
	int fCounter = 1;
	int fCounterNext = fCounter+1;
	
	private boolean fResetOnIdle = false;
	private final List<RankedItem> fOnIdleList = new ArrayList<RankedItem>();
	private final LinkedList<IToolRunnable> fNextIdleList = new LinkedList<IToolRunnable>();
	
	private final LinkedList<IToolRunnable> fHotList = new LinkedList<IToolRunnable>();
	
	
	Queue(final ToolProcess<?> process) {
		fProcess = process;
	}
	
	
	private final IStatus acceptSubmit(final ToolStatus toolStatus) {
		if (toolStatus == ToolStatus.TERMINATED) {
			return new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, -1,
					NLS.bind(Messages.ToolController_ToolTerminated_message, fProcess.getLabel(0)), null);
		}
		return Status.OK_STATUS;
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
	
	public synchronized int size() {
		if (fSingleIOCache != null) {
			return 1;
		}
		return fList.size();
	}
	
	/**
	 * Submits the runnable for the tool.
	 * <p>
	 * The runnable will be added to the queue and will be run, if it's its turn.
	 * 
	 * @param runnable the runnable to add
	 * @return the status of the queue operation.
	 */
	public IStatus add(final IToolRunnable runnable) {
		if (runnable == null) {
			throw new NullPointerException("runnable"); //$NON-NLS-1$
		}
		return doAdd(new IToolRunnable[] { runnable });
	}
	
	/**
	 * Submits the runnables for the tool.
	 * <p>
	 * The runnables will be added en block to the queue and will be runned, if it's its turn.
	 * 
	 * @param runnables the runnables to add.
	 * @return the status of the queue operation.
	 */
	public IStatus add(final IToolRunnable[] runnables) {
		if (runnables == null) {
			throw new NullPointerException("runnables"); //$NON-NLS-1$
		}
		for (int i = 0; i < runnables.length; i++) {
			if (runnables[i] == null) {
				throw new NullPointerException("runnable["+i+']'); //$NON-NLS-1$
			}
		}
		return doAdd(runnables);
	}
	
	private synchronized IStatus doAdd(final IToolRunnable[] runnables) {
		final ToolStatus toolStatus = fProcess.getToolStatus();
		final IStatus status = acceptSubmit(toolStatus);
		if (status.getSeverity() < IStatus.ERROR) {
			if (toolStatus.isWaiting()) {
				internalAdd(runnables, true);
				notifyAll();
			}
			else {
				internalAdd(runnables, false);
			}
		}
		return status;
	}
	
	public void remove(final IToolRunnable runnable) {
		if (runnable == null) {
			throw new NullPointerException("runnable"); //$NON-NLS-1$
		}
		doRemove(new IToolRunnable[] { runnable });
	}
	
	public void remove(final IToolRunnable[] runnables) {
		if (runnables == null) {
			throw new NullPointerException("runnables"); //$NON-NLS-1$
		}
		for (int i = 0; i < runnables.length; i++) {
			if (runnables[i] == null) {
				throw new NullPointerException("runnable["+i+']'); //$NON-NLS-1$
			}
		}
		doRemove(runnables);
	}
	
	private void doRemove(final IToolRunnable[] runnables) {
		synchronized (this) {
			checkFinishedCache();
			checkIOCache();
			final List<IToolRunnable> removed = new ArrayList<IToolRunnable>(runnables.length);
			boolean checkInsert = false;
			for (final IToolRunnable runnable : runnables) {
				final int index = fList.indexOf(runnable);
				if (index >= 0 && runnable.changed(IToolRunnable.REMOVING_FROM, fProcess)) {
					fList.remove(index);
					removed.add(runnable);
					if (!checkInsert && index < fInsertIndex) {
						checkInsert = true;
					}
				}
			}
			if (checkInsert) {
				fInsertIndex = fList.indexOf(fInsertRunnable);
			}
	//		IToolRunnable[] queueElements = fList.toArray(new IToolRunnable[fList.size()]);
	//		addDebugEvent(COMPLETE_CHANGE, queueElements);
			final IToolRunnable[] array = removed.toArray(new IToolRunnable[removed.size()]);
			addChangeEvent(IToolRunnable.REMOVING_FROM, array);
			fireEvents();
		}
	}
	
	public void move(final IToolRunnable[] runnables, final Queue to) {
		if (runnables == null) {
			throw new NullPointerException("runnables"); //$NON-NLS-1$
		}
		if (to == null) {
			throw new NullPointerException("to"); //$NON-NLS-1$
		}
		final IToolRunnable[] array;
		synchronized (this) {
			checkFinishedCache();
			checkIOCache();
			final List<IToolRunnable> removed = new ArrayList<IToolRunnable>(runnables.length);
			boolean checkInsert = false;
			for (final IToolRunnable runnable : runnables) {
				final int index = fList.indexOf(runnable);
				if (index >= 0 && runnable.changed(IToolRunnable.MOVING_FROM, fProcess)) {
					fList.remove(index);
					removed.add(runnable);
					if (!checkInsert && index < fInsertIndex) {
						checkInsert = true;
					}
				}
			}
			if (checkInsert) {
				fInsertIndex = fList.indexOf(fInsertRunnable);
			}
			array = removed.toArray(new IToolRunnable[removed.size()]);
			addChangeEvent(IToolRunnable.MOVING_FROM, array);
			fireEvents();
		}
		
		synchronized (to) {
			to.checkFinishedCache();
			to.checkIOCache();
			if (to == this && fInsertIndex >= 0) {
				fList.addAll(fInsertIndex, new ConstList<IToolRunnable>(array));
				for (int i = 0; i < array.length; i++) {
					array[i].changed(IToolRunnable.MOVING_TO, to.fProcess);
				}
				addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT,
						new Delta(IToolRunnable.MOVING_TO, fInsertIndex, array));
				fInsertIndex += array.length;
			}
			else {
				to.fList.addAll(new ConstList<IToolRunnable>(array));
				for (int i = 0; i < array.length; i++) {
					array[i].changed(IToolRunnable.MOVING_TO, to.fProcess);
				}
				to.addChangeEvent(IToolRunnable.MOVING_TO, array);
			}
			to.fireEvents();
			to.notifyAll();
		}
	}
	
	public void moveAll(final Queue to) {
		if (to == null) {
			throw new NullPointerException("to"); //$NON-NLS-1$
		}
		final IToolRunnable[] array;
		synchronized (this) {
			checkFinishedCache();
			checkIOCache();
			final List<IToolRunnable> removed = new ArrayList<IToolRunnable>(fList.size());
			for (final Iterator<IToolRunnable> iter = fList.iterator(); iter.hasNext();) {
				final IToolRunnable runnable = iter.next();
				if (runnable.changed(IToolRunnable.MOVING_FROM, fProcess)) {
					iter.remove();
					removed.add(runnable);
				}
			}
			if (fInsertIndex >= 0) {
				fInsertIndex = fList.indexOf(fInsertRunnable);
			}
			array = removed.toArray(new IToolRunnable[removed.size()]);
			addChangeEvent(IToolRunnable.MOVING_FROM, array);
			fireEvents();
		}
		
		synchronized (to) {
			to.checkFinishedCache();
			to.checkIOCache();
			to.fList.addAll(new ConstList<IToolRunnable>(array));
			for (int i = 0; i < array.length; i++) {
				array[i].changed(IToolRunnable.MOVING_TO, to.fProcess);
			}
			to.addChangeEvent(IToolRunnable.MOVING_TO, array);
			to.fireEvents();
			to.notifyAll();
		}
	}
	
	public IStatus addOnIdle(final IToolRunnable runnable, final int rank) {
		if (runnable == null) {
			throw new NullPointerException("runnable"); //$NON-NLS-1$
		}
		synchronized (this) {
			final ToolStatus toolStatus = fProcess.getToolStatus();
			final IStatus status = acceptSubmit(toolStatus);
			if (status.getSeverity() < IStatus.ERROR) {
				final RankedItem item = new RankedItem(runnable, rank);
				
				int idx = fOnIdleList.indexOf(item);
				if (idx >= 0 && fOnIdleList.get(idx).rank != rank) {
					fOnIdleList.remove(idx);
					if (!fResetOnIdle) {
						fNextIdleList.remove(item.runnable);
					}
					idx = -1;
				}
				if (idx < 0) {
					idx = 0;
					for (; idx < fOnIdleList.size(); idx++) {
						if (fOnIdleList.get(idx).rank > rank) {
							break;
						}
					}
					fOnIdleList.add(idx, item);
					
					if (!fResetOnIdle) {
						if (idx == fOnIdleList.size()-1) { // last
							fNextIdleList.add(item.runnable);
						}
						else {
							final RankedItem next = fOnIdleList.get(idx+1);
							final int nextIdx = fNextIdleList.indexOf(next.runnable);
							if (nextIdx >= 0) {
								fNextIdleList.add(nextIdx, item.runnable);
							}
						}
					}
				}
				notifyAll();
			}
			return status;
		}
	}
	
	public void removeOnIdle(final IToolRunnable runnable) {
		if (runnable == null) {
			throw new NullPointerException("runnable"); //$NON-NLS-1$
		}
		synchronized (this) {
			fOnIdleList.remove(new RankedItem(runnable, 0));
			fNextIdleList.remove(runnable);
		}
	}
	
	public boolean isHotSupported() {
		return true;
	}
	
	public IStatus addHot(final IToolRunnable runnable) {
		return addHot(runnable, 0);
	}
	
	public IStatus addHot(final IToolRunnable runnable, final int strategy) {
		if (runnable == null) {
			throw new NullPointerException("runnable"); //$NON-NLS-1$
		}
		synchronized (this) {
			final ToolStatus toolStatus = fProcess.getToolStatus();
			final IStatus status = acceptSubmit(toolStatus);
			if (status.getSeverity() < IStatus.ERROR) {
				if ((strategy & 1) != 0) {
					if (fHotList.contains(runnable)) {
						return Status.OK_STATUS;
					}
				}
				fHotList.add(runnable);
				if (fHotList.size() > 0) {
					notifyAll();
					final ToolController<?> controller = fProcess.getController();
					if (controller != null) {
						controller.scheduleHotMode();
					}
				}
			}
			return status;
		}
	}
	
	public void removeHot(final IToolRunnable runnable) {
		if (runnable == null) {
			throw new NullPointerException("runnable"); //$NON-NLS-1$
		}
		synchronized (this) {
			if (fHotList.remove(runnable)) {
				runnable.changed(IToolRunnable.REMOVING_FROM, fProcess);
			}
		}
	}
	
	
	void internalAdd(final IToolRunnable[] runnables, final boolean allowCache) {
		if (allowCache && fSingleIOCache == null && fList.isEmpty()
				&& runnables.length == 1) {
			fSingleIOCache = runnables;
			return;
		}
		
		checkFinishedCache();
		checkIOCache();
		if (fInsertIndex >= 0)  {
			if (runnables.length == 1) {
				fList.add(fInsertIndex, runnables[0]);
			}
			else {
				fList.addAll(fInsertIndex, new ConstList<IToolRunnable>(runnables));
			}
			addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT,
					new Delta(IToolRunnable.ADDING_TO, fInsertIndex, runnables));
			fInsertIndex += runnables.length;
		}
		else {
			if (runnables.length == 1) {
				fList.add(runnables[0]);
			}
			else {
				fList.addAll(new ConstList<IToolRunnable>(runnables));
			}
			addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT,
					new Delta(IToolRunnable.ADDING_TO, -1, runnables));
		}
		fireEvents();
	}
	
	void internalAddInsert(final IToolRunnable runnable) {
		checkFinishedCache();
		checkIOCache();
		
		fInsertRunnableStack.add(runnable);
		fInsertRunnable = runnable;
		fInsertIndex = 0;
		fList.add(0, runnable);
		addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT,
				new Delta(IToolRunnable.ADDING_TO, 0, new IToolRunnable[] { runnable }));
		fireEvents();
	}
	
	void internalRemoveInsert(final IToolRunnable runnable) {
		checkFinishedCache();
		checkIOCache();
		
		final List<IToolRunnable> removed = new ArrayList<IToolRunnable>(fInsertIndex+1);
		final int insertStackIdx = fInsertRunnableStack.indexOf(runnable);
		if (insertStackIdx < 0) {
			return;
		}
		final Iterator<IToolRunnable> iter = fList.iterator();
		if (insertStackIdx < fInsertRunnableStack.size()-1) {
			final IToolRunnable start = fInsertRunnableStack.get(insertStackIdx+1);
			while (iter.hasNext()) {
				if (iter.next() == start) {
					break;
				}
			}
		}
		while (iter.hasNext()) {
			final IToolRunnable toRemove = iter.next();
			if (toRemove == runnable) {
				iter.remove();
				removed.add(toRemove);
				break;
			}
			if (toRemove.changed(IToolRunnable.REMOVING_FROM, fProcess)) {
				iter.remove();
				removed.add(toRemove);
			}
		}
		final IToolRunnable[] array = removed.toArray(new IToolRunnable[removed.size()]);
		addChangeEvent(IToolRunnable.REMOVING_FROM, array);
		fInsertRunnableStack.remove(runnable);
		if (fInsertRunnableStack.isEmpty()) {
			fInsertRunnable = null;
			fInsertIndex = -1;
		}
		else {
			fInsertRunnable = fInsertRunnableStack.get(fInsertRunnableStack.size()-1);
			fInsertIndex = fList.indexOf(fInsertRunnable);
		}
		fireEvents();
	}
	
	void internalScheduleIdle(final IToolRunnable runnable) {
		if (runnable == null) {
			throw new NullPointerException();
		}
		fNextIdleList.add(runnable);
	}
	
	void internalRemoveIdle(final IToolRunnable runnable) {
		if (runnable == null) {
			throw new NullPointerException();
		}
		fNextIdleList.remove(runnable);
	}
	
	int internalNext() {
		if (!fHotList.isEmpty()) {
			return RUN_HOT;
		}
		final IToolRunnable runnable;
		if (fSingleIOCache != null) {
			runnable = fSingleIOCache[0];
		}
		else if (!fList.isEmpty() && fInsertIndex != 0) {
			runnable = fList.get(0);
		}
		else if (!fNextIdleList.isEmpty()) {
			runnable = fNextIdleList.get(0);
		}
		else {
			return RUN_NONE;
		}
		return (runnable instanceof ISystemRunnable) ?
				RUN_OTHER : RUN_DEFAULT;
	}
	
	boolean internalNextHot() {
		return !fHotList.isEmpty();
	}
	
	IToolRunnable internalPollHot() {
		return fHotList.poll();
	}
	
	void internalCheck() {
		checkFinishedCache();
		checkIOCache();
		fireEvents();
	}
	
	void internalResetIdle() {
		fResetOnIdle = false;
		for (int i = fOnIdleList.size()-1; i >= 0; i--) {
			if (!fNextIdleList.remove(fOnIdleList.get(i).runnable)) {
				break;
			}
		}
		for (int i = 0; i < fOnIdleList.size(); i++) {
			fNextIdleList.add(fOnIdleList.get(i).runnable);
		}
	}
	
	IToolRunnable internalPoll() {
		checkFinishedCache();
		
		IToolRunnable[] runnable;
		if (fSingleIOCache != null) {
			runnable = fSingleIOCache;
			if (fInsertIndex >= 0) {
				addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT,
						new Delta(IToolRunnable.ADDING_TO, fInsertIndex, fSingleIOCache));
			}
			else {
				addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT,
						new Delta(IToolRunnable.ADDING_TO, -1, fSingleIOCache));
			}
			fSingleIOCache = null;
			if (fResetOnIdle) {
				internalResetIdle();
			}
			fCounter = fCounterNext++;
		}
		else if (!fList.isEmpty() && fInsertIndex != 0) {
			runnable = new IToolRunnable[] { fList.poll() };
			if (fInsertIndex >= 0) {
				fInsertIndex--;
			}
			if (fResetOnIdle) {
				internalResetIdle();
			}
			fCounter = fCounterNext++;
		}
		else {
			runnable = new IToolRunnable[] { fNextIdleList.poll() };
			fResetOnIdle = true;
		}
		addChangeEvent(IToolRunnable.STARTING, runnable);
		
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
		fFinishedCacheDetail = detail;
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
				array[i].changed(IToolRunnable.BEING_ABANDONED, fProcess);
			}
			addDebugEvent(DebugEvent.TERMINATE, DebugEvent.UNSPECIFIED,
					new Delta(IToolRunnable.BEING_ABANDONED, -1, array) );
			fList.clear();
		}
		if (!fHotList.isEmpty()){
			final IToolRunnable[] array = fHotList.toArray(new IToolRunnable[fHotList.size()]);
			for (int i = 0; i < array.length; i++) {
				array[i].changed(IToolRunnable.BEING_ABANDONED, fProcess);
			}
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
			if (fInsertIndex >= 0) {
				addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT,
						new Delta(IToolRunnable.ADDING_TO, fInsertIndex, fSingleIOCache));
				fList.add(fInsertIndex, fSingleIOCache[0]);
				fInsertIndex++;
			}
			else {
				addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT,
						new Delta(IToolRunnable.ADDING_TO, -1, fSingleIOCache));
				fList.add(fSingleIOCache[0]);
			}
			fSingleIOCache = null;
		}
	}
	
	private void addChangeEvent(final int deltaType, final IToolRunnable[] deltaData) {
		addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT, new Delta(deltaType, -1, deltaData));
	}
	
	private void addDebugEvent(final int code, final int detail, final Delta delta) {
		final DebugEvent event = new DebugEvent(this, code, detail);
		event.setData(delta);
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
