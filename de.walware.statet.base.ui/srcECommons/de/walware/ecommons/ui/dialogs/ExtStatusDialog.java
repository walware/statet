/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.dialogs;

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


public class ExtStatusDialog extends StatusDialog {
	
	
	/**
	 * @see StatusDialog#StatusDialog(Shell)
	 */
	public ExtStatusDialog(final Shell parent) {
		super(parent);
	}
	
	
	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	protected Control createButtonBar(final Composite parent) {
		final Composite composite = (Composite) super.createButtonBar(parent);
		((GridLayout) composite.getLayout()).verticalSpacing = 0;
		return composite;
	}
	
}
