/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommon.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import de.walware.eclipsecommon.internal.ui.Messages;


public class ResourceSelectionDialog extends SelectionStatusDialog {
    
	// constants
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 500;
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 300;

	// the root element to populate the viewer with
	private IContainer fRoot;
	private boolean fAllowNew = false;

    // the visual selection widget group
    private TreeAndListGroup fSelectionGroup;
    private IFile fSelectedResource;
    private Text fTextField;
    private boolean fIgnoreTextFieldModifications = false;


    /**
     * Creates a resource selection dialog rooted at the given element.
     *
     * @param parentShell the parent shell
     * @param rootElement the root element to populate this dialog with
     * @param message the message to be displayed at the top of this dialog, or
     *    <code>null</code> to display a default message
     */
    public ResourceSelectionDialog(Shell parentShell, String message) {
        super(parentShell);
        
        setTitle(Messages.ResourceSelectionDialog_title);
        if (message != null) {
			setMessage(message);
		} else {
			setMessage(Messages.ResourceSelectionDialog_message);
		}
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }
    
    public void setAllowNewResources(boolean allowNew) {
    	
    	fAllowNew = allowNew;
    }
    
    public void setRootElement(IContainer rootElement) {
    	
    	fRoot = rootElement;
    }
    
    public void addFileFilter(String label, String fileNamePattern) {
    	
    	// TODO (enh) add filter
    }

    /**
     * Visually checks the previously-specified elements in the container (left)
     * portion of this dialog's resource selection viewer.
     */
    private void checkInitialSelections() {
    	
        List initial = getInitialElementSelections();
        if (!initial.isEmpty()) {
        	Object o = initial.get(0);
        	if (o instanceof IFile) {
        		fSelectionGroup.selectListElement(o);
        	}
        	else {
        		fSelectionGroup.selectTreeElement(o);
        	}
        }
    }

    @Override
    public void create() {
        super.create();
        initDialog();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
    	
    	// check settings
    	if (fRoot == null) {
    		fRoot = ResourcesPlugin.getWorkspace().getRoot();
    	}

    	// page group
        Composite composite = (Composite) super.createDialogArea(parent);

        createMessageArea(composite);
        fSelectionGroup = new TreeAndListGroup(composite, fRoot,
                getResourceProvider(IResource.FOLDER | IResource.PROJECT | IResource.ROOT), 
                WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(),
                getResourceProvider(IResource.FILE), 
                WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(), false);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
        gd.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
        fSelectionGroup.getControl().setLayoutData(gd);
        
        fTextField = new Text(composite, SWT.BORDER | SWT.SINGLE);
        fTextField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		fSelectionGroup.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IResource selection = (IResource) 
						((IStructuredSelection) event.getSelection()).getFirstElement();
    			if (selection != null) {
    				setResource(selection);
    				validate();
    			}
    		}
    	});
    	fSelectionGroup.addDoubleClickListener(new IDoubleClickListener() {
    		public void doubleClick(DoubleClickEvent event) {
    			okPressed();
    		}
    	});
    	fTextField.addModifyListener(new ModifyListener() {
    		public void modifyText(ModifyEvent e) {
    			if (!fIgnoreTextFieldModifications) {
	    			setResource(fTextField.getText());
	    			validate();
    			}
    		}
    	});
        
        return composite;
	}

    /**
     * Returns a content provider for <code>IResource</code>s that returns 
     * only children of the given resource type.
     */
    private ITreeContentProvider getResourceProvider(final int resourceType) {
    	
        return new WorkbenchContentProvider() {
            public Object[] getChildren(Object o) {
            	try {
            		if (o instanceof IContainer) {
	                    IResource[] members = ((IContainer) o).members();
	
	                    //filter out the desired resource types
	                    ArrayList<IResource> results = new ArrayList<IResource>(members.length);
	                    for (int i = 0; i < members.length; i++) {
	                        if (((members[i].getType() & resourceType) != 0)
	                        		&& members[i].isAccessible() ) {
	                            results.add(members[i]);
	                        }
	                    }
	                    return results.toArray();
            		}
            	} catch (CoreException e) {
            	}
                return new Object[0];
            }
        };
    }

    /**
     * Initializes this dialog's controls.
     */
    private void initDialog() {
    	
    	fSelectionGroup.initFields();
		checkInitialSelections();
		validate();
    }

    protected void validate() {
    	
    	boolean isValid = false;
    	if (fSelectedResource != null) {
    		IPath path = fSelectedResource.getFullPath();
    		IStatus test = ResourcesPlugin.getWorkspace().validatePath(path.toString(), IResource.FILE);
			isValid = (test.getCode() == IStatus.OK && fRoot.getFullPath().isPrefixOf(path)
						&& (fAllowNew || fSelectedResource.exists()) );
    	}
    	getOkButton().setEnabled(isValid);
    }
    
    private void setResource(String resourcePath) {
    	
    	String path = resourcePath.trim();
		if (path != null && path.length() > 0 && path.charAt(0) != '/') {
			path = '/'+path;
		}
		try {
	    	IFile resource = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
    		fSelectedResource = resource;
		}
		catch (IllegalArgumentException e){
			fSelectedResource = null;
		}
    }
    
    private void setResource(IResource resource) {
    	
    	String path = "";
    	if (resource != null) {
    		path = resource.getFullPath().makeRelative().toString();
    	}
    	if (resource instanceof IFile) {
    		fSelectedResource = (IFile) resource;
    	} 
    	else {
    		fSelectedResource = null;
    	}
    	fIgnoreTextFieldModifications = true;
    	fTextField.setText(path);
    	fIgnoreTextFieldModifications = false;
	}
    
    @Override
    protected void computeResult() {
    	
        setSelectionResult(new IResource[] { fSelectedResource });
    }
}
