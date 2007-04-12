/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.ui.util.ExceptionHandler;


public abstract class NewElementWizard extends Wizard implements INewWizard {

	
	protected static class NewFileCreator {
		
		protected IPath fContainerPath;
		protected String fResourceName;
		
		/** No direct access, use getFileHandle() */
		private IFile fCachedFileHandle;
		
		public NewFileCreator(IPath containerPath, String resourceName) {
			
			fContainerPath = containerPath;
			fResourceName = resourceName;
		}
		
		/**
		 * Return the filehandle of the new file.
		 * The Filehandle is cached. File can exists or not exists.
		 * @return
		 */
		public IFile getFileHandle() {
			
			if (fCachedFileHandle == null) {
		    	IPath newFilePath = fContainerPath.append(fResourceName);
				fCachedFileHandle = createFileHandle(newFilePath);
			}
			return fCachedFileHandle;
		}
		
	    /**
	     * Creates a file resource handle for the file with the given workspace path.
	     * This method does not create the file resource; this is the responsibility
	     * of <code>createFile</code>.
	     *
	     * @param filePath the path of the file resource to create a handle for
	     * @return the new file resource handle
	     * @see #createFile
	     */
	    protected IFile createFileHandle(IPath filePath) {
	    	
	        return ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
	    }

	    /**
	     * Creates a new file resource in the selected container and with the selected
	     * name. Creates any missing resource containers along the path; does nothing if
	     * the container resources already exist.
	     * <p>
	     * In normal usage, this method is invoked after the user has pressed Finish on
	     * the wizard; the enablement of the Finish button implies that all controls on 
	     * on this page currently contain valid values.
	     * </p>
	     * <p>
	     * Note that this page caches the new file once it has been successfully
	     * created; subsequent invocations of this method will answer the same
	     * file resource without attempting to create it again.
	     * </p>
	     * <p>
	     * This method should be called within a workspace modify operation since
	     * it creates resources.
	     * </p>
	     *
	     * @return the created file resource, or <code>null</code> if the file
	     *    was not created
	     * @throws InterruptedException 
	     * @throws InvocationTargetException 
	     * @throws CoreException 
	     */
	    public void createFile(final IProgressMonitor monitor) 
	    		throws InvocationTargetException, InterruptedException, CoreException {
	    	
	    	IPath containerPath = fContainerPath;
	    	IFile newFileHandle = getFileHandle();
	        InputStream initialContents = getInitialFileContents(newFileHandle);

	        try {
	        	assert (containerPath != null);
	        	assert (newFileHandle != null);
	        	
		        monitor.beginTask("Create new file...", 1000);
		        ContainerGenerator generator = new ContainerGenerator(containerPath);
		        generator.generateContainer(new SubProgressMonitor(monitor, 500));
		        doCreateFile(newFileHandle, initialContents, new SubProgressMonitor(monitor, 500));
	        } 
	        finally {
	            monitor.done();
	        }
	    }
	    
	    /**
	     * Creates a file resource given the file handle and contents.
	     *
	     * @param fileHandle the file handle to create a file resource with
	     * @param contents the initial contents of the new file resource, or
	     *   <code>null</code> if none (equivalent to an empty stream)
	     * @param monitor the progress monitor to show visual progress with
	     * @exception CoreException if the operation fails
	     * @exception OperationCanceledException if the operation is canceled
	     */
	    protected static void doCreateFile(IFile fileHandle, InputStream contents, IProgressMonitor monitor) 
	    		throws CoreException {
	    	
	        if (contents == null)
	            contents = new ByteArrayInputStream(new byte[0]);

	        try {
	        	monitor.beginTask("Create file...", 1000);
	        	
	            // Create a new file resource in the workspace
	            IPath path = fileHandle.getFullPath();
	            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	            int numSegments = path.segmentCount();
	            if (numSegments > 2 && !root.getFolder(path.removeLastSegments(1)).exists()) {
	                // If the direct parent of the path doesn't exist, try to create the
	                // necessary directories.
	                for (int i = numSegments - 2; i > 0; i--) {
	                    IFolder folder = root.getFolder(path.removeLastSegments(i));
	                    if (!folder.exists()) {
	                        folder.create(false, true, new SubProgressMonitor(monitor, 500/(numSegments-2)));
	                    }
	                }
	            }
                if (monitor.isCanceled())
                	throw new OperationCanceledException();

                fileHandle.create(contents, false, new SubProgressMonitor(monitor, 500));
    	        if (monitor.isCanceled())
    	            throw new OperationCanceledException();
	        } 
	        catch (CoreException e) {
	            // If the file already existed locally, just refresh to get contents
	            if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
	                fileHandle.refreshLocal(IResource.DEPTH_ZERO, null);
	            else
	                throw e;
	        }

	    }

		/**
		 * Returns a stream containing the initial contents to be given to new file resource
		 * instances.  <b>Subclasses</b> may wish to override.  This default implementation
		 * provides no initial contents.
		 *
		 * @return initial contents to be given to new file resource instances
		 */
		protected InputStream getInitialFileContents(IFile newFileHandle) {

			return null;
		}
	}
	
	protected static class ProjectCreator {
		
		/** Name of project */
		protected String fProjectName;
		/** Path of project, null for default location */
		protected IPath fNewPath;
		
		private IProject fCachedProjectHandle;
		private IProject[] fRefProjects;
		
		
	    public ProjectCreator(String name, IPath path, IProject[] projects) {

			fProjectName = name;
			fNewPath = path;
			fRefProjects = projects;
		}

		/**
	     * Returns a project resource handle for the current project name field value.
	     * <p>
	     * Note: Handle is cached. This method does not create the project resource; 
	     * this is the responsibility of <code>IProject::create</code>.
	     * </p>
	     *
	     * @return the project resource handle
	     */
	    public IProject getProjectHandle() {
	    	
	    	if (fCachedProjectHandle == null)
	    		fCachedProjectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(fProjectName);
	    	
	    	return fCachedProjectHandle;
	    }
		
	    /**
	     * Creates a new project resource with the selected name.
	     * <p>
	     * In normal usage, this method is invoked after the user has pressed Finish
	     * on the wizard; the enablement of the Finish button implies that all
	     * controls on the pages currently contain valid values.
	     * </p>
	     * 
	     * @return the created project resource, or <code>null</code> if the
	     *         project was not created
	     * @throws CoreException 
	     */
	    public void createProject(IProgressMonitor monitor) 
	    		throws InvocationTargetException, InterruptedException, CoreException {
	    	
	    	IProject projectHandle = getProjectHandle();

	    	IWorkspace workspace = ResourcesPlugin.getWorkspace();
	        IProjectDescription description = workspace.newProjectDescription(projectHandle.getName());
	        description.setLocation(fNewPath);

	        // update the referenced project if provided
	        if (fRefProjects != null && fRefProjects.length > 0) {
	        	description.setReferencedProjects(fRefProjects);
	        }
	    	
	    	try {
	    		Assert.isNotNull(projectHandle);
	    		
	    		doCreateProject(projectHandle, description, monitor);
	    	}
	    	finally {
	    		monitor.done();
	    	}
	    }
	    
	    private static void doCreateProject(IProject projectHandle, IProjectDescription description, IProgressMonitor monitor) 
	    		throws CoreException {
	    	
	        // run the new project creation operation
	        try {
                monitor.beginTask("Create Project", 1000); //$NON-NLS-1$

                projectHandle.create(description, new SubProgressMonitor(monitor, 500));
                if (monitor.isCanceled()) {
                	throw new OperationCanceledException();
                }
                projectHandle.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 1000));
                if (monitor.isCanceled()) {
                	throw new OperationCanceledException();
                }
	        }
	        finally {
	        	monitor.done();
	        }
	    }
	}
	
	private IWorkbench fWorkbench;
	private IStructuredSelection fSelection;
	
	public NewElementWizard() {
		
        setDialogSettings(StatetPlugin.getDefault().getDialogSettings());
		setNeedsProgressMonitor(true);
	}
			
	/**
	 * Subclasses should override to perform the actions of the wizard.
	 * This method is run in the wizard container's context as a workspace runnable.
	 * @param monitor
	 * @throws InterruptedException
	 * @throws CoreException
	 * @throws InvocationTargetException 
	 */
	protected abstract void doFinish(IProgressMonitor monitor) throws InterruptedException, CoreException, InvocationTargetException;
	
	/**
	 * @return the scheduling rule for creating the element.
	 */
	protected ISchedulingRule getSchedulingRule() {
		return ResourcesPlugin.getWorkspace().getRoot(); // look all by default
	}
	
	/**
	 * @return true if the runnable should be run in a separate thread, and false to run in the same thread
	 */
	protected boolean canRunForked() {
		
		return true;
	}
	
//	public abstract IJavaElement getCreatedElement();


	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		
		fWorkbench = workbench;
		fSelection = currentSelection;
	}

	public boolean performFinish() {

        WorkspaceModifyOperation op = new WorkspaceModifyOperation(getSchedulingRule()) {
        	protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
				try {
					if (monitor == null) {
						monitor = new NullProgressMonitor();
					}
					doFinish(monitor);
				} 
				catch (InterruptedException e) {
					throw new OperationCanceledException(e.getMessage());
				} 
				catch (CoreException e) {
					throw new InvocationTargetException(e);
				} 
				finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(canRunForked(), true, op);
		} catch (InvocationTargetException e) {
			handleFinishException(getShell(), e);
			return false;
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}

	protected void handleFinishException(Shell shell, InvocationTargetException e) {
		
		ExceptionHandler.handle(e, shell, 
				StatetWizardsMessages.NewElementWizard_error_DuringOperation_message);
	}
	
	
	public IStructuredSelection getSelection() {
		return fSelection;
	}

	public IWorkbench getWorkbench() {
		return fWorkbench;
	}


	
/* Helper methods for subclasses **********************************************/	
	
    /**
     * Returns the scheduling rule to use when creating the resource at
     * the given container path. The rule should be the creation rule for
     * the top-most non-existing parent.
     * @param resource The resource being created
     * @return The scheduling rule for creating the given resource
     * @since 3.1
     */
    protected ISchedulingRule createRule(IResource resource) {
		IResource parent = resource.getParent();
    	while (parent != null) {
    		if (parent.exists())
    			return resource.getWorkspace().getRuleFactory().createRule(resource);
    		resource = parent;
    		parent = parent.getParent();
    	}
		return resource.getWorkspace().getRoot();
	}

    protected void openResource(final IFile resource) {
		
		final IWorkbenchPage activePage = UIAccess.getActiveWorkbenchPage(true);
		if (activePage != null) {
			final Display display = getShell().getDisplay();
			if (display != null) {
				display.asyncExec(new Runnable() {
					public void run() {
						try {
							IDE.openEditor(activePage, resource, true);
						} catch (PartInitException e) {
							StatetPlugin.logUnexpectedError(e);
						}
					}
				});
			}
		}
	}

	protected void selectAndReveal(IResource newResource) {
		
		BasicNewResourceWizard.selectAndReveal(newResource, fWorkbench.getActiveWorkbenchWindow());
	}
	
	protected void updatePerspective(IConfigurationElement config) {
		
		BasicNewProjectResourceWizard.updatePerspective(config);		
	}

}
