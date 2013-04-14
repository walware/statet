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

package de.walware.statet.rtm.base.ui.rexpr;

import static de.walware.statet.rtm.base.ui.rexpr.RExprTypeUIAdapter.PRIORITY_INVALID;

import java.util.List;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;

import de.walware.ecommons.emf.core.util.IContext;



class RExprDropAdapter extends DropTargetAdapter {
	
	
	private final List<RExprTypeUIAdapter> fUIAdapters;
	
	
	public RExprDropAdapter(final List<RExprTypeUIAdapter> uiAdapters) {
		fUIAdapters = uiAdapters;
	}
	
	
	@Override
	public void dragEnter(final DropTargetEvent event) {
		dragOperationChanged(event);
		
		if (event.detail == DND.DROP_NONE) {
			return;
		}
		if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
			final ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
			if (selection instanceof IStructuredSelection) {
				if (isValidInput(selection, event)) {
					return;
				}
			}
			
			event.detail = DND.DROP_NONE;
			return;
		}
	}
	
	@Override
	public void dragOperationChanged(final DropTargetEvent event) {
		if (event.detail == DND.DROP_DEFAULT || event.detail == DND.DROP_NONE) {
			if ((event.operations & DND.DROP_COPY) != 0) {
				event.detail = DND.DROP_COPY;
			}
			else {
				event.detail = DND.DROP_NONE;
			}
		}
	}
	
	@Override
	public void drop(final DropTargetEvent event) {
		if (!performDrop(event)) {
			event.detail = DND.DROP_NONE;
		}
	}
	
	protected boolean performDrop(final DropTargetEvent event) {
		if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
			final ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
			if (selection instanceof IStructuredSelection) {
				return setInput(selection, event);
			}
			return false;
		}
		if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
			final String text = (String) event.data;
			if (text != null && !text.isEmpty()) {
				return insertText(text);
			}
			return false;
		}
		return false;
	}
	
	
	protected boolean isValidInput(final Object input, final DropTargetEvent event) {
		for (final RExprTypeUIAdapter uiAdapters : fUIAdapters) {
			if (uiAdapters.isValidInput(input, getContext()) > PRIORITY_INVALID) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean setInput(final Object input, final DropTargetEvent event) {
		int selectedPriority = PRIORITY_INVALID;
		RExprTypeUIAdapter selectedAdapter = null;
		for (final RExprTypeUIAdapter uiAdapters : fUIAdapters) {
			final int priority = uiAdapters.isValidInput(input, getContext());
			if ((priority > PRIORITY_INVALID) && ((uiAdapters.getType().getTypeKey() == getCurrentTypeKey()) ?
					(priority >= selectedPriority) : (priority > selectedPriority) )) {
				selectedPriority = priority;
				selectedAdapter = uiAdapters;
			}
		}
		if (selectedAdapter != null) {
			final List<String> exprs = selectedAdapter.getInputExprs(input, getContext());
			if (exprs != null && exprs.size() == 1) {
				setExpr(selectedAdapter.getType().getTypeKey(), exprs.get(0), event.time);
				return true;
			}
		}
		return false;
	}
	
	
	protected String getCurrentTypeKey() {
		return null;
	}
	
	protected IContext getContext() {
		return null;
	}
	
	protected boolean setExpr(final String typeKey, final String expr, final int time) {
		return false;
	}
	
	protected boolean insertText(final String text) {
		return false;
	}
	
}