/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.dialogs.groups;

import java.util.Arrays;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.ui.dialogs.Layouter;
import de.walware.ecommons.ui.util.UIAccess;


public class ButtonGroup {

	
	public static interface ButtonListener {
		
		void handleButtonPressed(int buttonIdx);
	}
	
	
	private Composite fComposite;
	
	private Button[] fButtons;
	private String[] fButtonsLabel;
	private boolean[] fButtonsEnabled;
	
	private ButtonListener fListener;
	
	
	public ButtonGroup(String[] buttonsLabel, ButtonListener listener) {
		
		fButtonsLabel = buttonsLabel;
		fButtonsEnabled = new boolean[fButtonsLabel.length]; // default disabled
		Arrays.fill(fButtonsEnabled, true);
		
		fListener = listener;
	}

	public void createGroup(Layouter layouter) {

		fComposite = layouter.composite;
		
		SelectionListener listener = new SelectionListener() {
			
			public void widgetDefaultSelected(SelectionEvent e) {
				doButtonSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
				doButtonSelected(e);
			}
		};
		
		fButtons = new Button[fButtonsLabel.length];
		for (int i = 0; i < fButtonsLabel.length; i++) {
			if (fButtonsLabel[i] != null) {
				fButtons[i] = layouter.addButton(fButtonsLabel[i], listener);
				fButtons[i].setEnabled(fButtonsEnabled[i]);
			}
			else
				layouter.addFiller();
		}
	}
	
	
	public void doButtonSelected(SelectionEvent event) {
		
		for (int i = 0; i < fButtons.length; i++) {
			if (event.widget == fButtons[i]) {
				fListener.handleButtonPressed(i);
				return;
			}
		}
	}
	
	public void enableButton(int buttonIdx, boolean enable) {
		
		fButtonsEnabled[buttonIdx] = enable;
		if (fComposite.isEnabled())
			fButtons[buttonIdx].setEnabled(enable);
	}
	
	public void updateButtonStatet() {
		
		if (fButtons == null || !UIAccess.isOkToUse(fComposite))
			return;
		
		boolean global = fComposite.isEnabled();
		for (int i = 0; i < fButtons.length; i++) {
			if (UIAccess.isOkToUse(fButtons[i]))
				fButtons[i].setEnabled(global && fButtonsEnabled[i]);
		}
	}

	public boolean isButtonEnabled(int idx) {

		return fButtonsEnabled[idx];
	}
	
}
