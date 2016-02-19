/*=============================================================================#
 # Copyright (c) 2005-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import de.walware.ecommons.ui.SharedMessages;
import de.walware.ecommons.ui.SharedUIResources;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.workbench.ContainerContentProvider;


/**
 * Workbench-level composite for choosing a container.
 */
public class RProjectContainerComposite extends Composite {
	
	
	private class CollapseAllAction extends Action {
		
		CollapseAllAction() {
			super();
			setText(SharedMessages.CollapseAllAction_label); 
			setDescription(SharedMessages.CollapseAllAction_description); 
			setToolTipText(SharedMessages.CollapseAllAction_tooltip); 
			setImageDescriptor(SharedUIResources.getImages().getDescriptor(SharedUIResources.LOCTOOL_COLLAPSEALL_IMAGE_ID));
		}
		
		@Override
		public void run() {
			fTreeViewer.collapseAll();
		}
		
	}
	
	private class ExpandAllAction extends Action {
		
		ExpandAllAction() {
			super();
			setText(SharedMessages.ExpandAllAction_label); 
			setDescription(SharedMessages.ExpandAllAction_description); 
			setToolTipText(SharedMessages.ExpandAllAction_tooltip); 
			setImageDescriptor(SharedUIResources.getImages().getDescriptor(SharedUIResources.LOCTOOL_EXPANDALL_IMAGE_ID));
		}
		
		@Override
		public void run() {
			fTreeViewer.expandAll();
		}
		
	}
	
	private class RLabelProvider extends LabelProvider {
		
		private final WorkbenchLabelProvider fSuper;
		
		public RLabelProvider() {
			fSuper = new WorkbenchLabelProvider();
		}
		
		@Override
		public Image getImage(final Object element) {
			return fSuper.getImage(element);
		}
		
		@Override
		public String getText(final Object element) {
			String text = fSuper.getText(element);
			if (fBaseContainer != null && fBaseContainer.equals(element)) {
				text = text + " (BASE)";
			}
			return text;
		}
		
	}
	
	// sizing constants
	private static final int SIZING_SELECTION_PANE_WIDTH = 320;
	private static final int SIZING_SELECTION_PANE_HEIGHT = 300;
	
	
	private final IProject fProject;
	private IContainer fBaseContainer;
	
	private TreeViewer fTreeViewer;
	private ToolBarManager fRightToolBarMgr;
	
	
	/**
	 * Creates a new instance of the widget.
	 * 
	 * @param parent The parent widget of the group.
	 * @param listener A listener to forward events to.  Can be null if
	 *     no listener is required.
	 * @param allowNewContainerName Enable the user to type in a new container
	 *     name instead of just selecting from the existing ones.
	 * @param message The text to present to the user.
	 * @param showClosedProjects Whether or not to show closed projects.
	 * @param heightHint height hint for the drill down composite
	 */
	public RProjectContainerComposite(final Composite parent, 
			final IProject project) {
		super(parent, SWT.NONE);
		fProject = project;
		createContents();
	}
	
	/**
	 * Creates the contents of the composite.
	 * 
	 * @param heightHint height hint for the drill down composite
	 */
	public void createContents() {
		setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 1));
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		createTreeViewer();
		Dialog.applyDialogFont(this);
	}
	
	/**
	 * Returns a new drill down viewer for this dialog.
	 * 
	 * @param heightHint height hint for the drill down composite
	 * @return a new drill down viewer
	 */
	protected void createTreeViewer() {
		// Create group with dril down toolbar and tree.
		final Composite treeGroup = new Composite(this, SWT.BORDER);
		
		final GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = layout.verticalSpacing = layout.horizontalSpacing = 0;
		layout.numColumns = 2;
		treeGroup.setLayout(layout);
		
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = SIZING_SELECTION_PANE_WIDTH;
		gd.heightHint = SIZING_SELECTION_PANE_HEIGHT;
		treeGroup.setLayoutData(gd);
		
		final ToolBar toolBar = new ToolBar(treeGroup, SWT.FLAT);
		toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1));
		fRightToolBarMgr = new ToolBarManager(toolBar);
		
		final Label filler = new Label(treeGroup, SWT.LEFT );
		gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		gd.heightHint = 2;
		filler.setLayoutData(gd);
		
		// Create tree viewer
		fTreeViewer = new TreeViewer(treeGroup, SWT.NONE);
		fTreeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		fRightToolBarMgr.add(new CollapseAllAction());
		fRightToolBarMgr.add(new ExpandAllAction());
		
		fRightToolBarMgr.update(true);
		
		// layout group
		treeGroup.layout();
		
		final ContainerContentProvider cp = new ContainerContentProvider();
		fTreeViewer.setContentProvider(cp);
		fTreeViewer.setLabelProvider(new RLabelProvider());
		fTreeViewer.setSorter(new ViewerSorter());
//		fTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
//			public void selectionChanged(final SelectionChangedEvent event) {
//				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
//				containerSelectionChanged((IContainer) selection.getFirstElement()); // allow null
//			}
//		});
		fTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(final DoubleClickEvent event) {
				final ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					final Object item = ((IStructuredSelection) selection).getFirstElement();
					if(item == null) {
						return;
					}
					if (fTreeViewer.getExpandedState(item)) {
						fTreeViewer.collapseToLevel(item, 1);
					}
					else {
						fTreeViewer.expandToLevel(item, 1);
					}
				}
			}
		});
		
		// This has to be done after the viewer has been laid out
		fTreeViewer.setInput(new IContainer[] { fProject });
		
		fTreeViewer.expandToLevel(2);
		if (fBaseContainer != null) {
			fTreeViewer.expandToLevel(fBaseContainer, 1);
		}
	}
	
	/**
	 * Gives focus to one of the widgets in the group, as determined by the group.
	 */
	public void setInitialFocus() {
		fTreeViewer.getTree().setFocus();
	}
	
	public void setBaseContainer(final IPath path) {
		if (path == null) {
			fBaseContainer = null;
		}
		else if (path.segmentCount() == 0) {
			fBaseContainer = fProject;
		}
		else {
			fBaseContainer = fProject.getFolder(path);
		}
		fTreeViewer.refresh(true);
	}
	
	public void toggleBaseContainer() {
		final Object element = ((IStructuredSelection) fTreeViewer.getSelection()).getFirstElement();
		if (element instanceof IContainer) {
			if (element != null && !element.equals(fBaseContainer)) {
				fBaseContainer = (IContainer) element;
			}
			else {
				fBaseContainer = null;
			}
		}
		else {
			fBaseContainer = null;
		}
		fTreeViewer.refresh(true);
	}
	
	public IPath getBaseContainer() {
		if (fBaseContainer == null) {
			return null;
		}
		return fBaseContainer.getProjectRelativePath();
	}
	
	
//	public static IStatus validate(IPath path) {
//		// validate Container
//		if (path == null || path.isEmpty()) {
//			return new StatusInfo(IStatus.ERROR, Messages.ContainerSelectionControl_error_FolderEmpty);
//		}
//		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
//		final String projectName = path.segment(0);
//		if (projectName == null || !root.getProject(projectName).exists())
//			return new StatusInfo(IStatus.ERROR, Messages.ContainerSelectionControl_error_ProjectNotExists);
//		//path is invalid if any prefix is occupied by a file
//		while (path.segmentCount() > 1) {
//			if (root.getFile(path).exists()) {
//				return new StatusInfo(IStatus.ERROR, NLS.bind(
//						Messages.ContainerSelectionControl_error_PathOccupied, 
//						path.makeRelative() ));
//			}
//			path = path.removeLastSegments(1);
//		}
//		return new StatusInfo();
//	}
	
}
