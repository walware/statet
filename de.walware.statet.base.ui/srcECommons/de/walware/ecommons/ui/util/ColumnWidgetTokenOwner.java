/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.util;

import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.IWidgetTokenKeeperExtension;
import org.eclipse.jface.text.IWidgetTokenOwner;
import org.eclipse.jface.text.IWidgetTokenOwnerExtension;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.widgets.Control;


/**
 * Adapts {@link IWidgetTokenOwner} and extensions for a ColumnViewer
 */
public class ColumnWidgetTokenOwner implements IWidgetTokenOwner, IWidgetTokenOwnerExtension {
	
	
	private final ColumnViewer fViewer;
	
	private IWidgetTokenKeeper fWidgetTokenKeeper;
	
	
	public ColumnWidgetTokenOwner(final ColumnViewer viewer) {
		fViewer = viewer;
	}
	
	
	public ColumnViewer getViewer() {
		return fViewer;
	}
	
	public Control getControl() {
		return fViewer.getControl();
	}
	
	public boolean requestWidgetToken(final IWidgetTokenKeeper requester) {
		if (getControl() != null) {
			if (fWidgetTokenKeeper != null) {
				if (fWidgetTokenKeeper == requester) {
					return true;
				}
				if (fWidgetTokenKeeper.requestWidgetToken(this)) {
					fWidgetTokenKeeper = requester;
					return true;
				}
			}
			else {
				fWidgetTokenKeeper = requester;
				return true;
			}
		}
		return false;
	}
	
	public boolean requestWidgetToken(final IWidgetTokenKeeper requester, final int priority) {
		if (getControl() != null) {
			if (fWidgetTokenKeeper != null) {
				if (fWidgetTokenKeeper == requester) {
					return true;
				}
				boolean accepted= false;
				if (fWidgetTokenKeeper instanceof IWidgetTokenKeeperExtension)  {
					final IWidgetTokenKeeperExtension extension = (IWidgetTokenKeeperExtension) fWidgetTokenKeeper;
					accepted = extension.requestWidgetToken(this, priority);
				}
				else  {
					accepted = fWidgetTokenKeeper.requestWidgetToken(this);
				}
				
				if (accepted) {
					fWidgetTokenKeeper = requester;
					return true;
				}
			}
			else {
				fWidgetTokenKeeper = requester;
				return true;
			}
		}
		return false;
	}
	
	public void releaseWidgetToken(final IWidgetTokenKeeper tokenKeeper) {
		if (fWidgetTokenKeeper == tokenKeeper) {
			fWidgetTokenKeeper = null;
		}
	}
	
	public boolean moveFocusToWidgetToken() {
		if (fWidgetTokenKeeper instanceof IWidgetTokenKeeperExtension) {
			return ((IWidgetTokenKeeperExtension) fWidgetTokenKeeper).setFocus(this);
		}
		return false;
	}
	
}
