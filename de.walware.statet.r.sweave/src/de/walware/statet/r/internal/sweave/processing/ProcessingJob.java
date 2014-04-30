/*=============================================================================#
 # Copyright (c) 2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.processing;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;


public class ProcessingJob extends Job {
	
	
	private final RweaveTexTool task;
	
	
	public ProcessingJob(final RweaveTexTool task) {
		super(task.getLabel());
		
		setUser(false);
		setPriority(Job.BUILD);
		setRule(new DocumentRule(task.getSweaveFile()));
		
		this.task= task;
	}
	
	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		final Thread thread= new Thread("SweaveWorker") {
			@Override
			public void run() {
				ProcessingJob.this.task.run(monitor);
			}
		};
		thread.setDaemon(true);
		thread.start();
		while (true) {
			try {
				thread.join();
				break;
			}
			catch (final InterruptedException e) {
			}
		}
		
		if (this.task.getStatus().getSeverity() == IStatus.CANCEL) {
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}
	
	@Override
	protected void canceling() {
		try {
			this.task.terminate();
		}
		catch (final DebugException e) {
		}
	}
	
}
