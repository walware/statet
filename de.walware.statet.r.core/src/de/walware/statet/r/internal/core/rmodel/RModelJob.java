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

import de.walware.statet.r.core.rmodel.IManagableRUnit;
import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 *
 */
public class RModelJob extends Job {
	
	
	private class TaskDetail {
		final IManagableRUnit u;
		AstInfo oldAst;
		AstInfo newAst;
		
		
		public TaskDetail(final IManagableRUnit u) {
			this.u = u;
		}
		
		@Override
		public final boolean equals(final Object obj) {
			return u.equals(((TaskDetail) obj).u);
		}
		
		@Override
		public final int hashCode() {
			return u.hashCode();
		}
	}
	
	
	private LinkedList<IRSourceUnit> fTaskQueue = new LinkedList<IRSourceUnit>();
	private HashMap<IRSourceUnit, TaskDetail> fTaskDetail = new HashMap<IRSourceUnit, TaskDetail>();
	private boolean fWorking = false;
	
	private RModelManager fManager;
	
	
	public RModelJob(final RModelManager manager) {
		super("RModel Updater"); //$NON-NLS-1$
		
		setSystem(true);
		setUser(false);
		setPriority(Job.LONG);
		
		fManager = manager;
	}
	
	
	synchronized void add(final IManagableRUnit u, final AstInfo oldAst, final AstInfo newAst) {
		TaskDetail task = fTaskDetail.get(u);
		if (task == null) {
			task = new TaskDetail(u);
			task.oldAst = oldAst;
			task.newAst = newAst;
			fTaskDetail.put(u, task);
			fTaskQueue.add(u);
		}
		else {
			task.newAst = newAst;
		}
		
		if (!fWorking) {
			schedule(250);
		}
	}
	
	
	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		TaskDetail task;
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
				runTask(task);
			}
			catch (final Throwable e) {
				RCorePlugin.logError(-1, "R Model Update", e); //$NON-NLS-1$
			}
		}
	}
	
	private void runTask(final TaskDetail task) {
		final ModelDelta delta = new ModelDelta(task.u, task.oldAst, task.newAst);
		fManager.fireDelta(delta, task.u.getWorkingContext());
	}
}
