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

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.CommandParameter;
import org.eclipse.emf.edit.command.PasteFromClipboardCommand;
import org.eclipse.jface.viewers.IStructuredSelection;

import de.walware.ecommons.emf.core.util.IEMFEditPropertyContext;


public class PasteEObjectHandler extends EditEObjectHandler {
	
	
	public PasteEObjectHandler() {
	}
	
	
	@Override
	protected Command createCommand(final IStructuredSelection selection,
			final IEMFEditPropertyContext context) {
		final IObservable observable = context.getPropertyObservable();
		final EObject owner = (EObject) ((IObserving) observable).getObserved();
		final int index = CommandParameter.NO_INDEX;
//		if (observable instanceof IObservableList && selection.size() == 1) {
//			index = ((IObservableList) observable).indexOf(selection.getFirstElement());
//		}
		return PasteFromClipboardCommand.create(context.getEditingDomain(), owner,
				context.getEFeature(), index);
	}
	
}
