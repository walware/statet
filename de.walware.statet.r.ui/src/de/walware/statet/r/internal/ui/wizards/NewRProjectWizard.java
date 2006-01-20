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

package de.walware.statet.r.internal.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;

import de.walware.statet.ext.ui.wizards.NewElementWizard;
import de.walware.statet.ext.ui.wizards.StatetWizardsMessages;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.ui.RImages;



public class NewRProjectWizard extends NewElementWizard {
    
	
	private WizardNewProjectCreationPage fFirstPage;
	private WizardNewProjectReferencePage fReferencePage;

	private ProjectCreator fNewRProject;

	private IConfigurationElement fPerspConfig;
	
    public NewRProjectWizard() {
    	
        setDefaultPageImageDescriptor(RImages.DESC_WIZBAN_NEWRPROJECT);
        setWindowTitle(Messages.NewRProjectWizard_title); 
    }
  
    public void addPages() {
    	
        super.addPages();
        fFirstPage = new NewRProjectWizardPage();
        addPage(fFirstPage);
        
        // only add page if there are already projects in the workspace
        if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {
            fReferencePage = new WizardNewProjectReferencePage("BasicProjectReferencePage"); //$NON-NLS-1$
            fReferencePage.setTitle(StatetWizardsMessages.NewProjectReferencePage_title);
            fReferencePage.setDescription(StatetWizardsMessages.NewProjectReferencePage_description);
            addPage(fReferencePage);
        }
        
    }
    
//    protected ISchedulingRule getSchedulingRule() { // root-rule required to change project description
    
    @Override
    public boolean performFinish() {

    	// befor super, because eclipse-pages access ui
    	fNewRProject = new ProjectCreator(
    			fFirstPage.getProjectName(),
    			(fFirstPage.useDefaults()) ? null : fFirstPage.getLocationPath(),
    			(fReferencePage != null) ? fReferencePage.getReferencedProjects() : null );

    	boolean result = super.performFinish();

    	IProject newProject = null;
        if (result && newProject != null) {
        	updatePerspective(fPerspConfig);
        	selectAndReveal(newProject);
        }
    	
    	return result;
    }
    
	protected void doFinish(IProgressMonitor monitor) throws InterruptedException, CoreException, InvocationTargetException {
    
		try {
			monitor.beginTask("Create new project...", 1000); //$NON-NLS-1$
	
			fNewRProject.createProject(new SubProgressMonitor(monitor, 500) );
			
			RProject.addNature(fNewRProject.getProjectHandle(), new SubProgressMonitor(monitor, 500));
//		fFirstPage.saveSettings();
		}
		finally {
			monitor.done();
		}
	}
    
}
