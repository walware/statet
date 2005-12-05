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

import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import de.walware.eclipsecommon.ui.dialogs.Layouter;
import de.walware.eclipsecommon.ui.dialogs.StatusInfo;
import de.walware.eclipsecommon.ui.dialogs.StatusUtil;
import de.walware.statet.base.StatetProject;
import de.walware.statet.ext.ui.dialogs.ContainerSelectionComposite;
import de.walware.statet.ext.ui.dialogs.StatetDialogsMessages;


/**
 * Standard main page for a wizard that creates a file resource.
 * <p>
 * This page may be used by clients as-is; it may be also be subclassed to suit.
 * </p>
 * <p>
 * Subclasses may override
 * <ul>
 *   <li><code>getInitialContents</code></li>
 *   <li><code>getNewFileLabel</code></li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may extend
 * <ul>
 *   <li><code>handleEvent</code></li>
 * </ul>
 * </p>
 */
public abstract class NewElementWizardPage extends WizardPage {
	
    public class ResourceGroup implements Listener {
    	
    	private static final int SIZING_CONTAINER_GROUP_HEIGHT = 250;
    	private static final String DIALOGSETTING_ENABLEFILTER = "de.walware.statet.NewElementWizard.ContainerFilter";

    	private class StatetProjectFilter extends ContainerSelectionComposite.ContainerFilter {
    		
    		@Override
    		public boolean select(IContainer container) {

				IProject project = container.getProject();
				try {
					if (project.hasNature(StatetProject.ID))
						return true;
				} catch (CoreException e) {	}

				return false;
			}
    	}
    	
    	private ContainerSelectionComposite fContainerGroup;
    	private Text fResourceNameControl;
    	
    	private IPath fContainerFullPath;
    	private String fResourceName;
    	private String fResourceNameDefaultSuffix;
    	private boolean fResourceNameEdited;
    	

    	public ResourceGroup(String defaultResourceNameExtension) {
    		
    		fResourceNameDefaultSuffix = defaultResourceNameExtension;
    	}
    	
    	public void createGroup(Layouter parentLayouter) {
    		
    		Layouter layouter = new Layouter(new Composite(parentLayouter.fComposite, SWT.NONE), 2);
    		layouter.fComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    		
            // resource and container group
            fContainerGroup = new ContainerSelectionComposite(
                    layouter.fComposite, true, false, 
                    null, SIZING_CONTAINER_GROUP_HEIGHT);
            fContainerGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

            fContainerGroup.setListener(this);
            boolean enableFilter = true;
            if (getDialogSettings().get(DIALOGSETTING_ENABLEFILTER) != null)
            	enableFilter = getDialogSettings().getBoolean(DIALOGSETTING_ENABLEFILTER);
            fContainerGroup.setToggleFilter(new StatetProjectFilter(), enableFilter);
            
            fResourceNameControl = layouter.addLabeledTextControl(getNewFileLabel());
            fResourceNameControl.addListener(SWT.Modify, this);
            
            initFields();
        }
    
        /**
         * Returns the label to display in the file name specification visual
         * component group.
         * <p>
         * Subclasses may reimplement.
         * </p>
         *
         * @return the label to display in the file name specification visual
         *     component group
         */
        protected String getNewFileLabel() {
            return StatetWizardsMessages.ResourceGroup_NewFile_label;
        }

        /**
         * Sets the initial contents of the container name entry field, based upon
         * either a previously-specified initial value or the ability to determine
         * such a value.
         */
        protected void initFields() {
        	
        	IPath path = null;
        	
            if (fContainerFullPath != null)
                path = fContainerFullPath;
            else {
                Iterator it = fResourceSelection.iterator();
                if (it.hasNext()) {
                    Object object = it.next();
                    IResource selectedResource = null;
                    if (object instanceof IResource) {
                        selectedResource = (IResource) object;
                    } else if (object instanceof IAdaptable) {
                        selectedResource = (IResource) ((IAdaptable) object).getAdapter(IResource.class);
                    }
                    if (selectedResource != null) {
                        if (selectedResource.getType() == IResource.FILE)
                            selectedResource = selectedResource.getParent();
                        if (selectedResource.isAccessible())
                        	path = selectedResource.getFullPath();
                    }
                }
            }
            if (path != null)
            	fContainerGroup.selectContainer(path);

            if (fResourceName != null)
                fResourceNameControl.setText(fResourceName);
        }

        public void handleEvent(Event event) {
        	
        	fContainerFullPath = fContainerGroup.getContainerFullPath();
        	String name =  fResourceNameControl.getText();
        	if (!fResourceNameEdited && event.widget == fResourceNameControl 
        			&& name.length() > 0 && !name.equals(fResourceName)) {
        		fResourceNameEdited = true;
        	}
        	fResourceName = name;

        	validatePage();
        }

//        /**
//         * Sets the value of this page's container name field, or stores
//         * it for future use if this page's controls do not exist yet.
//         *
//         * @param path the full path to the container
//         */
//        public void setContainerFullPath(IPath path) {
//        	
//        	if (fResourceGroup != null)
//        		fResourceGroup.setContainerFullPath(path); // update fResourceName?
//        	else
//        		fContainerFullPath = path;
//        }
//		 
//        /**
//         * Sets the value of this page's file name field, or stores
//         * it for future use if this page's controls do not exist yet.
//         *
//         * @param value new file name
//         */
//        public void setFileName(String value) {
//        	
//        	if (fResourceGroup != null)
//        		fResourceGroup.setResource(value); // update fResourceName?
//        	else
//        		fResourceName = value;
//        }
		 
    	/**
         * Returns the current full path of the containing resource as entered or 
         * selected by the user, or its anticipated initial value.
         *
         * @return the container's full path, anticipated initial value, 
         *   or <code>null</code> if no path is known
         */
        public IPath getContainerFullPath() {
        	
            return fContainerFullPath;
        }

        /**
         * Returns the current file name as entered by the user, or its anticipated
         * initial value.
         *
         * @return the file name, its anticipated initial value, or <code>null</code>
         *   if no file name is known
         */
        public String getResourceName() {

            String name = fResourceName;
            if (!name.endsWith(fResourceNameDefaultSuffix))
            	name += fResourceNameDefaultSuffix;

            return name;
        }
        
        public void setFocus() {
        	
        	fResourceNameControl.setFocus();
        }
        
        /**
         * 
         * @return true, if ok
         */
        public IStatus validate() {

            // don't attempt to validate controls until they have been created
            if (fContainerGroup == null)
                return null;

            IStatus containerSelectionStatus = ContainerSelectionComposite.validate(fContainerFullPath);
            if (containerSelectionStatus.matches(IStatus.ERROR))
            	return containerSelectionStatus;

            IStatus resourceNameStatus = validateResourceName(fResourceName);
            if (resourceNameStatus == null || resourceNameStatus.matches(IStatus.ERROR))
            	return resourceNameStatus;
            
            IPath path = fContainerGroup.getContainerFullPath().append(getResourceName());

            // no warnings
            return validateFullResourcePath(path);
        }
        
        protected IStatus validateResourceName(String resourceName) {
        	
        	if (resourceName == null || resourceName.trim().length() == 0) {
        		if (fResourceNameEdited)
        			return new StatusInfo(IStatus.ERROR, NLS.bind(
        					StatetWizardsMessages.ResourceGroup_error_EmptyName,
        					StatetDialogsMessages.Resources_File));
        		else
        			return null;
        	}

            if (!(new Path("")).isValidSegment(resourceName)) //$NON-NLS-1$
            	return new StatusInfo(IStatus.ERROR, NLS.bind(
            			StatetWizardsMessages.ResourceGroup_error_InvalidFilename,
            			resourceName));

            return new StatusInfo();
        }
        
        /**
         * Returns a <code>boolean</code> indicating whether the specified resource
         * path represents a valid new resource in the workbench.  An error message
         * is stored for future reference if the path  does not represent a valid
         * new resource path.
         *
         * @param resourcePath the path to validate
         * @return IStatus indicating validity of the resource path
         */
        protected IStatus validateFullResourcePath(IPath resourcePath) {
        	
            IWorkspace workspace = ResourcesPlugin.getWorkspace();

            IStatus result = workspace.validatePath(resourcePath.toString(), IResource.FILE);
            if (!result.isOK()) {
            	return result;
            }

            if ( // !allowExistingResources && 
            		workspace.getRoot().getFolder(resourcePath).exists() 
            		|| workspace.getRoot().getFile(resourcePath).exists())
            	return new StatusInfo(IStatus.ERROR, StatetWizardsMessages.ResourceGroup_error_ResourceExists);
        
            return new StatusInfo();
        }
        
        
        public void saveSettings() {
        	
        	getDialogSettings().put(DIALOGSETTING_ENABLEFILTER, fContainerGroup.getToggleFilterSetting());
        }
    }
    
    /** the current resource selection */
    protected IStructuredSelection fResourceSelection;

    /**
     * Creates a new file creation wizard page. If the initial resource selection 
     * contains exactly one container resource then it will be used as the default
     * container resource.
     *
     * @param pageName the name of the page
     * @param selection the current resource selection
     */
    public NewElementWizardPage(String pageName, IStructuredSelection selection) {
    	
        super(pageName);
        setPageComplete(false);

        fResourceSelection = selection;
    }


    /** (non-Javadoc)
     * Method declared on IDialogPage.
     */
    public void createControl(Composite parent) {
        
    	initializeDialogUnits(parent);
    	
        // top level group
    	GridLayout layout = new GridLayout();
    	layout.marginHeight = 0;
    	Layouter layouter = new Layouter(new Composite(parent, SWT.NONE), layout);
    	layouter.fComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    	
    	createContents(layouter);
    	
//    	validatePage();
    	// Show description on opening
    	setErrorMessage(null);
    	setMessage(null);
    	setControl(layouter.fComposite);
    }
    

    /**
     * Subclasses should ADDHELP
     * 
     * @param layouter
     */
    protected abstract void createContents(Layouter layouter);

    
    /**
     * Returns whether this page's controls currently all contain valid 
     * values.
     *
     * @return <code>true</code> if all controls are valid, and
     *   <code>false</code> if at least one is invalid
     */
    protected void validatePage() {
    	
    	updateStatus(new StatusInfo());
    }
    
	/**
	 * Updates the status line and the OK button according to the given status
	 * 
	 * @param status status to apply
	 */
	protected void updateStatus(IStatus status) {
		
		if (status != null) {
			setPageComplete(!status.matches(IStatus.ERROR));
		} else {
			setPageComplete(false);
			status = new StatusInfo();
		}
		Control control = getControl();
		if (control != null && control.isVisible())
			StatusUtil.applyToStatusLine(this, status);
	}
    
}
