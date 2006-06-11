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

package de.walware.statet.nico.ui.console;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.ui.NicoMessages;
import de.walware.statet.nico.ui.NicoUITools;


class SubmitPasteAction extends Action {

	
	private static Pattern fLineSplitPattern = Pattern.compile("\\r(\\n)?|\\n");
	
	
	private NIConsolePage fView;
	
	public SubmitPasteAction(NIConsolePage consolePage) {
		
		super(NicoMessages.PasteSubmitAction_name);
		
		setId(ActionFactory.PASTE.getId());
		setActionDefinitionId(IWorkbenchActionDefinitionIds.PASTE);
		
		fView = consolePage;
	}
	
	@Override
	public void run() {
		
		Transfer transfer = TextTransfer.getInstance();
		String text = (String) fView.getClipboard().getContents(transfer);
		ToolController controller = fView.getConsole().getProcess().getController();
		
		if (text == null || controller == null)
			return;
		
		NicoUITools.runSubmitInBackground(
				controller.getProcess(),
				createRunnable(controller, text), 
				fView.getSite().getShell());
	}
	
	
	static IRunnableWithProgress createRunnable(final ToolController controller, final String text) {
		
		return new IRunnableWithProgress () {
			public void run(IProgressMonitor monitor) throws InterruptedException {

				try {
					monitor.beginTask(NicoUITools.createSubmitMessage(controller.getProcess()), 1000);
					
					String[] lines = splitString(text);
					monitor.worked(200);
					
					controller.submit(lines, SubmitType.CONSOLE, 
							new SubProgressMonitor(monitor, 800));
				}
				finally {
					monitor.done();
				}
			}
		};
	}
	
	static String[] splitString(String text) {
		
		String[] lines = fLineSplitPattern.split(text);
		return lines;
	}
}
