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

import static de.walware.statet.rtm.base.ui.rexpr.RExprTypeUIAdapter.PRIORITY_DEFAULT;
import static de.walware.statet.rtm.base.ui.rexpr.RExprTypeUIAdapter.PRIORITY_INVALID;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;

import de.walware.ecommons.emf.core.util.IContext;
import de.walware.ecommons.models.core.util.IElementSourceProvider;

import de.walware.statet.rtm.rtdata.types.RTypedExpr;


class RExprViewerDropAdapter extends ViewerDropAdapter {
	
	
	private final List<RExprTypeUIAdapter> fUIAdapters;
	
	private final IContext fContext;
	
	
	protected RExprViewerDropAdapter(final Viewer viewer, final List<RExprTypeUIAdapter> uiAdapters,
			final IContext context) {
		super(viewer);
		
		fUIAdapters = uiAdapters;
		fContext = context;
		setFeedbackEnabled(true);
	}
	
	
	@Override
	protected int determineLocation(final DropTargetEvent event) {
		final int location = super.determineLocation(event);
		if (location == LOCATION_ON) {
			return LOCATION_AFTER;
		}
		return location;
	}
	
	@Override
	public boolean validateDrop(final Object target, final int operation, final TransferData transferType) {
		final DropTargetEvent event = getCurrentEvent();
		
		if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
			if (isValidInput(LocalSelectionTransfer.getTransfer().getSelection(), event)) {
				return true;
			}
			return false;
		}
		return false;
	}
	
	@Override
	public boolean performDrop(final Object data) {
		final DropTargetEvent event = getCurrentEvent();
		if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
			return setInput(LocalSelectionTransfer.getTransfer().getSelection(), event);
		}
		return false;
	}
	
	protected boolean isValidInput(final Object input, final DropTargetEvent event) {
		if (input instanceof IStructuredSelection) {
			final IStructuredSelection selection = (IStructuredSelection) input;
			if (selection.getFirstElement() instanceof RTypedExpr) {
				if ((event.operations & (DND.DROP_MOVE | DND.DROP_COPY)) == 0) {
					return false;
				}
				final List list = selection.toList();
				for (final Object element : list) {
					if (!(element instanceof RTypedExpr)
							|| !isTypeSupported(((RTypedExpr) element).getTypeKey()) ) {
						return false;
					}
				}
				int operation = event.detail;
				if (operation != DND.DROP_COPY) {
					operation = DND.DROP_COPY;
					if (selection instanceof IElementSourceProvider) {
						final Object source = ((IElementSourceProvider) selection).getElementSource();
						final int move = (source instanceof IContext) ? isMoveValid((IContext) source, input) : PRIORITY_INVALID;
						if (move > PRIORITY_INVALID && (event.detail == DND.DROP_MOVE
								|| (move > PRIORITY_DEFAULT && event.detail == DND.DROP_DEFAULT) )) {
							operation = DND.DROP_MOVE;
						}
					}
				}
				overrideOperation(operation);
				return true;
			}
		}
		
		{	if ((event.operations & DND.DROP_COPY) == 0) {
				return false;
			}
			for (final RExprTypeUIAdapter uiAdapters : fUIAdapters) {
				if (uiAdapters.isValidInput(input, fContext) > PRIORITY_INVALID) {
					overrideOperation(DND.DROP_COPY);
					return true;
				}
			}
			return false;
		}
	}
	
	protected boolean setInput(final Object input, final DropTargetEvent event) {
		if (input instanceof IStructuredSelection) {
			final IStructuredSelection selection = (IStructuredSelection) input;
			if (selection.getFirstElement() instanceof RTypedExpr) {
				if (event.detail == DND.DROP_MOVE) {
					moveExprs((IContext) ((IElementSourceProvider) input).getElementSource(),
							selection.toList(), getInsertIndex(), event.time );
				}
				else {
					final List<RTypedExpr> list = selection.toList();
					final List<RTypedExpr> values = new ArrayList<RTypedExpr>(list.size());
					for (final RTypedExpr expr : list) {
						values.add(new RTypedExpr(expr.getTypeKey(), expr.getExpr()));
					}
					insertExprs(values, getInsertIndex(), event.time);
				}
				return true;
			}
		}
		
		{	int selectedPriority = PRIORITY_INVALID;
			RExprTypeUIAdapter selectedAdapter = null;
			for (final RExprTypeUIAdapter uiAdapters : fUIAdapters) {
				final int priority = uiAdapters.isValidInput(input, fContext);
				if ((priority > PRIORITY_INVALID) && ((uiAdapters.getType().getTypeKey() == getCurrentTypeKey()) ?
						(priority >= selectedPriority) : (priority > selectedPriority) )) {
					selectedPriority = priority;
					selectedAdapter = uiAdapters;
				}
			}
			if (selectedAdapter != null) {
				final List<String> exprs = selectedAdapter.getInputExprs(input, fContext);
				if (exprs != null && !exprs.isEmpty()) {
					final String typeKey = selectedAdapter.getType().getTypeKey();
					final List<RTypedExpr> values = new ArrayList<RTypedExpr>(exprs.size());
					for (final String expr : exprs) {
						values.add(new RTypedExpr(typeKey, expr));
					}
					insertExprs(values, getInsertIndex(), event.time);
					return true;
				}
			}
			return false;
		}
	}
	
	protected int getInsertIndex() {
		int index = getIndex(getCurrentTarget());
		if (index >= 0) {
			final int location = getCurrentLocation();
			switch (location) {
			case LOCATION_BEFORE:
				break;
			case LOCATION_AFTER:
				index++;
				break;
			default:
				index = -1;
				break;
			}
		}
		return index;
	}
	
	
	protected Object getSource() {
		return fContext;
	}
	
	protected int getIndex(final Object element) {
		return -1;
	}
	
	protected int isMoveValid(final IContext source, final Object input) {
		if (!canMove(source, input)) {
			return PRIORITY_INVALID;
		}
		if (getSource() == source) {
			return PRIORITY_DEFAULT + 10;
		}
		if (input instanceof IStructuredSelection) {
			final IStructuredSelection selection = (IStructuredSelection) input;
			if (selection.getFirstElement() instanceof RTypedExpr) {
				int selectedPriority = Integer.MAX_VALUE;
				final List<String> checked = new ArrayList<String>(fUIAdapters.size());
				for (final Object element : selection.toList()) {
					final RTypedExpr expr = (RTypedExpr) element;
					if (checked.contains(expr.getTypeKey())) {
						continue;
					}
					checked.add(expr.getTypeKey());
					
					final RExprTypeUIAdapter uiAdapter = getUIAdapter(expr.getTypeKey());
					final int priority = uiAdapter.isMoveValid(input, source, fContext);
					if (priority <= PRIORITY_INVALID) {
						return PRIORITY_INVALID;
					}
					if (priority < priority) {
						selectedPriority = priority;
					}
				}
				return selectedPriority;
			}
		}
		return PRIORITY_INVALID;
	}
	
	protected boolean canMove(final IContext source, final Object input) {
		return false;
	}
	
	protected boolean isTypeSupported(final String typeKey) {
		return (getUIAdapter(typeKey) != null);
	}
	
	protected RExprTypeUIAdapter getUIAdapter(final String typeKey) {
		for (final RExprTypeUIAdapter uiAdapter : fUIAdapters) {
			if (uiAdapter.getType().getTypeKey() == typeKey) {
				return uiAdapter;
			}
		}
		return null;
	}
	
	protected String getCurrentTypeKey() {
		return null;
	}
	
	protected void insertExprs(final List<RTypedExpr> exprs, final int index, final int time) {
	}
	
	protected void moveExprs(final IContext source, final List<RTypedExpr> exprs, final int index, final int time) {
	}
	
}
