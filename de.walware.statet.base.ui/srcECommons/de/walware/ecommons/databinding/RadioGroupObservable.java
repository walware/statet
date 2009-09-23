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

package de.walware.ecommons.databinding;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;


/**
 * Observable for a radio button group
 */
public class RadioGroupObservable extends AbstractSWTObservableValue implements SelectionListener {
	
	
	private Button[] fButtons;
	private int fIdx;
	
	
	public RadioGroupObservable(final Realm realm, final Button[] buttons) {
		super(realm, buttons[0]);
		fButtons = buttons;
		
		for (final Button button : buttons) {
			button.addSelectionListener(this);
		}
		fIdx = -1;
	}
	
	
	public Object getValueType() {
		return Integer.class;
	}
	
	@Override
	protected Object doGetValue() {
		return fIdx;
	}
	
	@Override
	protected void doSetValue(final Object value) {
		int idx = ((Integer) value).intValue();
		if (idx < 0 || idx > fButtons.length) {
			idx = 0;
		}
		fIdx = idx;
		for (int i = 0; i < fButtons.length; i++) {
			fButtons[i].setSelection(idx == i);
		}
	}
	
	
	public void widgetDefaultSelected(final SelectionEvent e) {
	}
	
	public void widgetSelected(final SelectionEvent e) {
		if (((Button) e.widget).getSelection()) {
			for (int i = 0; i < fButtons.length; i++) {
				if (e.widget == fButtons[i]) {
					final int old = fIdx;
					if (i != old) {
						fIdx = i;
						fireValueChange(Diffs.createValueDiff(old, i));
					}
					return;
				}
			}
		}
	}
	
}
