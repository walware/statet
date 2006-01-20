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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.ext.templates.TemplatesUtil;
import de.walware.statet.ext.ui.wizards.NewElementWizard;
import de.walware.statet.r.codegeneration.CodeGeneration;
import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.ui.RImages;


public class NewRFileCreationWizard extends NewElementWizard {
    
	
	private static class NewRFileCreator extends NewFileCreator {

		public NewRFileCreator(IPath containerPath, String resourceName) {
			
			super(containerPath, resourceName);
		}

		protected InputStream getInitialFileContents(IFile newFileHandle) {
		
			String lineDelimiter = TemplatesUtil.getLineSeparator(newFileHandle.getProject());
			String content;
			try {
				RResourceUnit rcu = new RResourceUnit(newFileHandle);
				content = CodeGeneration.getNewRFileContent(rcu, lineDelimiter);
				if (content != null)
					return new ByteArrayInputStream(content.getBytes());
			} catch (CoreException e) {
				StatetPlugin.logUnexpectedError(e);
			}
			return null;
		}
	}
	
	private NewRFileCreationWizardPage fFirstPage;
	private NewFileCreator fNewRFile;

	
    public NewRFileCreationWizard() {
    	
        setDefaultPageImageDescriptor(RImages.DESC_WIZBAN_NEWRFILE);
        setWindowTitle(Messages.NewRScriptFileWizard_title); 
    }
  
    public void addPages() {
    	
        super.addPages();
        fFirstPage = new NewRFileCreationWizardPage(getSelection());
        addPage(fFirstPage);
    }
    
    @Override
    protected ISchedulingRule getSchedulingRule() {
	    
    	ISchedulingRule rule = createRule(fNewRFile.getFileHandle());
    	if (rule != null)
    		return rule;
    	
    	return super.getSchedulingRule();
    }
    
    @Override
    public boolean performFinish() {

    	// befor super, so it can be used in getSchedulingRule
        fNewRFile = new NewRFileCreator(
        		fFirstPage.fResourceGroup.getContainerFullPath(),
        		fFirstPage.fResourceGroup.getResourceName() );

    	boolean result = super.performFinish();

    	IFile newFile = fNewRFile.getFileHandle();
    	if (result && newFile != null) {
    		// select and open file
    		selectAndReveal(newFile);
    		openResource(newFile);
    	}
    	
    	return result;
    }
    
	protected void doFinish(IProgressMonitor monitor) throws InterruptedException, CoreException, InvocationTargetException {

		try {
			monitor.beginTask("Create new file...", 1000); //$NON-NLS-1$

			fNewRFile.createFile(new SubProgressMonitor(monitor, 900) );
		
			fFirstPage.saveSettings();
			monitor.worked(100);
		}
		finally {
			monitor.done();
		}
	}
    
}
