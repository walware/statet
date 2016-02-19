/*=============================================================================#
 # Copyright (c) 2012-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.pkgmanager;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;


public class StatusLabelProvider extends CellLabelProvider {
	
	
	public StatusLabelProvider() {
	}
	
	
	@Override
	public void update(final ViewerCell cell) {
		final Object element = cell.getElement();
		if (element instanceof IStatus) {
			final IStatus status = (IStatus) element;
			cell.setImage(getImage(status));
			cell.setText(status.getMessage());
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	public Image getImage(final IStatus status) {
		switch (status.getSeverity()) {
		case IStatus.ERROR:
			return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
		case IStatus.WARNING:
			return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
		default:
			return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
		}
	}
	
	@Override
	public Image getToolTipImage(final Object element) {
		if (element instanceof IStatus) {
			return getImage((IStatus) element);
		}
		return null;
	}
	
	@Override
	public String getToolTipText(final Object element) {
		if (element instanceof IStatus) {
			return ((IStatus) element).getMessage();
		}
		return null;
	}
	
}
