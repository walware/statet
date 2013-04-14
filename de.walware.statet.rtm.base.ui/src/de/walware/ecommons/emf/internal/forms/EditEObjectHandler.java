/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.emf.internal.forms;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import de.walware.ecommons.emf.core.util.IEMFEditPropertyContext;
import de.walware.ecommons.ui.content.IElementSourceProvider;
import de.walware.ecommons.ui.util.ViewerUtil;
import de.walware.ecommons.workbench.ui.WorkbenchUIUtil;


public abstract class EditEObjectHandler extends AbstractHandler {
	
	
	public EditEObjectHandler() {
	}
	
	
	protected ISelection getSelection(final Object context) {
		final ISelectionProvider selectionProvider = ViewerUtil.getSelectionProvider(
				WorkbenchUIUtil.getActiveFocusControl(context) );
		return (selectionProvider != null) ? selectionProvider.getSelection() : null;
	}
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		final ISelection selection = getSelection(evaluationContext);
		setBaseEnabled(selection instanceof IStructuredSelection
				&& isValidSelection((IStructuredSelection) selection));
	}
	
	protected boolean isValidSelection(final IStructuredSelection selection) {
		if (selection instanceof IElementSourceProvider) {
			final Object elementSource = ((IElementSourceProvider) selection).getElementSource();
			return (elementSource instanceof IEMFEditPropertyContext
					&& ((IEMFEditPropertyContext) elementSource).getEFeature() instanceof EReference);
		}
		return false;
	}
	
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ISelection selection = getSelection(event.getApplicationContext());
		if (selection instanceof IStructuredSelection && isValidSelection((IStructuredSelection) selection)) {
			final IEMFEditPropertyContext context = (IEMFEditPropertyContext) ((IElementSourceProvider) selection).getElementSource();
			final Command command = createCommand((IStructuredSelection) selection, context);
			if (command.canExecute()) {
				context.getEditingDomain().getCommandStack().execute(command);
			}
		}
		return null;
	}
	
	protected abstract Command createCommand(IStructuredSelection selection,
			IEMFEditPropertyContext context);
	
}
