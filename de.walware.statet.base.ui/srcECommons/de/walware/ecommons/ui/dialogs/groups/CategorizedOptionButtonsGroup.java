/*=============================================================================#
 # Copyright (c) 2005-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.ui.dialogs.groups;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.walware.ecommons.ui.dialogs.groups.ButtonGroup.ButtonListener;
import de.walware.ecommons.ui.dialogs.groups.CategorizedOptionsGroup.CategorizedItem;


public abstract class CategorizedOptionButtonsGroup<ItemT extends CategorizedItem> 
		extends CategorizedOptionsGroup<ItemT> {

	
	protected ButtonGroup fButtonGroup;
	
	
	public CategorizedOptionButtonsGroup(final String[] buttonLabels) {
		super(true, false);
	
		fButtonGroup = new ButtonGroup(buttonLabels, new ButtonListener() {
			@Override
			public void handleButtonPressed(final int buttonIdx) {
				final IStructuredSelection selection = getSelectedItems();
				CategorizedOptionButtonsGroup.this.handleButtonPressed(buttonIdx, getSingleItem(selection), selection);
			}
		});

	}

	@Override
	protected Control createOptionsControl(final Composite parent) {
		final Layouter options = new Layouter(new Composite(parent, SWT.NONE), 1);
		fButtonGroup.createGroup(options);
		
		return options.composite;
	}

	public abstract void handleButtonPressed(int buttonIdx, ItemT item, IStructuredSelection rawSelection);
	
}
