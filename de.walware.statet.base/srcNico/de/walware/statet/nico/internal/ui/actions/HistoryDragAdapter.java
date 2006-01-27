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

package de.walware.statet.nico.internal.ui.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;

import de.walware.statet.nico.ui.views.HistoryView;


public class HistoryDragAdapter extends DragSourceAdapter {
	
	
	private HistoryView fView;
	
	private IStructuredSelection fCurrentSelection;
	
	
	public HistoryDragAdapter(HistoryView view) {
		
		fView = view;
	}
	
	@Override
	public void dragStart(DragSourceEvent event) {
		
		fCurrentSelection = 
			(IStructuredSelection) fView.getTableViewer().getSelection();
		
		event.doit = (fCurrentSelection != null && fCurrentSelection.size() > 0);
	}
	
	@Override
	public void dragSetData(DragSourceEvent event) {
		
		String text = HistoryView.createTextBlock(fCurrentSelection);
		event.data = text;
	}
	
	@Override
	public void dragFinished(DragSourceEvent event) {
		
		fCurrentSelection = null;
	}
}
