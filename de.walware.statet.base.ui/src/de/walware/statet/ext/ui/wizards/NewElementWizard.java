/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.ui.util.WorkbenchUIUtil;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


public abstract class NewElementWizard extends Wizard implements INewWizard, IExecutableExtension {
	
	
	protected static class NewFileCreator {
		
		protected IPath fContainerPath;
		protected String fResourceName;
		protected IRegion fInitialSelection;
		
		
		/** No direct access, use getFileHandle() */
		private IFile fCachedFileHandle;
		
		public NewFileCreator(final IPath containerPath, final String resourceName) {
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
				final IPath newFilePath = fContainerPath.append(fResourceName);
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
		protected IFile createFileHandle(final IPath filePath) {
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
			final IPath containerPath = fContainerPath;
			final IFile newFileHandle = getFileHandle();
			final InputStream initialContents = getInitialFileContentStream(newFileHandle);
			
			try {
				assert (containerPath != null);
				assert (newFileHandle != null);
				
				monitor.beginTask(NLS.bind(StatetWizardsMessages.NewElement_CreateFile_task, newFileHandle.getName()), 1000);
				final ContainerGenerator generator = new ContainerGenerator(containerPath);
				generator.generateContainer(new SubProgressMonitor(monitor, 500));
				doCreateFile(newFileHandle, initialContents, monitor, 500);
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
		private static void doCreateFile(final IFile fileHandle, InputStream contents, final IProgressMonitor monitor, final int ticks)
				throws CoreException {
			if (contents == null) {
				contents = new ByteArrayInputStream(new byte[0]);
			}
			try {
				// Create a new file resource in the workspace
				final IPath path = fileHandle.getFullPath();
				final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				final int numSegments = path.segmentCount();
				if (numSegments > 2 && !root.getFolder(path.removeLastSegments(1)).exists()) {
					// If the direct parent of the path doesn't exist, try to create the
					// necessary directories.
					for (int i = numSegments - 2; i > 0; i--) {
						final IFolder folder = root.getFolder(path.removeLastSegments(i));
						if (!folder.exists()) {
							folder.create(false, true, new SubProgressMonitor(monitor, 500/(numSegments-2)));
						}
					}
				}
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				fileHandle.create(contents, false, new SubProgressMonitor(monitor, 500));
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
			}
			catch (final CoreException e) {
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
		protected InputStream getInitialFileContentStream(final IFile newFileHandle) {
			final String content = getInitialFileContent(newFileHandle);
			if (content == null) {
				return null;
			}
			try {
				// encoding of content type
				final IContentTypeManager manager = Platform.getContentTypeManager();
				final IContentType contentType = manager.getContentType(getContentType(newFileHandle));
				if (contentType != null) {
					final String charset = contentType.getDefaultCharset();
					if (charset != null) {
						return new ByteArrayInputStream(content.getBytes(charset));
					}
				}
				// encoding of container
				final String charset = newFileHandle.getCharset(true);
				if (charset != null) {
					return new ByteArrayInputStream(content.getBytes(charset));
				}
			}
			catch (final UnsupportedEncodingException e) {
			}
			catch (final CoreException e) {
			}
			return new ByteArrayInputStream(content.getBytes());
		}
		
		/**
		 * Returns the content type id of the new file.
		 * Used e.g. to lookup the encoding.
		 * @return id
		 */
		public String getContentType(final IFile newFileHandle) {
			return IContentTypeManager.CT_TEXT;
		}
		
		/**
		 * Returns a stream containing the initial contents to be given to new file resource
		 * instances.  <b>Subclasses</b> may wish to override.  This default implementation
		 * provides no initial contents.
		 * 
		 * @return initial contents to be given to new file resource instances
		 */
		protected String getInitialFileContent(final IFile newFileHandle) {
			return null;
		}
		
		public IRegion getInitialSelection() {
			return fInitialSelection;
		}
		
	}
	
	protected class ProjectCreator {
		
		/** Name of project */
		protected String fProjectName;
		/** Path of project, null for default location */
		protected IPath fNewPath;
		
		private IProject fCachedProjectHandle;
		private IProject[] fRefProjects;
		private IWorkingSet[] fWorkingSets;
		
		public ProjectCreator(final String name, final IPath path,
				final IProject[] projects, final IWorkingSet[] workingSets) {
			fProjectName = name;
			fNewPath = path;
			fRefProjects = projects;
			fWorkingSets = workingSets;
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
		 * @return
		 * 
		 * @return the created project resource, or <code>null</code> if the
		 *     project was not created
		 * @throws CoreException
		 */
		public IProject createProject(final IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException, CoreException {
			final IProject projectHandle = getProjectHandle();
			
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			final IProjectDescription description = workspace.newProjectDescription(projectHandle.getName());
			description.setLocation(fNewPath);
			
			// update the referenced project if provided
			if (fRefProjects != null && fRefProjects.length > 0) {
				description.setReferencedProjects(fRefProjects);
			}
			
			try {
				Assert.isNotNull(projectHandle);
				
				monitor.beginTask(StatetWizardsMessages.NewElement_CreateProject_task, 2500);
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				doCreateProject(projectHandle, description, new SubProgressMonitor(monitor, 1000));
				doConfigProject(projectHandle, new SubProgressMonitor(monitor, 1000));
				doAddToWorkingSets(projectHandle, new SubProgressMonitor(monitor, 500));
				return projectHandle;
			}
			finally {
				monitor.done();
			}
		}
		
		private void doCreateProject(final IProject project, final IProjectDescription description, final IProgressMonitor monitor)
				throws CoreException {
			// run the new project creation operation
			monitor.beginTask("Install Project", 1000); //$NON-NLS-1$
			
			project.create(description, new SubProgressMonitor(monitor, 500));
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			project.open(new SubProgressMonitor(monitor, 500));
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
		
		protected void doConfigProject(final IProject project, final IProgressMonitor monitor) throws CoreException {
		}
		
		private void doAddToWorkingSets(final IProject project, final IProgressMonitor monitor) {
			if (fWorkingSets != null && fWorkingSets.length > 0) {
				monitor.beginTask(StatetWizardsMessages.NewElement_AddProjectToWorkingSet_task, 1);
				getWorkbench().getWorkingSetManager().addToWorkingSets(project, fWorkingSets);
			}
			monitor.done();
		}
		
	}
	
	
	private IWorkbench fWorkbench;
	private IStructuredSelection fSelection;
	
	private IConfigurationElement fConfigElement;
	
	
	public NewElementWizard() {
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Stores the configuration element for the wizard. The config element will
	 * be used in <code>performFinish</code> to set the result perspective.
	 */
	public void setInitializationData(final IConfigurationElement config, final String propertyName, final Object data) {
		fConfigElement = config;
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
	
	
	public void init(final IWorkbench workbench, final IStructuredSelection currentSelection) {
		fWorkbench = workbench;
		fSelection = currentSelection;
	}
	
	@Override
	public boolean performFinish() {
		final WorkspaceModifyOperation op = new WorkspaceModifyOperation(getSchedulingRule()) {
			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
				try {
					if (monitor == null) {
						monitor = new NullProgressMonitor();
					}
					doFinish(monitor);
				}
				catch (final InterruptedException e) {
					throw new OperationCanceledException(e.getMessage());
				}
				catch (final CoreException e) {
					throw new InvocationTargetException(e);
				}
				finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(canRunForked(), true, op);
		} catch (final InvocationTargetException e) {
			handleFinishException(getShell(), e);
			return false;
		} catch (final InterruptedException e) {
			return false;
		}
		return true;
	}
	
	protected void handleFinishException(final Shell shell, final InvocationTargetException e) {
		StatusManager.getManager().handle(new Status(Status.ERROR, StatetUIPlugin.PLUGIN_ID, -1,
				StatetWizardsMessages.NewElement_error_DuringOperation_message, e),
				StatusManager.LOG | StatusManager.SHOW);
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
			WorkbenchUIUtil.openEditor(activePage, resource, null);
		}
	}
	
	protected void openResource(final NewFileCreator file) {
		if (file.getFileHandle() == null) {
			return;
		}
		final IWorkbenchPage activePage = UIAccess.getActiveWorkbenchPage(true);
		if (activePage != null) {
			WorkbenchUIUtil.openEditor(activePage, file.getFileHandle(), file.getInitialSelection());
		}
	}
	
	protected void selectAndReveal(final IResource newResource) {
		BasicNewResourceWizard.selectAndReveal(newResource, fWorkbench.getActiveWorkbenchWindow());
	}
	
	protected void updatePerspective() {
		BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
	}
	
}
