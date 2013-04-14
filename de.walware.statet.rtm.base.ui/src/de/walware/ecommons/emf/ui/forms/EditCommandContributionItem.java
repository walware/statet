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

package de.walware.ecommons.emf.ui.forms;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.edit.command.CommandActionDelegate;
import org.eclipse.emf.edit.command.CommandParameter;
import org.eclipse.emf.edit.command.CreateChildCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;

import de.walware.ecommons.ui.actions.SimpleContributionItem;


public class EditCommandContributionItem extends SimpleContributionItem {
	
	
	private final CommandParameter fCommandParameter;
	private final EditingDomain fEditingDomain;
	private final IObservableValue fObservable;
	
	private final Object fOwner;
	private final Command fCommand;
	
	
	public EditCommandContributionItem(final CommandParameter commandParameter,
			final EditingDomain editingDomain, final IObservableValue observable) {
		super("", null); //$NON-NLS-1$
		
		fCommandParameter = commandParameter;
		fEditingDomain = editingDomain;
		fObservable = observable;
		
		fOwner = fObservable.getValue();
		fCommand = createCommand();
		
		update((CommandActionDelegate) fCommand);
	}
	
	
	protected Command createCommand() {
		return CreateChildCommand.create(fEditingDomain, fOwner,
				fCommandParameter, Collections.singletonList(fOwner) );
	}
	
	private void update(final CommandActionDelegate delegate) {
		setIcon(ExtendedImageRegistry.getInstance().getImageDescriptor(delegate.getImage()));
		setText(delegate.getText());
		setTooltip(delegate.getToolTipText());
	}
	
	@Override
	public boolean isEnabled() {
		return fEditingDomain != null && fObservable.getValue() == fOwner
				&& fCommand.canExecute();
	}
	
	@Override
	protected void execute() throws ExecutionException {
		if (!isEnabled()) {
			return;
		}
		fEditingDomain.getCommandStack().execute(fCommand);
		
		executed(fCommand.getResult());
	}
	
	protected void executed(final Collection<?> result) {
	}
	
}
