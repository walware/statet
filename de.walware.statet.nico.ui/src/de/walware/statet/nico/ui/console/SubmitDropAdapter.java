/*******************************************************************************
 * Copyright (c) 2005-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.console;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;

import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.LocalTaskTransfer;
import de.walware.statet.nico.internal.ui.LocalTaskTransfer.Data;
import de.walware.statet.nico.ui.NicoUITools;


public class SubmitDropAdapter implements DropTargetListener {
	
	
	public static class TaskSet {
		public ToolProcess process;
	}
	
	
	private final NIConsolePage fPage;
	
	
	public SubmitDropAdapter(final NIConsolePage page) {
		fPage = page;
	}
	
	
	@Override
	public void dragEnter(final DropTargetEvent event) {
		validate(event);
	}
	
	@Override
	public void dragLeave(final DropTargetEvent event) {
	}
	
	@Override
	public void dragOperationChanged(final DropTargetEvent event) {
		validate(event);
	}
	
	@Override
	public void dragOver(final DropTargetEvent event) {
		event.feedback = DND.FEEDBACK_NONE;
	}
	
	@Override
	public void dropAccept(final DropTargetEvent event) {
		validate(event);
	}
	
	@Override
	public void drop(final DropTargetEvent event) {
		if (LocalTaskTransfer.getTransfer().isSupportedType(event.currentDataType)) {
			final LocalTaskTransfer.Data data = (Data) event.data;
			final ToolProcess process = fPage.getConsole().getProcess();
			if (data == null || process.isTerminated()) {
				return;
			}
			data.process.getQueue().move(data.runnables, process.getQueue());
			return;
		}
		if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
			final String text = (String) event.data;
			final ToolController controller = fPage.getConsole().getProcess().getController();
			
			if (text == null || controller == null) {
				return;
			}
			
			final IRunnableWithProgress runnable = SubmitPasteAction.createRunnable(controller, text);
			NicoUITools.runSubmitInBackground(controller.getTool(), runnable, fPage.getSite().getShell());
			return;
		}
	}
	
	
	private void validate(final DropTargetEvent event) {
		final ToolProcess process = fPage.getConsole().getProcess();
		if (LocalTaskTransfer.getTransfer().isSupportedType(event.currentDataType)) {
			if (( (event.operations & DND.DROP_MOVE) == DND.DROP_MOVE)
					&& process.getMainType().equals(LocalTaskTransfer.getTransfer().getMainType())
					&& !process.isTerminated() ) {
				event.detail = DND.DROP_MOVE;
				return;
			}
		}
		if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
			if (( (event.operations & DND.DROP_COPY) == DND.DROP_COPY) 
					&& !fPage.getConsole().getProcess().isTerminated() ) {
				event.detail = DND.DROP_COPY;
				return;
			}
		}
		event.detail = DND.DROP_NONE;
	}
	
}
