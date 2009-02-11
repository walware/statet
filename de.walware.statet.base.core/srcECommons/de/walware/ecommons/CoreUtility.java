/*******************************************************************************
 * Copyright (c) 2005-2009 IBM Corporation and 
 *   WalWare/StatET-Project (http://www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation in JDT
 *     Stephan Wahlbrink - adaptations to StatET
 *******************************************************************************/

package de.walware.ecommons;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.internal.workspace.Messages;


public class CoreUtility {
	
	
	private static final class BuildJob extends Job {
		
		private final IProject fProject;
		
		private BuildJob(final String name, final IProject project) {
			super(name);
			
			fProject = project;
		}
		
		public boolean isCoveredBy(final BuildJob other) {
			if (other.fProject == null)
				return true;
			
			return (fProject != null && fProject.equals(fProject));
		}
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			// check for other BuildJobs
			try {
				synchronized (getClass()) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					final Job[] buildJobs = Job.getJobManager().find(ResourcesPlugin.FAMILY_MANUAL_BUILD);
					for (final Job job : buildJobs) {
						if (job != this && job instanceof BuildJob) {
							final BuildJob buildJob = (BuildJob) job;
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
			catch (final CoreException e) {
				return e.getStatus();
			}
			catch (final OperationCanceledException e) {
				return Status.CANCEL_STATUS;
			}
			finally {
				monitor.done();
			}
			return Status.OK_STATUS;
		}
		
		@Override
		public boolean belongsTo(final Object family) {
			return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
		}
		
	}
	
	/**
	 * Returns a build job
	 * @param project The project to build or <code>null</code> to build the workspace.
	 */
	public static Job getBuildJob(final IProject project) {
		final Job buildJob = new BuildJob(Messages.CoreUtility_Build_Job_title, project);
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
