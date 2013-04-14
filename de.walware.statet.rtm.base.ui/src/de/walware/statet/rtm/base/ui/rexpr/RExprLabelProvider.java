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

import java.util.List;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

import de.walware.statet.rtm.rtdata.types.RExpr;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;


public class RExprLabelProvider extends CellLabelProvider implements ILabelProvider {
	
	
	private final List<RExprTypeUIAdapter> fUIAdapters;
	
	
	public RExprLabelProvider(final List<RExprTypeUIAdapter> uiAdapters) {
		fUIAdapters = uiAdapters;
	}
	
	
	private RExprTypeUIAdapter getAdapter(final String typeKey) {
		for (final RExprTypeUIAdapter uiAdapter : fUIAdapters) {
			if (uiAdapter.getType().getTypeKey() == typeKey) {
				return uiAdapter;
			}
		}
		return null;
	}
	
	@Override
	public Image getImage(final Object element) {
		if (element instanceof RTypedExpr) {
			final RExprTypeUIAdapter uiAdapter = getAdapter(((RTypedExpr) element).getTypeKey());
			if (uiAdapter != null) {
				return uiAdapter.getImage();
			}
		}
		return null;
	}
	
	@Override
	public String getText(final Object element) {
		if (element instanceof RExpr) {
			return ((RExpr) element).getExpr();
		}
		return ""; //$NON-NLS-1$
	}
	
	@Override
	public void update(final ViewerCell cell) {
		final Object element = cell.getElement();
		cell.setImage(getImage(element));
		cell.setText(getText(element));
	}
	
}
