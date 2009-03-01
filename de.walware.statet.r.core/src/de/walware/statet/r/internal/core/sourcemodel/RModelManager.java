/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.walware.ecommons.FastList;
import de.walware.ecommons.ltk.ECommonsLTK;
import de.walware.ecommons.ltk.IElementChangedListener;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.WorkingContext;

import de.walware.statet.r.core.model.IManagableRUnit;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RManagedWorkingCopy;
import de.walware.statet.r.core.model.RModel;


/**
 * 
 */
public class RModelManager implements IModelManager {
	
	
	private static class ContextItem {
		
		final WorkingContext context;
		final HashMap<String, SuItem<IRSourceUnit>> copies;
		final ReferenceQueue<IRSourceUnit> copiesToClean;
		final HashMap<String, SuItem<ISourceUnit>> worksheets;
		final ReferenceQueue<ISourceUnit> worksheetsToClean;
		final FastList<IElementChangedListener> modelListeners;
		
		public ContextItem(final WorkingContext context) {
			this.context = context;
			this.copies = new HashMap<String, SuItem<IRSourceUnit>>();
			this.copiesToClean = new ReferenceQueue<IRSourceUnit>();
			this.worksheets = new HashMap<String, SuItem<ISourceUnit>>();
			this.worksheetsToClean = new ReferenceQueue<ISourceUnit>();
			this.modelListeners = new FastList<IElementChangedListener>(IElementChangedListener.class, FastList.IDENTITY);
		}
		
		@Override
		public int hashCode() {
			return context.hashCode();
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (obj instanceof ContextItem) {
				return ( ((ContextItem) obj).context == this.context);
			}
			return false;
		}
		
	}
	
	private static final class SuItem<T> extends WeakReference<T> {
		
		private String fKey;
		private T fStrongReference;
		
		public SuItem(final String key, final T su, final ReferenceQueue<T> queue) {
			super(su);
			fKey = key;
			fStrongReference = su;
		}
		
		public String getKey() {
			return fKey;
		}
		
		public void loosen() {
			fStrongReference = null;
		}
		
		public void tighten() {
			if (fStrongReference == null) {
				fStrongReference = get();
			}
		}
		
		public T get(final boolean includeDeleted) {
			if (fStrongReference != null) {
				return fStrongReference;
			}
			if (includeDeleted) {
				return super.get();
			}
			return null;
		}
		
		public void dispose() {
			fStrongReference = null;
			clear();
		}
		
	}
	
	private class CleanupJob extends Job {
		
		private final Object fScheduleLock = new Object();
		
		public CleanupJob() {
			super("R Model Cleanup"); //$NON-NLS-1$
			setUser(false);
			setSystem(true);
			setPriority(DECORATE);
		}
		
		void initialSchedule() {
			synchronized (fScheduleLock) {
				schedule(180000);
			}
		}
		
		void dispose() {
			synchronized (fScheduleLock) {
				cancel();
			}
		}
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			final int count = cleanUp();
			
			synchronized (fScheduleLock) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				else {
					schedule(count > 0 ? 60000 : 180000);
					return Status.OK_STATUS;
				}
			}
		}
		
	}
	
	private class RefreshJob extends Job {
		
		
		private final List<IRSourceUnit> fList;
		
		
		public RefreshJob(final WorkingContext context) {
			super("R Model Refresh"); //$NON-NLS-1$
			setUser(false);
			setSystem(true);
			setPriority(DECORATE);
			
			fList = getWorkingCopies(context, false);
		}
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			for (final IRSourceUnit su : fList) {
				if (su instanceof RManagedWorkingCopy) {
					fReconciler.reconcile((RManagedWorkingCopy) su, IModelManager.MODEL_FILE, true, monitor);
				}
			}
			return Status.OK_STATUS;
		}
		
	}
	
	
	private FastList<ContextItem> fContexts = new FastList<ContextItem>(ContextItem.class, FastList.EQUALITY);
	private RReconciler fReconciler = new RReconciler(this);
	private RModelEventJob fEventJob = new RModelEventJob(this);
	private CleanupJob fCleanupJob = new CleanupJob();
	
	
	public RModelManager() {
		getContextItem(ECommonsLTK.PERSISTENCE_CONTEXT, true);
		getContextItem(ECommonsLTK.EDITOR_CONTEXT, true);
		
		fCleanupJob.initialSchedule();
	}
	
	
	public void dispose() {
		fCleanupJob.dispose();
		fEventJob.dispose();
	}
	
	public void addElementChangedListener(final IElementChangedListener listener, final WorkingContext context) {
		final ContextItem contextEntry = getContextItem(context, true);
		contextEntry.modelListeners.add(listener);
	}
	
	public void removeElementChangedListener(final IElementChangedListener listener, final WorkingContext context) {
		final ContextItem contextItem = getContextItem(context, false);
		if (contextItem == null) {
			return;
		}
		contextItem.modelListeners.remove(listener);
	}
	
	
	private ContextItem getContextItem(final WorkingContext context, final boolean create) {
		final ContextItem[] contexts = fContexts.toArray();
		for (final ContextItem contextItem : contexts) {
			if (contextItem.context == context) {
				return contextItem;
			}
		}
		if (!create) {
			return null;
		}
		fContexts.add(new ContextItem(context));
		return getContextItem(context, true);
	}
	
	private int cleanUp() {
		final ContextItem[] contexts = fContexts.toArray();
		int count = 0;
		for (final ContextItem contextItem : contexts) {
			SuItem<?> suItem;
			while ((suItem = (SuItem<?>) contextItem.copiesToClean.poll()) != null){
				synchronized (contextItem) {
					if (contextItem.copies.get(suItem.getKey()) == suItem) {
						contextItem.copies.remove(suItem.getKey());
					}
					suItem.dispose();
					count++;
				}
			}
			while ((suItem = (SuItem<?>) contextItem.worksheetsToClean.poll()) != null){
				synchronized (contextItem) {
					if (contextItem.worksheets.get(suItem.getKey()) == suItem) {
						contextItem.worksheets.remove(suItem.getKey());
					}
					suItem.dispose();
					count++;
				}
			}
		}
		return count;
	}
	
	
	public void registerWorkingCopy(final IRSourceUnit copy) {
		assert (copy.getModelTypeId().equals(RModel.TYPE_ID) &&
				copy.getElementType() == IRSourceUnit.R_WORKSPACE_SU);
		
		final ContextItem contextItem = getContextItem(copy.getWorkingContext(), true);
		synchronized (contextItem) {
			final String key = copy.getId();
			final SuItem<IRSourceUnit> suItem = contextItem.copies.get(key);
			if (suItem != null) {
				if (suItem.get() == copy) {
					suItem.tighten();
					return;
				}
				else {
					suItem.dispose();
				}
			}
			contextItem.copies.put(key, new SuItem<IRSourceUnit>(key, copy, contextItem.copiesToClean));
		}
	}
	
	public void removeWorkingCopy(final IRSourceUnit copy) {
		final ContextItem contextItem = getContextItem(copy.getWorkingContext(), true);
		synchronized (contextItem) {
			final SuItem<IRSourceUnit> suItem = contextItem.copies.get(copy.getId());
			if (suItem != null) {
				suItem.loosen();
			}
		}
	}
	
	public void registerDependentUnit(final ISourceUnit copy) {
		assert (copy.getModelTypeId().equals(RModel.TYPE_ID) ?
				copy.getElementType() == IRSourceUnit.R_OTHER_SU : true);
		
		final ContextItem contextItem = getContextItem(copy.getWorkingContext(), true);
		synchronized (contextItem) {
			final String key = copy.getId()+'+'+copy.getModelTypeId();
			final SuItem<ISourceUnit> suItem = contextItem.worksheets.get(key);
			if (suItem != null) {
				if (suItem.get() == copy) {
					suItem.tighten();
					return;
				}
				else {
					suItem.dispose();
				}
			}
			contextItem.worksheets.put(key, new SuItem<ISourceUnit>(key, copy, contextItem.worksheetsToClean));
		}
	}
	
	public void deregisterDependentUnit(final ISourceUnit copy) {
		final ContextItem contextItem = getContextItem(copy.getWorkingContext(), true);
		synchronized (contextItem) {
			final SuItem<ISourceUnit> suItem = contextItem.worksheets.get(copy.getId()+'+'+copy.getModelTypeId());
			if (suItem != null) {
				suItem.loosen();
			}
		}
	}
	
	public IRSourceUnit getWorkingCopy(final String id, final WorkingContext context) {
		final ContextItem contextItem = getContextItem(context, false);
		if (contextItem != null) {
			synchronized (contextItem) {
				final SuItem<IRSourceUnit> suItem = contextItem.copies.get(id);
				if (suItem != null) {
					return suItem.get(true);
				}
			}
		}
		return null;
	}
	
	public ISourceUnit getWorksheetCopy(final String type, final String id, final WorkingContext context) {
		final ContextItem contextItem = getContextItem(context, false);
		if (contextItem != null) {
			synchronized (contextItem) {
				final SuItem<ISourceUnit> suItem = contextItem.worksheets.get(id+'+'+type);
				if (suItem != null) {
					return suItem.get(true);
				}
			}
		}
		return null;
	}
	
	public List<IRSourceUnit> getWorkingCopies(final WorkingContext context) {
		return getWorkingCopies(context, false);
	}
	
	public List<IRSourceUnit> getWorkingCopies(final String id, final boolean includeDeleted) {
		final ArrayList<IRSourceUnit> copies = new ArrayList<IRSourceUnit>();
		final ContextItem[] contexts = fContexts.toArray();
		for (final ContextItem contextItem : contexts) {
			synchronized (contextItem) {
				final SuItem<IRSourceUnit> suItem = contextItem.copies.get(id);
				if (suItem != null) {
					final IRSourceUnit copy = suItem.get(includeDeleted);
					if (copy != null) {
						copies.add(copy);
					}
				}
			}
		}
		return copies;
	}
	
	public List<IRSourceUnit> getWorkingCopies(final WorkingContext context, final boolean includeDeleted) {
		final ContextItem contextItem = getContextItem(context, false);
		if (contextItem == null) {
			return new ArrayList<IRSourceUnit>(0);
		}
		synchronized (contextItem) {
			final Collection<SuItem<IRSourceUnit>> entries = contextItem.copies.values();
			final ArrayList<IRSourceUnit> copies = new ArrayList<IRSourceUnit>(entries.size());
			for (final SuItem<IRSourceUnit> suItem : entries) {
				final IRSourceUnit su = suItem.get(includeDeleted);
				if (su != null) {
					copies.add(su);
				}
			}
			return copies;
		}
	}
	
	/**
	 * Refresh reuses existing ast
	 */
	public void refresh(final WorkingContext context) {
		new RefreshJob(context).schedule();
	}
	
	public void reconcile(final ISourceUnit u, final int level, final boolean reconciler, final IProgressMonitor monitor) {
		if (u instanceof IManagableRUnit) {
			fReconciler.reconcile((IManagableRUnit) u, level, reconciler, monitor);
		}
	}
	
	public RModelEventJob getEventJob() {
		return fEventJob;
	}
	
	public IElementChangedListener[] getElementChangedListeners(final WorkingContext context) {
		final ContextItem contextItem = getContextItem(context, false);
		if (context == null) {
			return new IElementChangedListener[0];
		}
		return contextItem.modelListeners.toArray();
	}
	
}
