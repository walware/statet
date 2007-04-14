/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.dialogs.groups;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.walware.eclipsecommons.ui.dialogs.Layouter;


public abstract class TableOptionButtonsGroup<ItemT extends SelectionItem> 
		extends TableOptionsGroup<ItemT> 
		implements ButtonGroup.ButtonListener {
	
	
	protected ButtonGroup fButtonGroup;
	private int fRemoveButtonIdx = -1;
	
	
	public TableOptionButtonsGroup(String[] buttonLabels) {
		super(true, true);
		fButtonGroup = new ButtonGroup(buttonLabels, this);
	}
	
	@Override
	protected Control createOptionsControl(Composite parent, GridData gd) {
		
		Layouter options = new Layouter(new Composite(parent, SWT.NONE), 1);
		fButtonGroup.createGroup(options);
		
		return options.fComposite;
	}
	
	/**
	 * Sets the buttons-action for pressing DEL
	 * @param buttonIdx idx of button with DEL funktion
	 */
	public void setRemoveButtonIndex(int buttonIdx) {
		
		fRemoveButtonIdx = buttonIdx;
	}
	
	public abstract void handleButtonPressed(int buttonIdx);
	
	/**
	 * Handles key events in the table viewer. Specifically
	 * when the delete key is pressed.
	 */
	protected void handleKeyPressed(KeyEvent event) {
		
		if (event.character == SWT.DEL && event.stateMask == 0) {
			if (fRemoveButtonIdx != -1 && fButtonGroup.isButtonEnabled(fRemoveButtonIdx)) {
				handleButtonPressed(fRemoveButtonIdx);
			}
		} 
	}	
	
}
