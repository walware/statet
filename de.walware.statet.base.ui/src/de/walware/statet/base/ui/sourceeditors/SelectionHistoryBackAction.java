/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.ui.sourceeditors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.texteditor.IUpdate;

import de.walware.statet.base.ui.IStatetUICommandIds;


/**
 * Action to restore last selection;
 */
public class SelectionHistoryBackAction extends Action implements IUpdate {
	
	private StatextEditor1<?> fEditor;
	private SelectionHistory fHistory;
	
	public SelectionHistoryBackAction(final StatextEditor1<?>editor, final SelectionHistory history) {
		super();
		assert (editor != null);
		assert (history != null);
		fEditor = editor;
		fHistory = history;
		setId("RestoreLastSelection"); //$NON-NLS-1$
		setActionDefinitionId(IStatetUICommandIds.SELECT_LAST);
		update();
	}
	
	public void update() {
		setEnabled(!fHistory.isEmpty());
	}
	
	@Override
	public void run() {
		final IRegion old = fHistory.getLast();
		if (old != null) {
			try {
				fHistory.ignoreSelectionChanges();
				fEditor.selectAndReveal(old.getOffset(), old.getLength());
			} finally {
				fHistory.listenToSelectionChanges();
			}
		}
	}
}
