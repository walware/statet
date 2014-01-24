/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.emf.internal.forms;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.ui.EMFEditUIPlugin;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;


public class RedoCommandHandler extends AbstractHandler implements IElementUpdater {
	
	
	private final EditingDomain fDomain;
	
	
	public RedoCommandHandler(final EditingDomain domain) {
		fDomain = domain;
	}
	
	
	@Override
	public void setEnabled(final Object evaluationContext) {
		setBaseEnabled(fDomain.getCommandStack().canRedo());
	}
	
	@Override
	public void updateElement(final UIElement element, final Map parameters) {
		final Command command = fDomain.getCommandStack().getRedoCommand();
		if (command != null) {
			final String name = EMFEditUIPlugin.INSTANCE.getString("_UI_Redo_menu_item", true); //$NON-NLS-1$
			element.setText(NLS.bind(name, getLabel(command)));
			element.setTooltip(NLS.bind(name, getTipLabel(command)));
		}
	}
	
	protected String getLabel(final Command command) {
		String label = command.getLabel();
		if (label == null) {
			label = ""; //$NON-NLS-1$
		}
		return label;
	}
	
	protected String getTipLabel(final Command command) {
		String label = command.getDescription();
		if (label == null) {
			label = getLabel(command);
		}
		return label;
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		fDomain.getCommandStack().redo();
		return null;
	}
	
}
