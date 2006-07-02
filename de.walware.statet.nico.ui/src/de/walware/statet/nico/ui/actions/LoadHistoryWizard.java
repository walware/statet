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

package de.walware.statet.nico.ui.actions;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.ext.ui.wizards.AbstractWizard;
import de.walware.statet.nico.core.NicoCoreMessages;
import de.walware.statet.nico.core.runtime.History;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.NicoUIMessages;
import de.walware.statet.nico.ui.internal.LoadHistoryPage;
import de.walware.statet.nico.ui.internal.NicoUIPlugin;
import de.walware.statet.ui.util.ExceptionHandler;


/**
 *
 */
public class LoadHistoryWizard extends AbstractWizard {

	
	static final String STORE_SECTION = "HistoryWizards"; //$NON-NLS-1$ shared with save
	
	private ToolProcess fProcess;
	private LoadHistoryPage fPage;

	
	public LoadHistoryWizard(ToolProcess process) {

		super();
		
		fProcess = process;
		
		setDialogSettings(NicoUIPlugin.getDefault(), STORE_SECTION);
		setWindowTitle(NicoUIMessages.LoadHistory_title);
		setNeedsProgressMonitor(false);
//		setDefaultPageImageDescriptor();
	}
	
	@Override
	public void addPages() {
		
		fPage = new LoadHistoryPage(fProcess);
		addPage(fPage);
	}
	
	@Override
	public boolean performFinish() {
		
		fPage.saveSettings();
		
		try {
			final History history = fProcess.getHistory();
			final IFile wsFile = fPage.fResourceInWorkspace;
			final IFileStore efsFile = fPage.fResourceInEFS;
			final String charset = fPage.fEncoding;

			assert (history != null && (wsFile != null || efsFile != null) && charset != null);
			
			Job job = new Job(NicoCoreMessages.LoadHistoryJob_label) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						if (wsFile != null) {
							history.load(wsFile, charset, false, monitor);
						}
						else {
							history.load(efsFile, charset, false, monitor);
						}
						return Status.OK_STATUS;
					} catch (CoreException e) {
						ExceptionHandler.handle(e.getStatus());
						return Status.OK_STATUS;
					}
					catch (OperationCanceledException e) {
						return Status.CANCEL_STATUS;
					}
				}
			};
			job.setUser(true);
			job.schedule();
		}
		catch (Exception e) {
			StatetPlugin.logUnexpectedError(e);
			return false;
		}
		
		return true;
	}
}
