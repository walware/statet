/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;

import de.walware.eclipsecommons.ui.dialogs.Layouter;


public abstract class ListedOptionsGroup<ItemT extends SelectionItem> extends SelectionOptionsGroup<ItemT> {

	
	public List fListControl;
	
	public ListedOptionsGroup(boolean grabSelectionHorizontal,	boolean grabVertical) {
		super(grabSelectionHorizontal, grabVertical);

	}

	public ListedOptionsGroup() {
		super();
	}

	@Override
	protected Control createSelectionControl(Composite parent, GridData gd) {

		fListControl = new List(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);

		fListControl.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				handleListSelection();
			}
		});
		
		return fListControl;
	}
	
	@Override
	public void initFields() {
		super.initFields();
		
		String[] listItems = new String[fSelectionModel.size()];
		for (int i = 0; i < listItems.length; i++) {
			listItems[i] = fSelectionModel.get(i).fName;
		}
		fListControl.setItems(listItems);
		
		fListControl.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (Layouter.isOkToUse(fListControl)) {
					fListControl.select(0);
					handleListSelection();
				}
			}
		});
	}

}
