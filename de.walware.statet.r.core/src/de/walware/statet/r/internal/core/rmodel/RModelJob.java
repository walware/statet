/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.rmodel;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.IModelManager;

import de.walware.statet.r.core.rmodel.IManagableRUnit;
import de.walware.statet.r.core.rmodel.IRModelInfo;
import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 * Worker for r model manager
 */
public class RModelJob extends Job {
	
	
	private class Task {
		
		private final IManagableRUnit fUnit;
		private AstInfo fNewAst;
		private int fFinishedLevel = RModelManager.AST;
		private int fWaitingCount;
		
		
		public Task(final IManagableRUnit u) {
			fUnit = u;
		}
		
		@Override
		public final boolean equals(final Object obj) {
			return fUnit.equals(((Task) obj).fUnit);
		}
		
		@Override
		public final int hashCode() {
			return fUnit.hashCode();
		}
		
		private final void wait(int level) {
			if (level > IModelManager.MODEL_DEPENDENCIES) {
				level = IModelManager.MODEL_DEPENDENCIES;
			}
			synchronized (fUnit.getModelLockObject()) {
				fWaitingCount++;
				while (fFinishedLevel < level) {
					try {
						fUnit.getModelLockObject().wait();
					} catch (final InterruptedException e) {
						Thread.interrupted();
					}
				}
				fWaitingCount--;
			}
		}
		
		private final void finished(final int level) {
			fFinishedLevel = level;
			fUnit.getModelLockObject().notifyAll();
		}
		
		public void run() {
			IRModelInfo newModel = null;
			IRModelInfo oldModel = null;
			boolean isOK = false;
			
			try {
				newModel = fScopeAnalyzer.update(fUnit, fNewAst);
				oldModel = fUnit.getCurrentRModel();
				isOK = (newModel != null);
			}
			finally {
				synchronized (fUnit.getModelLockObject()) {
					if (isOK) {
						final AstInfo<RAstNode> oldAst = fUnit.getCurrentRAst();
						if (oldAst == null || oldAst.stamp == newModel.getStamp()) {
							// otherwise, the ast is probably newer
							fUnit.setRAst(newModel.getAst());
						}
						fUnit.setRModel(newModel);
					}
					
					finished(RModelManager.MODEL_DEPENDENCIES); // eigentlich MODEL_FILE
				}
			}
			
			final ModelDelta delta = new ModelDelta(fUnit, (oldModel != null) ? oldModel.getAst() : null, fNewAst, newModel);
			fManager.fireDelta(delta, fUnit.getWorkingContext());
		}
		
	}
	
	
	private LinkedList<IRSourceUnit> fTaskQueue = new LinkedList<IRSourceUnit>();
	private HashMap<IRSourceUnit, Task> fTaskDetail = new HashMap<IRSourceUnit, Task>();
	private boolean fWorking = false;
	
	private ScopeAnalyzer fScopeAnalyzer;
	private RModelManager fManager;
	
	
	public RModelJob(final RModelManager manager) {
		super("RModel Updater"); //$NON-NLS-1$
		setPriority(Job.LONG);
		setSystem(true);
		setUser(false);
		
		fManager = manager;
		fScopeAnalyzer = new ScopeAnalyzer();
	}
	
	
	void addReconcile(final IManagableRUnit u, final AstInfo newAst, final int waitLevel) {
		Task task;
		final boolean important = (waitLevel > RModelManager.AST);
		synchronized (this) {
			task = fTaskDetail.get(u);
			if (task == null) {
				task = new Task(u);
				task.fNewAst = newAst;
				fTaskDetail.put(u, task);
				fTaskQueue.add(u);
			}
			else {
				task.fNewAst = newAst;
			}
			
			if (!fWorking) {
				schedule(important ? 0 : 250);
			}
		}
		
		if (important) {
			task.wait(waitLevel);
		}
	}
	
	
	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		Task task;
		while (true) {
			synchronized (this) {
				final IRSourceUnit u = fTaskQueue.poll();
				if (u == null) {
					fWorking = false;
					return Status.OK_STATUS;
				}
				fWorking = true;
				task = fTaskDetail.remove(u);
			}
			
			try {
				task.run();
			}
			catch (final Throwable e) {
				RCorePlugin.logError(-1, "R Model Update", e); //$NON-NLS-1$
			}
		}
	}
	
}
