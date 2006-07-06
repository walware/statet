/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and 
 *    StatET-Project (http://www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation in JDT
 *    Stephan Wahlbrink - adaptations to StatET
 *******************************************************************************/

package de.walware.statet.base.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import de.walware.statet.base.core.internal.Messages;


public class CoreUtility {

	
	private static final class BuildJob extends Job {
		
		private final IProject fProject;
		
		private BuildJob(String name, IProject project) {
			super(name);
			
			fProject = project;
		}
		
		public boolean isCoveredBy(BuildJob other) {
			if (other.fProject == null)
				return true;
			
			return (fProject != null && fProject.equals(fProject));
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			// check for other BuildJobs
			try {
				synchronized (getClass()) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
			        Job[] buildJobs = Platform.getJobManager().find(ResourcesPlugin.FAMILY_MANUAL_BUILD);
			        for (Job job : buildJobs) {
			        	if (job != this && job instanceof BuildJob) {
			        		BuildJob buildJob = (BuildJob) job;
			        		if (buildJob.isCoveredBy(this)) {
			        			buildJob.cancel();  // cancel all other build jobs of our kind
			        		}
			        	}
					}
				}
			
				if (fProject != null) {
					monitor.beginTask(NLS.bind(Messages.CoreUtility_Build_ProjectTask_name, fProject.getName()), 2); 
					fProject.build(IncrementalProjectBuilder.FULL_BUILD, new SubProgressMonitor(monitor,1));
					ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new SubProgressMonitor(monitor,1));
				} else {
					monitor.beginTask(Messages.CoreUtility_Build_AllTask_name, 2); 
					ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, new SubProgressMonitor(monitor, 2));
				}
			} 
			catch (CoreException e) {
				return e.getStatus();
			} 
			catch (OperationCanceledException e) {
				return Status.CANCEL_STATUS;
			}
			finally {
				monitor.done();
			}
			return Status.OK_STATUS;
		}
		
		public boolean belongsTo(Object family) {
			
			return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
		}
	}
	
	/**
	 * Returns a build job
	 * @param project The project to build or <code>null</code> to build the workspace.
	 */
	public static Job getBuildJob(final IProject project) {
		
		Job buildJob = new BuildJob(Messages.CoreUtility_Build_Job_title, project);
		buildJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		buildJob.setUser(true);
		return buildJob;
	}
	
	/**
	 * Starts a build in the background.
	 * @param project The project to build or <code>null</code> to build the workspace.
	 */
	public static void startBuildInBackground(final IProject project) {
		getBuildJob(project).schedule();
	}

}
