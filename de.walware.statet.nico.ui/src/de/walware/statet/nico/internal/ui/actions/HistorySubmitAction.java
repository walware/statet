/*******************************************************************************
 * Copyright (c) 2005-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;

import de.walware.statet.nico.core.runtime.History.Entry;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.ui.NicoUIMessages;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.views.HistoryView;


public class HistorySubmitAction extends Action {
	
	
	private final HistoryView fView;
	
	
	public HistorySubmitAction(final HistoryView view) {
		super(NicoUIMessages.SubmitAction_name);
		
		setId("de.walware.statet.nico.addviews.submit"); //$NON-NLS-1$
		
		fView = view;
	}
	
	@Override
	public void run() {
		final Entry[] selection = fView.getSelection();
		final ToolProcess process = fView.getTool();
		final ToolController controller = (process != null) ? process.getController() : null;
		if (selection == null || controller == null) {
			return;
		}
		
		final IRunnableWithProgress runnable = new IRunnableWithProgress() {
			@Override
			public void run(final IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					monitor.beginTask(NicoUITools.createSubmitMessage(controller.getTool()), 1000);
					
					final List<String> commands = HistoryView.createCommandList(selection);
					monitor.worked(200);
					
					final IStatus status = controller.submit(commands, SubmitType.EDITOR,
							new SubProgressMonitor(monitor, 800));
					if (status.getSeverity() >= IStatus.ERROR) {
						throw new CoreException(status);
					}
				}
				catch (final CoreException e) {
					throw new InvocationTargetException(e);
				}
				finally {
					monitor.done();
				}
			}
			
		};
		NicoUITools.runSubmitInBackground(process, runnable, fView.getSite().getShell());
	}
	
}
