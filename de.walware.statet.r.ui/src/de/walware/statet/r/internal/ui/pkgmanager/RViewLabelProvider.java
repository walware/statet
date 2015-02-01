/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.pkgmanager;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import de.walware.ecommons.ui.util.MessageUtil;

import de.walware.statet.r.core.pkgmanager.IRView;


public class RViewLabelProvider extends CellLabelProvider {
	
	
	public RViewLabelProvider() {
	}
	
	
	@Override
	public void update(final ViewerCell cell) {
		final Object element = cell.getElement();
		if (element instanceof IRView) {
			final IRView view = (IRView) element;
			cell.setText(view.getName());
			return;
		}
		cell.setText(""); //$NON-NLS-1$
	}
	
	@Override
	public String getToolTipText(final Object element) {
		if (element instanceof IRView) {
			final IRView view = (IRView) element;
			return MessageUtil.escapeForTooltip(view.getTopic());
		}
		return null;
	}
	
}
