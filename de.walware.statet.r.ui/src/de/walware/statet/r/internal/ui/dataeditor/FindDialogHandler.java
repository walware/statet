/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

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
		this.fWorkbenchPart = part;
	}
	
	
	protected RDataTableComposite getTable() {
		return (RDataTableComposite) this.fWorkbenchPart.getAdapter(RDataTableComposite.class);
	}
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		final RDataTableComposite table = getTable();
		setBaseEnabled(table != null);
		
		final IWorkbenchPage page = this.fWorkbenchPart.getSite().getPage();
		if (page.getActivePart() == this.fWorkbenchPart) {
			final FindDataDialog dialog = FindDataDialog.get(page.getWorkbenchWindow(), false);
			if (dialog != null) {
				dialog.update(this.fWorkbenchPart);
			}
		}
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = this.fWorkbenchPart.getSite().getWorkbenchWindow();
		
		final FindDataDialog dialog = FindDataDialog.get(window, true);
		dialog.update(this.fWorkbenchPart);
		dialog.open();
		
		return null;
	}
	
}
