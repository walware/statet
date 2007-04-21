/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.operation.IRunnableWithProgress;

import de.walware.statet.base.ui.util.ExceptionHandler;
import de.walware.statet.ext.ui.wizards.AbstractWizard;
import de.walware.statet.nico.core.runtime.History;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.LoadHistoryPage;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;
import de.walware.statet.nico.ui.NicoUIMessages;


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
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						history.load(file, charset, false, monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		}
		catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, null, null);
			return false;
		}
		catch (OperationCanceledException e) {
			return false;
		}
		catch (Exception e) {
			NicoUIPlugin.logError(NicoUIPlugin.INTERNAL_ERROR, "Error of unexpected type occured, when performing load history.", e); //$NON-NLS-1$
			return false;
		}
		
		return true;
	}
}
