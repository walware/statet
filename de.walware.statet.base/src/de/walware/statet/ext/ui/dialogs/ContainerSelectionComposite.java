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

package de.walware.statet.ext.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.DrillDownAdapter;

import de.walware.eclipsecommons.ui.dialogs.StatusInfo;
import de.walware.statet.ui.StatetImages;


/**
 * Workbench-level composite for choosing a container.
 */
public class ContainerSelectionComposite extends Composite {
	

   	public static abstract class ContainerFilter extends ViewerFilter {
		
		IPath fExcludePath; 
		
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			
			if (!(element instanceof IContainer))
				return true; // never
			
			IContainer container = (IContainer) element;
			if (fExcludePath != null) {
				if (container.getFullPath().isPrefixOf(fExcludePath))
					return true;
			}
			
			return select(container);
		}
		
		public abstract boolean select(IContainer container);
	}

   	
   	private class CollapseAllAction extends Action {
		
		CollapseAllAction() {
			super();
			setText(StatetDialogsMessages.CollapseAllAction_label); 
			setDescription(StatetDialogsMessages.CollapseAllAction_description); 
			setToolTipText(StatetDialogsMessages.CollapseAllAction_tooltip); 
			setImageDescriptor(StatetImages.DESC_LOCTOOL_COLLAPSEALL);
		}
	 
		@Override
		public void run() {
			
			fTreeViewer.collapseAll();
		}
	}
	
   	private class ExpandAllAction extends Action {
		
   		ExpandAllAction() {
			super();
			setText(StatetDialogsMessages.ExpandAllAction_label); 
			setDescription(StatetDialogsMessages.ExpandAllAction_description); 
			setToolTipText(StatetDialogsMessages.ExpandAllAction_tooltip); 
			setImageDescriptor(StatetImages.DESC_LOCTOOL_EXPANDALL);
		}
	 
		@Override
		public void run() {
			
			fTreeViewer.expandAll();
		}
	}

   	private class ToggleFilterAction extends Action {
		
		ToggleFilterAction() {
			super();
			setText(StatetDialogsMessages.FilterFavouredContainersAction_label); 
			setDescription(StatetDialogsMessages.FilterFavouredContainersAction_description); 
			setToolTipText(StatetDialogsMessages.FilterFavouredContainersAction_tooltip); 
			setImageDescriptor(StatetImages.DESC_LOCTOOL_FILTER);
			setDisabledImageDescriptor(StatetImages.DESC_LOCTOOLD_FILTER);
			setChecked(false);
		}
		
		@Override
		public void run() {
			
			boolean enable = isChecked();
			doToggleFilter(enable);
			fIsToggleFilterActivated = enable;
		}
		
		void doToggleFilter(boolean enable) {
			
			if (enable) {
				fTreeViewer.addFilter(fToggledFilter);
			} else {
				fTreeViewer.removeFilter(fToggledFilter);
			}
		}
	}

	
	// sizing constants
	private static final int SIZING_SELECTION_PANE_WIDTH = 320;
//	private static final int SIZING_SELECTION_PANE_HEIGHT = 300;

	
	// Enable user to type in new container name
    private boolean fAllowNewContainerName = true;

    // show all projects by default
    private boolean fShowClosedProjects = true;

    // Last selection made by user
    private IContainer fSelectedContainer;

    // handle on parts
    private Text fContainerNameField;
    private TreeViewer fTreeViewer;
    private ToolBarManager fRightToolBarMgr;
    
    private boolean fIsToggleFilterActivated;
	private ContainerFilter fToggledFilter;

    // The listener to notify of events
    private Listener fListener;


//    /**
//     * Creates a new instance of the widget.
//     *
//     * @param parent The parent widget of the group.
//     * @param listener A listener to forward events to. Can be null if
//     *	 no listener is required.
//     * @param allowNewContainerName Enable the user to type in a new container
//     *  name instead of just selecting from the existing ones.
//     */
//    public ContainerSelectionControl(Composite parent, Listener listener, boolean allowNewContainerName) {
//    	
//        this(parent, listener, allowNewContainerName, null);
//    }
//
//    /**
//     * Creates a new instance of the widget.
//     *
//     * @param parent The parent widget of the group.
//     * @param listener A listener to forward events to.  Can be null if
//     *	 no listener is required.
//     * @param allowNewContainerName Enable the user to type in a new container
//     *  name instead of just selecting from the existing ones.
//     * @param message The text to present to the user.
//     */
//    public ContainerSelectionControl(Composite parent, Listener listener, boolean allowNewContainerName, 
//    		String message) {
//    	
//        this(parent, listener, allowNewContainerName, message, true);
//    }
//
//    /**
//     * Creates a new instance of the widget.
//     *
//     * @param parent The parent widget of the group.
//     * @param listener A listener to forward events to.  Can be null if
//     *	 no listener is required.
//     * @param allowNewContainerName Enable the user to type in a new container
//     *  name instead of just selecting from the existing ones.
//     * @param message The text to present to the user.
//     * @param showClosedProjects Whether or not to show closed projects.
//     */
//    public ContainerSelectionControl(Composite parent, Listener listener, boolean allowNewContainerName, 
//    		String message, boolean showClosedProjects) {
//    	
//        this(parent, listener, allowNewContainerName, message,
//                showClosedProjects, SIZING_SELECTION_PANE_HEIGHT);
//    }

    /**
     * Creates a new instance of the widget.
     *
     * @param parent The parent widget of the group.
     * @param listener A listener to forward events to.  Can be null if
     *	 no listener is required.
     * @param allowNewContainerName Enable the user to type in a new container
     *  name instead of just selecting from the existing ones.
     * @param message The text to present to the user.
     * @param showClosedProjects Whether or not to show closed projects.
     * @param heightHint height hint for the drill down composite
     */
    public ContainerSelectionComposite(Composite parent, 
    		boolean allowNewContainerName, boolean showClosedProjects, String message, int heightHint) {
    	
        super(parent, SWT.NONE);
        fAllowNewContainerName = allowNewContainerName;
        fShowClosedProjects = showClosedProjects;
        if (message == null) {
        	if (allowNewContainerName)
                message = StatetDialogsMessages.ContainerSelectionControl_label_EnterOrSelectFolder;
            else
                message = StatetDialogsMessages.ContainerSelectionControl_label_SelectFolder;
        }
        createContents(message, heightHint);
    }

    /**
     * Creates the contents of the composite.
     * 
     * @param heightHint height hint for the drill down composite
     */
    public void createContents(String message, int heightHint) {
    	
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        setLayout(layout);
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label label = new Label(this, SWT.WRAP);
        label.setText(message);

        if (fAllowNewContainerName) {
            fContainerNameField = new Text(this, SWT.SINGLE | SWT.BORDER);
            fContainerNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            fContainerNameField.setFont(this.getFont());
        } else {
            // filler...
            new Label(this, SWT.NONE); // ben�tigt?
        }

        createTreeViewer(heightHint);
        Dialog.applyDialogFont(this);
    }

    /**
     * Returns a new drill down viewer for this dialog.
     *
     * @param heightHint height hint for the drill down composite
     * @return a new drill down viewer
     */
    protected void createTreeViewer(int heightHint) {
        // Create group with dril down toolbar and tree.
        Composite treeGroup = new Composite(this, SWT.BORDER);

        GridLayout layout = new GridLayout();
        layout.marginHeight = layout.marginWidth = layout.verticalSpacing = layout.horizontalSpacing = 0;
        layout.numColumns = 2;
        treeGroup.setLayout(layout);

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = SIZING_SELECTION_PANE_WIDTH;
        gd.heightHint = heightHint;
        treeGroup.setLayoutData(gd);

		// Create a toolbars.
        ToolBarManager leftToolBarMgr = new ToolBarManager(SWT.FLAT);
        ToolBar toolBar = leftToolBarMgr.createControl(treeGroup);
        toolBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

        toolBar = new ToolBar(treeGroup, SWT.FLAT);
        toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1));
        fRightToolBarMgr = new ToolBarManager(toolBar);
        
		Label filler = new Label(treeGroup, SWT.LEFT );
		gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		gd.heightHint = 2;
		filler.setLayoutData(gd);

        // Create tree viewer
        fTreeViewer = new TreeViewer(treeGroup, SWT.NONE);
        fTreeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        // Fill toolbars
        DrillDownAdapter adapter = new DrillDownAdapter(fTreeViewer);
        adapter.addNavigationActions(leftToolBarMgr);
        
        fRightToolBarMgr.add(new CollapseAllAction());
        fRightToolBarMgr.add(new ExpandAllAction());

        leftToolBarMgr.update(true);
        fRightToolBarMgr.update(true);

        // layout group
        treeGroup.layout();
        
        ContainerContentProvider cp = new ContainerContentProvider();
        cp.showClosedProjects(fShowClosedProjects);
        fTreeViewer.setContentProvider(cp);
        fTreeViewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
        fTreeViewer.setSorter(new ViewerSorter());
        fTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                containerSelectionChanged((IContainer) selection.getFirstElement()); // allow null
            }
        });
        fTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    Object item = ((IStructuredSelection) selection).getFirstElement();
                    if(item == null)
                    	return;
                    if (fTreeViewer.getExpandedState(item))
                        fTreeViewer.collapseToLevel(item, 1);
                    else
                        fTreeViewer.expandToLevel(item, 1);
                }
            }
        });

        // This has to be done after the viewer has been laid out
        fTreeViewer.setInput(ResourcesPlugin.getWorkspace());
    }

    public void setToggleFilter(ContainerFilter filter, boolean initialEnabled) {
    	
    	fToggledFilter = filter;
    	fIsToggleFilterActivated = initialEnabled;
   		ToggleFilterAction action = new ToggleFilterAction();
   		fRightToolBarMgr.add(new Separator());
    	fRightToolBarMgr.add(action);

    	// check and init
    	action.doToggleFilter(true);
    	if (fTreeViewer.getTree().getItemCount() == 0) {
    		action.doToggleFilter(false);
    		action.setChecked(false);
    		action.setEnabled(false);
    	} 
    	else {
    		action.setChecked(initialEnabled);
    		if (!initialEnabled)
    			action.doToggleFilter(false);
    	}
    	
    	fRightToolBarMgr.update(true);
    	fRightToolBarMgr.getControl().getParent().layout();
    }
    
    public boolean getToggleFilterSetting() {
    	
    	return fIsToggleFilterActivated;
    }

    /**
     * Listener will be notified, if container selection changed.
     * 
     * @param listener
     */
    public void setListener(Listener listener) {
    	
    	fListener = listener;
    	if (fContainerNameField != null)
            fContainerNameField.addListener(SWT.Modify, fListener);
    }
    
    /**
     * Gives focus to one of the widgets in the group, as determined by the group.
     */
    public void setInitialFocus() {
    	
        if (fAllowNewContainerName)
            fContainerNameField.setFocus();
        else
            fTreeViewer.getTree().setFocus();
    }

    /**
     * The container selection has changed in the
     * tree view. Update the container name field
     * value and notify all listeners.
     */
    public void containerSelectionChanged(IContainer container) {
    	
        if (fAllowNewContainerName) {
            if (container != null) {
            	fSelectedContainer = container;
            	fContainerNameField.setText(container.getFullPath().makeRelative().toString());
            }
        } else {
        	fSelectedContainer = container;
	        // fire an event so the parent can update its controls
	        if (fListener != null) {
	            Event changeEvent = new Event();
	            changeEvent.type = SWT.Selection;
	            changeEvent.widget = this;
	            fListener.handleEvent(changeEvent);
	        }
        }
    }


    /**
     * Returns the currently entered container name.
     * <p>
     * Note that the container may not exist yet if the user
     * entered a new container name in the field.
     * 
     * @return Path of Container, <code>null</code> if the field is empty.
     */
    public IPath getContainerFullPath() {
    	
        if (fAllowNewContainerName) {
            String pathName = fContainerNameField.getText();
            if (pathName == null || pathName.length() < 1)
                return null;
            else
                //The user may not have made this absolute so do it for them
                return (new Path(pathName)).makeAbsolute();
        } else {
            if (fSelectedContainer == null)
                return null;
            else
                return fSelectedContainer.getFullPath();
        }
    }

    /**
     * Sets the selected existing container.
     */
    public void selectContainer(IContainer container) {
    	
        fSelectedContainer = container;

        //expand to and select the specified container
        List<IContainer> itemsToExpand = new ArrayList<IContainer>();
        IContainer parent = container.getParent();
        while (parent != null) {
            itemsToExpand.add(0, parent);
            parent = parent.getParent();
        }
        // update filter
    	if (fToggledFilter != null) {
    		fToggledFilter.fExcludePath = container.getFullPath();
    		fTreeViewer.refresh();
    	}
    	// update selection
        fTreeViewer.setExpandedElements(itemsToExpand.toArray());
        fTreeViewer.setSelection(new StructuredSelection(container), true);
    }
    
    /**
     * Sets the value of this page's container.
     *
     * @param path Full path to the container.
     */
    public void selectContainer(IPath path) {
    	
        IResource initial = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
        if (initial != null) {
            if (!(initial instanceof IContainer)) {
                initial = initial.getParent();
            }
            selectContainer((IContainer) initial);
        }
    }

    
    public static IStatus validate(IPath path) {
        // validate Container
        if (path == null || path.isEmpty()) {
        	return new StatusInfo(IStatus.ERROR, StatetDialogsMessages.ContainerSelectionControl_error_FolderEmpty);
        }
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        String projectName = path.segment(0);
        if (projectName == null || !root.getProject(projectName).exists())
        	return new StatusInfo(IStatus.ERROR, StatetDialogsMessages.ContainerSelectionControl_error_ProjectNotExists);
        //path is invalid if any prefix is occupied by a file
        while (path.segmentCount() > 1) {
        	if (root.getFile(path).exists()) {
        		return new StatusInfo(IStatus.ERROR, NLS.bind(
        				StatetDialogsMessages.ContainerSelectionControl_error_PathOccupied, 
        				path.makeRelative() ));
        	}
        	path = path.removeLastSegments(1);
        }
        return new StatusInfo();
    }
    
}
