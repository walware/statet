/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.dataeditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import de.walware.statet.r.ui.dataeditor.RDataTableComposite;


public class FindDialogHandler extends AbstractHandler {
	
	
	private final IWorkbenchPart fWorkbenchPart;
	
	
	public FindDialogHandler(final IWorkbenchPart part) {
		fWorkbenchPart = part;
	}
	
	
	protected RDataTableComposite getTable() {
		return (RDataTableComposite) fWorkbenchPart.getAdapter(RDataTableComposite.class);
	}
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		final RDataTableComposite table = getTable();
		setBaseEnabled(table != null);
		
		final IWorkbenchPage page = fWorkbenchPart.getSite().getPage();
		if (page.getActivePart() == fWorkbenchPart) {
			final FindDataDialog dialog = FindDataDialog.get(page.getWorkbenchWindow(), false);
			if (dialog != null) {
				dialog.update(fWorkbenchPart);
			}
		}
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = fWorkbenchPart.getSite().getWorkbenchWindow();
		
		final FindDataDialog dialog = FindDataDialog.get(window, true);
		dialog.update(fWorkbenchPart);
		dialog.open();
		
		return null;
	}
	
}
