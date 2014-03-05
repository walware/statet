/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.sourcemodel;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.WorkingContext;
import de.walware.ecommons.ltk.core.impl.AbstractModelManager;
import de.walware.ecommons.ltk.core.impl.SourceUnitModelContainer;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.model.IRFrame;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRModelManager;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RChunkElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.model.RSuModelContainer;


public class RModelManager extends AbstractModelManager implements IRModelManager {
	
	
	private static class RContextItem extends ContextItem {
		
		public final HashMap<String, ISourceUnit> worksheets;
		
		public RContextItem(final WorkingContext context) {
			super(context);
			this.worksheets = new HashMap<>();
		}
		
	}
	
	
	private final RReconciler fReconciler = new RReconciler(this);
	private final RModelEventJob fEventJob = new RModelEventJob(this);
	
	private final RModelIndex fIndex = new RModelIndex(this);
	
	
	public RModelManager() {
		super(RModel.TYPE_ID);
		getContextItem(LTK.PERSISTENCE_CONTEXT, true);
		getContextItem(LTK.EDITOR_CONTEXT, true);
	}
	
	
	public void dispose() {
		fEventJob.dispose();
		fIndex.dispose();
	}
	
	
	public RModelEventJob getEventJob() {
		return fEventJob;
	}
	
	public RModelIndex getIndex() {
		return fIndex;
	}
	
	
	@Override
	protected ContextItem doCreateContextItem(final WorkingContext context) {
		return new RContextItem(context);
	}
	
	@Override
	public void registerDependentUnit(final ISourceUnit copy) {
		assert (copy.getModelTypeId().equals(RModel.TYPE_ID) ?
				copy.getElementType() == IRSourceUnit.R_OTHER_SU : true);
		
		final RContextItem contextItem = (RContextItem) getContextItem(
				copy.getWorkingContext(), true );
		synchronized (contextItem) {
			final String key = copy.getId()+'+'+copy.getModelTypeId();
			contextItem.worksheets.put(key, copy);
		}
	}
	
	@Override
	public void deregisterDependentUnit(final ISourceUnit copy) {
		final RContextItem contextItem = (RContextItem) getContextItem(
				copy.getWorkingContext(), true );
		synchronized (contextItem) {
			contextItem.worksheets.remove(copy.getId()+'+'+copy.getModelTypeId());
		}
	}
	
	public ISourceUnit getWorksheetCopy(final String type, final String id, final WorkingContext context) {
		final RContextItem contextItem = (RContextItem) getContextItem(
				context, false );
		if (contextItem != null) {
			synchronized (contextItem) {
				return contextItem.worksheets.get(id+'+'+type);
			}
		}
		return null;
	}
	
	
	@Override
	public void reconcile(final SourceUnitModelContainer<?, ?> adapter,
			final int level, final IProgressMonitor monitor) {
		if (adapter instanceof RSuModelContainer) {
			fReconciler.reconcile((RSuModelContainer) adapter, level, monitor);
		}
	}
	
	@Override
	protected void reconcile(final ISourceUnit su, final int level, final IProgressMonitor monitor) {
		final RSuModelContainer adapter = (RSuModelContainer) su.getAdapter(RSuModelContainer.class);
		if (adapter != null) {
			fReconciler.reconcile(adapter, (IModelManager.MODEL_FILE | IModelManager.RECONCILER), monitor);
		}
	}
	
	@Override
	public IRModelInfo reconcile(final IRSourceUnit su, final ISourceUnitModelInfo modelInfo,
			final List<? extends RChunkElement> chunks,
			final int level, final IProgressMonitor monitor) {
		return fReconciler.reconcile(su, modelInfo, chunks, level, monitor);
	}
	
	
	@Override
	public IRFrame getProjectFrame(final IRProject rProject) throws CoreException {
		return fIndex.getProjectFrame(rProject);
	}
	
	@Override
	public List<ISourceUnit> findReferencingSourceUnits(final IRProject rProject, final RElementName name,
			final IProgressMonitor monitor) throws CoreException {
		return fIndex.findReferencingSourceUnits(rProject, name, monitor);
	}
	
}
