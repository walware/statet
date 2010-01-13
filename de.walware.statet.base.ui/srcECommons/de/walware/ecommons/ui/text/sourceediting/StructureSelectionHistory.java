/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.text.sourceediting;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.texteditor.IUpdate;

import de.walware.ecommons.FastList;


/**
 * History of selections.
 */
public class StructureSelectionHistory {
	
	
	private List<IRegion> fHistory;
	private ISourceEditor fSourceEditor;
	private ISelectionChangedListener fSelectionListener;
	private int fSelectionChangeListenerCounter;
	private FastList<IUpdate> fUpdateActions = new FastList<IUpdate>(IUpdate.class);
	
	
	public StructureSelectionHistory(final ISourceEditor editor) {
		fSourceEditor = editor;
		fHistory = new ArrayList<IRegion>();
		fSelectionListener = new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				if (fSelectionChangeListenerCounter == 0) {
					flush();
				}
			}
		};
		fSourceEditor.getViewer().getSelectionProvider().addSelectionChangedListener(fSelectionListener);
	}
	
	
	public void addUpdateListener(final IUpdate action) {
		fUpdateActions.add(action);
	}
	
	private final void updateState() {
		final IUpdate[] actions = fUpdateActions.toArray();
		for (int i = 0; i < actions.length; i++) {
			actions[i].update();
		}
	}
	
	public boolean isEmpty() {
		return fHistory.isEmpty();
	}
	
	public void remember(final IRegion range) {
		fHistory.add(range);
		updateState();
	}
	
	public IRegion getLast() {
		if (isEmpty())
			return null;
		final IRegion result = fHistory.remove(fHistory.size() - 1);
		updateState();
		return result;
	}
	
	public void flush() {
		if (fHistory.isEmpty()) {
			return;
		}
		fHistory.clear();
		updateState();
	}
	
	public void ignoreSelectionChanges() {
		fSelectionChangeListenerCounter++;
	}
	
	public void listenToSelectionChanges() {
		fSelectionChangeListenerCounter--;
	}
	
	public void dispose() {
		fSourceEditor.getViewer().getSelectionProvider().removeSelectionChangedListener(fSelectionListener);
	}
	
}
