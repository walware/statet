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

package de.walware.statet.nico.addviews;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

import de.walware.statet.nico.Messages;
import de.walware.statet.nico.runtime.SubmitType;
import de.walware.statet.nico.runtime.ToolController;
import de.walware.statet.nico.runtime.History.Entry;


class SubmitAction extends BaseSelectionListenerAction {

	
	private HistoryView fView;
	
	
	public SubmitAction(HistoryView view) {
		
		super(Messages.SubmitAction_name);
		
		setId("de.walware.statet.nico.addviews.submit");
		
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
		final ToolController controller = fView.getController();
		
		if (selection == null || controller == null)
			return;
		
		IRunnableWithProgress runnable = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) throws InterruptedException {
				
				monitor.beginTask(controller.createSubmitMessage(), 3);
				
				String[] commands = createCommandArray(selection);
				monitor.worked(1);
				
				controller.submit(commands, SubmitType.OTHER, monitor);
				monitor.done();
			}
			
		};
		controller.runSubmitInBackground(runnable, fView.getSite().getShell());
	}
	
	public void dispose() {
		
		fView.getTableViewer().removeSelectionChangedListener(this);
		fView = null;
	}
	
	static String[] createCommandArray(IStructuredSelection selection) {
		
		Object[] elements = selection.toArray();
		String[] commands = new String[elements.length];
		for (int i = 0; i < commands.length; i++) {
			commands[i] = ((Entry) elements[i]).getCommand();
		}
		
		return commands;
	}
}
