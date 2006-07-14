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

package de.walware.statet.r.ui.internal.wizards;

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
import de.walware.statet.ext.ui.wizards.NewElementWizard;
import de.walware.statet.r.codegeneration.CodeGeneration;
import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.internal.RUIPlugin;


public class NewRdFileCreationWizard extends NewElementWizard {
    
	
	private static class NewRdFileCreator extends NewFileCreator {

		public NewRdFileCreator(IPath containerPath, String resourceName) {
			
			super(containerPath, resourceName);
		}

		protected InputStream getInitialFileContents(IFile newFileHandle) {
		
			String lineDelimiter = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			String content;
			try {
				RResourceUnit rcu = new RResourceUnit(newFileHandle);
				content = CodeGeneration.getNewRdFileContent(rcu, lineDelimiter);
				if (content != null)
					return new ByteArrayInputStream(content.getBytes());
			} catch (CoreException e) {
				StatetPlugin.logUnexpectedError(e);
			}
			return null;
		}
	}
	
	private NewRdFileCreationWizardPage fFirstPage;
	private NewFileCreator fNewRFile;

	
	public NewRdFileCreationWizard() {
    	
        setDefaultPageImageDescriptor(RUI.getImageDescriptor(RUIPlugin.IMG_WIZBAN_NEWRDFILE));
        setWindowTitle(Messages.NewRDocFileWizard_title); 
    }
  
	public void addPages() {
    	
        super.addPages();
        fFirstPage = new NewRdFileCreationWizardPage(getSelection());
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
        fNewRFile = new NewRdFileCreator(
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
	
			fNewRFile.createFile(new SubProgressMonitor(monitor, 800) );
			
			fFirstPage.saveSettings();
			monitor.worked(200);
		}
		finally {
			monitor.done();
		}
	}
    
}
