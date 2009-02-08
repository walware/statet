/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import de.walware.ecommons.ui.util.DialogUtil;

import de.walware.statet.ext.templates.TemplatesUtil.EvaluatedTemplate;
import de.walware.statet.ext.ui.wizards.NewElementWizard;

import de.walware.statet.r.codegeneration.CodeGeneration;
import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.RUI;


public class NewRdFileCreationWizard extends NewElementWizard {
	
	
	private static class NewRdFileCreator extends NewFileCreator {
		
		public NewRdFileCreator(final IPath containerPath, final String resourceName) {
			super(containerPath, resourceName);
		}
		
		@Override
		public String getContentType(final IFile newFileHandle) {
			return IRSourceUnit.RD_CONTENT;
		}
		
		@Override
		protected String getInitialFileContent(final IFile newFileHandle) {
			final String lineDelimiter = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				final RResourceUnit rcu = RResourceUnit.createTempUnit(newFileHandle, "rd"); //$NON-NLS-1$
				final EvaluatedTemplate data = CodeGeneration.getNewRdFileContent(rcu, lineDelimiter);
				if (data != null) {
					fInitialSelection = data.getRegionToSelect();
					return data.getContent();
				}
			} catch (final CoreException e) {
				RUIPlugin.logError(RUIPlugin.IO_ERROR, "Error occured when applying template to new Rd file.", e); //$NON-NLS-1$
			}
			return null;
		}
	}
	
	private NewRdFileCreationWizardPage fFirstPage;
	private NewFileCreator fNewRdFile;
	
	
	public NewRdFileCreationWizard() {
		setDialogSettings(DialogUtil.getDialogSettings(RUIPlugin.getDefault(), "NewElementWizard")); //$NON-NLS-1$
		setDefaultPageImageDescriptor(RUI.getImageDescriptor(RUIPlugin.IMG_WIZBAN_NEWRDFILE));
		setWindowTitle(Messages.NewRDocFileWizard_title);
	}
	
	@Override
	public void addPages() {
		super.addPages();
		fFirstPage = new NewRdFileCreationWizardPage(getSelection());
		addPage(fFirstPage);
	}
	
	@Override
	protected ISchedulingRule getSchedulingRule() {
		final ISchedulingRule rule = createRule(fNewRdFile.getFileHandle());
		if (rule != null)
			return rule;
		
		return super.getSchedulingRule();
	}
	
	@Override
	public boolean performFinish() {
		// befor super, so it can be used in getSchedulingRule
		fNewRdFile = new NewRdFileCreator(
				fFirstPage.fResourceGroup.getContainerFullPath(),
				fFirstPage.fResourceGroup.getResourceName() );
		
		final boolean result = super.performFinish();
		
		if (result && fNewRdFile.getFileHandle() != null) {
			// select and open file
			selectAndReveal(fNewRdFile.getFileHandle());
			openResource(fNewRdFile);
		}
		
		return result;
	}
	
	@Override
	protected void doFinish(final IProgressMonitor monitor) throws InterruptedException, CoreException, InvocationTargetException {
		try {
			monitor.beginTask("Create new file...", 1000); //$NON-NLS-1$
			
			fNewRdFile.createFile(new SubProgressMonitor(monitor, 800) );
			
			fFirstPage.saveSettings();
			monitor.worked(200);
		}
		finally {
			monitor.done();
		}
	}
	
}
