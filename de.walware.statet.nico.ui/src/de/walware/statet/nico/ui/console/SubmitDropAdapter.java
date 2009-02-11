/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
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

import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.ui.NicoUITools;


public class SubmitDropAdapter implements DropTargetListener {
	
	
	private NIConsolePage fPage;
	
	
	public SubmitDropAdapter(final NIConsolePage page) {
		fPage = page;
	}
	
	
	public void dragEnter(final DropTargetEvent event) {
		validate(event);
	}
	
	public void dragLeave(final DropTargetEvent event) {
	}
	
	public void dragOperationChanged(final DropTargetEvent event) {
		validate(event);
	}
	
	public void dragOver(final DropTargetEvent event) {
		event.feedback = DND.FEEDBACK_NONE;
	}
	
	public void dropAccept(final DropTargetEvent event) {
		validate(event);
	}
	
	public void drop(final DropTargetEvent event) {
		final String text = (String) event.data;
		final ToolController controller = fPage.getConsole().getProcess().getController();
		
		if (text == null || controller == null)
			return;
		
		final IRunnableWithProgress runnable = SubmitPasteAction.createRunnable(controller, text);
		NicoUITools.runSubmitInBackground(controller.getProcess(), runnable, fPage.getSite().getShell());
	}
	
	
	private void validate(final DropTargetEvent event) {
		if (( (event.operations & DND.DROP_COPY) == DND.DROP_COPY) 
				&& !fPage.getConsole().getProcess().isTerminated() ) {
			event.detail = DND.DROP_COPY;
		}
		else {
			event.detail = DND.DROP_NONE;
		}
	}
	
}
