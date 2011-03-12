/*******************************************************************************
 * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.walware.ecommons.FastList;
import de.walware.ecommons.ltk.IElementChangedListener;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.WorkingContext;

import de.walware.statet.r.core.RProject;
import de.walware.statet.r.core.model.IManagableRUnit;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRModelManager;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;


public class RModelManager implements IRModelManager {
	
	
	private static class ContextItem {
		
		final WorkingContext context;
		final HashMap<String, ISourceUnit> worksheets;
		final FastList<IElementChangedListener> modelListeners;
		
		public ContextItem(final WorkingContext context) {
			this.context = context;
			this.worksheets = new HashMap<String, ISourceUnit>();
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
	
	private class RefreshJob extends Job {
		
		
		private final List<ISourceUnit> fList;
		
		
		public RefreshJob(final WorkingContext context) {
			super("R Model Refresh"); //$NON-NLS-1$
			setUser(false);
			setSystem(true);
			setPriority(DECORATE);
			
			fList = LTK.getSourceUnitManager().getOpenSourceUnits(RModel.TYPE_ID, context);
		}
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			for (final ISourceUnit su : fList) {
				if (su instanceof IManagableRUnit) {
					fReconciler.reconcile((IManagableRUnit) su, IModelManager.MODEL_FILE, true, monitor);
				}
			}
			return Status.OK_STATUS;
		}
		
	}
	
	
	private final FastList<ContextItem> fContexts = new FastList<ContextItem>(ContextItem.class, FastList.EQUALITY);
	private final RReconciler fReconciler = new RReconciler(this);
	private final RModelEventJob fEventJob = new RModelEventJob(this);
	
	private final RModelIndex fIndex = new RModelIndex(this);
	
	
	public RModelManager() {
		getContextItem(LTK.PERSISTENCE_CONTEXT, true);
		getContextItem(LTK.EDITOR_CONTEXT, true);
	}
	
	
	public void dispose() {
		fEventJob.dispose();
		fIndex.dispose();
	}
	
	public RModelIndex getIndex() {
		return fIndex;
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
	
	public void registerDependentUnit(final ISourceUnit copy) {
		assert (copy.getModelTypeId().equals(RModel.TYPE_ID) ?
				copy.getElementType() == IRSourceUnit.R_OTHER_SU : true);
		
		final ContextItem contextItem = getContextItem(copy.getWorkingContext(), true);
		synchronized (contextItem) {
			final String key = copy.getId()+'+'+copy.getModelTypeId();
			contextItem.worksheets.put(key, copy);
		}
	}
	
	public void deregisterDependentUnit(final ISourceUnit copy) {
		final ContextItem contextItem = getContextItem(copy.getWorkingContext(), true);
		synchronized (contextItem) {
			contextItem.worksheets.remove(copy.getId()+'+'+copy.getModelTypeId());
		}
	}
	
	public ISourceUnit getWorksheetCopy(final String type, final String id, final WorkingContext context) {
		final ContextItem contextItem = getContextItem(context, false);
		if (contextItem != null) {
			synchronized (contextItem) {
				return contextItem.worksheets.get(id+'+'+type);
			}
		}
		return null;
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
	
	public IRModelInfo reconcile2(final ISourceUnit u, final int level, final boolean reconciler, final IProgressMonitor monitor) {
		if (u instanceof IManagableRUnit) {
			return fReconciler.reconcile((IManagableRUnit) u, level, reconciler, monitor);
		}
		return null;
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
	
	
	public IRFrame getProjectFrame(final RProject project) {
		return fIndex.getProjectFrame(project.getProject());
	}
	
	public List<String> findReferencingSourceUnits(final IProject project, final RElementName name) {
		return fIndex.findReferencingSourceUnits(project, name);
	}
	
}
