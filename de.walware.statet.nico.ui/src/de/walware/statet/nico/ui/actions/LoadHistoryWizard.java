/*******************************************************************************
 * Copyright (c) 2006-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ui.util.DialogUtil;

import de.walware.statet.nico.core.runtime.History;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.LoadHistoryPage;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;
import de.walware.statet.nico.ui.NicoUIMessages;


/**
 * 
 */
public class LoadHistoryWizard extends Wizard {
	
	
	static final String STORE_SECTION = "tools/LoadSaveHistoryWizard"; //$NON-NLS-1$ shared with save
	
	
	private final ToolProcess fProcess;
	private LoadHistoryPage fPage;
	
	
	public LoadHistoryWizard(final ToolProcess process) {
		super();
		
		fProcess = process;
		
		setDialogSettings(DialogUtil.getDialogSettings(NicoUIPlugin.getDefault(), STORE_SECTION));
		setWindowTitle(NicoUIMessages.LoadHistory_title);
//		setDefaultPageImageDescriptor();
		setNeedsProgressMonitor(true);
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
			final Object file = fPage.getFile();
			final String charset = fPage.fEncoding;
			
			assert (history != null);
			assert (file != null);
			assert (charset != null);
			
			getContainer().run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException {
					final IStatus status = history.load(file, charset, false, monitor);
					if (status.getSeverity() == IStatus.ERROR) {
						throw new InvocationTargetException(new CoreException(status));
					}
				}
			});
			return true;
		}
		catch (final OperationCanceledException e) {
			return false;
		}
		catch (final Exception e) {
			if (e instanceof InvocationTargetException) {
				final Throwable cause = ((InvocationTargetException) e).getTargetException();
				if (cause instanceof CoreException) {
					StatusManager.getManager().handle(((CoreException) cause).getStatus(),
						StatusManager.LOG | StatusManager.SHOW);
					return false;
				}
			}
			NicoUIPlugin.logError(NicoUIPlugin.INTERNAL_ERROR, "Error of unexpected type occured, when performing load history.", e); //$NON-NLS-1$
			return false;
		}
	}
	
}
