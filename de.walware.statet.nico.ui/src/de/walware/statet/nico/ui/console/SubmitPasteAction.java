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

package de.walware.statet.nico.ui.console;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.actions.ActionFactory;

import de.walware.ecommons.text.TextUtil;

import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.ui.NicoUIMessages;
import de.walware.statet.nico.ui.NicoUITools;


class SubmitPasteAction extends Action {
	
	
	private final NIConsolePage fView;
	
	
	public SubmitPasteAction(final NIConsolePage consolePage) {
		super(NicoUIMessages.PasteSubmitAction_name);
		
		setId(ActionFactory.PASTE.getId());
		setActionDefinitionId(IWorkbenchCommandConstants.EDIT_PASTE);
		
		fView = consolePage;
	}
	
	
	@Override
	public void run() {
		final Transfer transfer = TextTransfer.getInstance();
		final String text = (String) fView.getClipboard().getContents(transfer);
		final ToolController controller = fView.getConsole().getProcess().getController();
		
		if (text == null || controller == null) {
			return;
		}
		
		NicoUITools.runSubmitInBackground(
				controller.getTool(),
				createRunnable(controller, text),
				fView.getSite().getShell());
	}
	
	
	static IRunnableWithProgress createRunnable(final ToolController controller, final String text) {
		return new IRunnableWithProgress () {
			@Override
			public void run(final IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					monitor.beginTask(NicoUITools.createSubmitMessage(controller.getTool()), 1000);
					
					final List<String> lines = TextUtil.toLines(text);
					monitor.worked(200);
					
					final IStatus status = controller.submit(lines, SubmitType.CONSOLE,
							new SubProgressMonitor(monitor, 800) );
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
	}
	
}
