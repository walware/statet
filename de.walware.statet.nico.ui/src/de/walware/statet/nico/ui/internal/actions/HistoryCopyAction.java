/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.internal.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

import de.walware.eclipsecommons.ui.SharedMessages;

import de.walware.statet.nico.ui.views.HistoryView;
import de.walware.statet.ui.util.DNDUtil;


public class HistoryCopyAction extends BaseSelectionListenerAction {

	
	private HistoryView fView;
	
	
	public HistoryCopyAction(HistoryView view) {
		
		super(SharedMessages.CopyAction_name);
		setToolTipText(SharedMessages.CopyAction_tooltip);
		
		setId(ActionFactory.COPY.getId());
		setActionDefinitionId(IWorkbenchActionDefinitionIds.COPY);
		
		fView = view;
		view.getTableViewer().addSelectionChangedListener(this);
	}
	
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		
		return (selection.size() > 0);
	}
	
	@Override
	public void run() {
		
		String text = HistoryView.createTextBlock(getStructuredSelection());
		DNDUtil.setContent(fView.getClipboard(), 
				new String[] { text }, 
				new Transfer[] { TextTransfer.getInstance() } );
	}

// Lifecycle with view
//	public void dispose() {
//		
//		fView.getTableViewer().removeSelectionChangedListener(this);
//		fView = null;
//	}
}
