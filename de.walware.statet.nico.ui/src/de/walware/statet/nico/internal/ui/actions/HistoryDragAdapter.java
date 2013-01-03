/*******************************************************************************
 * Copyright (c) 2005-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.internal.ui.actions;

import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;

import de.walware.statet.nico.core.runtime.History.Entry;
import de.walware.statet.nico.ui.views.HistoryView;


public class HistoryDragAdapter extends DragSourceAdapter {
	
	
	private final HistoryView fView;
	
	private Entry[] fCurrentSelection;
	
	
	public HistoryDragAdapter(final HistoryView view) {
		fView = view;
	}
	
	
	@Override
	public void dragStart(final DragSourceEvent event) {
		fCurrentSelection = fView.getSelection();
		
		event.doit = (fCurrentSelection != null && fCurrentSelection.length > 0);
	}
	
	@Override
	public void dragSetData(final DragSourceEvent event) {
		final String text = HistoryView.createTextBlock(fCurrentSelection);
		event.data = text;
	}
	
	@Override
	public void dragFinished(final DragSourceEvent event) {
		fCurrentSelection = null;
	}
	
}
