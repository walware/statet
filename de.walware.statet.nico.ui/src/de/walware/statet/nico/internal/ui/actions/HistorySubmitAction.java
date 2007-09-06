/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.History.Entry;
import de.walware.statet.nico.ui.NicoUIMessages;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.views.HistoryView;


public class HistorySubmitAction extends BaseSelectionListenerAction {

	
	private HistoryView fView;
	
	
	public HistorySubmitAction(HistoryView view) {
		
		super(NicoUIMessages.SubmitAction_name);
		
		setId("de.walware.statet.nico.addviews.submit"); //$NON-NLS-1$
		
		fView = view;
		view.getTableViewer().addSelectionChangedListener(this);
	}
	
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		
		return (selection.size() > 0);
	}
	
	@Override
	public void run() {
		
		final IStructuredSelection selection = getStructuredSelection();
		ToolProcess process = fView.getTool();
		final ToolController controller = (process != null) ? process.getController() : null;
		if (selection == null || controller == null)
			return;
		
		IRunnableWithProgress runnable = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) throws InterruptedException {
				
				try {
					monitor.beginTask(NicoUITools.createSubmitMessage(controller.getProcess()), 1000);
					
					String[] commands = createCommandArray(selection);
					monitor.worked(200);
					
					controller.submit(commands, SubmitType.EDITOR,
							new SubProgressMonitor(monitor, 800));
				}
				finally {
					monitor.done();
				}
			}
			
		};
		NicoUITools.runSubmitInBackground(process, runnable, fView.getSite().getShell());
	}
	
// Lifecycle with view
//	public void dispose() {
//
//		fView.getTableViewer().removeSelectionChangedListener(this);
//		fView = null;
//	}
	
	static String[] createCommandArray(IStructuredSelection selection) {
		
		Object[] elements = selection.toArray();
		String[] commands = new String[elements.length];
		for (int i = 0; i < commands.length; i++) {
			commands[i] = ((Entry) elements[i]).getCommand();
		}
		
		return commands;
	}
}
