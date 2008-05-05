/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

import de.walware.eclipsecommons.ui.SharedMessages;
import de.walware.eclipsecommons.ui.util.DNDUtil;

import de.walware.statet.nico.ui.views.HistoryView;


public class HistoryCopyAction extends Action {
	
	
	private HistoryView fView;
	
	
	public HistoryCopyAction(final HistoryView view) {
		super(SharedMessages.CopyAction_name);
		setToolTipText(SharedMessages.CopyAction_tooltip);
		
		setId(ActionFactory.COPY.getId());
		setActionDefinitionId(IWorkbenchActionDefinitionIds.COPY);
		
		fView = view;
	}
	
	@Override
	public void run() {
		final String text = HistoryView.createTextBlock(fView.getSelection());
		DNDUtil.setContent(fView.getClipboard(), 
				new String[] { text }, 
				new Transfer[] { TextTransfer.getInstance() } );
	}
	
}
