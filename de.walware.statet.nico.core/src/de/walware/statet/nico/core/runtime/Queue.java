/*=============================================================================#
 # Copyright (c) 2006-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.nico.core.runtime;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.osgi.util.NLS;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.ecommons.ts.IQueue;
import de.walware.ecommons.ts.ISystemReadRunnable;
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
		public final ImList<IToolRunnable> data;
		
		private Delta(final int type, final int position, final ImList<IToolRunnable> data) {
			this.type= type;
			this.position= position;
			this.data= data;
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
	public static final int QUEUE_INFO= 1;
	
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
//	public static final int QUEUE_MAJOR_CHANGE= 2;
	
	
	static final int RUN_NONE= -2;
	static final int RUN_SUSPEND= -1;
	static final int RUN_RESERVED= 1;
	static final int RUN_HOT= 2;
	static final int RUN_OTHER= 3;
	static final int RUN_DEFAULT= 4;
	
	private static class RankedItem {
		
		final IToolRunnable runnable;
		final int rank;
		
		public RankedItem(final IToolRunnable runnable, final int rank) {
			this.runnable= runnable;
			this.rank= rank;
		}
		
		
		@Override
		public int hashCode() {
			return this.runnable.hashCode();
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			final RankedItem other= (RankedItem) obj;
			return (this.runnable.equals(other.runnable) );
		}
		
	}
	
	private final LinkedList<IToolRunnable> list= new LinkedList<>();
	private ImList<IToolRunnable> singleIOCache= null;
	private IToolRunnable insertRunnable= null;
	private final List<IToolRunnable> insertRunnableStack= new ArrayList<>();
	private int insertIndex= -1;
	private final Deque<ImList<IToolRunnable>> finishedExpected= new ArrayDeque<>();
	private final List<DebugEvent> eventList= new ArrayList<>(5);
	
	private final ToolProcess process;
	
	private boolean resetOnIdle= false;
	private final List<RankedItem> onIdleList= new ArrayList<>();
	private final LinkedList<IToolRunnable> nextIdleList= new LinkedList<>();
	
	private final LinkedList<IToolRunnable> hotList= new LinkedList<>();
	
	
	Queue(final ToolProcess process) {
		this.process= process;
	}
	
	
	private final IStatus acceptSubmit(final ToolStatus toolStatus) {
		if (toolStatus == ToolStatus.TERMINATED) {
			return new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, -1,
					NLS.bind(Messages.ToolController_ToolTerminated_message, this.process.getLabel(0)), null);
		}
		return Status.OK_STATUS;
	}
	
	public synchronized void sendElements() {
		checkIOCache();
		final IToolRunnable[] queueElements= this.list.toArray(new IToolRunnable[this.list.size()]);
		final DebugEvent event= new DebugEvent(this, DebugEvent.MODEL_SPECIFIC, QUEUE_INFO);
		event.setData(queueElements);
		this.eventList.add(event);
		fireEvents();
	}
	
	public synchronized int size() {
		if (this.singleIOCache != null) {
			return 1;
		}
		return this.list.size();
	}
	
	/**
	 * Submits the runnable for the tool.
	 * <p>
	 * The runnable will be added to the queue and will be run, if it's its turn.
	 * 
	 * @param runnable the runnable to add
	 * @return the status of the queue operation.
	 */
	@Override
	public IStatus add(final IToolRunnable runnable) {
		if (runnable == null) {
			throw new NullPointerException("runnable"); //$NON-NLS-1$
		}
		return doAdd(ImCollections.newList(runnable));
	}
	
	/**
	 * Submits the runnables for the tool.
	 * <p>
	 * The runnables will be added en block to the queue and will be runned, if it's its turn.
	 * 
	 * @param runnables the runnables to add.
	 * @return the status of the queue operation.
	 */
	public IStatus add(final List<IToolRunnable> runnables) {
		if (runnables == null) {
			throw new NullPointerException("runnables"); //$NON-NLS-1$
		}
		final ImList<IToolRunnable> finalRunnables= ImCollections.toList(runnables);
		for (int i= 0; i < finalRunnables.size(); i++) {
			if (runnables.get(i) == null) {
				throw new NullPointerException("runnable["+i+']'); //$NON-NLS-1$
			}
		}
		return doAdd(finalRunnables);
	}
	
	private synchronized IStatus doAdd(final ImList<IToolRunnable> runnables) {
		final ToolStatus toolStatus= this.process.getToolStatus();
		final IStatus status= acceptSubmit(toolStatus);
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
	
	@Override
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
		for (int i= 0; i < runnables.length; i++) {
			if (runnables[i] == null) {
				throw new NullPointerException("runnable["+i+']'); //$NON-NLS-1$
			}
		}
		doRemove(runnables);
	}
	
	private void doRemove(final IToolRunnable[] runnables) {
		synchronized (this) {
			checkIOCache();
			final List<IToolRunnable> removed= new ArrayList<>(runnables.length);
			boolean checkInsert= false;
			for (final IToolRunnable runnable : runnables) {
				final int index= this.list.indexOf(runnable);
				if (index >= 0 && runnable.changed(IToolRunnable.REMOVING_FROM, this.process)) {
					this.list.remove(index);
					removed.add(runnable);
					if (!checkInsert && index < this.insertIndex) {
						checkInsert= true;
					}
				}
			}
			if (checkInsert) {
				this.insertIndex= this.list.indexOf(this.insertRunnable);
			}
	//		IToolRunnable[] queueElements= fList.toArray(new IToolRunnable[fList.size()]);
	//		addDebugEvent(COMPLETE_CHANGE, queueElements);
			final ImList<IToolRunnable> finalRunnables= ImCollections.toList(removed);
			addChangeEvent(IToolRunnable.REMOVING_FROM, finalRunnables);
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
		final ImList<IToolRunnable> finalRunnables;
		synchronized (this) {
			checkIOCache();
			final List<IToolRunnable> removed= new ArrayList<>(runnables.length);
			boolean checkInsert= false;
			for (final IToolRunnable runnable : runnables) {
				final int index= this.list.indexOf(runnable);
				if (index >= 0 && runnable.changed(IToolRunnable.MOVING_FROM, this.process)) {
					this.list.remove(index);
					removed.add(runnable);
					if (!checkInsert && index < this.insertIndex) {
						checkInsert= true;
					}
				}
			}
			if (checkInsert) {
				this.insertIndex= this.list.indexOf(this.insertRunnable);
			}
			finalRunnables= ImCollections.toList(removed);
			addChangeEvent(IToolRunnable.MOVING_FROM, finalRunnables);
			fireEvents();
		}
		
		synchronized (to) {
			to.checkIOCache();
			if (to == this && this.insertIndex >= 0) {
				this.list.addAll(this.insertIndex, finalRunnables);
				for (final IToolRunnable runnable : finalRunnables) {
					runnable.changed(IToolRunnable.MOVING_TO, to.process);
				}
				addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT,
						new Delta(IToolRunnable.MOVING_TO, this.insertIndex, finalRunnables));
				this.insertIndex+= finalRunnables.size();
			}
			else {
				to.list.addAll(finalRunnables);
				for (final IToolRunnable runnable : finalRunnables) {
					runnable.changed(IToolRunnable.MOVING_TO, to.process);
				}
				to.addChangeEvent(IToolRunnable.MOVING_TO, finalRunnables);
			}
			to.fireEvents();
			to.notifyAll();
		}
	}
	
	public void moveAll(final Queue to) {
		if (to == null) {
			throw new NullPointerException("to"); //$NON-NLS-1$
		}
		final ImList<IToolRunnable> finalRunnables;
		synchronized (this) {
			checkIOCache();
			final List<IToolRunnable> removed= new ArrayList<>(this.list.size());
			for (final Iterator<IToolRunnable> iter= this.list.iterator(); iter.hasNext();) {
				final IToolRunnable runnable= iter.next();
				if (runnable.changed(IToolRunnable.MOVING_FROM, this.process)) {
					iter.remove();
					removed.add(runnable);
				}
			}
			if (this.insertIndex >= 0) {
				this.insertIndex= this.list.indexOf(this.insertRunnable);
			}
			finalRunnables= ImCollections.toList(removed);
			addChangeEvent(IToolRunnable.MOVING_FROM, finalRunnables);
			fireEvents();
		}
		
		synchronized (to) {
			to.checkIOCache();
			to.list.addAll(finalRunnables);
			for (final IToolRunnable runnable : finalRunnables) {
				runnable.changed(IToolRunnable.MOVING_TO, to.process);
			}
			to.addChangeEvent(IToolRunnable.MOVING_TO, finalRunnables);
			to.fireEvents();
			to.notifyAll();
		}
	}
	
	public IStatus addOnIdle(final ISystemReadRunnable runnable, final int rank) {
		if (runnable == null) {
			throw new NullPointerException("runnable"); //$NON-NLS-1$
		}
		synchronized (this) {
			final ToolStatus toolStatus= this.process.getToolStatus();
			final IStatus status= acceptSubmit(toolStatus);
			if (status.getSeverity() < IStatus.ERROR) {
				final RankedItem item= new RankedItem(runnable, rank);
				
				int idx= this.onIdleList.indexOf(item);
				if (idx >= 0 && this.onIdleList.get(idx).rank != rank) {
					this.onIdleList.remove(idx);
					if (!this.resetOnIdle) {
						this.nextIdleList.remove(item.runnable);
					}
					idx= -1;
				}
				if (idx < 0) {
					idx= 0;
					for (; idx < this.onIdleList.size(); idx++) {
						if (this.onIdleList.get(idx).rank > rank) {
							break;
						}
					}
					this.onIdleList.add(idx, item);
					
					if (!this.resetOnIdle) {
						if (idx == this.onIdleList.size()-1) { // last
							this.nextIdleList.add(item.runnable);
						}
						else {
							final RankedItem next= this.onIdleList.get(idx+1);
							final int nextIdx= this.nextIdleList.indexOf(next.runnable);
							if (nextIdx >= 0) {
								this.nextIdleList.add(nextIdx, item.runnable);
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
			this.onIdleList.remove(new RankedItem(runnable, 0));
			this.nextIdleList.remove(runnable);
		}
	}
	
	@Override
	public boolean isHotSupported() {
		return true;
	}
	
	@Override
	public IStatus addHot(final IToolRunnable runnable) {
		return addHot(runnable, 0);
	}
	
	public IStatus addHot(final IToolRunnable runnable, final int strategy) {
		if (runnable == null) {
			throw new NullPointerException("runnable"); //$NON-NLS-1$
		}
		synchronized (this) {
			final ToolStatus toolStatus= this.process.getToolStatus();
			final IStatus status= acceptSubmit(toolStatus);
			if (status.getSeverity() < IStatus.ERROR) {
				if ((strategy & 1) != 0) {
					if (this.hotList.contains(runnable)) {
						return Status.OK_STATUS;
					}
				}
				this.hotList.add(runnable);
				if (this.hotList.size() > 0) {
					notifyAll();
					final ToolController controller= this.process.getController();
					if (controller != null) {
						controller.scheduleHotMode();
					}
				}
			}
			return status;
		}
	}
	
	@Override
	public void removeHot(final IToolRunnable runnable) {
		if (runnable == null) {
			throw new NullPointerException("runnable"); //$NON-NLS-1$
		}
		synchronized (this) {
			if (this.hotList.remove(runnable)) {
				runnable.changed(IToolRunnable.REMOVING_FROM, this.process);
			}
		}
	}
	
	
	void internalAdd(final ImList<IToolRunnable> runnables, final boolean allowCache) {
		if (allowCache && this.singleIOCache == null && this.list.isEmpty()
				&& runnables.size() == 1) {
			this.singleIOCache= runnables;
			return;
		}
		
		checkIOCache();
		if (this.insertIndex >= 0)  {
			if (runnables.size() == 1) {
				this.list.add(this.insertIndex, runnables.get(0));
			}
			else {
				this.list.addAll(this.insertIndex, runnables);
			}
			addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT,
					new Delta(IToolRunnable.ADDING_TO, this.insertIndex, runnables) );
			this.insertIndex += runnables.size();
		}
		else {
			if (runnables.size() == 1) {
				this.list.add(runnables.get(0));
			}
			else {
				this.list.addAll(runnables);
			}
			addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT,
					new Delta(IToolRunnable.ADDING_TO, -1, runnables) );
		}
		fireEvents();
	}
	
	void internalAddInsert(final IToolRunnable runnable) {
		checkIOCache();
		
		this.insertRunnableStack.add(runnable);
		this.insertRunnable= runnable;
		this.insertIndex= 0;
		this.list.add(0, runnable);
		addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT,
				new Delta(IToolRunnable.ADDING_TO, 0, ImCollections.newList(runnable)) );
		fireEvents();
	}
	
	void internalRemoveInsert(final IToolRunnable runnable) {
		checkIOCache();
		
		final List<IToolRunnable> removed= new ArrayList<>(this.insertIndex+1);
		final int insertStackIdx= this.insertRunnableStack.indexOf(runnable);
		if (insertStackIdx < 0) {
			return;
		}
		final Iterator<IToolRunnable> iter= this.list.iterator();
		if (insertStackIdx < this.insertRunnableStack.size()-1) {
			final IToolRunnable start= this.insertRunnableStack.get(insertStackIdx+1);
			while (iter.hasNext()) {
				if (iter.next() == start) {
					break;
				}
			}
		}
		while (iter.hasNext()) {
			final IToolRunnable toRemove= iter.next();
			if (toRemove == runnable) {
				iter.remove();
				removed.add(toRemove);
				break;
			}
			if (toRemove.changed(IToolRunnable.REMOVING_FROM, this.process)) {
				iter.remove();
				removed.add(toRemove);
			}
		}
		final ImList<IToolRunnable> finalRunnables= ImCollections.toList(removed);
		addChangeEvent(IToolRunnable.REMOVING_FROM, finalRunnables);
		this.insertRunnableStack.remove(runnable);
		if (this.insertRunnableStack.isEmpty()) {
			this.insertRunnable= null;
			this.insertIndex= -1;
		}
		else {
			this.insertRunnable= this.insertRunnableStack.get(this.insertRunnableStack.size()-1);
			this.insertIndex= this.list.indexOf(this.insertRunnable);
		}
		fireEvents();
	}
	
	void internalScheduleIdle(final IToolRunnable runnable) {
		if (runnable == null) {
			throw new NullPointerException();
		}
		this.nextIdleList.add(runnable);
	}
	
	void internalRemoveIdle(final IToolRunnable runnable) {
		if (runnable == null) {
			throw new NullPointerException();
		}
		this.nextIdleList.remove(runnable);
	}
	
	
	int internalNext() {
		if (!this.hotList.isEmpty()) {
			return RUN_HOT;
		}
		final IToolRunnable runnable;
		if (this.singleIOCache != null) {
			runnable= this.singleIOCache.get(0);
		}
		else if (!this.list.isEmpty() && this.insertIndex != 0) {
			runnable= this.list.get(0);
		}
		else if (!this.nextIdleList.isEmpty()) {
			runnable= this.nextIdleList.get(0);
		}
		else {
			return RUN_NONE;
		}
		return (runnable instanceof ISystemRunnable) ?
				RUN_OTHER : RUN_DEFAULT;
	}
	
	boolean internalNextHot() {
		return !this.hotList.isEmpty();
	}
	
	void internalCheck() {
		checkIOCache();
		fireEvents();
	}
	
	void internalResetIdle() {
		this.resetOnIdle= false;
		for (int i= this.onIdleList.size()-1; i >= 0; i--) {
			if (!this.nextIdleList.remove(this.onIdleList.get(i).runnable)) {
				break;
			}
		}
		for (int i= 0; i < this.onIdleList.size(); i++) {
			this.nextIdleList.add(this.onIdleList.get(i).runnable);
		}
	}
	
	
	IToolRunnable internalPoll() {
		final ImList<IToolRunnable> finalRunnable;
		if (this.singleIOCache != null) {
			finalRunnable= this.singleIOCache;
			if (this.insertIndex >= 0) {
				addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT,
						new Delta(IToolRunnable.ADDING_TO, this.insertIndex, this.singleIOCache));
			}
			else {
				addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT,
						new Delta(IToolRunnable.ADDING_TO, -1, this.singleIOCache));
			}
			this.singleIOCache= null;
			if (this.resetOnIdle) {
				internalResetIdle();
			}
		}
		else if (!this.list.isEmpty() && this.insertIndex != 0) {
			finalRunnable= ImCollections.newList(this.list.poll());
			if (this.insertIndex >= 0) {
				this.insertIndex--;
			}
			if (this.resetOnIdle) {
				internalResetIdle();
			}
		}
		else {
			finalRunnable= ImCollections.newList(this.nextIdleList.poll());
			this.resetOnIdle= true;
		}
		addChangeEvent(IToolRunnable.STARTING, finalRunnable);
		
		fireEvents();
		this.finishedExpected.push(finalRunnable);
		return finalRunnable.get(0);
	}
	
	IToolRunnable internalPollHot() {
		return this.hotList.poll();
	}
	
	/**
	 * Not necessary in synchronized block
	 */
	void internalFinished(final IToolRunnable runnable, final int detail) {
		assert (runnable == this.finishedExpected.peek().get(0));
		
		addChangeEvent(detail, this.finishedExpected.poll());
	}
	
	List<IToolRunnable> internalGetList() {
		internalCheck();
		return this.list;
	}
	
	List<IToolRunnable> internalGetCurrentList() {
		internalCheck();
		if (this.insertIndex >= 0) {
			return this.list.subList(0, this.insertIndex);
		}
		else {
			return this.list;
		}
	}
	
	void dispose() {
		checkIOCache();
		if (!this.list.isEmpty()) {
			final ImList<IToolRunnable> finalRunnables= ImCollections.toList(this.list);
			for (final IToolRunnable runnable : finalRunnables) {
				runnable.changed(IToolRunnable.BEING_ABANDONED, this.process);
			}
			addDebugEvent(DebugEvent.TERMINATE, DebugEvent.UNSPECIFIED,
					new Delta(IToolRunnable.BEING_ABANDONED, -1, finalRunnables) );
			this.list.clear();
		}
		if (!this.hotList.isEmpty()){
			final IToolRunnable[] array= this.hotList.toArray(new IToolRunnable[this.hotList.size()]);
			for (int i= 0; i < array.length; i++) {
				array[i].changed(IToolRunnable.BEING_ABANDONED, this.process);
			}
			this.hotList.clear();
		}
		if (!this.onIdleList.isEmpty()){
			final RankedItem[] array= this.onIdleList.toArray(new RankedItem[this.onIdleList.size()]);
			for (int i= 0; i < array.length; i++) {
				array[i].runnable.changed(IToolRunnable.BEING_ABANDONED, this.process);
			}
			this.onIdleList.clear();
		}
		fireEvents();
	}
	
	
	private void checkIOCache() {
		if (this.singleIOCache != null) {
			if (this.insertIndex >= 0) {
				addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT,
						new Delta(IToolRunnable.ADDING_TO, this.insertIndex, this.singleIOCache) );
				this.list.add(this.insertIndex, this.singleIOCache.get(0));
				this.insertIndex++;
			}
			else {
				addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT,
						new Delta(IToolRunnable.ADDING_TO, -1, this.singleIOCache) );
				this.list.add(this.singleIOCache.get(0));
			}
			this.singleIOCache= null;
		}
	}
	
	private void addChangeEvent(final int deltaType, final ImList<IToolRunnable> deltaData) {
		addDebugEvent(DebugEvent.CHANGE, DebugEvent.CONTENT, new Delta(deltaType, -1, deltaData));
	}
	
	private void addDebugEvent(final int code, final int detail, final Delta delta) {
		final DebugEvent event= new DebugEvent(this, code, detail);
		event.setData(delta);
		synchronized (this.eventList) {
			this.eventList.add(event);
		}
	}
	
	private void fireEvents() {
		if (this.eventList.isEmpty()) {
			return;
		}
		final DebugPlugin manager= DebugPlugin.getDefault();
		synchronized (this.eventList) {
			if (manager != null) {
				manager.fireDebugEventSet(this.eventList.toArray(new DebugEvent[this.eventList.size()]));
			}
			this.eventList.clear();
		}
	}
	
}
