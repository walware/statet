/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.rtm.base.ui.editors;

import static de.walware.statet.rtm.base.ui.RtModelUIPlugin.RUN_R_TASK_COMMAND_ID;

import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.emf.ui.forms.EFEditor;
import de.walware.ecommons.ui.actions.HandlerCollection;
import de.walware.ecommons.ui.actions.HandlerContributionItem;

import de.walware.statet.rtm.base.internal.ui.actions.RunRTaskHandler;
import de.walware.statet.rtm.base.internal.ui.editors.RCodeGenEditorInput;
import de.walware.statet.rtm.base.internal.ui.editors.RTaskGenManager;
import de.walware.statet.rtm.base.ui.IRtDescriptor;
import de.walware.statet.rtm.base.ui.RTaskSnippet;
import de.walware.statet.rtm.base.ui.RtModelUIPlugin;


public abstract class RTaskEditor extends EFEditor {
	
	
	public static final String MAIN_PAGE_ID = "Main"; //$NON-NLS-1$
	
	
	private final RTaskGenManager fRCodeGenAdapter;
	
	
	/**
	 * This creates a model editor.
	 * 
	 * @param descriptor 
	 */
	public RTaskEditor(final IRtDescriptor descriptor) {
		super(descriptor);
		
		fRCodeGenAdapter = new RTaskGenManager(this);
		
		getEditingDomain().getResourceSet().eAdapters().add(fRCodeGenAdapter);
	}
	
	
	@Override
	public IRtDescriptor getModelDescriptor() {
		return (IRtDescriptor) super.getModelDescriptor();
	}
	
	@Override
	protected RtFormToolkit createToolkit(final Display display) {
		return new RtFormToolkit(RtModelUIPlugin.getDefault().getFormColors(display));
	}
	
	/**
	 * This is the method used by the framework to install your own controls.
	 * @generated
	 */
	@Override
	public void addPages() {
		// Only creates the other pages if there is something that can be edited
		//
		if (!getEditingDomain().getResourceSet().getResources().isEmpty()) {
			try {
				// Create a page for the selection tree view.
//				{	final ViewerPane viewerPane = new ViewerPane(getSite().getPage(), AbstractRTaskEditor.this) {
//						@Override
//						public Viewer createViewer(final Composite composite) {
//							final Tree tree = new Tree(composite, SWT.MULTI);
//							final TreeViewer newTreeViewer = new TreeViewer(tree);
//							return newTreeViewer;
//						}
//						@Override
//						public void requestActivation() {
//							super.requestActivation();
//							setCurrentViewerPane(this);
//						}
//					};
//					viewerPane.createControl(getContainer());
//					
//					fSelectionViewer = (TreeViewer)viewerPane.getViewer();
//					fSelectionViewer.setContentProvider(new AdapterFactoryContentProvider(fAdapterFactory));
//					
//					fSelectionViewer.setLabelProvider(new AdapterFactoryLabelProvider(fAdapterFactory));
//					fSelectionViewer.setInput(fEditingDomain.getResourceSet());
//					fSelectionViewer.setSelection(new StructuredSelection(fEditingDomain.getResourceSet().getResources().get(0)), true);
//					viewerPane.setTitle(fEditingDomain.getResourceSet());
//					
//					new AdapterFactoryTreeEditor(fSelectionViewer.getTree(), fAdapterFactory);
//					
//					createContextMenuFor(fSelectionViewer);
//					final int pageIndex = addPage(viewerPane.getControl());
//					setPageText(pageIndex, Messages.RTaskEditor_FirstPage_label);
//				}
				addFormPages();
				
				final RCodePage rCodePage = new RCodePage(this);
				
				addPage(rCodePage, new RCodeGenEditorInput(fRCodeGenAdapter.getCodeFragment()));
			}
			catch (final PartInitException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, RtModelUIPlugin.PLUGIN_ID, 0,
						"An error occured when creating the pages for the R task editor.", e ));
			}
			
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (getPageCount() > 0) {
						setActivePage(0);
					}
				}
			});
		}
	}
	
	protected abstract void addFormPages() throws PartInitException;
	
	/**
	 * This is used to track the active viewer.
	 * @generated
	 */
	@Override
	protected void pageChange(final int pageIndex) {
		super.pageChange(pageIndex);
		
//		if (fContentOutlinePage != null) {
//			handleContentOutlineSelection(fContentOutlinePage.getSelection());
//		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(final Class required) {
		if (RTaskSnippet.class.equals(required)) {
			return fRCodeGenAdapter.getRTaskSnippet();
		}
		return super.getAdapter(required);
	}
	
//	@Override
//	public IContentOutlinePage createContentOutlinePage() {
//		final EFRawOutlinePage page = new EFRawOutlinePage(this);
		
		// Listen to selection so that we can handle it is a special way
//		page.addSelectionChangedListener(new ISelectionChangedListener() {
//			// This ensures that we handle selections correctly
//			@Override
//			public void selectionChanged(final SelectionChangedEvent event) {
//				handleContentOutlineSelection(event.getSelection());
//			}
//		});
//		
//		return page;
//	}
	
	/**
	 * This deals with how we want selection in the outliner to affect the other views.
	 * @generated
	 */
//	public void handleContentOutlineSelection(final ISelection selection) {
//		if (fCurrentViewerPane != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
//			final Iterator<?> selectedElements = ((IStructuredSelection)selection).iterator();
//			if (selectedElements.hasNext()) {
//				// Get the first selected element
//				final Object selectedElement = selectedElements.next();
//				
//				// If it's the selection viewer, then we want it to select the same selection as this selection
//				if (fCurrentViewerPane.getViewer() == fSelectionViewer) {
//					final ArrayList<Object> selectionList = new ArrayList<Object>();
//					selectionList.add(selectedElement);
//					while (selectedElements.hasNext()) {
//						selectionList.add(selectedElements.next());
//					}
//					
//					// Set the selection to the widget
//					fSelectionViewer.setSelection(new StructuredSelection(selectionList));
//				}
//				else {
//					// Set the input to the widget
//					if (fCurrentViewerPane.getViewer().getInput() != selectedElement) {
//						fCurrentViewerPane.getViewer().setInput(selectedElement);
//						fCurrentViewerPane.setTitle(selectedElement);
//					}
//				}
//			}
//		}
//	}
	
	@Override
	protected void operationFailed(final String operation, final Exception e) {
		StatusManager.getManager().handle(new Status(IStatus.ERROR, RtModelUIPlugin.PLUGIN_ID, 0,
				NLS.bind("An error occurred when '{0}' in R task editor.", operation),
				e ));
	}
	
	@Override
	protected void createActions(final IServiceLocator locator, final HandlerCollection handlers) {
		super.createActions(locator, handlers);
		
		final IContextService contextService = (IContextService) locator.getService(IContextService.class);
		contextService.activateContext(RtModelUIPlugin.R_TASK_EDITOR_CONTEXT_ID);
		
		final IHandlerService handlerService = (IHandlerService) locator.getService(IHandlerService.class);
		{	final IHandler2 handler = new RunRTaskHandler(this);
			handlers.add(RUN_R_TASK_COMMAND_ID, handler);
			handlerService.activateHandler(RUN_R_TASK_COMMAND_ID, handler);
		}
	}
	
	@Override
	protected void contributeToPages(final IToolBarManager manager) {
		super.contributeToPages(manager);
		
		{	final IHandler2 handler = fHandlers.get(RUN_R_TASK_COMMAND_ID);
			manager.add(new HandlerContributionItem(new CommandContributionItemParameter(getSite(),
					null, RUN_R_TASK_COMMAND_ID, HandlerContributionItem.STYLE_PUSH ),
					handler ));
		}
	}
	
}
