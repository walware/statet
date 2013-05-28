/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.emf.ui.forms;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.ui.MarkerHelper;
import org.eclipse.emf.common.ui.editor.ProblemEditorPart;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.emf.edit.ui.dnd.EditingDomainViewerDropAdapter;
import org.eclipse.emf.edit.ui.dnd.LocalTransfer;
import org.eclipse.emf.edit.ui.dnd.ViewerDragAdapter;
import org.eclipse.emf.edit.ui.provider.UnwrappingSelectionProvider;
import org.eclipse.emf.edit.ui.util.EditUIMarkerHelper;
import org.eclipse.emf.edit.ui.util.EditUIUtil;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;

import de.walware.ecommons.emf.core.util.RuleSet;
import de.walware.ecommons.emf.internal.forms.CopyEObjectHandler;
import de.walware.ecommons.emf.internal.forms.CutEObjectHandler;
import de.walware.ecommons.emf.internal.forms.EFEditingDomain;
import de.walware.ecommons.emf.internal.forms.EditorSelectionProvider;
import de.walware.ecommons.emf.internal.forms.PasteEObjectHandler;
import de.walware.ecommons.ui.actions.HandlerCollection;

import de.walware.statet.rtm.base.internal.ui.editors.Messages;


public abstract class EFEditor extends FormEditor
		implements IEditingDomainProvider, IMenuListener, IGotoMarker,
				ITabbedPropertySheetPageContributor {
	
	
	private class ResourceManager implements IResourceChangeListener {
		
		/**
		 * Resources that have been removed since last activation.
		 * @generated
		 */
		private final Collection<Resource> fRemovedResources = new ArrayList<Resource>();
		
		/**
		 * Resources that have been changed since last activation.
		 * @generated
		 */
		private final Collection<Resource> fChangedResources = new ArrayList<Resource>();
		
		/**
		 * Resources that have been saved.
		 * @generated
		 */
		private final Collection<Resource> fSavedResources = new ArrayList<Resource>();
		
		@Override
		public void resourceChanged(final IResourceChangeEvent event) {
			final IResourceDelta delta = event.getDelta();
			try {
				class ResourceDeltaVisitor implements IResourceDeltaVisitor {
					
					protected ResourceSet resourceSet = getEditingDomain().getResourceSet();
					protected Collection<Resource> changedResources = new ArrayList<Resource>();
					protected Collection<Resource> removedResources = new ArrayList<Resource>();
					
					@Override
					public boolean visit(final IResourceDelta delta) {
						if (delta.getResource().getType() == IResource.FILE) {
							if (delta.getKind() == IResourceDelta.REMOVED ||
									delta.getKind() == IResourceDelta.CHANGED && delta.getFlags() != IResourceDelta.MARKERS) {
								final Resource resource = resourceSet.getResource(URI.createPlatformResourceURI(delta.getFullPath().toString(), true), false);
								if (resource != null) {
									if (delta.getKind() == IResourceDelta.REMOVED) {
										removedResources.add(resource);
									}
									else if (!fSavedResources.remove(resource)) {
										changedResources.add(resource);
									}
								}
							}
						}
						
						return true;
					}
					
					public Collection<Resource> getChangedResources() {
						return changedResources;
					}
					
					public Collection<Resource> getRemovedResources() {
						return removedResources;
					}
				}
				
				final ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();
				delta.accept(visitor);
				
				if (!visitor.getRemovedResources().isEmpty()) {
					getSite().getShell().getDisplay().asyncExec
						(new Runnable() {
							 @Override
							public void run() {
								fRemovedResources.addAll(visitor.getRemovedResources());
								if (!isDirty()) {
									getSite().getPage().closeEditor(EFEditor.this, false);
								}
							}
						});
				}
				
				if (!visitor.getChangedResources().isEmpty()) {
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							fChangedResources.addAll(visitor.getChangedResources());
							if (getSite().getPage().getActiveEditor() == EFEditor.this) {
								handleActivate();
							}
						}
					});
				}
			}
			catch (final CoreException e) {
				operationFailed("processing resource change", e); //$NON-NLS-1$
			}
		}
		
		public void processChanges() {
			if (!fRemovedResources.isEmpty()) {
				if (handleDirtyConflict()) {
					getSite().getPage().closeEditor(EFEditor.this, false);
				}
				else {
					fRemovedResources.clear();
					fChangedResources.clear();
					fSavedResources.clear();
				}
			}
			else if (!fChangedResources.isEmpty()) {
				fChangedResources.removeAll(fSavedResources);
				handleChangedResources();
				fChangedResources.clear();
				fSavedResources.clear();
			}
		}
		
		/**
		 * Handles what to do with changed resources on activation.
		 * @generated
		 */
		protected void handleChangedResources() {
			if (!fChangedResources.isEmpty() && (!isDirty() || handleDirtyConflict())) {
				if (isDirty()) {
					fChangedResources.addAll(fEditingDomain.getResourceSet().getResources());
				}
				fEditingDomain.getCommandStack().flush();
				
				fUpdateProblemIndication = false;
				for (final Resource resource : fChangedResources) {
					if (resource.isLoaded()) {
						resource.unload();
						try {
							resource.load(Collections.EMPTY_MAP);
						}
						catch (final IOException exception) {
							if (!fResourceToDiagnosticMap.containsKey(resource)) {
								fResourceToDiagnosticMap.put(resource, analyzeResourceProblems(resource, exception));
							}
						}
					}
				}
				
				final ISelectionProvider selectionProvider = getSite().getSelectionProvider();
				if (AdapterFactoryEditingDomain.isStale(selectionProvider.getSelection())) {
					selectionProvider.setSelection(StructuredSelection.EMPTY);
				}
				
				fUpdateProblemIndication = true;
				updateProblemIndication();
			}
		}
		
	}
	
	
	private final IEFModelDescriptor fModelDescriptor;
	
	/**
	 * This listens for workspace changes.
	 */
	private final ResourceManager fResourceListener = new ResourceManager();
	
	/**
	 * Map to store the diagnostic associated with a resource.
	 * @generated
	 */
	private final Map<Resource, Diagnostic> fResourceToDiagnosticMap = new LinkedHashMap<Resource, Diagnostic>();
	
	/**
	 * The MarkerHelper is responsible for creating workspace resource markers presented
	 * in Eclipse's Problems View.
	 * @generated
	 */
	private final MarkerHelper fMarkerHelper = new EditUIMarkerHelper();
	
	/**
	 * Controls whether the problem indication should be updated.
	 * @generated
	 */
	protected boolean fUpdateProblemIndication = true;
	
	/**
	 * Adapter used to update the problem indication when resources are demanded loaded.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final EContentAdapter fProblemIndicationAdapter = new EContentAdapter() {
		@Override
		public void notifyChanged(final Notification notification) {
			if (notification.getNotifier() instanceof Resource) {
				switch (notification.getFeatureID(Resource.class)) {
					case Resource.RESOURCE__IS_LOADED:
					case Resource.RESOURCE__ERRORS:
					case Resource.RESOURCE__WARNINGS: {
						final Resource resource = (Resource)notification.getNotifier();
						final Diagnostic diagnostic = analyzeResourceProblems(resource, null);
						if (diagnostic.getSeverity() != Diagnostic.OK) {
							fResourceToDiagnosticMap.put(resource, diagnostic);
						}
						else {
							fResourceToDiagnosticMap.remove(resource);
						}
						
						if (fUpdateProblemIndication) {
							getSite().getShell().getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									updateProblemIndication();
								}
							});
						}
						break;
					}
				}
			}
			else {
				super.notifyChanged(notification);
			}
		}
		
		@Override
		protected void setTarget(final Resource target) {
			basicSetTarget(target);
		}
		
		@Override
		protected void unsetTarget(final Resource target) {
			basicUnsetTarget(target);
		}
	};
	
	public class ContextAdapterFactory implements IAdapterFactory {
		
		@Override
		public Class[] getAdapterList() {
			return null;
		}
		
		@Override
		public Object getAdapter(final Object adaptableObject, final Class required) {
			if (required.equals(RuleSet.class)) {
				return fModelDescriptor.getRuleSet();
			}
			return null;
		}
		
	}
	
	
	/**
	 * This is the content outline page.
	 * @generated
	 */
	private IContentOutlinePage fContentOutlinePage;
	
	private final List<IPropertySheetPage> fPropertySheetPages = new ArrayList<IPropertySheetPage>(2);
	
	/**
	 * This listens for when the outline becomes active
	 * @generated
	 */
	private final IPartListener fPartListener = new IPartListener() {
		@Override
		public void partActivated(final IWorkbenchPart p) {
			if (p instanceof ContentOutline) {
				if (((ContentOutline) p).getCurrentPage() == fContentOutlinePage) {
					getActionBarContributor().setActiveEditor(EFEditor.this);
					
//					setCurrentViewer(fContentOutlineViewer);
				}
			}
			else if (p instanceof PropertySheet) {
				if (fPropertySheetPages.contains(((PropertySheet) p).getCurrentPage())) {
					getActionBarContributor().setActiveEditor(EFEditor.this);
					handleActivate();
				}
			}
			else if (p == EFEditor.this) {
				handleActivate();
			}
		}
		@Override
		public void partBroughtToTop(final IWorkbenchPart p) {
		}
		@Override
		public void partClosed(final IWorkbenchPart p) {
		}
		@Override
		public void partDeactivated(final IWorkbenchPart p) {
		}
		@Override
		public void partOpened(final IWorkbenchPart p) {
		}
	};
	
	/**
	 * This keeps track of the editing domain that is used to track all changes to the model.
	 */
	private EFEditingDomain fEditingDomain;
	
	
	/**
	 * This is the one adapter factory used for providing views of the model.
	 * @generated
	 */
	private ComposedAdapterFactory fAdapterFactory;
	
	private EFDataBindingSupport fDataBinding;
	
	private final EditorSelectionProvider fSelectionProvider = new EditorSelectionProvider(this);
	
	protected HandlerCollection fHandlers;
	
	
	protected EFEditor(final IEFModelDescriptor modelDescriptor) {
		super();
		fModelDescriptor = modelDescriptor;
		
		initializeEditingDomain(modelDescriptor.createItemProviderAdapterFactory());
	}
	
	
	public IEFModelDescriptor getModelDescriptor() {
		return fModelDescriptor;
	}
	
	@Override
	public String getContributorId() {
		return fModelDescriptor.getEditorID();
	}
	
	protected void initializeEditingDomain(final AdapterFactory itemAdapterFactory) {
		// Create an adapter factory that yields item providers.
		fAdapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
		
		fAdapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
		fAdapterFactory.addAdapterFactory(itemAdapterFactory);
		fAdapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
		
		// Create the command stack that will notify this editor as commands are executed.
		final BasicCommandStack commandStack = new BasicCommandStack();
		
		// Add a listener to set the most recent command's affected objects to be the selection of the viewer with focus.
		commandStack.addCommandStackListener(new CommandStackListener() {
			@Override
			public void commandStackChanged(final EventObject event) {
				getContainer().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						firePropertyChange(IEditorPart.PROP_DIRTY);
						
						// Try to select the affected objects.
						//
						final Command mostRecentCommand = ((CommandStack)event.getSource()).getMostRecentCommand();
						if (mostRecentCommand != null) {
							selectObject((Collection<? extends EObject>) mostRecentCommand.getAffectedObjects());
						}
					}
				});
			}
		});
		
		// Create the editing domain with a special command stack.
		fEditingDomain = new EFEditingDomain(fAdapterFactory, commandStack);
	}
	
	/**
	 * This returns the editing domain as required by the {@link IEditingDomainProvider} interface.
	 * This is important for implementing the static methods of {@link AdapterFactoryEditingDomain}
	 * and for supporting {@link org.eclipse.emf.edit.ui.action.CommandAction}.
	 * @generated
	 */
	@Override
	public EditingDomain getEditingDomain() {
		return fEditingDomain;
	}
	
	/**
	 * @generated
	 */
	public AdapterFactory getAdapterFactory() {
		return fAdapterFactory;
	}
	
	/**
	 * @generated
	 */
	protected EFEditorActionBarContributor getActionBarContributor() {
		return (EFEditorActionBarContributor) getEditorSite().getActionBarContributor();
	}
	
	
	/**
	 * This is called during startup.
	 * @generated
	 */
	@Override
	public void init(final IEditorSite site, final IEditorInput editorInput) {
		setSite(site);
		setInputWithNotify(editorInput);
		setPartName(editorInput.getName());
		site.setSelectionProvider(fSelectionProvider);
		site.getPage().addPartListener(fPartListener);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(fResourceListener, IResourceChangeEvent.POST_CHANGE);
	}
	
	/**
	 * Handles activation of the editor or it's associated views.
	 * @generated
	 */
	protected void handleActivate() {
		// Recompute the read only state.
		final Map<Resource, Boolean> resourceToReadOnlyMap = fEditingDomain.getResourceToReadOnlyMap();
		if (resourceToReadOnlyMap != null) {
			resourceToReadOnlyMap.clear();
			
			// Refresh any actions that may become enabled or disabled.
			final ISelectionProvider selectionProvider = getSite().getSelectionProvider();
			selectionProvider.setSelection(selectionProvider.getSelection());
		}
		
		fResourceListener.processChanges();
	}
	
	/**
	 * This is the method called to load a resource into the editing domain's resource set based on the editor's input.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createModel() {
		final IEditorInput input = getEditorInput();
		if (input instanceof DirectResourceEditorInput) {
			fEditingDomain.getResourceSet().getResources().add(((DirectResourceEditorInput) input).getResource());
			return;
		}
		final URI resourceURI = EditUIUtil.getURI(input);
		Exception exception = null;
		Resource resource = null;
		try {
			// Load the resource through the editing domain.
			//
			resource = fEditingDomain.getResourceSet().getResource(resourceURI, true);
		}
		catch (final Exception e) {
			exception = e;
			resource = fEditingDomain.getResourceSet().getResource(resourceURI, false);
		}
		
		final Diagnostic diagnostic = analyzeResourceProblems(resource, exception);
		if (diagnostic.getSeverity() != Diagnostic.OK) {
			fResourceToDiagnosticMap.put(resource, analyzeResourceProblems(resource, exception));
		}
		fEditingDomain.getResourceSet().eAdapters().add(fProblemIndicationAdapter);
	}
	
	protected void resourceLoaded() {
		if (fDataBinding != null) {
		}
	}
	
	/**
	 * Returns a diagnostic describing the errors and warnings listed in the resource
	 * and the specified exception (if any).
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Diagnostic analyzeResourceProblems(final Resource resource, final Exception exception) {
		if (!resource.getErrors().isEmpty() || !resource.getWarnings().isEmpty()) {
			final BasicDiagnostic basicDiagnostic = new BasicDiagnostic(Diagnostic.ERROR,
					fModelDescriptor.getEditorPluginID(), 0,
					NLS.bind(Messages.EFEditor_error_ProblemsInFile_message, resource.getURI()),
					new Object [] { exception == null ? (Object)resource : exception } );
			basicDiagnostic.merge(EcoreUtil.computeDiagnostic(resource, true));
			return basicDiagnostic;
		}
		else if (exception != null) {
			return new BasicDiagnostic(Diagnostic.ERROR,
					fModelDescriptor.getEditorPluginID(), 0,
					NLS.bind(Messages.EFEditor_error_ProblemsInFile_message, resource.getURI()),
					new Object[] { exception } );
		}
		else {
			return Diagnostic.OK_INSTANCE;
		}
	}
	
	/**
	 * Shows a dialog that asks if conflicting changes should be discarded.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected boolean handleDirtyConflict() {
		return MessageDialog.openQuestion(getSite().getShell(),
				Messages.EFEditor_FileConflict_title,
				Messages.EFEditor_FileConflict_message );
	}
	
	
	/**
	 * This is for implementing {@link IEditorPart} and simply tests the command stack.
	 * @generated
	 */
	@Override
	public boolean isDirty() {
		return ((BasicCommandStack) fEditingDomain.getCommandStack()).isSaveNeeded();
	}
	
	/**
	 * This always returns true because it is not currently supported.
	 * @generated
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	/**
	 * This is for implementing {@link IEditorPart} and simply saves the model file.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void doSave(final IProgressMonitor progressMonitor) {
		if (getEditorInput() instanceof DirectResourceEditorInput
				&& fEditingDomain.getResourceSet().getResources().get(0).getURI().equals(DirectResourceEditorInput.NO_URI)) {
			doSaveAs();
			return;
		}
		// Save only resources that have actually changed
		final Map<Object, Object> saveOptions = new HashMap<Object, Object>();
		saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
		saveOptions.put(XMLResource.OPTION_ENCODING, "UTF-8"); //$NON-NLS-1$
		
		// Do the work within an operation because this is a long running activity that modifies the workbench
		final WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
			// This is the method that gets invoked when the operation runs
			@Override
			public void execute(final IProgressMonitor monitor) {
				// Save the resources to the file system
				boolean first = true;
				for (final Resource resource : fEditingDomain.getResourceSet().getResources()) {
					if ((first || !resource.getContents().isEmpty() || isPersisted(resource)) && !fEditingDomain.isReadOnly(resource)) {
						try {
							final long timeStamp = resource.getTimeStamp();
							resource.save(saveOptions);
							if (resource.getTimeStamp() != timeStamp) {
								fResourceListener.fSavedResources.add(resource);
							}
						}
						catch (final Exception exception) {
							fResourceToDiagnosticMap.put(resource, analyzeResourceProblems(resource, exception));
						}
						first = false;
					}
				}
			}
		};
		
		fUpdateProblemIndication = false;
		try {
			// This runs the options, and shows progress
			new ProgressMonitorDialog(getSite().getShell()).run(true, false, operation);
			
			// Refresh the necessary state
			((BasicCommandStack) fEditingDomain.getCommandStack()).saveIsDone();
			firePropertyChange(IEditorPart.PROP_DIRTY);
		}
		catch (final Exception e) {
			// Something went wrong that shouldn't
			operationFailed("saving config", e); //$NON-NLS-1$
		}
		fUpdateProblemIndication = true;
		updateProblemIndication();
	}
	
	/**
	 * This returns whether something has been persisted to the URI of the specified resource.
	 * The implementation uses the URI converter from the editor's resource set to try to open an input stream. 
	 * @generated
	 */
	protected boolean isPersisted(final Resource resource) {
		boolean result = false;
		try {
			final InputStream stream = fEditingDomain.getResourceSet().getURIConverter().createInputStream(resource.getURI());
			if (stream != null) {
				result = true;
				stream.close();
			}
		}
		catch (final IOException e) {
			// Ignore
		}
		return result;
	}
	
	/**
	 * This also changes the editor's input.
	 * @generated
	 */
	@Override
	public void doSaveAs() {
		IPath path;
		
		final SaveAsDialog saveAsDialog = new SaveAsDialog(getSite().getShell());
		if (saveAsDialog.open() != Window.OK
				|| (path = saveAsDialog.getResult()) == null) {
			return;
		}
		
		if (path.getFileExtension() == null && fModelDescriptor.getDefaultFileExtension() != null) {
			path = path.addFileExtension(fModelDescriptor.getDefaultFileExtension());
		}
		final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		if (file != null) {
			doSaveAs(URI.createPlatformResourceURI(file.getFullPath().toString(), true), new FileEditorInput(file));
		}
	}
	
	/**
	 * @generated
	 */
	protected void doSaveAs(final URI uri, final IEditorInput editorInput) {
		(fEditingDomain.getResourceSet().getResources().get(0)).setURI(uri);
		setInputWithNotify(editorInput);
		setPartName(editorInput.getName());
		
		final IStatusLineManager statusLine = getActionBarContributor().getActionBars().getStatusLineManager();
		final IProgressMonitor progressMonitor = (statusLine != null) ?
				statusLine.getProgressMonitor() : new NullProgressMonitor();
		doSave(progressMonitor);
	}
	
	/**
	 * Updates the problems indication with the information described in the specified diagnostic.
	 * @generated
	 */
	protected void updateProblemIndication() {
		if (fUpdateProblemIndication) {
			final BasicDiagnostic diagnostic = new BasicDiagnostic(Diagnostic.OK,
					fModelDescriptor.getEditorPluginID(), 0,
					null,
					new Object [] { fEditingDomain.getResourceSet() });
			for (final Diagnostic childDiagnostic : fResourceToDiagnosticMap.values()) {
				if (childDiagnostic.getSeverity() != Diagnostic.OK) {
					diagnostic.add(childDiagnostic);
				}
			}
			
			int lastEditorPage = getPageCount() - 1;
			if (lastEditorPage >= 0 && getEditor(lastEditorPage) instanceof ProblemEditorPart) {
				((ProblemEditorPart)getEditor(lastEditorPage)).setDiagnostic(diagnostic);
				if (diagnostic.getSeverity() != Diagnostic.OK) {
					setActivePage(lastEditorPage);
				}
			}
			else if (diagnostic.getSeverity() != Diagnostic.OK) {
				final ProblemEditorPart problemEditorPart = new ProblemEditorPart();
				problemEditorPart.setDiagnostic(diagnostic);
				problemEditorPart.setMarkerHelper(fMarkerHelper);
				try {
					addPage(++lastEditorPage, problemEditorPart, getEditorInput());
					setPageText(lastEditorPage, problemEditorPart.getPartName());
					setActivePage(lastEditorPage);
					showTabs();
				}
				catch (final PartInitException e) {
					operationFailed("updating problem indicators", e); //$NON-NLS-1$
				}
			}
			
			if (fMarkerHelper.hasMarkers(fEditingDomain.getResourceSet())) {
				fMarkerHelper.deleteMarkers(fEditingDomain.getResourceSet());
				if (diagnostic.getSeverity() != Diagnostic.OK) {
					try {
						fMarkerHelper.createMarkers(diagnostic);
					}
					catch (final CoreException e) {
						operationFailed("updating problem indicators", e); //$NON-NLS-1$
					}
				}
			}
		}
	}
	
	/**
	 * @generated
	 */
	@Override
	public void gotoMarker(final IMarker marker) {
		try {
			if (marker.getType().equals(EValidator.MARKER)) {
				final String uriAttribute = marker.getAttribute(EValidator.URI_ATTRIBUTE, null);
				if (uriAttribute != null) {
					final URI uri = URI.createURI(uriAttribute);
					final EObject eObject = fEditingDomain.getResourceSet().getEObject(uri, true);
					if (eObject != null) {
						selectObject(Collections.singletonList((EObject) fEditingDomain.getWrapper(eObject)));
					}
				}
			}
		}
		catch (final CoreException e) {
			operationFailed("going to marker", e); //$NON-NLS-1$
		}
	}
	
	
	@Override
	public EFToolkit getToolkit() {
		return (EFToolkit) super.getToolkit();
	}
	
	@Override
	protected abstract EFToolkit createToolkit(Display display);
	
	@Override
	protected void createPages() {
		// Creates the model from the editor input
		createModel();
		
		fDataBinding = new EFDataBindingSupport(this, createContextAdapterFactory());
		
		super.createPages(); // #addPages()
		
		getContainer().addControlListener(new ControlAdapter() {
			boolean guard = false;
			@Override
			public void controlResized(final ControlEvent event) {
				if (!guard) {
					guard = true;
					hideTabs();
					guard = false;
				}
			}
		});
		
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				updateProblemIndication();
			}
		});
		
		fHandlers = new HandlerCollection();
		createActions(getSite(), fHandlers);
	}
	
	protected IAdapterFactory createContextAdapterFactory() {
		return new ContextAdapterFactory();
	}
	
	public EFDataBindingSupport getDataBinding() {
		return fDataBinding;
	}
	
	protected void createActions(final IServiceLocator locator, final HandlerCollection handlers) {
		final IHandlerService handlerService = (IHandlerService) locator.getService(IHandlerService.class);
//		{	final IHandler2 handler = new UndoCommandHandler(getEditingDomain());
//			handlers.add(IWorkbenchCommandConstants.EDIT_UNDO, handler);
//			handlerService.activateHandler(IWorkbenchCommandConstants.EDIT_UNDO, handler);
//		}
//		{	final IHandler2 handler = new RedoCommandHandler(getEditingDomain());
//			handlers.add(IWorkbenchCommandConstants.EDIT_REDO, handler);
//			handlerService.activateHandler(IWorkbenchCommandConstants.EDIT_REDO, handler);
//		}
		
		{	final IHandler2 handler = new CutEObjectHandler();
			handlers.add(IWorkbenchCommandConstants.EDIT_CUT + '~' +
					IEFPropertyExpressions.EOBJECT_LIST_ID, handler );
			handlerService.activateHandler(IWorkbenchCommandConstants.EDIT_CUT, handler,
					IEFPropertyExpressions.EOBJECT_LIST_EXPRESSION );
		}
		{	final IHandler2 handler = new CopyEObjectHandler();
			handlers.add(IWorkbenchCommandConstants.EDIT_COPY + '~' +
					IEFPropertyExpressions.EOBJECT_LIST_ID, handler );
			handlerService.activateHandler(IWorkbenchCommandConstants.EDIT_COPY, handler,
					IEFPropertyExpressions.EOBJECT_LIST_EXPRESSION );
		}
		{	final IHandler2 handler = new PasteEObjectHandler();
			handlers.add(IWorkbenchCommandConstants.EDIT_PASTE + '~' +
					IEFPropertyExpressions.EOBJECT_LIST_ID, handler );
			handlerService.activateHandler(IWorkbenchCommandConstants.EDIT_PASTE, handler,
					IEFPropertyExpressions.EOBJECT_LIST_EXPRESSION );
		}
	}
	
	protected void contributeToPages(final IToolBarManager manager) {
	}
	
	/**
	 * This creates a context menu for the viewer and adds a listener as well registering the menu for extension.
	 * @generated
	 */
	protected void createRawContextMenuFor(final StructuredViewer viewer) {
		final MenuManager contextMenu = new MenuManager("#PopUp"); //$NON-NLS-1$
		contextMenu.add(new Separator("additions")); //$NON-NLS-1$
		contextMenu.setRemoveAllWhenShown(true);
		contextMenu.addMenuListener(this);
		final Menu menu= contextMenu.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(contextMenu, new UnwrappingSelectionProvider(viewer));
		
		final int dndOperations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
		final Transfer[] transfers = new Transfer[] { LocalTransfer.getInstance() };
		viewer.addDragSupport(dndOperations, transfers, new ViewerDragAdapter(viewer));
		viewer.addDropSupport(dndOperations, transfers, new EditingDomainViewerDropAdapter(getEditingDomain(), viewer));
	}
	
	/**
	 * This implements {@link org.eclipse.jface.action.IMenuListener} to help fill the context menus with contributions from the Edit menu.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void menuAboutToShow(final IMenuManager menuManager) {
//		((IMenuListener)getEditorSite().getActionBarContributor()).menuAboutToShow(menuManager);
	}
	
	
	protected void selectObject(final Collection<? extends EObject> objects) {
		// getSite().getShell().getDisplay().asyncExec(runnable)
	}
	
	protected void setStatusLineManager(final ISelection selection) {
//		final IStatusLineManager statusLineManager = fCurrentViewer != null && fCurrentViewer == fContentOutlineViewer ?
//				fContentOutlineStatusLineManager : getActionBars().getStatusLineManager();
		final IStatusLineManager statusLineManager = getActionBarContributor().getActionBars().getStatusLineManager();
		
		if (statusLineManager != null) {
			if (selection instanceof IStructuredSelection) {
				final Collection<?> collection = ((IStructuredSelection)selection).toList();
				switch (collection.size()) {
					case 0: {
						statusLineManager.setMessage(""); //$NON-NLS-1$
						break;
					}
					case 1: {
						final String text = new AdapterFactoryItemDelegator(getAdapterFactory()).getText(collection.iterator().next());
						statusLineManager.setMessage(text);
						break;
					}
					default: {
						statusLineManager.setMessage(NLS.bind(Messages.EFEditor_MultiObjectSelected_message,
								Integer.toString(collection.size() )));
						break;
					}
				}
			}
			else {
				statusLineManager.setMessage(""); //$NON-NLS-1$
			}
		}
	}
	
	
	/**
	 * If there is just one page in the multi-page editor part,
	 * this hides the single tab at the bottom.
	 * @generated
	 */
	protected void hideTabs() {
		if (getPageCount() <= 1) {
//			setPageText(0, ""); //$NON-NLS-1$
			if (getContainer() instanceof CTabFolder) {
				((CTabFolder) getContainer()).setTabHeight(1);
				final Point point = getContainer().getSize();
				getContainer().setSize(point.x, point.y + 6);
			}
		}
	}
	
	/**
	 * If there is more than one page in the multi-page editor part,
	 * this shows the tabs at the bottom.
	 * @generated
	 */
	protected void showTabs() {
		if (getPageCount() > 1) {
//			setPageText(0, Messages.RTaskEditor_FirstPage_label);
			if (getContainer() instanceof CTabFolder) {
				((CTabFolder) getContainer()).setTabHeight(SWT.DEFAULT);
				final Point point = getContainer().getSize();
				getContainer().setSize(point.x, point.y - 6);
			}
		}
	}
	
	protected IContentOutlinePage getContentOutlinePage() {
		if (fContentOutlinePage == null) {
			fContentOutlinePage = createContentOutlinePage();
		}
		return fContentOutlinePage;
	}
	
	protected IContentOutlinePage createContentOutlinePage() {
		return null;
	}
	
	protected IPropertySheetPage getPropertySheetPage() {
		final IPropertySheetPage page = createPropertySheetPage();
		fPropertySheetPages.add(page);
		return page;
	}
	
	protected IPropertySheetPage createPropertySheetPage() {
		return new EFPropertySheetPage(this);
	}
	
	EditorSelectionProvider getSelectionProvider() {
		return fSelectionProvider;
	}
	
	@Override
	protected void pageChange(final int newPageIndex) {
		fSelectionProvider.update();
		super.pageChange(newPageIndex);
	}
	
	/**
	 * This is here for the listener to be able to call it.
	 * @generated
	 */
	@Override
	protected void firePropertyChange(final int action) {
		super.firePropertyChange(action);
	}
	
	protected abstract void operationFailed(String string, Exception e);
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(final Class required) {
		if (Control.class.equals(required)) {
			return getContainer();
		}
		if (IContentOutlinePage.class.equals(required)) {
			return getContentOutlinePage();
		}
		if (IPropertySheetPage.class.equals(required)) {
			return getPropertySheetPage();
		}
		if (IGotoMarker.class.equals(required)) {
			return this;
		}
		return super.getAdapter(required);
	}
	
	
	void onPropertySheetDisposed(final IPropertySheetPage page) {
		fPropertySheetPages.remove(page);
	}
	
	/**
	 * @generated
	 */
	@Override
	public void dispose() {
		fUpdateProblemIndication = false;
		
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(fResourceListener);
		
		getSite().getPage().removePartListener(fPartListener);
		
		fAdapterFactory.dispose();
		
		if (getActionBarContributor().getActiveEditor() == this) {
			getActionBarContributor().setActiveEditor(null);
		}
		
		for (final IPropertySheetPage page : fPropertySheetPages) {
			page.dispose();
		}
		fPropertySheetPages.clear();
		
		if (fContentOutlinePage != null) {
			fContentOutlinePage.dispose();
			fContentOutlinePage = null;
		}
		if (fHandlers != null) {
			fHandlers.dispose();
			fHandlers = null;
		}
		
		super.dispose();
	}
	
}
