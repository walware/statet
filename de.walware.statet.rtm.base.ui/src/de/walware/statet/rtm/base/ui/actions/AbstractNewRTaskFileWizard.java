/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet)
 * and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.rtm.base.ui.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.statet.rtm.base.internal.ui.actions.Messages;
import de.walware.statet.rtm.base.ui.IRtDescriptor;
import de.walware.statet.rtm.base.ui.RtModelUIPlugin;


/**
 * This is a simple wizard for creating a new model file.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 */
public abstract class AbstractNewRTaskFileWizard extends Wizard implements INewWizard {
	
	
	protected final IRtDescriptor fRtDescriptor;
	
	/**
	 * This is the file creation page.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ModelWizardNewFileCreationPage fNewFileCreationPage;
	
	/**
	 * Remember the selection during initialization for populating the default container.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IStructuredSelection fSelection;
	
	/**
	 * Remember the workbench during initialization.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IWorkbench fWorkbench;
	
	
	protected AbstractNewRTaskFileWizard(final IRtDescriptor descriptor) {
		fRtDescriptor = descriptor;
	}
	
	/**
	 * This just records the information.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection selection) {
		fWorkbench = workbench;
		fSelection = selection;
		setWindowTitle(NLS.bind(Messages.NewRTaskWizard_title, fRtDescriptor.getName()));
//		setDefaultPageImageDescriptor(ExtendedImageRegistry.INSTANCE.getImageDescriptor(RtGGPlotEditorPlugin.INSTANCE.getImage("full/wizban/NewGGPlot"))); //$NON-NLS-1$
	}
	
	/**
	 * Create a new model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	protected EObject createInitialModel() {
		return fRtDescriptor.createInitialModelObject();
	}
	
	/**
	 * Do the work after everything is specified.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean performFinish() {
		try {
			// Remember the file
			final IFile modelFile = getModelFile();
			
			// Do the work within an operation
			final WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
				@Override
				protected void execute(final IProgressMonitor progressMonitor) throws InvocationTargetException {
					try {
						// Create a resource set
						final ResourceSet resourceSet = new ResourceSetImpl();
						
						// Get the URI of the model file
						final URI fileURI = URI.createPlatformResourceURI(modelFile.getFullPath().toString(), true);
						
						// Create a resource for this file
						final Resource resource = resourceSet.createResource(fileURI, fRtDescriptor.getDefaultContentTypeID());
						
						// Add the initial model object to the contents
						final EObject rootObject = createInitialModel();
						if (rootObject != null) {
							resource.getContents().add(rootObject);
						}
						
						// Save the contents of the resource to the file system
						final Map<Object, Object> options = new HashMap<Object, Object>();
						options.put(XMLResource.OPTION_ENCODING, "UTF-8"); //$NON-NLS-1$
						resource.save(options);
					}
					catch (final IOException e) {
						throw new InvocationTargetException(e);
					}
					finally {
						progressMonitor.done();
					}
				}
			};
			
			getContainer().run(false, false, operation);
			
			final PerspectiveUtil perspectiveUtil = new PerspectiveUtil();
			perspectiveUtil.updatePerspective(fRtDescriptor.getAssociatedPerspectiveId());
			
			// Select the new file resource in the current view
			final IWorkbenchWindow workbenchWindow = fWorkbench.getActiveWorkbenchWindow();
			final IWorkbenchPage page = workbenchWindow.getActivePage();
			final IWorkbenchPart activePart = page.getActivePart();
			if (activePart instanceof ISetSelectionTarget) {
				final ISelection targetSelection = new StructuredSelection(modelFile);
				getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						((ISetSelectionTarget)activePart).selectReveal(targetSelection);
					}
				});
			}
			
			// Open an editor on the new file
			try {
				page.openEditor(new FileEditorInput(modelFile),
						fWorkbench.getEditorRegistry().getDefaultEditor(
								modelFile.getFullPath().toString(),
								Platform.getContentTypeManager().getContentType(fRtDescriptor.getDefaultContentTypeID())).getId());
			}
			catch (final PartInitException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, RtModelUIPlugin.PLUGIN_ID, 0,
						NLS.bind(Messages.NewRTaskWizard_error_OpenEditor_message, fRtDescriptor.getName()),
						e ), StatusManager.LOG | StatusManager.SHOW );
				return false;
			}
			
			return true;
		}
		catch (final Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, RtModelUIPlugin.PLUGIN_ID, 0,
					NLS.bind(Messages.NewRTaskWizard_error_CreateFile_message, fRtDescriptor.getName()),
					e ), StatusManager.LOG | StatusManager.SHOW );
			return false;
		}
	}
	
	/**
	 * This is the one page of the wizard.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public class ModelWizardNewFileCreationPage extends WizardNewFileCreationPage {
		/**
		 * Pass in the selection.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public ModelWizardNewFileCreationPage(final String pageId, final IStructuredSelection selection) {
			super(pageId, selection);
			setFileExtension(fRtDescriptor.getDefaultFileExtension());
		}
		
		/**
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public IFile getModelFile() {
			return ResourcesPlugin.getWorkspace().getRoot().getFile(getContainerFullPath().append(getFileName()));
		}
	}
	
	/**
	 * The framework calls this to create the contents of the wizard.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void addPages() {
		// Create a page, set the title, and the initial model file name
		
		fNewFileCreationPage = new ModelWizardNewFileCreationPage("NewRTask.NewFile", fSelection); //$NON-NLS-1$
		fNewFileCreationPage.setTitle(NLS.bind(Messages.NewRTaskWizard_NewFile_title, fRtDescriptor.getName()));
		fNewFileCreationPage.setDescription(NLS.bind(Messages.NewRTaskWizard_NewFile_description, fRtDescriptor.getName()));
		addPage(fNewFileCreationPage);
		
		// Try and get the resource selection to determine a current directory for the file dialog
		if (fSelection != null && !fSelection.isEmpty()) {
			// Get the resource...
			final Object selectedElement = fSelection.iterator().next();
			if (selectedElement instanceof IResource) {
				// Get the resource parent, if its a file
				IResource selectedResource = (IResource)selectedElement;
				if (selectedResource.getType() == IResource.FILE) {
					selectedResource = selectedResource.getParent();
				}
				
				// This gives us a directory...
				if (selectedResource instanceof IFolder || selectedResource instanceof IProject) {
					// Set this for the container
					fNewFileCreationPage.setContainerFullPath(selectedResource.getFullPath());
					
//					// Make up a unique new name here
//					String defaultModelBaseFilename = RtGGPlotEditorPlugin.INSTANCE.getString("_UI_GGPlotEditorFilenameDefaultBase"); //$NON-NLS-1$
//					String defaultModelFilenameExtension = fRtDescriptor.getDefaultContentTypeFileExtension();
//					String modelFilename = defaultModelBaseFilename + "." + defaultModelFilenameExtension; //$NON-NLS-1$
//					for (int i = 1; ((IContainer)selectedResource).findMember(modelFilename) != null; ++i) {
//						modelFilename = defaultModelBaseFilename + i + "." + defaultModelFilenameExtension; //$NON-NLS-1$
//					}
//					fNewFileCreationPage.setFileName(modelFilename);
				}
			}
		}
	}
	
	/**
	 * Get the file from the page.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public IFile getModelFile() {
		return fNewFileCreationPage.getModelFile();
	}
	
}
