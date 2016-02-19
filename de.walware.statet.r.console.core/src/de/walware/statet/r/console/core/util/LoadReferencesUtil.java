/*=============================================================================#
 # Copyright (c) 2015-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.console.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImList;

import de.walware.rj.data.RReference;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;


public class LoadReferencesUtil {
	
	
	public static final int MAX_EXPLICITE_WAIT= 250;
	public static final int MAX_AUTO_WAIT= 100;
	
	
	private class LoadRunnable extends LoadReferenceRunnable implements Runnable {
		
		public LoadRunnable(final RReference reference, final RProcess tool,
				final int stamp, final String cause) {
			super(reference, tool, stamp, cause);
			setFinishRunnable(this);
		}
		
		public LoadRunnable(final RElementName name, final RProcess tool,
				final int stamp, final String cause) {
			super(name, tool, stamp, cause);
			setFinishRunnable(this);
		}
		
		
		@Override
		public void run() {
			taskFinished(this);
		}
		
	}
	
	
	private final RProcess tool;
	
	private final Map<Object, LoadRunnable> resolveTasks= new HashMap<>();
	private final AtomicInteger resolveTasksScheduled= new AtomicInteger();
	
	private long waitMillis;
	
	private List<ICombinedRElement> resolvedElements;
	
	
	public LoadReferencesUtil(final RProcess tool, final long waitTimeout) {
		if (tool == null) {
			throw new NullPointerException("tool"); //$NON-NLS-1$
		}
		this.tool= tool;
		this.waitMillis= waitTimeout;
	}
	
	
	public long getWaitTimeout() {
		return this.waitMillis;
	}
	
	public void setWaitTimeout(final long millis) {
		this.waitMillis= millis;
	}
	
	public ICombinedRElement resolve(final RReference reference, final int loadOptions) {
		if (reference instanceof ICombinedRElement) {
			final RProcess elementProcess= LoadReferenceRunnable.findRProcess(
					(ICombinedRElement) reference );
			if (elementProcess != this.tool) {
				return null;
			}
		}
		else {
			return null;
		}
		
		final Long key= Long.valueOf(reference.getHandle());
		LoadRunnable task= this.resolveTasks.get(key);
		if (task != null) {
			final int currentOptions= task.getLoadOptions();
			if ((currentOptions & loadOptions) == loadOptions) {
				return null;
			}
			synchronized (task) {
				if (!task.isStarted()) {
					task.setLoadOptions(currentOptions | loadOptions);
					return null;
				}
			}
		}
		task= new LoadRunnable(reference, this.tool, 0, "Content Assist");
		this.resolveTasks.put(key, task);
		
		return schedule(task);
	}
	
	public ICombinedRElement resolve(final RElementName name, final int loadOptions) {
		final RElementName key= name;
		LoadRunnable task= this.resolveTasks.get(key);
		if (task != null) {
			final int currentOptions= task.getLoadOptions();
			if ((currentOptions & loadOptions) == loadOptions) {
				return task.getResolvedElement();
			}
			synchronized (task) {
				if (!task.isStarted()) {
					task.setLoadOptions(currentOptions | loadOptions);
					return null;
				}
			}
		}
		task= new LoadRunnable(name, this.tool, 0, "Content Assist");
		this.resolveTasks.put(key, task);
		
		return schedule(task);
	}
	
	private ICombinedRElement schedule(final LoadRunnable task) {
		synchronized (task) {
			final long startTime= System.nanoTime();
			if (task.getTool().getQueue().addHot(task).isOK()) {
				try {
					final long wait= this.waitMillis;
					if (wait > 0) {
						task.wait(wait);
					}
					if (task.isFinished()) {
						return task.getResolvedElement();
					}
					else {
						this.resolveTasksScheduled.incrementAndGet();
						return null;
					}
				}
				catch (final InterruptedException e) {
					task.cancel();
				}
				finally {
					this.waitMillis-= TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime); 
				}
			}
			return null;
		}
	}
	
	protected void taskFinished(final LoadRunnable runnable) {
		final ICombinedRElement element= runnable.getResolvedElement();
		if (element != null) {
			if (this.resolvedElements == null) {
				this.resolvedElements= new ArrayList<>();
			}
			this.resolvedElements.add(element);
		}
		
		if (this.resolveTasksScheduled.decrementAndGet() == 0) {
			allFinished((this.resolvedElements != null) ?
					ImCollections.toList(this.resolvedElements) :
					ImCollections.<ICombinedRElement>emptyList() );
		}
	}
	
	protected void allFinished(final ImList<ICombinedRElement> resolvedElements) {
	}
	
}
