/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.dialogs.groups;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.walware.ecommons.ui.dialogs.Layouter;
import de.walware.ecommons.ui.dialogs.groups.ButtonGroup.ButtonListener;


public abstract class TableOptionButtonsGroup<ItemT extends Object> 
		extends TableOptionsGroup<ItemT> {
	
	
	protected ButtonGroup fButtonGroup;
	private int fRemoveButtonIdx = -1;
	private int fDefaultButtonIdx = -1;
	
	
	public TableOptionButtonsGroup(String[] buttonLabels) {
		super(true, true);
		fButtonGroup = new ButtonGroup(buttonLabels, new ButtonListener() {
			public void handleButtonPressed(int buttonIdx) {
				IStructuredSelection selection = getSelectedItems();
				TableOptionButtonsGroup.this.handleButtonPressed(buttonIdx, getSingleItem(selection), selection);
			}
		});
	}
	
	@Override
	protected Control createOptionsControl(Composite parent) {
		
		Layouter options = new Layouter(new Composite(parent, SWT.NONE), 1);
		fButtonGroup.createGroup(options);
		
		return options.composite;
	}
	
	/**
	 * Sets the buttons-action for pressing DEL
	 * @param buttonIdx idx of button with DEL funktion
	 */
	public void setRemoveButton(int buttonIdx) {
		
		fRemoveButtonIdx = buttonIdx;
	}
	
	/**
	 * Sets the buttons-action for double click
	 * @param buttonIdx idx of button
	 */
	public void setDefaultButton(int buttonIdx) {
		
		fDefaultButtonIdx = buttonIdx;
	}

	public abstract void handleButtonPressed(int buttonIdx, ItemT item, IStructuredSelection rawSelection);
	
	@Override
	protected void handleDoubleClick(ItemT item, IStructuredSelection rawSelection) {
		
		if (fDefaultButtonIdx != -1 && fButtonGroup.isButtonEnabled(fDefaultButtonIdx)) {
			handleButtonPressed(fDefaultButtonIdx, item, rawSelection);
		}
	}
	
	/**
	 * Handles key events in the table viewer. Specifically
	 * when the delete key is pressed.
	 */
	protected void handleKeyPressed(KeyEvent event) {
		
		if (event.character == SWT.DEL && event.stateMask == 0) {
			if (fRemoveButtonIdx != -1 && fButtonGroup.isButtonEnabled(fRemoveButtonIdx)) {
				IStructuredSelection selection = getSelectedItems();
				handleButtonPressed(fRemoveButtonIdx, getSingleItem(selection), selection);
			}
		} 
	}	
	
}
